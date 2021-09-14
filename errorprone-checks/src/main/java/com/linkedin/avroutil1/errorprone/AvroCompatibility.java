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
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewClassTree;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.stream.Collectors;


@AutoService(BugChecker.class)
@BugPattern(name = "AvroCompatibility", summary = "Prevents usage of incompatible Avro APIs", severity = BugPattern.SeverityLevel.WARNING, suppressionAnnotations = {
    SuppressWarnings.class, SuppressFBWarnings.class})
public class AvroCompatibility extends BugChecker implements BugChecker.NewClassTreeMatcher {
  private static final Matcher<ExpressionTree> BINARY_DECODER =
      Matchers.constructor().forClass("org.apache.avro.io.BinaryDecoder");

  private static final Matcher<ExpressionTree> BOUNDED_MEMORY_DECODER =
      Matchers.constructor().forClass("org.apache.avro.io.BoundedMemoryDecoder");

  @Override
  public Description matchNewClass(NewClassTree tree, VisitorState state) {
    if (BINARY_DECODER.matches(tree, state) || BOUNDED_MEMORY_DECODER.matches(tree, state)) {
      SuggestedFix fix = SuggestedFix.builder()
          .addImport("com.linkedin.avroutil1.compatibility.AvroCompatibilityHelper")
          .replace(tree,
              "AvroCompatibilityHelper.newBinaryDecoder(" +
                  tree.getArguments().stream().map(state::getSourceForNode)
                  .collect(Collectors.joining(", "))
                  + ")")
          .build();

      return describeMatch(tree, fix);
    }
    return Description.NO_MATCH;
  }

}