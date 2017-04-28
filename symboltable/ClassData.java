package symboltable;
import java.util.Vector;

import syntaxtree.NodeToken;

public class ClassData extends SymbolData {
  Vector<MethodData> methods;

  public ClassData(NodeToken className) {
    super(SymbolType.ST_CLASS);
    methods = new Vector<>();
  }

  public void addMethod(MethodData methodData) {
    methods.add(methodData);
  }

  public Vector<MethodData> getMethods() {
    return methods;
  }
}
