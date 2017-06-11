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

  private String getReg(Object o){
    String reg = varRegMap.get(o.toString());
    if(reg != null) return reg;
    else return o.toString();
  }

  public void visit(VAssign a) throws RuntimeException {
    // Surprising note: we can't have both left and right side as stack variables
    // Soln: pass right hand side to $v0 first
    String lhs = getReg(a.dest), rhs = getReg(a.source);
    if (lhs.charAt(0) != '$' && rhs.charAt(0) != '$') {
      System.out.println("$v0 = " + rhs);
      rhs = "$v0";
    }

    // Print the assignment
    System.out.println(lhs + " = " + rhs);
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
    String Out = new String();

    // to store return value 
    if(c.dest != null) {
      Out += getReg(c.dest) + " = ";
    }

    // get name of builtin function 
    Out += c.op.name + '(';

    // arguments inside parentheses (at most 2)
    boolean usedv0 = false; 
    for(VOperand arg : c.args) {
      String argStr = getReg(arg.toString());

      // Argument can't be from stack. Put into $v# register before builtin call.
      if (argStr.charAt(0) != '$') {
        // No one is using v0-v1 right now. This should be fine. (Also it was a lesani hint)
        String vArg = "$v0";
        if (usedv0)
          vArg = "$v1";
        else
          usedv0 = true;

        System.out.println(vArg + " = " + argStr);
        argStr = vArg;
      }

      // Print argument
      Out += argStr + ' ';
    }

    // End parenthesis
    Out = Out.substring(0, Out.length() -1);  // Remove extra space
    Out += ')';
    System.out.println(Out);
  }

  public void visit(VCall c) throws RuntimeException {
    // The code that puts arguments into a registers
    for(int aRegCnt = 0; aRegCnt < c.args.length; aRegCnt++) {
      VOperand arg = c.args[aRegCnt];

      // Avoid using a registers on right hand side
      String srcName = getReg(arg.toString());
      if (srcName.startsWith("$a"))
        srcName = "in[" + srcName.charAt(2) + "]";

      if(aRegCnt < 4) { 
        // If an $a register is available
        String regName = "$a" + aRegCnt;
        System.out.println(regName + " = " + srcName);
      }
      else {
        // If an $a register is not available, spill to out stack
        String regName = "out[" + aRegCnt + ']';

        // LHS and RHS can't both be from stack (See VAssign)
        if (srcName.charAt(0) != '$') {
          System.out.println("$v0 = " + srcName);
          srcName = "$v0";
        }

        System.out.println(regName + " = " + srcName);
      }
    }

    // The line of code that calls a function with arguments
    System.out.println("call "+ getReg(c.addr));
  }

  public void visit(VGoto g) throws RuntimeException {
    System.out.println("goto " + g.target);
  }

  public void visit(VMemRead r) throws RuntimeException {
    String base = ((VMemRef.Global)r.source).base.toString();
    int byteOffset = ((VMemRef.Global)r.source).byteOffset;
    if(base.equals("this") && byteOffset == 0)
    {
      System.out.println(getReg(r.dest) + " = [" + getReg("this") + "]");
    }
    else
    {
      String Out = getReg(r.dest) + " = [" + getReg(base);
      if(byteOffset != 0)
      {
        Out += " + " +  byteOffset;
      }
      Out += ']';
      System.out.println(Out);
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
    if(r.value != null) {
      System.out.println("$v0 = " + getReg(r.value));
    }
  }

  private LinkedHashSet<String> calcTRegBackup() {
    LinkedHashSet<String> tRegBackup = new LinkedHashSet<>();

    for (varLiveness vl : liveList) {

    }

    return tRegBackup;
  }
}
