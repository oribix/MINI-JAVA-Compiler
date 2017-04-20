package symboltable;
import syntaxtree.*;
import java.util.HashMap;

//Wrapper for Hashmap which implements a scope
public class Scope{
  private HashMap<Symbol, SymbolData> scope;

  Scope(){
    scope = new HashMap<Symbol, SymbolData>();
  }

  void addSymbol(NodeToken n, SymbolType type){
    Symbol symbol = new Symbol(n);
    SymbolData sd = new SymbolData(type);
    scope.put(symbol, sd);
  }
}
