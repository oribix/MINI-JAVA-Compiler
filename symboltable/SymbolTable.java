package symboltable;
import syntaxtree.*;
import java.util.ArrayDeque;

public class SymbolTable{

  //stack of scopes implemented with ArrayDeque
  //This is the main Data structure for the SymbolTable
  private ArrayDeque<Scope> scopeStack;
  private ClassRefChecker crc;

  //Symbol Table Constructor
  public SymbolTable(){
    scopeStack = new ArrayDeque<Scope>();
    newScope();
  }

  //Enter a new scope
  public void newScope(){
    scopeStack.push(new Scope());
  }

  //exits the current scope
  public void exitScope(){
    scopeStack.pop();
  }

  public Scope getCurrentScope(){
    Scope currScope = scopeStack.peek();

    if(currScope == null)
      System.out.println("error: scope is null!");

    return currScope;
  }

  public Scope getGlobalScope() {
    Scope globalScope = scopeStack.getLast();

    if(globalScope == null)
      System.out.println("error: scope is null!");

    return globalScope;
  }

  //returns Symbol Data of the node passed in if it exists in the Symbol table
  //else null
  public SymbolData getSymbolData(NodeToken n, SymbolType st){
    String name = n.toString();
    for(Scope scope : scopeStack){
      SymbolData sd = scope.getSymbolData(n, st);
      if(sd != null) return sd;
    }
    return null;
  }

  public void addSymbol(NodeToken n, SymbolType type){
    Scope scope = getCurrentScope();
    scope.addSymbol(n, type);
  }
}
