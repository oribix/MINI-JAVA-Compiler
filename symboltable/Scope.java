package symboltable;
import syntaxtree.*;
import java.util.HashMap;
import java.util.Vector;

//Wrapper for Hashmap which implements a scope
public class Scope{
  protected HashMap<Symbol, SymbolData>
    scopeClasses,
    scopeMethods;

  private HashMap<Symbol, SymbolData>
    scopeVariables;

  Scope(){
    scopeClasses = new HashMap<Symbol, SymbolData>();
    scopeMethods = new HashMap<Symbol, SymbolData>();
    scopeVariables = new HashMap<Symbol, SymbolData>();
  }

  public void addSymbol(NodeToken n, SymbolType type){
    addSymbol(n, new SymbolData(type));
  }

  public void addSymbol(NodeToken n, SymbolData data){
    Symbol symbol = new Symbol(n);

    // Error check: no doubles of an identifier in relevant scope
    HashMap<Symbol, SymbolData> scope = null;
    switch (data.type) {
      case ST_INT:
      case ST_INT_ARR:
      case ST_BOOLEAN:      // For now, just do ST_STRING_ARR code. Change if needed.
      case ST_STRING_ARR:
      case ST_CLASS_VAR:
        scope = scopeVariables;
      break;

      case ST_METHOD:
        scope = scopeMethods;
      break;

      case ST_CLASS:
      case ST_CLASS_EXTENDS:
        scope = scopeClasses;
      break;

      default:
        //System.out.println("Error: case " + data.type + " not implemented yet.");
        //System.out.println("\nADD CASE TO Scope.Java PLEASE!\n");
    }

    if (scope.containsKey(symbol)){
      DebugErr("error: \"" + n + "\" not distinct");
      System.exit(-1);
    }
    else
      scope.put(symbol, data);
  }

  public SymbolData getSymbolData(NodeToken n, SymbolType type) {
    switch (type) {
      case ST_VARIABLE:
        return scopeVariables.get(new Symbol(n));
      case ST_METHOD:
        return scopeMethods.get(new Symbol(n));
      case ST_CLASS:
      case ST_CLASS_EXTENDS:
        return scopeClasses.get(new Symbol(n));
      default:
        //System.out.println("Error: Did not specify SymbolType");
        return null;
    }
  }

  // Used by SymbolTable for adding methods
  // Should ONLY be used on global scope. This function fails otherwise.
  protected void addMethodToClass(NodeToken classToken, MethodData methodData) {
    if (!scopeClasses.containsKey(new Symbol(classToken))) {
      DebugErr("error: attempted to add method in non-global scope\n");
      System.exit(-1);
    }

    ClassData classData = (ClassData) getSymbolData(classToken, SymbolType.ST_CLASS);
    classData.addMethod(methodData);
    //System.out.println(classToken + "," + classData.getType() + " adds " + methodData.getDeepType());
  }

  // Used by SymbolTable for retrieving methods
  // Should ONLY be used on global scope. This function fails otherwise.
  protected MethodData getMethodFromClass(NodeToken classToken, NodeToken methodToken) {
    if (!scopeClasses.containsKey(new Symbol(classToken))) {
      DebugErr("error: attempted to retrieve method in non-global scope\n");
      System.exit(-1);
    }

    ClassData classData = (ClassData) getSymbolData(classToken, SymbolType.ST_CLASS);
    String methodName = methodToken.toString();
    for (MethodData methodData : classData.getMethods())
      if (methodData.getName().toString().equals(methodName))
        return methodData;

    // Parent class may have function
    if (classData.getParent() != null)
      return getMethodFromClass(classData.getParent(), methodToken);

    return null;
  }

  protected int getMethodIndexFromClass(NodeToken classToken, NodeToken methodToken) {
    if (!scopeClasses.containsKey(new Symbol(classToken))) {
      DebugErr("error: attempted to retrieve method in non-global scope\n");
      System.exit(-1);
    }

    // Look for method in list of methods
    ClassData classData = (ClassData) getSymbolData(classToken, SymbolType.ST_CLASS);
    String methodName = methodToken.toString();
    Vector<MethodData> methods = classData.getMethods();
    for (int i = 0; i < methods.size(); i++)
      if (methods.get(i).getName().toString().equals(methodName))
        return i + getMethodIndexHelper(classData.getParent()); // Index + size of parent method lists

    // Parent class may have function
    if (classData.getParent() != null)
      return getMethodIndexFromClass(classData.getParent(), methodToken);

    return -1;
  }

  // To get parent method list sizes
  private int getMethodIndexHelper(NodeToken classToken) {
    if (classToken == null)
      return 0;

    ClassData classData = (ClassData) getSymbolData(classToken, SymbolType.ST_CLASS);
    return classData.getMethods().size() + getMethodIndexHelper(classData.getParent());
  }

  public void addFieldVarToClass(NodeToken classToken, NodeToken n, SymbolData data) {
    if (!scopeClasses.containsKey(new Symbol(classToken))) {
      DebugErr("error: attempted to add method in non-global scope\n");
      System.exit(-1);
    }

    ClassData classData = (ClassData) getSymbolData(classToken, SymbolType.ST_CLASS);
    classData.addFieldVar(n, data);
    //System.out.println(classToken + "," + classData.getType() + " adds " + n + ", " + data.getFormalType());
  }

  protected int getClassFieldSize(NodeToken classToken) {
    if (!scopeClasses.containsKey(new Symbol(classToken))) {
      DebugErr("error: attempted to retrieve class in non-global scope\n");
      System.exit(-1);
    }

    ClassData classData = (ClassData) getSymbolData(classToken, SymbolType.ST_CLASS);

    // if parent exists
    if (classData.getParent() != null) {
      ClassData parentClassData = (ClassData) scopeClasses.get(new Symbol(classData.getParent()));
      return classData.getFieldSize() + parentClassData.getFieldSize();
    }

    return classData.getFieldSize();
  }

  // All field var index functions are for VAPOR code generation
  public int getFieldVarIndex(NodeToken varToken, NodeToken classToken) {
    // Any arg is null
    if (varToken == null || classToken == null)
      return -1;

    // function not called in global scope
    if (!scopeClasses.containsKey(new Symbol(classToken))) {
      DebugErr("error: attempted to retrieve class in non-global scope\n");
      System.exit(-1);
    }

    // Look for field index of varToken
    ClassData cd = (ClassData) getSymbolData(classToken, SymbolType.ST_CLASS);
    if (cd != null) {
      int index = cd.getFieldVarIndex(varToken);

      if (index != -1)
        return index + getFieldVarIndexHelper(cd.getParent());

      return getFieldVarIndex(varToken, cd.getParent());
    }

    return -1;
  }

  private int getFieldVarIndexHelper(NodeToken classToken) {
    if (classToken == null)
      return 0;

    ClassData classData = (ClassData) getSymbolData(classToken, SymbolType.ST_CLASS);
    return classData.getFieldSize() + getFieldVarIndexHelper(classData.getParent());
  }

  // Debugging code
  public void PrintAll() {
    System.out.println("Classes:");
    for (Symbol s : scopeClasses.keySet())
      System.out.println(s);
    System.out.println("Methods:");
    for (Symbol s : scopeMethods.keySet())
      System.out.println(s);
    System.out.println("Variables:");
    for (Symbol s : scopeVariables.keySet())
      System.out.println(s);
  }

  private void DebugErr(String s) {
    //System.err.println(s);
    System.err.println("Type error");
  }
}
