// Copyright 2012 Google Inc. All Rights Reserved.

package com.rohidekar.callgraph;

/**
 * TODO: Why do we have this?
 *
 * @author ssarnobat@google.com (Sridhar Sarnobat)
 */
public class DeferredClassVisit {
  private String className;

  public DeferredClassVisit(String className) {
    this.setClassName(className);
  }

  public String getClassName() {
    return className;
  }

  private void setClassName(String className) {
    this.className = className;
  }

}
