package symboltable;
import syntaxtree.*;

//Symbol Class
public class Symbol{
  String name;

  public Symbol(NodeToken n){
    name = n.toString();
  }

  @Override
  public boolean equals(Object o) {
    return o == null ? false : this.name.equals(((Symbol) o).name);
  }
};
