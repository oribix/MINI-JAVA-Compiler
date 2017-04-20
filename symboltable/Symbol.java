package symboltable;
import syntaxtree.*;

//Symbol Class
public class Symbol{
  String name;

  public Symbol(NodeToken n){
    name = n.toString();
  }
};
