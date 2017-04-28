package symboltable;
import syntaxtree.*;
import java.util.HashMap;

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
        System.out.println("Error: case " + data.type + " not implemented yet.");
        System.out.println("\nADD CASE TO Scope.Java PLEASE!\n");
    }

    if (scope.containsKey(symbol)){
      System.err.println("error: class \"" + n + "\" not distinct");
      System.exit(-1);
    }
    else
      scope.put(symbol, data);

    // Debugging
    System.out.println("pushed \"" + n + ": " + data.type + "," + data.getDeepType() +"\" into scope");
    //for (Symbol s : scope.keySet())
    //  System.out.println("in scope: " + s);
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
        System.out.println("Error: Did not specify SymbolType");
        return null;
    }
  }

  // Used by SymbolTable for adding methods
  // Should ONLY be used on global scope. This function fails otherwise.
  protected void addMethodToClass(NodeToken classToken, MethodData methodData) {
    if (!scopeClasses.containsKey(new Symbol(classToken))) {
      System.err.println("error: attempted to add method in non-global scope\n");
      System.exit(-1);
    }

    ClassData classData = (ClassData) getSymbolData(classToken, SymbolType.ST_CLASS);
    classData.addMethod(methodData);
    System.out.println(classToken + "," + classData.getType() + " adds " + methodData.getDeepType());
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
}
