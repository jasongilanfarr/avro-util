/*
 * Copyright 2021 LinkedIn Corp.
 * Licensed under the BSD 2-Clause License (the "License").
 * See License in the project root for license information.
 */

package com.linkedin.avroutil1.errorprone;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.JCTree;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.stream.Collectors;

/**
 * missing: 1.5+ DecoderFactory.jsonDecoder
 * missing: 1.8+ new Fixed(), GenericRecordBuilder
 */
@AutoService(BugChecker.class)
@BugPattern(name = "AvroCompatibility", summary = "Prevents usage of incompatible Avro APIs", severity = BugPattern.SeverityLevel.WARNING, suppressionAnnotations = {
    SuppressWarnings.class, SuppressFBWarnings.class})
public class AvroCompatibility extends BugChecker implements BugChecker.NewClassTreeMatcher,
                                                             BugChecker.MethodInvocationTreeMatcher,
                                                             BugChecker.InstanceOfTreeMatcher {
  private static final Matcher<ExpressionTree> BINARY_DECODER =
      Matchers.constructor().forClass("org.apache.avro.io.BinaryDecoder");
  private static final Matcher<ExpressionTree> BINARY_ENCODER =
      Matchers.constructor().forClass("org.apache.avro.io.BinaryEncoder");
  private static final Matcher<ExpressionTree> ENUM_SYMBOL =
      Matchers.constructor().forClass("org.apache.avro.generic.GenericData.EnumSymbol");
  private static final Matcher<ExpressionTree> JSON_DECODER =
      Matchers.constructor().forClass("org.apache.avro.io.JsonDecoder");
  private static final Matcher<ExpressionTree> JSON_ENCODER =
      Matchers.constructor().forClass("org.apache.avro.io.JsonEncoder");


  private static final Matcher<ExpressionTree> FIELD_DEFAULT =
      Matchers.instanceMethod()
      .onDescendantOf("org.apache.avro.Schema.Field")
      .namedAnyOf("defaultValue", "defaultVal");

  @Override
  public Description matchNewClass(NewClassTree tree, VisitorState state) {
    if (BINARY_DECODER.matches(tree, state)) {
      SuggestedFix fix = SuggestedFix.builder()
          .addImport("com.linkedin.avroutil1.compatibility.AvroCompatibilityHelper")
          .replace(tree,
              "AvroCompatibilityHelper.newBinaryDecoder(" +
                  tree.getArguments().stream().map(state::getSourceForNode)
                  .collect(Collectors.joining(", "))
                  + ")")
          .build();

      return describeMatch(tree, fix);
    } else if (BINARY_ENCODER.matches(tree, state)) {
      SuggestedFix fix = SuggestedFix.builder()
          .addImport("com.linkedin.avroutil1.compatibility.AvroCompatibilityHelper")
          .replace(tree,
              "AvroCompatibilityHelper.newBinaryEncoder(" +
                  tree.getArguments().stream().map(state::getSourceForNode)
                      .collect(Collectors.joining(", "))
                  + ")")
          .build();
      return describeMatch(tree, fix);
    } else if (ENUM_SYMBOL.matches(tree, state)) {
      SuggestedFix fix;
      if (tree.getArguments().size() == 1) {
        fix = SuggestedFix.builder()
            .addImport("com.linkedin.avroutil1.compatibility.AvroCompatibilityHelper")
            .replace(tree,
                "/* Avro Migration FIX ME: Unable to derive schema for the enum symbol */\n"
                + "AvroCompatibilityHelper.newEnumSymbol(null, " +
                    tree.getArguments().stream().map(state::getSourceForNode)
                        .collect(Collectors.joining(", "))
                    + ")")
            .build();
      } else {
        fix = SuggestedFix.builder()
          .addImport("com.linkedin.avroutil1.compatibility.AvroCompatibilityHelper")
          .replace(tree,
              "AvroCompatibilityHelper.newEnumSymbol(" +
                  tree.getArguments().stream().map(state::getSourceForNode)
                      .collect(Collectors.joining(", "))
                  + ")")
          .build();
      }
      return describeMatch(tree, fix);
    } else if (JSON_DECODER.matches(tree, state)) {
      SuggestedFix fix = SuggestedFix.builder()
          .addImport("com.linkedin.avroutil1.compatibility.AvroCompatibilityHelper")
          .replace(tree,
              "AvroCompatibilityHelper.newCompatibleJsonDecoder(" +
                  tree.getArguments().stream().map(state::getSourceForNode)
                      .collect(Collectors.joining(", "))
                  + ")")
          .build();

      return describeMatch(tree, fix);
    } else if (JSON_ENCODER.matches(tree, state)) {
      SuggestedFix fix = SuggestedFix.builder()
          .addImport("com.linkedin.avroutil1.compatibility.AvroCompatibilityHelper")
          .replace(tree,
              "AvroCompatibilityHelper.newJsonEncoder(" +
                  tree.getArguments().stream().map(state::getSourceForNode)
                      .collect(Collectors.joining(", "))
                  + ")")
          .build();
    }
    return Description.NO_MATCH;
  }

  @Override
  public Description matchInstanceOf(InstanceOfTree tree, VisitorState state) {
    if (ASTHelpers.isSameType(ASTHelpers.getType(tree.getType()), state.getTypeFromString(" org.apache.avro.generic.GenericRecord"), state)) {
      return describeMatch(tree, SuggestedFix.builder()
        .addImport("com.linkedin.avroutil1.compatibility.AvroCompatibilityHelper")
          .replace(tree, "AvroCompatibilityHelper.isGenericRecord(" + state.getSourceForNode(tree.getExpression()) + ")")
      .build());
    }
    if (ASTHelpers.isSameType(ASTHelpers.getType(tree.getType()), state.getTypeFromString("org.apache.avro.specific.SpecificDatunReader$SchemaConstructable")))
    return Description.NO_MATCH;
  }

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (FIELD_DEFAULT.matches(tree, state)) {
      // TODO: Any reason to do generic instead of specific?
      boolean assignment =
          state.getPath().getParentPath().getLeaf().getKind() == Tree.Kind.VARIABLE;
      String identifier = ((JCTree.JCFieldAccess) ((JCTree.JCMethodInvocation) tree).getMethodSelect()).selected.toString();

      if (assignment) {
        JCTree.JCVariableDecl declaration = ((JCTree.JCVariableDecl) state.getPath().getParentPath().getLeaf());

        return describeMatch(state.getPath().getParentPath().getLeaf(), SuggestedFix.builder()
            .addImport("com.linkedin.avroutil1.compatibility.AvroCompatibilityHelper")
            .replace(state.getPath().getParentPath().getLeaf(),
                SuggestedFixes.prettyType(declaration.type, state) + " " + declaration.name +  " = null;\n"
                + "if (AvroCompatibilityHelper.fieldHasDefault(" +identifier
                    + ")) {\n    "
                    + ((JCTree.JCVariableDecl) state.getPath().getParentPath().getLeaf()).name.toString() + " = "
                    + "AvroCompatibilityHelper.getSpecificDefaultValue("
                    + identifier + ");\n" + "}")
            .build());
      } else {
        return describeMatch(tree, SuggestedFix.builder()
            .addImport("com.linkedin.avroutil1.compatibility.AvroCompatibilityHelper")
            .replace(tree,
                "if (AvroCompatibilityHelper.fieldHasDefault(" +
                   identifier  +")) {\n     "
                + "AvroCompatibilityHelper.getSpecificDefaultValue("
                    + identifier + ");\n" + "}")
            .build());
      }


    }
    return Description.NO_MATCH;
  }
}