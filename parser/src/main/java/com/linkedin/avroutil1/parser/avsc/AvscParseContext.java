/*
 * Copyright 2021 LinkedIn Corp.
 * Licensed under the BSD 2-Clause License (the "License").
 * See License in the project root for license information.
 */

package com.linkedin.avroutil1.parser.avsc;

import com.linkedin.avroutil1.model.AvroNamedSchema;
import com.linkedin.avroutil1.model.AvroSchema;
import com.linkedin.avroutil1.parser.Located;
import com.linkedin.avroutil1.parser.exceptions.AvroSyntaxException;

import java.io.File;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * maintains state for parse operations in progress for a single avsc source
 * - completed (parsed) schemas, unresolved references, current namespace etc
 */
public class AvscParseContext {
    /**
     * represents the resource being parsed (typically an avsc file or a raw avsc string)
     */
    protected final URI uri;
    /**
     * current (closest-defined) namespace. changes during parsing - pushed when a new
     * namespace is defined, popped when leaving the scope of a defined namespace.
     * initial value is [""]
     */
    protected final Deque<String> namespaceStack = new ArrayDeque<>();
    /**
     * all schemas defined (not simply referenced) within the current avsc being parsed.
     * the spec expects all nested schemas to be defined inline, so typical (large)
     * schemas may have 10s+ of nested record/enum/unions defined therein
     */
    protected final List<Located<AvroSchema>> definedSchemas = new ArrayList<>();
    /**
     * schemas out of the above collection that have a name, by their full name
     */
    protected final Map<String, Located<AvroSchema>> definedNamedSchemas = new HashMap<>();
    /**
     * the top level (root, outer-most) schema in the avsc being parsed.
     * avsc sources typically have a single top level schema, though it may be
     * a primitive or a union/collection
     */
    protected Located<AvroSchema> topLevelSchema = null;

    public AvscParseContext(String avsc) {
        try {
            uri = new URI("avsc://" + avsc.hashCode());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        initializeNamespace();
    }

    public AvscParseContext(File avsc) {
        uri = avsc.toURI();
        initializeNamespace();
    }

    protected void initializeNamespace() {
        if (!namespaceStack.isEmpty()) {
            throw new IllegalStateException("this is a bug");
        }
        namespaceStack.push("");
    }

    public String getCurrentNamespace() {
        return namespaceStack.peek();
    }

    public void pushNamespace(String newNamespace) {
        if (newNamespace == null || newNamespace.isEmpty()) {
            throw new IllegalArgumentException("new namespace cannot be null or empty");
        }
        if (newNamespace.equals(getCurrentNamespace())) {
            throw new IllegalArgumentException("new namespace " + newNamespace + " same as current namespace");
        }
        namespaceStack.push(newNamespace);
    }

    public void popNamespace() {
        if (namespaceStack.isEmpty()) {
            throw new IllegalStateException("this is a bug");
        }
        namespaceStack.pop();
    }

    public void defineSchema(Located<AvroSchema> schema, boolean isTopLevel) {
        if (schema == null) {
            throw new IllegalArgumentException("schema cannot be null");
        }
        if (isTopLevel) {
            if (topLevelSchema != null) {
                throw new IllegalStateException("cannot set " + schema + " as top level schema "
                        + "for context as thats already set to " + topLevelSchema);
            }
            topLevelSchema = schema;
        }
        definedSchemas.add(schema);
        AvroSchema s = schema.getValue();
        if (s.type().isNamed()) {
            AvroNamedSchema namedSchema = (AvroNamedSchema) s;
            Located<AvroSchema> other = definedNamedSchemas.putIfAbsent(namedSchema.getFullName(), schema);
            if (other != null) {
                throw new AvroSyntaxException(s + " defined in " + schema.getLocation() + " conflicts with " + other);
            }
        }
    }

    public Located<AvroSchema> getTopLevelSchema() {
        return topLevelSchema;
    }
}
