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
    Symbol symbol = new Symbol(n);
    SymbolData sd = new SymbolData(type);

    // Error check: no doubles of an identifier in relevant scope
    HashMap<Symbol, SymbolData> scope = null;
    switch (type) {
      case ST_INT:
      case ST_INT_ARR:
      case ST_BOOLEAN:      // For now, just do ST_STRING_ARR code. Change if needed.
      case ST_STRING_ARR:
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
        System.out.println("Error check for scope not implemented yet.");
    }

    if (scope.containsKey(symbol))
      System.err.println("error: class \"" + n + "\" not distinct");
    else
      scope.put(symbol, sd);

    //for (Symbol s : scope.keySet())
    //  System.out.println("in scope: " + s);

    // Debugging
    System.out.println("pushed " + n + ": " + type + " into scope");
  }

  public SymbolData getSymbolData(NodeToken n, SymbolType type) {
    switch (type) {
      case ST_INT:
        return null;
      case ST_CLASS:
        return scopeClasses.get(new Symbol(n));
      case ST_CLASS_EXTENDS:
        return scopeClasses.get(new Symbol(n));
      default:
        return null;
    }
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
