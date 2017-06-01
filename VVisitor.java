import cs132.vapor.ast.VAssign;
import cs132.vapor.ast.VBranch;
import cs132.vapor.ast.VBuiltIn;
import cs132.vapor.ast.VCall;
import cs132.vapor.ast.VGoto;
import cs132.vapor.ast.VMemRead;
import cs132.vapor.ast.VMemWrite;
import cs132.vapor.ast.VReturn;
import cs132.vapor.ast.VInstr.VisitorPR;
import cs132.vapor.ast.VOperand;
import cs132.vapor.ast.VMemRef;

public class VVisitor extends 
  VisitorPR<String, String, RuntimeException> {
    
    
  public String visit(String s, VAssign a) throws RuntimeException {
    return "VAssign";
  }
  
  public String visit(String s, VBranch b) throws RuntimeException {
    return "VBranch";
  }
  
  public String visit(String s, VBuiltIn c) throws RuntimeException {
    String code = new String();
    if(c.dest != null)
      code = c.dest + " = " + c.op.name + "(";
    else
      code = c.op.name + "(";
    String argz = new String();
    for(VOperand arg : c.args) {
      argz = argz + " " + arg.toString();
    }
    argz = argz.substring(1);
    code += argz;
    code += ")";
    System.out.println(code);
    return "VBuiltIn";
  }
  
  public String visit(String s, VCall c) throws RuntimeException {
    String code = new String();
    if(c.dest != null)
      code = c.dest + " = call " + c.addr + "(";
    else
      code = c.addr + "(";
    String argz = new String();
    for(VOperand arg : c.args) {
      argz = argz + " " + arg.toString();
    }
    argz = argz.substring(1);
    code += argz;
    code += ")";
    System.out.println(code);
    return "VCall";
  }
  
  public String visit(String s, VGoto g) throws RuntimeException {
    return "VGoto";
  }
  
  public String visit(String s, VMemRead r) throws RuntimeException {
    // System.out.println(r.dest + " = " + "[" + ((VMemRef.Global)r.source).base + "]");
    return "VMemRead";
  }
  
  public String visit(String s, VMemWrite w) throws RuntimeException {
    // System.out.println("[" + ((VMemRef.Global)w.dest).base + "] = " + w.source);
    return "VMemWrite";
  }
  
  public String visit(String s, VReturn r) throws RuntimeException {
    if(r.value == null) {
      System.out.println("ret");
    }
    else {
      System.out.println("ret"); //store return value in $v0
    }
    return "VReturn";
  }
}
