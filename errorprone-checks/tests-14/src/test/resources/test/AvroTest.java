/*
 * Copyright 2021 LinkedIn Corp.
 * Licensed under the BSD 2-Clause License (the "License").
 * See License in the project root for license information.
 */

package test;

import java.io.OutputStream;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.io.BinaryDecoder;
import java.io.InputStream;
import org.apache.avro.io.BinaryEncoder;


public class AvroTest {
  public void binaryEncoder() {
    // BUG: Diagnostic contains: Prevents usage of incompatible Avro APIs
    BinaryDecoder decoder = new BinaryDecoder((InputStream) null);
    // BUG: Diagnostic contains: Prevents usage of incompatible Avro APIs
    BinaryEncoder encoder = new BinaryEncoder((OutputStream) null);
    // BUG: Diagnostic contains: Prevents usage of incompatible Avro APIs
    GenericData.EnumSymbol symbol = new GenericData.EnumSymbol("symbol");
    Schema.Field field = null;
    // BUG: Diagnostic contains: Prevents usage of incompatible Avro APIs
    field.defaultValue();
    // BUG: Diagnostic contains: Prevents usage of incompatible Avro APIs
    Object defaultValue = field.defaultValue();
  }
}