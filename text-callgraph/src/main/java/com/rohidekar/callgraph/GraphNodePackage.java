// Copyright 2012 Google Inc. All Rights Reserved.

package com.rohidekar.callgraph;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author ssarnobat@google.com (Sridhar Sarnobat)
 *
 */
public class GraphNodePackage extends GraphNode {
  private static Logger log = Logger.getLogger(Relationships.class);

  private String pkgQualifiedName;

  public GraphNodePackage(String pkgQualifiedName) {
    super(pkgQualifiedName);
    this.pkgQualifiedName = pkgQualifiedName;
    if (pkgQualifiedName.length() < 1) {
      if (log.isEnabledFor(Level.WARN)) {
        log.warn("Probably a mistake");
      }
    }
  }

  @Override
  protected String printTreeNode() {
    return ClassUtils.getPackageCanonicalName(pkgQualifiedName);
  }

  @Override
  public int getPackageDepth() {
    return StringUtils.countMatches(this.pkgQualifiedName, ".");
  }

  @Override
  public String toString() {
    return ClassUtils.getShortCanonicalName(this.pkgQualifiedName);
  }

}
