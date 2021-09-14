/*
 * Copyright 2021 LinkedIn Corp.
 * Licensed under the BSD 2-Clause License (the "License").
 * See License in the project root for license information.
 */

package test;

import org.apache.avro.io.BinaryDecoder;
import java.io.InputStream;

public class AvroTest {
  public void binaryEncoder() {
    // BUG: Diagnostic contains: Prevents usage of incompatible Avro APIs
    BinaryDecoder decoder = new BinaryDecoder((InputStream) null);
  }
}