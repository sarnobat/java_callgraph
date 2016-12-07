// Copyright 2012 Google Inc. All Rights Reserved.

package com.rohidekar.callgraph;

import org.apache.bcel.classfile.JavaClass;

/**
 * @author ssarnobat@google.com (Sridhar Sarnobat)
 *
 */
public class DeferredSuperMethod {

  JavaClass parentClassOrInterface;
  String unqualifiedMethodName;
  MyInstruction target;

  public DeferredSuperMethod(
      JavaClass parentClassOrInterface, String unqualifiedMethodName, MyInstruction target) {
    this.parentClassOrInterface = parentClassOrInterface;
    this.unqualifiedMethodName = unqualifiedMethodName;
    this.target = target;
  }

  public MyInstruction gettarget() {
    return target;
  }

  public JavaClass getparentClassOrInterface() {
    return parentClassOrInterface;
  }

  public String getunqualifiedMethodName() {
    return unqualifiedMethodName;
  }

}
