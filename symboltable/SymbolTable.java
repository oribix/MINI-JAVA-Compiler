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
    // Check stack of scopes
    for(Scope scope : scopeStack){
      SymbolData sd = scope.getSymbolData(n, st);
      if(sd != null) return sd;
    }

    return null;
  }

  //returns Symbol Data of the node passed in if it exists in the Symbol table
  //else null
  public SymbolData getSymbolData(NodeToken varToken, SymbolType st, NodeToken classToken){
    // Normal getSymbolData()
    SymbolData sd = getSymbolData(varToken, st);

    if (sd != null) return sd;

    // Check parents' class fields
    return getFieldVar(varToken, classToken);
  }

  private SymbolData getFieldVar(NodeToken varToken, NodeToken classToken) {
    if (varToken == null || classToken == null)
      return null;

    ClassData cd = (ClassData) getGlobalScope().getSymbolData(classToken, SymbolType.ST_CLASS);

    if (cd != null) {
      SymbolData fieldData = cd.getFieldVar(varToken);

      if (fieldData != null)
        return fieldData;

      return getFieldVar(varToken, cd.getParent());
    }

    return null;
  }

  public void addSymbol(NodeToken n, SymbolType type){
    Scope scope = getCurrentScope();
    scope.addSymbol(n, type);

    //scope.PrintAll();
  }

  public void addSymbol(NodeToken n, SymbolData data){
    Scope scope = getCurrentScope();
    scope.addSymbol(n, data);

    //scope.PrintAll();
  }

  public boolean classExists(NodeToken n) {
    return getGlobalScope().getSymbolData(n, SymbolType.ST_CLASS) != null;
  }

  public void addMethodToClass(NodeToken classToken, MethodData methodData) {
    getGlobalScope().addMethodToClass(classToken, methodData);

    //getGlobalScope().PrintAll();
  }

  public MethodData getMethodFromClass(NodeToken classToken, NodeToken methodToken) {
    // Later: edit this function to look at extended class parents
    return getGlobalScope().getMethodFromClass(classToken, methodToken);
  }

  public void addFieldVarToClass(NodeToken classToken, NodeToken n, SymbolData data) {
    getGlobalScope().addFieldVarToClass(classToken, n, data);

    //getGlobalScope().PrintAll();
  }


}
