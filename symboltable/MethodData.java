package symboltable;
import java.util.Vector;

import syntaxtree.NodeToken;

// Work on this later, Daniel! First, the ClassRefChecker!
public class MethodData extends SymbolData {
  String methodName;
  SymbolType returnType;
  Vector<SymbolType> parameterTypes;

  static public MethodData mainInstance() {
    MethodData md = new MethodData();
    md.methodName = "main";
    md.returnType = SymbolType.ST_VOID;
    md.parameterTypes.add(SymbolType.ST_STRING_ARR);

    return md;
  }

  public MethodData() {
    super(SymbolType.ST_METHOD);
    this.methodName = "";
    this.returnType = SymbolType.ST_NULL;
    this.parameterTypes = new Vector<>();
  }

  public MethodData(NodeToken methodName, SymbolType returnType, Vector<SymbolType> parameterTypes) {
    super(SymbolType.ST_METHOD);
    this.methodName = methodName.toString();
    this.returnType = returnType;
    this.parameterTypes = parameterTypes;
  }

  // Builds type string from specifications
  public String methodType() {
    StringBuilder builder = new StringBuilder();
    builder.append('(');

    // Add arguments to type string
    for (int i = 0; i < parameterTypes.size() - 1; i++)
      builder.append(parameterTypes.get(i)).append(',');

    if (!parameterTypes.isEmpty())
      builder.append(parameterTypes.get(parameterTypes.size() - 1));

    builder.append(")->").append(returnType);

    return builder.toString();
  }
  
  public String getDeepType() {
    return methodType();
  }
}
