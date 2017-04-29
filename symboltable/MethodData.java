package symboltable;
import java.util.Vector;

import syntaxtree.NodeToken;

public class MethodData extends SymbolData {
  NodeToken methodName; 
  SymbolData returnType;
  Vector<SymbolData> parameterTypes;

  // nodeToken contains line and column info, which is why we need it in case of errors
  static public MethodData mainInstance(NodeToken mainToken) {
    MethodData md = new MethodData();
    md.methodName = mainToken;
    md.returnType = new SymbolData(SymbolType.ST_VOID);
    md.parameterTypes.add(new SymbolData(SymbolType.ST_STRING_ARR));

    return md;
  }

  public MethodData() {
    super(SymbolType.ST_METHOD);
    this.methodName = new NodeToken("");
    this.returnType = new SymbolData(SymbolType.ST_NULL);
    this.parameterTypes = new Vector<>();
  }

  public MethodData(NodeToken methodName, SymbolData returnType, Vector<SymbolData> parameterTypes) {
    super(SymbolType.ST_METHOD);
    this.methodName = methodName;
    this.returnType = returnType;
    this.parameterTypes = parameterTypes;

    if (parameterTypes == null)
      this.parameterTypes = new Vector<>();
  }

  // Builds type string from specifications
  public String methodType() {
    StringBuilder builder = new StringBuilder();
    builder.append('(');

    // Add arguments to type string
    for (int i = 0; i < parameterTypes.size() - 1; i++)
      builder.append(parameterTypes.get(i).getFormalType()).append(',');

    if (!parameterTypes.isEmpty())
      builder.append(parameterTypes.get(parameterTypes.size() - 1).getFormalType());

    builder.append(")->").append(returnType.getFormalType());

    return builder.toString();
  }
  
  public String getDeepType() {
    return methodType();
  }

  public NodeToken getName() {
    return methodName;
  }

  public SymbolData getReturnType() {
    return returnType;
  }

  public Vector<SymbolData> getParameterTypes() {
    return parameterTypes;
  }

  public boolean equals(Object o) {
    if (o instanceof MethodData) {
      MethodData md = (MethodData) o;
      return hashCode() == md.hashCode();
    }

    return false;
  }

  public int hashCode() {
    String uniqueType = methodType() + ": " + methodName.toString();
    return uniqueType.hashCode();
  }
}
