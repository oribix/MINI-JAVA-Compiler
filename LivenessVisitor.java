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
import java.util.Vector;
import java.util.Objects;

public class LivenessVisitor extends Visitor<RuntimeException> {
  public int lineNum = 1;
  Vector<varLiveness> liveList = new Vector<varLiveness>();

  public Vector<varLiveness> getLiveList(){
    return liveList;
  }
  
  public void resetLineNum()
  {
    lineNum = 1;
    liveList.clear();
  }

  public void addLiveness(varLiveness var) {
    boolean found = false;
    
    //update the end if the variable exist
    for(varLiveness vl : liveList) {
      if(vl.equals(var)) {
        found = true;
        vl.updateEnd(lineNum);
      }
    }

    //if the variable did not exist, then add it to the list
    if(!found) {
      liveList.add(var);
    }
  }

  public void removeRedundant(String[] varNames, VVarRef.Local[] paramNames)
  {
    Vector<varLiveness> ll = new Vector<>(liveList);
    for(varLiveness vl : ll)
    {
      String name = vl.getName();

      // Issue: var isn't assigned a register
      ////removes variables that have the same start and end line
      //if(vl.getStart() == vl.getEnd())
      //{
      //  System.out.println("One line: " + vl.getName());
      //  liveList.remove(vl);
      //  continue;
      //}

      //removes "variables" that aren't in function variable list
      boolean validName = false;
      for(String vName : varNames)
        if(name.equals(vName)) {
          validName = true;
        }

      if(!validName){
        liveList.remove(vl);
        continue;
      }

      //removes parameters (they already have a registers)
      for(VVarRef.Local pName : paramNames){
        if(name.equals(pName.ident)) {
          liveList.remove(vl);
          break;
        }
      }
    }
  }

  public void printLiveness() //for testing
  {
    for(int i = 0; i < liveList.size(); i++)
    {
      String name = liveList.get(i).getName();
      int start = liveList.get(i).getStart();
      int end = liveList.get(i).getEnd();
      String out =
        "Variable name: " + name + "\nLiveness begins at line: " +
        start + "\nLiveness ends at line: " + end + "\n-------------------------";
      System.out.println(out);
    }
  }

  public void visit(VAssign a) throws RuntimeException {
    varLiveness live = new varLiveness(a.source.toString(), lineNum); // Src var
    addLiveness(live);
    live = new varLiveness(a.dest.toString(), lineNum);               // Dst var
    addLiveness(live);
    ++lineNum;
  }

  public void visit(VBranch b) throws RuntimeException {
    varLiveness live = new varLiveness(b.value.toString(), lineNum);
    addLiveness(live);
    //varLiveness live2 = new varLiveness(b.target.toString(), lineNum);
    //addLiveness(live2);
    ++lineNum;
  }

  public void visit(VBuiltIn c) throws RuntimeException {
    if(c.dest != null)
    {
      varLiveness live1 = new varLiveness(c.dest.toString(), lineNum);
      addLiveness(live1);
    }
    else
    {
      varLiveness live1 = new varLiveness(c.op.name, lineNum);
      addLiveness(live1);
    }
    for(VOperand arg : c.args) {
      varLiveness live = new varLiveness(arg.toString(), lineNum);
      addLiveness(live);
    }
    ++lineNum;
  }

  public void visit(VCall c) throws RuntimeException {
    String code = new String();
    if(c.dest != null)
    {
      varLiveness live = new varLiveness(c.addr.toString(), lineNum);
      addLiveness(live);
      varLiveness live2 = new varLiveness(c.dest.toString(), lineNum);
      addLiveness(live2);
    }
    else
    {
      varLiveness live = new varLiveness(c.addr.toString(), lineNum);
      addLiveness(live);
    }
    for(VOperand arg : c.args) {
      varLiveness live3 = new varLiveness(arg.toString(), lineNum);
      addLiveness(live3);
    }
    ++lineNum;
  }

  public void visit(VGoto g) throws RuntimeException {
    //varLiveness live = new varLiveness(g.target.toString(), lineNum);
    //addLiveness(live);
    ++lineNum;
  }

  public void visit(VMemRead r) throws RuntimeException {
    String base = ((VMemRef.Global)r.source).base.toString();
    int byteOffset = ((VMemRef.Global)r.source).byteOffset;
    varLiveness live = new varLiveness(r.dest.toString(), lineNum);
    addLiveness(live);
    if(!(base.equals("this") && byteOffset == 0))
    {
       varLiveness live2 = new varLiveness(base, lineNum);
      addLiveness(live2);
    }
    ++lineNum;
  }

  public void visit(VMemWrite w) throws RuntimeException {
    String base = ((VMemRef.Global)w.dest).base.toString();
    int byteOffset = ((VMemRef.Global)w.dest).byteOffset;
    String src = w.source.toString();
    varLiveness live = new varLiveness(base, lineNum);
    addLiveness(live);
    if(byteOffset != 0){
      varLiveness live2 = new varLiveness(src, lineNum);
      addLiveness(live2);
    }
    ++lineNum;
  }

  public void visit(VReturn r) throws RuntimeException {
    if(r.value != null) {
      varLiveness live = new varLiveness(r.value.toString(), lineNum);
      addLiveness(live);
    }
    ++lineNum;
  }
}
