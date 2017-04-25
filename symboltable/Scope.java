package symboltable;
import syntaxtree.*;
import java.util.HashMap;

//Wrapper for Hashmap which implements a scope
public class Scope{
  private HashMap<Symbol, SymbolData> scopeClasses,
      scopeMethods,
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
    switch (type) {
      case ST_BOOLEAN:      // For now, just do ST_STRING_ARR code. Change if needed.
      case ST_INT:          
      case ST_INT_ARR:
      case ST_STRING_ARR:
        if (scopeVariables.containsKey(symbol))
          System.err.println("error: variable \"" + n + "\" not distinct");
        else
          scopeClasses.put(symbol, sd);
        
        break;

      case ST_CLASS_EXTENDS: // For now, just do ST_CLASS code. Change if needed.
      case ST_CLASS:
        if (scopeClasses.containsKey(symbol))
          System.err.println("error: class \"" + n + "\" not distinct");
        else
          scopeClasses.put(symbol, sd);
          
        break;

      default:
        System.out.println("Error check for scope not implemented yet.");
    }


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
