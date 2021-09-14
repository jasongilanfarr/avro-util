/*
 * Copyright 2021 LinkedIn Corp.
 * Licensed under the BSD 2-Clause License (the "License").
 * See License in the project root for license information.
 */

package test;

import com.linkedin.avroutil1.compatibility.AvroCompatibilityHelper;
import java.io.InputStream;
import org.apache.avro.io.BinaryDecoder;



public class AvroTest {
  public void binaryEncoder() {
    // BUG: Diagnostic Contains: Prevents usage of incompatible Avro APIs
    BinaryDecoder decoder = AvroCompatibilityHelper.newBinaryDecoder((InputStream) null);
  }
}