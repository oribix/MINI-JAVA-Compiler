import cs132.vapor.ast.VAssign;
import cs132.vapor.ast.VBranch;
import cs132.vapor.ast.VBuiltIn;
import cs132.vapor.ast.VCall;
import cs132.vapor.ast.VGoto;
import cs132.vapor.ast.VMemRead;
import cs132.vapor.ast.VMemRef;
import cs132.vapor.ast.VMemWrite;
import cs132.vapor.ast.VReturn;
import cs132.vapor.ast.VInstr.VisitorPR;

public class VVisitor extends 
  VisitorPR<String, String, RuntimeException> {
    
    
  public String visit(String s, VAssign a) throws RuntimeException {
    return "VAssign";
  }
  
  public String visit(String s, VBranch b) throws RuntimeException {
    String ifString = "";
    if(b.positive)
      ifString = "if";
    else
      ifString = "if0";

    System.out.println(ifString + " " + b.value + " goto " + b.target);

    return "VBranch";
  }
  
  public String visit(String s, VBuiltIn c) throws RuntimeException {
    return "VBuiltIn";
  }
  
  public String visit(String s, VCall c) throws RuntimeException {
    return "VCall";
  }
  
  public String visit(String s, VGoto g) throws RuntimeException {
    System.out.println("goto " + g.target);
    return "VGoto";
  }
  
  public String visit(String s, VMemRead r) throws RuntimeException {
    String base = ((VMemRef.Global)r.source).base.toString();
    int byteOffset = ((VMemRef.Global)r.source).byteOffset;

    if(base.equals("this") && byteOffset == 0)
      System.out.println(r.dest + " = [this]");
    else
      System.out.println(r.dest + " = [" + base + " + " +  byteOffset + "]");

    return "VMemRead";
  }
  
  public String visit(String s, VMemWrite w) throws RuntimeException {
    String base = ((VMemRef.Global)w.dest).base.toString();
    int byteOffset = ((VMemRef.Global)w.dest).byteOffset;

    String src = w.source.toString();

    if(byteOffset == 0)
      System.out.println("[" + base + "] = " + src);
    else
      System.out.println("[" + base + " + " + byteOffset + "] = " + src);

    return "VMemWrite";
  }
  
  public String visit(String s, VReturn r) throws RuntimeException {
    return "VReturn";
  }
}
