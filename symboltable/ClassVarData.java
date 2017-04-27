package symboltable;
import java.util.Vector;

import syntaxtree.*;

public class ClassVarData extends SymbolData {
  String className;
  //Vector<MethodData> methods; // Do after ClassRefChecker

  public ClassVarData(NodeToken className) {
    super(SymbolType.ST_CLASS_VAR);
    this.className = className.toString();
  }

  public String getDeepType() {
    return className;
  }
}
