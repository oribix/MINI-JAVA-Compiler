package symboltable;

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

  // Intended to return the "most formal" type.
  // So if getDeepType is not null, then return that.
  // Otherwise, return getType
  public String getFormalType() {
    if (getDeepType() == null)
      return getType().toString();
    return getDeepType();
  }

  public boolean equals(Object o) {
    return o instanceof SymbolData && type.equals(((SymbolData) o).type);
  }

  public int hashCode() {
    return type.hashCode();
  }
}
