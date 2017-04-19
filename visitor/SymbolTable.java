package visitor;
import syntaxtree.*;
import java.util.ArrayDeque;
import java.util.HashMap;

public class SymbolTable{

  //stack of scopes implemented with ArrayDeque
  //This is the main Data structure for the SymbolTable
  private ArrayDeque<Scope> scopeStack;

  //Wrapper for Hashmap which implements a scope
  private class Scope{
    private HashMap<Symbol, SymbolType> scope;

    Scope(){
      scope = new HashMap<Symbol, SymbolType>();
    }

    void addSymbol(NodeToken n, SymbolType type){
      Symbol symbol = new Symbol(n);
      scope.put(symbol, type);
    }
  }

  //Symbol Class
  private class Symbol{
    String name;

    public Symbol(NodeToken n){
      name = n.toString();
    }
  };

  public enum SymbolType {ST_NULL, ST_INT, ST_CLASS};

  //Holds all info to be associated with a Symbol
  private class SymbolData{
    SymbolType type;
  }

  //Symbol Table Constructor
  public SymbolTable(){
    scopeStack = new ArrayDeque<Scope>();
    newScope();
  }

  //Enter a new scope
  void newScope(){
    scopeStack.push(new Scope());
  }

  //exits the current scope
  void exitScope(){
    scopeStack.pop();
  }

}
