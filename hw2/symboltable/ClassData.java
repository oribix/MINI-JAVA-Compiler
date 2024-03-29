package symboltable;
import java.util.Vector;
import java.util.LinkedHashMap;

import syntaxtree.NodeToken;

public class ClassData extends SymbolData {
  NodeToken className;
  Vector<MethodData> methods;
  LinkedHashMap<String, SymbolData> fields;
  NodeToken parent;

  public ClassData(NodeToken className) {
    super(SymbolType.ST_CLASS);
    this.className = className;
    methods = new Vector<>();
    fields = new LinkedHashMap<>();
    parent = null;
  }

  public ClassData(NodeToken className, NodeToken parentName) {
    super(SymbolType.ST_CLASS);
    this.className = className;
    methods = new Vector<>();
    fields = new LinkedHashMap<>();
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

  public int getFieldVarIndex(NodeToken n) {
    String varName = n.toString();
    int index = 0;
    for (String s : fields.keySet()) {
      if (varName.equals(s))
        return index;
      index++;
    }

    return -1;
  }

  public int getFieldSize() {
    return fields.size();
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
