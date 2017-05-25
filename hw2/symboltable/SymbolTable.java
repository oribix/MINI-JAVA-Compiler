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

    //if(currScope == null)
    //  System.out.println("error: scope is null!");

    return currScope;
  }

  public Scope getGlobalScope() {
    Scope globalScope = scopeStack.getLast();

    //if(globalScope == null)
    //  System.out.println("error: scope is null!");

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
    return getGlobalScope().getFieldVar(varToken, classToken);
  }

  // Checks that var is NOT in method or args scope before trying to get index.
  public int getFieldVarIndex(NodeToken varToken, NodeToken classToken){
    if (scopeStack.size() < 4) {
      System.err.println("error: called SymbolTable.isFieldVar outside of method");
      System.exit(-1);
    }

    // Method, argument scopes (in that order)
    Scope[] scopes = {scopeStack.pop(), scopeStack.pop()};

    // Is in method or args scope.
    boolean isLocalVar = scopes[0].getSymbolData(varToken, SymbolType.ST_VARIABLE) != null ||
        scopes[1].getSymbolData(varToken, SymbolType.ST_VARIABLE) != null;

    int fieldVarIndex = -1; 
    if (!isLocalVar)
      fieldVarIndex = getGlobalScope().getFieldVarIndex(varToken, classToken);

    // put scopes back in stack
    for (int i = scopes.length - 1; i >= 0; i--)
      scopeStack.push(scopes[i]);

    return fieldVarIndex;
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
    return getGlobalScope().getMethodFromClass(classToken, methodToken);
  }

  public int getMethodIndexFromClass(NodeToken classToken, NodeToken methodToken) {
    return getGlobalScope().getMethodIndexFromClass(classToken, methodToken);
  }

  public void addFieldVarToClass(NodeToken classToken, NodeToken n, SymbolData data) {
    getGlobalScope().addFieldVarToClass(classToken, n, data);

    //getGlobalScope().PrintAll();
  }
  public int Size(){
	  return scopeStack.size();
  }

  public int getClassFieldSize(NodeToken classToken) {
    return getGlobalScope().getClassFieldSize(classToken);
  }

}
