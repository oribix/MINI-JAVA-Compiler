package symboltable;
import java.util.HashMap;
import java.util.Vector;

import syntaxtree.NodeToken;

public class ClassRefChecker {
  /* string as key to quickly find classes in map
   * Vector<NodeToken/MethodData> to store line and column number info for error messages */
  HashMap<String, Vector<NodeToken>> classMap;
  HashMap<String, Vector<MethodData>> methodMap;
  SymbolTable symbolTable;  // For typechecking backpatched methods

  public ClassRefChecker(SymbolTable table) {
    classMap = new HashMap<>();
    methodMap = new HashMap<>();
    symbolTable = table;
  }

  // Puts classMap into map of classMap that do not exist yet 
  public void verifyClassExists(NodeToken classToken) {
    Vector<NodeToken> nodes = classMap.get(classToken.toString());
    if (nodes == null) {
      classMap.put(classToken.toString(), new Vector<>());
      nodes = classMap.get(classToken.toString());
    }

    nodes.add(classToken);
  }

  // Puts classMap into map of classMap that do not exist yet 
  public void verifyMethodExists(NodeToken classToken, MethodData methodData) {
    Vector<MethodData> data = methodMap.get(classToken.toString());
    if (data == null) {
      methodMap.put(classToken.toString(), new Vector<>());
      data = methodMap.get(classToken.toString());
    }

    data.add(methodData);
  }

  // Called whenever class is created to remove objects of this class from classMap
  public void notifyClassExists(NodeToken classToken) {
    classMap.remove(classToken.toString());

    // Must not just remove methods. Have to typecheck them with original methods
    Vector<MethodData> methodData = methodMap.get(classToken.toString());
    if (methodData != null) {
      for (MethodData md : methodData) {
        MethodData origMD = symbolTable.getMethodFromClass(classToken, md.getName());
        if (origMD != null)
          System.out.println("In notify: " + origMD.getDeepType());
      }
    }

    System.out.println("In notify: " + classToken);
  }

  // Called at end of Goal to ensure no classes were unverified
  public void checkClassesExisted() {
    if (!classMap.isEmpty()) {
      System.err.println("Error: class still exists");
      System.err.println(classMap.toString());
      System.exit(-1);
      // Print real error message that specifies line number and column number
    }
    else
      System.out.println("Debug in ClassRefChecker: list is empty, good job.");
  }
}
