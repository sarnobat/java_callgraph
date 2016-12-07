package com.rohidekar.callgraph;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.ClassLoaderRepository;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.internal.core.util.ClassFileReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@RunWith(JUnit4.class)
public class MainTest extends TestCase {

  @Test
  public void testPrivate() {
    String[] args = {"/home/ssarnobat/workspaces/2012-06-01/06-01/"
    // "/home/ssarnobat/my/usb/Professional/call_graph_text_generator/privateTest"
        };
    Main.main(args);
  }

  @Test
  public void testPrivateGreybox() {
    String[] args = {"/home/ssarnobat/workspaces/2012-06-12/06-12"};
    ClassLoaderRepository clr = new ClassLoaderRepository(ClassLoader.getSystemClassLoader());

    JavaClass jc = null;
    try {
      jc = clr.loadClass("java.lang.String");
    } catch (ClassNotFoundException e) {
      fail();
    }
    assertNotNull(jc);
    Relationships relationships = new Relationships(args[0]);
    // --------------------------------------------------------------------------------------------
    int i = 0;
    for (String jc2 : relationships.getAllClassNames()) {
      if (jc2.startsWith("com.google.is.gcomp.backend.service.plan.PlanServiceNonBlocking")) {
        i++;
      }
    }
    if (i < 1) {
      fail();
    }
    assertNotNull(
        relationships.getClassDef(
            "com.google.is.gcomp.backend.service.plan.PlanServiceNonBlocking$GetDefaultPersonKeyCallback"));
    // --------------------------------------------------------------------------------------------

    assertNotNull(relationships.getClassDef(
        "com.google.is.gcomp.backend.service.impl.config.ConfigurationFileRowReader"));

    Map<String, GraphNode> allMethodNamesToGraphNodes =
        RelationshipToGraphTransformer.determineCallHierarchy(relationships);
    System.out.println("Method names: " + allMethodNamesToGraphNodes.size());
    // Nodes
    assertTrue(allMethodNamesToGraphNodes.containsKey(
        "com.google.is.gcomp.backend.service.impl.config.AssemblerContext.drainZeroOrOne()"));
    // TODO: Fix these
    // assertTrue(allMethodNamesToGraphNodes.containsKey(
    // "com.google.is.gcomp.frontend.client.events.CoplannerStatusEvent"));
    // assertTrue(allMethodNamesToGraphNodes.containsKey(
    // "com.google.is.gcomp.backend.service.impl.config.EmployeeAssembler.assemble()"));
    assertMethod(relationships,
        "com.google.is.gcomp.backend.service.impl.config.ConfigurationDataProcessorImpl.handleAllCsvFiles()");


    assertMethod(relationships,
        "com.google.is.gcomp.backend.service.impl.config.ConfigurationDataProcessor.processConfigurationFiles()");
    assertMethod(relationships,
        "com.google.is.gcomp.backend.service.impl.config.ConfigurationDataProcessorImpl.handleAllCsvFiles()");
    assertMethod(relationships,
        "com.google.is.gcomp.backend.service.impl.config.ConfigurationFileRowReader.readRow()");
    assertMethod(relationships,
        "com.google.is.gcomp.backend.service.impl.config.ConfigurationFileParser.parse()");
    assertNotNull(relationships.getClassDef(
        "com.google.is.gcomp.backend.service.impl.config.ConfigurationDataProcessorImpl"));
    assertNotNull(relationships.getClassDef(
        "com.google.is.gcomp.backend.service.impl.config.ConfigurationServiceImpl"));

    // TODO: why doesn't XsrfIncerceptor.intercept() get displayed anywhere?
    
    assertTrue(allMethodNamesToGraphNodes.containsKey(
        "com.google.is.gcomp.frontend.client.GcompApplicationLauncher.setupDeferredComponents()"));
  }

  private static void assertMethod(Relationships relationships, String methodNameQualified) {
    assertTrue(relationships.getAllMethodNames().contains(methodNameQualified));
    assertTrue(relationships.getMethod(methodNameQualified) != null);
    assertTrue(relationships.isVisitedMethod(methodNameQualified));
  }

