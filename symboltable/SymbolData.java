package symboltable;
//import syntaxtree.*;

//Holds all info to be associated with a Symbol
public class SymbolData{

  protected SymbolType type;

  public SymbolData(SymbolType type){
    this.type = type;
  }

  public SymbolType getType() {
    return type;
  }

  public String getDeepType() {
    return null;
  }
}
