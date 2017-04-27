package symboltable;
import java.util.HashMap;
import java.util.Vector;

import syntaxtree.NodeToken;

public class ClassRefChecker {
  /* string as key to quickly find classes in map
   * Vector<NodeToken> to store line and column number info for error messages */
  HashMap<String, Vector<NodeToken>> classMap;

  public ClassRefChecker() {
    classMap = new HashMap<>();
  }

  /* Puts classMap into map of classMap that do not exist yet
   * If unknown class is referred to twice, we do not overwrite
   * the old NodeToken in the map */
  public void verifyClassExists(NodeToken classToken) {
    Vector<NodeToken> nodes = classMap.get(classToken.toString());
    if (nodes == null) {
      classMap.put(classToken.toString(), new Vector<>());
      nodes = classMap.get(classToken.toString());
    }

    System.out.println("In verify: " + classToken);

    nodes.add(classToken);
  }

  // Called whenever class is created to remove objects of this class from classMap
  public void notifyClassExists(NodeToken classToken) {
    classMap.remove(classToken.toString());

    System.out.println("In notify: " + classToken);
  }

  // Called at end of Goal to ensure no classes were unverified
  public void checkClassesExisted() {
    if (!classMap.isEmpty()) {
      System.err.println("Error: class still exists");
      System.err.println(classMap.toString());
      // Print real error message that specifies line number and column number
    }
    else
      System.out.println("Debug in ClassRefChecker: list is empty, good job.");
  }
}
