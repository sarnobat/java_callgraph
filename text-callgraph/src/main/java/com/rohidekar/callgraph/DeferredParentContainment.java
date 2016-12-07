// Copyright 2012 Google Inc. All Rights Reserved.

package com.rohidekar.callgraph;

import org.apache.bcel.classfile.JavaClass;

/**
 * When the parent doesn't yet exist.
 *
 * @author ssarnobat@google.com (Sridhar Sarnobat)
 */
public class DeferredParentContainment {

  private String parentClassName;
  private JavaClass childClass;

  public DeferredParentContainment(String parentClassName, JavaClass childClass) {
    this.setParentClassName(parentClassName);
    this.setChildClass(childClass);
  }

  public String getParentClassName() {
    return parentClassName;
  }

  private void setParentClassName(String parentClassName) {
    this.parentClassName = parentClassName;
  }

  public JavaClass getChildClass() {
    return childClass;
  }

  private void setChildClass(JavaClass childClass) {
    this.childClass = childClass;
  }

}
