/*
 * Copyright 2021 LinkedIn Corp.
 * Licensed under the BSD 2-Clause License (the "License").
 * See License in the project root for license information.
 */

package test;

import com.linkedin.avroutil1.compatibility.AvroCompatibilityHelper;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;


public class AvroTest {
  public void binaryEncoder() {
    // BUG: Diagnostic contains: Prevents usage of incompatible Avro APIs
    BinaryDecoder decoder = AvroCompatibilityHelper.newBinaryDecoder((InputStream) null);
    // BUG: Diagnostic contains: Prevents usage of incompatible Avro APIs
    BinaryEncoder encoder = AvroCompatibilityHelper.newBinaryEncoder((OutputStream) null);
    // BUG: Diagnostic contains: Prevents usage of incompatible Avro APIs
    GenericData.EnumSymbol symbol = /* Avro Migration FIX ME: Unable to derive schema for the enum symbol */
        AvroCompatibilityHelper.newEnumSymbol(null, "symbol");
    Schema.Field field = null;
    // BUG: Diagnostic contains: Prevents usage of incompatible Avro APIs
    if (AvroCompatibilityHelper.fieldHasDefault(field)) {
      AvroCompatibilityHelper.getGenericDefaultValue(field);
    };
    // BUG: Diagnostic contains: Prevents usage of incompatible Avro APIs
    Object defaultValue = null;
    if (AvroCompatibilityHelper.fieldHasDefault(field)) {
      defaultValue = AvroCompatibilityHelper.getGenericDefaultValue(field);
    }
  }
}