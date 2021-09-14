/*
 * Copyright 2021 LinkedIn Corp.
 * Licensed under the BSD 2-Clause License (the "License").
 * See License in the project root for license information.
 */
package com.linkedin.avroutil1.errorprone;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class AvroCompatiblityTest {
  private CompilationTestHelper _helper;
  private BugCheckerRefactoringTestHelper _refactoringTestHelper;

  @BeforeMethod
  void setup() {
    _helper = CompilationTestHelper.newInstance(AvroCompatibility.class, getClass());
    _refactoringTestHelper = BugCheckerRefactoringTestHelper.newInstance(AvroCompatibility.class, getClass());
  }

  @Test
  void runTests() {
    _helper.addSourceFile("/test/AvroTest.java")
        .doTest();
  }

  @Test
  void fixes() {
    _refactoringTestHelper
        .addInput("/test/AvroTest.java")
        .addOutput("/fixed/AvroTest.java")
        .doTest();
  }

}