  @Test
  public void testJdt() {
    File classFile = new File(
        "/home/ssarnobat/workspaces/2012-06-01/06-01/classes_0_java/com/google/is/gcomp/frontend/client/GcompApplicationLauncher.class"
    // "/home/ssarnobat/my/usb/Professional/apache-pivot/helloworld/bin/MyTextArea.class"
    // "/home/ssarnobat/my/usb/Professional/call_graph_text_generator/lib/src/cassandra/src/src/bin/org/apache/cassandra/utils/ResourceWatcher.class"
        );
    try {
      byte[] classFileBytes = getBytesFromFile(classFile);
      ClassFileReader cfr = new ClassFileReader(classFileBytes, IClassFileReader.ALL);
      String className = new String(cfr.getClassName());
      // for (IMethodInfo m : cfr.getMethodInfos()) {
      // // m.
      // }
      System.out.println(className);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassFormatException e) {
      e.printStackTrace();
    }
  }

  private static byte[] getBytesFromFile(File file) throws IOException {
    InputStream is = new FileInputStream(file);

    // Get the size of the file
    long length = file.length();

    // You cannot create an array using a long type.
    // It needs to be an int type.
    // Before converting to an int type, check
    // to ensure that file is not larger than Integer.MAX_VALUE.
    if (length > Integer.MAX_VALUE) {
      // File is too large
    }

    // Create the byte array to hold the data
    byte[] bytes = new byte[(int) length];

    // Read in the bytes
    int offset = 0;
    int numRead = 0;
    while (offset < bytes.length
        && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
      offset += numRead;
    }

    // Ensure all the bytes have been read in
    if (offset < bytes.length) {
      throw new IOException("Could not completely read file " + file.getName());
    }

    // Close the input stream and return bytes
    is.close();
    return bytes;
  }

  @Test
  public void testJar() {
    String[] args = {
        "/home/ssarnobat/my/usb/Professional/call_graph_text_generator/text-callgraph/src/main/resources/lib/apache-cassandra-1.0.6.jar"
    // "/Users/sarnobat/Windows/usb/Professional/call_graph_text_generator/lib/apache-cassandra-1.0.6.jar"
        };
    Main.main(args);
  }

  @Test
  public void testDir() {
    String[] args =
        {"/Users/sarnobat/Windows/usb/Professional/call_graph_text_generator/cassandra/classes"};
    Main.main(args);
  }


  @Test
  public void testEmpty() {
    Main.main(null);

  }

  @Test
  public void testGwtRequestFactory() {
    String[] args =
        {"/Users/sarnobat/Windows/usb/Professional/GWT/gwt-2.4.0/requestfactory-server.jar"};
    Main.main(args);
  }

  @Test
  public void testGwtUser() {
    String[] args = {"/Users/sarnobat/Windows/usb/Professional/GWT/gwt-2.4.0/gwt-user.jar"};
    Main.main(args);
  }


  @Test
  public void testTomcat() {

    String[] args = {"/home/ssarnobat/trash/apache-tomcat-7.0.29/lib/catalina.jar" 
    		//"/usr/lib/intellij-idea-7.0/plugins/tomcat/lib/tomcat.jar"
        };
    Main.main(args);
  }

  @Test
  public void testJdbcConsole() {

    String[] args = {"/usr/lib/intellij-idea-10/plugins/DatabaseSupport/lib/jdbc-console.jar"};
    Main.main(args);
  }

  @Test
  public void testCommonsBeanUtils() {

    String[] args = {"/usr/lib/intellij-idea-7.0/plugins/maven/lib/commons-beanutils.jar"};
    Main.main(args);
  }

  @Test
  public void testCassandra() {

    String[] args = {"/home/ssarnobat/trash/apache-cassandra-1.1.1/lib/apache-cassandra-1.1.1.jar"};
    Main.main(args);
  }

  @Test
  public void testMyself() {
    String[] args = {
        "/home/ssarnobat/my/usb/Professional/call_graph_text_generator/text-callgraph/target/classes"};
    Main.main(args);

  }

  @Test
  public void stringTests() {
    Assert.assertTrue("com.google".contains("google"));
  }
}
