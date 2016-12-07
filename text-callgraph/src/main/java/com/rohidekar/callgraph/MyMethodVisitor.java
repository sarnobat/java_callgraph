package com.rohidekar.callgraph;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Type;
import org.apache.log4j.Logger;

import gr.gousiosg.javacg.stat.MethodVisitor;

import java.util.Collection;

public class MyMethodVisitor extends MethodVisitor {
  private static Logger log = Logger.getLogger(Main.class);
  private JavaClass visitedClass;
  private ConstantPoolGen constantsPool;
  private Relationships relationships;
  private String parentMethodQualifiedName;

  public MyMethodVisitor(MethodGen methodGen, JavaClass javaClass, Relationships relationships) {
    super(methodGen, javaClass);
    this.visitedClass = javaClass;
    this.constantsPool = methodGen.getConstantPool();
    this.parentMethodQualifiedName = MyInstruction.getQualifiedMethodName(methodGen, visitedClass);
    this.relationships = relationships;
    // main bit
    if (methodGen.getInstructionList() != null) {
      for (InstructionHandle instructionHandle = methodGen.getInstructionList().getStart();
          instructionHandle != null; instructionHandle = instructionHandle.getNext()) {
        Instruction anInstruction = instructionHandle.getInstruction();
        if (!shouldVisitInstruction(anInstruction)) {
          anInstruction.accept(this);
        }
      }
    }
    // We can't figure out the superclass method of the parent method because we don't know which
    // parent classes' method is overriden (there are several)
    // TODO: Wait, we can use the repository to get the java class.
    String unqualifiedMethodName =
        MyInstruction.getMethodNameUnqualified(parentMethodQualifiedName);
    relationships.setVisitedMethod(parentMethodQualifiedName);
    if (relationships.getMethod(parentMethodQualifiedName) == null) {
      relationships.addMethodDefinition(
          new MyInstruction(javaClass.getClassName(), unqualifiedMethodName));
    }
  }

  private static boolean shouldVisitInstruction(Instruction iInstruction) {
    return ((InstructionConstants.INSTRUCTIONS[iInstruction.getOpcode()] != null)
        && !(iInstruction instanceof ConstantPushInstruction)
        && !(iInstruction instanceof ReturnInstruction));
  }

  /** instance method */
  @Override
  public void visitINVOKEVIRTUAL(INVOKEVIRTUAL iInstruction) {
    addMethodCallRelationship(iInstruction.getReferenceType(constantsPool),
        iInstruction.getMethodName(constantsPool), iInstruction,
        iInstruction.getArgumentTypes(constantsPool));
  }

  /** super method, private method, constructor */
  @Override
  public void visitINVOKESPECIAL(INVOKESPECIAL iInstruction) {
    addMethodCallRelationship(iInstruction.getReferenceType(constantsPool),
        iInstruction.getMethodName(constantsPool), iInstruction,
        iInstruction.getArgumentTypes(constantsPool));
  }

  @Override
  public void visitINVOKEINTERFACE(INVOKEINTERFACE iInstruction) {
    addMethodCallRelationship(iInstruction.getReferenceType(constantsPool),
        iInstruction.getMethodName(constantsPool), iInstruction,
        iInstruction.getArgumentTypes(constantsPool));
  }

  @Override
  public void visitINVOKESTATIC(INVOKESTATIC iInstruction) {
    addMethodCallRelationship(iInstruction.getReferenceType(constantsPool),
        iInstruction.getMethodName(constantsPool), iInstruction,
        iInstruction.getArgumentTypes(constantsPool));
  }

  private void addMethodCallRelationship(Type iClass, String unqualifiedMethodName,
      @SuppressWarnings("unused") Instruction anInstruction,
      @SuppressWarnings("unused") Type[] argumentTypes) {
    if (!(iClass instanceof ObjectType)) {
      return;
    }
    ObjectType childClass = (ObjectType) iClass;
    MyInstruction target = new MyInstruction(childClass, unqualifiedMethodName);
    relationships.addMethodCall(parentMethodQualifiedName, target, target.printInstruction(true));
    if (relationships.getMethod(this.parentMethodQualifiedName) == null) {
      relationships.addMethodDefinition(
          new MyInstruction(childClass.getClassName(), unqualifiedMethodName));
    }
    // link to superclass method - note: this will not work for the top-level method (i.e.
    // parentMethodQualifiedName). Only for target.
    // We can't do it for the superclass without a JavaClass object. We don't know which superclass
    // the method overrides.
    linkMethodToSuperclassMethod(unqualifiedMethodName, target);
  }

  private void linkMethodToSuperclassMethod(String unqualifiedMethodName, MyInstruction target)
      throws IllegalAccessError {

    Collection<JavaClass> superClasses = relationships.getParentClassesAndInterfaces(visitedClass);
    for (JavaClass parentClassOrInterface : superClasses) {
      MyInstruction parentInstruction =
          getInstruction(parentClassOrInterface, unqualifiedMethodName, relationships);
      if (parentInstruction == null) {
        // It may be that we're looking in the wrong superclass/interface and that we should just
        // continue
        // carry on
        relationships.deferSuperMethodRelationshipCapture(
            new DeferredSuperMethod(parentClassOrInterface, unqualifiedMethodName, target));
      } else {
        if (log.isDebugEnabled()) {
          log.debug(parentInstruction.getMethodNameQualified() + " -> "
              + target.getMethodNameQualified());
        }
        relationships.addMethodCall(
            parentInstruction.getMethodNameQualified(), target, target.getMethodNameQualified());
      }
    }
  }

  public static MyInstruction getInstruction(JavaClass parentClassOrInterface,
      String unqualifiedChildMethodName, Relationships relationships) {
    String methodName = MyInstruction.getQualifiedMethodName(
        parentClassOrInterface.getClassName(), unqualifiedChildMethodName);
    MyInstruction instruction = relationships.getMethod(methodName);
    return instruction;
  }

  @Override
  public void start() {}
}
