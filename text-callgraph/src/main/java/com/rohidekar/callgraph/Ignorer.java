package com.rohidekar.callgraph;

import org.apache.bcel.classfile.JavaClass;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * TODO: Insert description here. (generated by ssarnobat)
 */
public class Ignorer {

  private static Logger log = Logger.getLogger(Ignorer.class);

  public static boolean shouldIgnore(JavaClass iClass) {
    return shouldIgnore(iClass.getClassName());
  }

  public static boolean shouldIgnore(String classFullName) {
    for (String substringToIgnore : Main.substringsToIgnore) {
      if (classFullName.contains(substringToIgnore)) {
        return true;
      }
    }
    if (log.isEnabledFor(Level.DEBUG)) {
      log.debug(classFullName + " was not ignored");
    }
    return false;
  }

}
