package symboltable;
import syntaxtree.*;

public class ClassVarData extends SymbolData {
  String className;

  public ClassVarData(NodeToken className) {
    super(SymbolType.ST_CLASS_VAR);
    this.className = className.toString();
  }

  public String getDeepType() {
    return className;
  }

  public boolean equals(Object o) {
    return o instanceof ClassVarData && className.equals(((ClassVarData) o).className);
  }

  public int hashCode() {
    return className.hashCode();
  }
}
