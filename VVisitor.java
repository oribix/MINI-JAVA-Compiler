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
    int aRegCnt = 0;
    for(VOperand arg : c.args) {
      if(aRegCnt < 4) { // If an $a register is available
        String regName = "$a" + aRegCnt;
        //paramRegMap.put(param.toString(), regName);
        //System.out.println(regName + " = " + getReg(arg.toString()));
        ++aRegCnt;
      }
      else {
        // If an $a register is not available, spill to out stack
        String regName = "out[" + aRegCnt + ']';
        //paramRegMap.put(param.toString(), regName); 
        //System.out.println(regName + " = " + getReg(arg.toString()));
        ++aRegCnt;
      }
    }
    String Out = new String();
    if(c.dest != null) {
      Out += getReg(c.dest) + " = ";
    }
    Out += c.op.name + '(';
    aRegCnt = 0;
    for(VOperand arg : c.args) {
              Out += getReg(arg.toString()) + ' ';

    }
    Out = Out.substring(0, Out.length() -1);
    Out += ')';
    System.out.println(Out);
  }

  public void visit(VCall c) throws RuntimeException {


    // The line of code that calls a function with arguments
    int aRegCnt = 0;
    for(VOperand arg : c.args) {
      if(aRegCnt < 4) { // If an $a register is available
        String regName = "$a" + aRegCnt;
        //paramRegMap.put(param.toString(), regName);
        System.out.println(regName + " = " + getReg(arg.toString()));
        ++aRegCnt;
      }
      else {
        // If an $a register is not available, spill to out stack
        String regName = "out[" + aRegCnt + ']';
        //paramRegMap.put(param.toString(), regName); 
        System.out.println(regName + " = " + getReg(arg.toString()));
        ++aRegCnt;
      }
    }
    System.out.println("call "+ getReg(c.dest));
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
        Out += " = [" + getReg(base) + " + " +  byteOffset;
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
