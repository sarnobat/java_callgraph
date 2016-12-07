// Copyright 2012 Google Inc. All Rights Reserved.

package com.rohidekar.callgraph;

import com.google.common.collect.Multimap;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.TreeModel;

/**
 * put -Xmx1024m in the VM args
 *
 * @author ssarnobat@google.com (Sridhar Sarnobat)
 */
public class Main {
  private static Logger log = Logger.getLogger(Main.class);
  // TODO (feature):add call relationship between 
  // TODO (feature): build containment hierarchy using import statements
  // TODO(feature): Order call graphs by depth
  // EmployeeAssembler
  // TODO(testing): make printGraphs return a string buffer so you can do assertions in unit tests

  // TODO: Create a configuration object. Have several profiles: automatic, maximal
  private static final boolean PRINT_CONTAINMENT = true;
  private static final boolean PRINT_CALL_TREE = true;
  private static final boolean PRINT_PACKAGE_ARCHITECTURE = true;


  public static final int MIN_TREE_DEPTH = 3;
  public static int MAX_TREE_DEPTH = 187;// 27 works, 30 breaks
  // Only print from roots this far below the top level package that contains classes
  public static final int ROOT_DEPTH = 27;

  static final String[] substringsToIgnore = {"java", "Logger", "Test", ".toString", "Exception",
  // "com.google.common",
  // "Property.getValue()",
  // "com.google.gwt",
  // "com.google.common",
  // ".get",
  // "<init>",
  // "com.google.inject",
  // "com.opensymphony",
  // "com.google.ads",
  // "com.google.corplogin",
  // "com.google.gse",
      };


  public static void main(String[] args) {
    Logger.getRootLogger().setLevel(Level.ERROR);
    ConsoleAppender consoleAppender = new ConsoleAppender();
    consoleAppender.setWriter(new OutputStreamWriter(System.out));
    consoleAppender.setLayout(new PatternLayout("%-5p [%t]: %m%n"));
    Logger.getRootLogger().addAppender(consoleAppender);
    String resource;
    if (args == null || args.length < 1) {
      //resource = "/home/ssarnobat/workspaces/2012-06-12/06-12/";
      //resource = "/Users/ssarnobat/work/src/saas-api/utils/target/classes";
      resource = "/Users/ssarnobat/github/nanohttpd/target";
      // TODO: use the current working directory as the class folder, not
      // an arbitrary jar
    } else {
      resource = args[0];
    }
    printGraphs(resource);
  }

  @SuppressWarnings("unchecked")
  private static void printGraphs(String resource) {
    Relationships relationships = new Relationships(resource);
    relationships.validate();
    if (PRINT_CALL_TREE) {
      Map<String, GraphNode> allMethodNamesToMethodNodes =
          RelationshipToGraphTransformer.determineCallHierarchy(relationships);
      relationships.validate();
      Set<GraphNode> rootMethodNodes = RootFinder.findRootCallers(allMethodNamesToMethodNodes);
      TreePrinter.printTrees(relationships, rootMethodNodes);
    }
    if (PRINT_PACKAGE_ARCHITECTURE) {
      Map<String, GraphNodePackage> allPacakgeNamesToPackageNodes =
          RelationshipToGraphTransformer.determinePackageStructure(relationships);
      Set<GraphNode> rootMethodNodes = RootFinder.findRoots(allPacakgeNamesToPackageNodes);
      if (log.isEnabledFor(Level.DEBUG)) {
        log.debug("Root package: " + rootMethodNodes.iterator().next().toString());
      }
      TreePrinter.printTrees(rootMethodNodes);
    }
    System.out.println("Containment Hierarchy");
    if (PRINT_CONTAINMENT) {
      Map<String, GraphNode> classNameToClassNodes =
          RelationshipToGraphTransformer.determineContainments(relationships);
      Set<GraphNode> rootClasses = RootFinder.findRootJavaClasses(classNameToClassNodes);
      Multimap<Integer, TreeModel> depthToTree = GraphNodeUtils.removeCyclicCalls(rootClasses);
      TreePrinter.printTrees(relationships, depthToTree);
    }
  }
}
