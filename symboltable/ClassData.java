package symboltable;
import java.util.Vector;
import java.util.HashMap;

import syntaxtree.NodeToken;

public class ClassData extends SymbolData {
  NodeToken className;
  Vector<MethodData> methods;
  HashMap<String, SymbolData> fields;
  NodeToken parent;

  public ClassData(NodeToken className) {
    super(SymbolType.ST_CLASS);
    this.className = className;
    methods = new Vector<>();
    fields = new HashMap<>();
    parent = null;
  }

  public ClassData(NodeToken className, NodeToken parentName) {
    super(SymbolType.ST_CLASS);
    this.className = className;
    methods = new Vector<>();
    fields = new HashMap<>();
    parent = parentName;
  }

  public void addMethod(MethodData methodData) {
    methods.add(methodData);
  }

  public Vector<MethodData> getMethods() {
    return methods;
  }

  public void addFieldVar(NodeToken n, SymbolData sd) {
    fields.put(n.toString(), sd);
  }

  public SymbolData getFieldVar(NodeToken n) {
    return fields.get(n.toString());
  }

  public NodeToken getClassName(){
    return className;
  }

  public NodeToken getParent() {
    return parent;
  }

  public boolean equals(Object o) {
    return o instanceof ClassData 
        && methods.equals(((ClassData) o).methods);
  }

  public int hashCode() {
    return methods.hashCode();
  }
}
