package com.rohidekar.callgraph;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
* Transforms relationships into graphs
 */
public class RelationshipToGraphTransformer {

  private static Logger log = Logger.getLogger(RelationshipToGraphTransformer.class);
  @VisibleForTesting
  public static Map<String, GraphNode> determineCallHierarchy(Relationships relationships) {
    relationships.validate();
    Map<String, GraphNode> allMethodNamesToMethods = new LinkedHashMap<String, GraphNode>();
    // Create a custom call graph structure from the multimap (flatten)
    for (String parentMethodNameKey : relationships.getAllMethodCallers()) {
      if (log.isEnabledFor(Level.DEBUG)) {
        log.debug(parentMethodNameKey);
      }
      if (Ignorer.shouldIgnore(parentMethodNameKey)) {
        continue;
      }
      GraphNodeInstruction parentEnd =
          (GraphNodeInstruction) allMethodNamesToMethods.get(parentMethodNameKey);
      if (parentEnd == null) {
        MyInstruction parentMethodInstruction =
            relationships.getMethod(parentMethodNameKey);
        if (parentMethodInstruction == null) {
          if (log.isEnabledFor(Level.WARN)) {
            log.warn("couldn't find instruction for  " + parentMethodNameKey);
          }
          continue;
        }
        parentEnd = new GraphNodeInstruction(parentMethodInstruction);
        allMethodNamesToMethods.put(parentMethodNameKey, parentEnd);
        if (parentEnd.toString().contains("Millis") && parentMethodNameKey.contains("Repository")) {
          throw new IllegalAccessError();
        }
      }
      if (parentEnd.toString().contains("Millis") && parentMethodNameKey.contains("Repository")) {
        throw new IllegalAccessError();
      }
      Collection<MyInstruction> calledMethods =
          relationships.getCalledMethods(parentMethodNameKey);
      for (MyInstruction childMethod : calledMethods) {
        if (Ignorer.shouldIgnore(childMethod.getMethodNameQualified())) {
          continue;
        }
        if (log.isEnabledFor(Level.DEBUG)) {
          log.debug("-> " + childMethod.getMethodNameQualified());
        }
        GraphNodeInstruction child =
            (GraphNodeInstruction) allMethodNamesToMethods.get(childMethod.getMethodNameQualified());
        if (child == null) {
          child = new GraphNodeInstruction(childMethod);
          allMethodNamesToMethods.put(childMethod.getMethodNameQualified(), child);
        }
        parentEnd.addChild(child);
        child.addParent(parentEnd);
      }
    }
    relationships.validate();
    return allMethodNamesToMethods;
  }

  public static Map<String, GraphNode> determineContainments(Relationships relationships)
      throws IllegalAccessError {
    Map<String, GraphNode> classNameToGraphNodeJavaClassMap =
        new LinkedHashMap<String, GraphNode>();
    log.info("Number of classes: " + relationships.getAllClassNames().size());
    Map<String, GraphNode> classNameToGraphNodeClassNameMap = classNameToGraphNodeJavaClassMap;
    // Create a custom containment graph structure from the multimap (this is effectively a a
    // map-reduce
    // task I think
    for (String aClassFullName : relationships.getAllClassNames()) {
      GraphNode aClassNameGraphNode = classNameToGraphNodeClassNameMap.get(aClassFullName);
      if (aClassNameGraphNode == null) {
        aClassNameGraphNode = new GraphNodeString(aClassFullName);
      }
      classNameToGraphNodeClassNameMap.put(aClassFullName, aClassNameGraphNode);
      // get all child class names
      for (String childClassName : relationships.getContainedClassNames(aClassFullName)) {
        // Don't ignore anything for now
        GraphNode childClassNameGraphNode = classNameToGraphNodeClassNameMap.get(childClassName);
        if (childClassNameGraphNode == null) {
          childClassNameGraphNode = new GraphNodeString(childClassName);
          classNameToGraphNodeClassNameMap.put(childClassName, childClassNameGraphNode);
        }
        aClassNameGraphNode.addChild(childClassNameGraphNode);
        childClassNameGraphNode.addParent(aClassNameGraphNode);
      }
    }
    return classNameToGraphNodeJavaClassMap;
  }
  public static Map<String, GraphNodePackage> determinePackageStructure(Relationships relationships) {
    Map<String, GraphNodePackage> allPacakgeNamesToPackageNodes =
        new LinkedHashMap<String, GraphNodePackage>();

    for (String parentPackage : relationships.getPackagesKeySet()) {
      // parent
      GraphNodePackage graphNodePackage = allPacakgeNamesToPackageNodes.get(parentPackage);
      if (graphNodePackage == null) {
        graphNodePackage = new GraphNodePackage(parentPackage);
        allPacakgeNamesToPackageNodes.put(parentPackage, graphNodePackage);
      }
      checkNotNull(graphNodePackage);
      // grandparent
      boolean hasParent = true;
      String aPackage = parentPackage;
      while (hasParent) {
        String grandParentPackage = ClassUtils.getPackageName(aPackage);
        if (grandParentPackage == null || grandParentPackage.length() < 1) {
          hasParent = false;
          break;
        }
        GraphNodePackage gngp = allPacakgeNamesToPackageNodes.get(grandParentPackage);
        if (gngp == null) {
          gngp = new GraphNodePackage(grandParentPackage);
          allPacakgeNamesToPackageNodes.put(grandParentPackage, gngp);
        }
        gngp.addChild(graphNodePackage);
        graphNodePackage.addParent(gngp);
        aPackage = grandParentPackage;
      }
      // children
      for (String childPackages : relationships.getChildPackagesOf(parentPackage)) {
        GraphNodePackage gnc = allPacakgeNamesToPackageNodes.get(childPackages);
        if (gnc == null) {
          gnc = new GraphNodePackage(childPackages);
          allPacakgeNamesToPackageNodes.put(childPackages, gnc);
        }
        gnc.addParent(graphNodePackage);
        graphNodePackage.addChild(gnc);
      }
    }
    return allPacakgeNamesToPackageNodes;
  }
}
