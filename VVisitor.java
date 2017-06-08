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
import java.util.LinkedHashSet;

public class VVisitor extends Visitor<RuntimeException> {
  HashMap<String, String> varRegMap;
  Vector<varLiveness> liveList;

  public VVisitor(HashMap<String, String> varRegMap, Vector<varLiveness> liveList) {
    this.varRegMap = varRegMap;
    this.liveList = liveList;
  }

  public void visit(VAssign a) throws RuntimeException {
    // FIXME: You can print a.dest, which contains the variable. For now, just print $t0.
    System.out.println("$t0" + " = " + a.source);
  }

  public void visit(VBranch b) throws RuntimeException {
    String ifString = "";
    if(b.positive)
      ifString = "if";
    else
      ifString = "if0";

    System.out.println(ifString + " " + b.value + " goto " + b.target);
  }

  public void visit(VBuiltIn c) throws RuntimeException {
    String code = new String();
    if(c.dest != null)
    {
      code = c.dest + " = " + c.op.name + "(";
    }
    else
    {
      code = c.op.name + "(";
    }
    String argz = new String();
    for(VOperand arg : c.args) {
      argz = argz + " " + arg.toString();
    }
    argz = argz.substring(1);
    code += argz;
    code += ")";
    System.out.println(code);
  }

  public void visit(VCall c) throws RuntimeException {


    // The line of code that calls a function with arguments
    String code = new String();
    if(c.dest != null)
    {
      code = c.dest + " = call " + c.addr + "(";
    }
    else
    {
      code = c.addr + "(";
    }
    String argz = new String();
    for(VOperand arg : c.args) {
      argz = argz + " " + arg.toString();
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
      System.out.println(r.dest + " = [this]");
    }
    else
    {
      System.out.println(r.dest + " = [" + base + " + " +  byteOffset + "]");
    }
  }

  public void visit(VMemWrite w) throws RuntimeException {
    String base = ((VMemRef.Global)w.dest).base.toString();
    int byteOffset = ((VMemRef.Global)w.dest).byteOffset;

    String src = w.source.toString();
    if(byteOffset == 0)
    {
      System.out.println("[" + base + "] = " + src);
    }
    else
    {
      System.out.println("[" + base + " + " + byteOffset + "] = " + src);
    }
  }

  public void visit(VReturn r) throws RuntimeException {
    if(r.value == null) {
      System.out.println("ret");
    }
    else {
      System.out.println("$v0 = " + r.value);
      System.out.println("ret"); //store return value in $v0
    }
  }

  private LinkedHashSet<String> calcTRegBackup() {
    for (varLiveness vl : liveList) {
    }
  }
}
