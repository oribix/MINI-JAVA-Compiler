import cs132.vapor.ast.VAssign;
import cs132.vapor.ast.VBranch;
import cs132.vapor.ast.VBuiltIn;
import cs132.vapor.ast.VCall;
import cs132.vapor.ast.VGoto;
import cs132.vapor.ast.VMemRead;
import cs132.vapor.ast.VMemWrite;
import cs132.vapor.ast.VReturn;
import cs132.vapor.ast.VInstr.VisitorPR;

public class VVisitor extends 
  VisitorPR<String, String, RuntimeException> {
    
    
  public String visit(String s, VAssign a) throws RuntimeException {
    return "thanks";
  }
  
  public String visit(String s, VBranch b) throws RuntimeException {
    return "thanks";
  }
  
  public String visit(String s, VBuiltIn c) throws RuntimeException {
    return "thanks";
  }
  
  public String visit(String s, VCall c) throws RuntimeException {
    return "thanks";
  }
  
  public String visit(String s, VGoto g) throws RuntimeException {
    return "thanks";
  }
  
  public String visit(String s, VMemRead r) throws RuntimeException {
    return "thanks";
  }
  
  public String visit(String s, VMemWrite w) throws RuntimeException {
    return "thanks";
  }
  
  public String visit(String s, VReturn r) throws RuntimeException {
    return "thanks";
  }
}
