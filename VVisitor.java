import cs132.vapor.ast.VAssign;
import cs132.vapor.ast.VBranch;
import cs132.vapor.ast.VBuiltIn;
import cs132.vapor.ast.VCall;
import cs132.vapor.ast.VGoto;
import cs132.vapor.ast.VMemRead;
import cs132.vapor.ast.VMemRef;
import cs132.vapor.ast.VMemWrite;
import cs132.vapor.ast.VReturn;
import cs132.vapor.ast.VInstr.Visitor;
import cs132.vapor.ast.VOperand;
import cs132.vapor.ast.VMemRef;
import cs132.vapor.ast.VVarRef;
import java.util.HashMap;
import java.util.Vector;
import java.util.Objects;

public class VVisitor extends Visitor<RuntimeException> {
  HashMap<String, String> varRegMap;

  public VVisitor(HashMap<String, String> varRegMap) {
    this.varRegMap = varRegMap;
  }

  String getReg(Object o){
    String reg = varRegMap.get(o.toString());
    if(reg != null) return reg;
    else return o.toString();
  }

  public void visit(VAssign a) throws RuntimeException {
    System.out.println(getReg(a.dest) + " = " + getReg(a.source));
  }

  public void visit(VBranch b) throws RuntimeException {
    String ifString = "";
    if(b.positive)
      ifString = "if";
    else
      ifString = "if0";

    System.out.println(ifString + " " + getReg(b.value) + " goto " + getReg(b.target));
  }

  public void visit(VBuiltIn c) throws RuntimeException {
    String code = new String();
    if(c.dest != null)
    {
      code = getReg(c.dest) + " = " + c.op.name + "(";
    }
    else
    {
      code = c.op.name + "(";
    }
    String argz = new String();
    for(VOperand arg : c.args) {
      argz = argz + " " + getReg(arg);
    }
    argz = argz.substring(1);
    code += argz;
    code += ")";
    System.out.println(code);
  }

  public void visit(VCall c) throws RuntimeException {
    String code = new String();
    if(c.dest != null)
    {
      code = getReg(c.dest) + " = call " + getReg(c.addr) + "(";
    }
    else
    {
      code = getReg(c.addr) + "(";
    }
    String argz = new String();
    for(VOperand arg : c.args) {
      argz = argz + " " + getReg(arg);
    }
    argz = argz.substring(1);
    code += argz;
    code += ")";
    System.out.println(code);
  }

  public void visit(VGoto g) throws RuntimeException {
    System.out.println("goto " + g.target);
  }

  public void visit(VMemRead r) throws RuntimeException {
    String base = ((VMemRef.Global)r.source).base.toString();
    int byteOffset = ((VMemRef.Global)r.source).byteOffset;
    if(base.equals("this") && byteOffset == 0)
    {
      System.out.println(getReg(r.dest) + " = [this]");
    }
    else
    {
      System.out.println(getReg(r.dest) + " = [" + getReg(base) + " + " +  byteOffset + "]");
    }
  }

  public void visit(VMemWrite w) throws RuntimeException {
    String base = ((VMemRef.Global)w.dest).base.toString();
    int byteOffset = ((VMemRef.Global)w.dest).byteOffset;

    String src = w.source.toString();
    if(byteOffset == 0)
    {
      System.out.println("[" + getReg(base) + "] = " + getReg(src));
    }
    else
    {
      System.out.println("[" + getReg(base) + " + " + byteOffset + "] = " + getReg(src));
    }
  }

  public void visit(VReturn r) throws RuntimeException {
    if(r.value == null) {
      System.out.println("ret");
    }
    else {
      System.out.println("$v0 = " + getReg(r.value));
      System.out.println("ret"); //store return value in $v0
    }
  }
}
