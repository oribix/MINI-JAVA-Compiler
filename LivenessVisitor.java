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
import cs132.vapor.ast.VOperand;
import cs132.vapor.ast.VMemRef;
import cs132.vapor.ast.VVarRef;
import java.util.Vector;
import java.util.Objects;

public class LivenessVisitor extends
  VisitorPR<String, String, RuntimeException> {
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
  public void addLiveness(varLiveness var)
  {
    boolean found = false;
    for(int i = 0; i < liveList.size(); i++)
    {
      if(Objects.equals(liveList.get(i).getName(), new String(var.getName())) )
      {
        found = true;
        liveList.get(i).updateEnd(lineNum);
      }
    }
    if(!found)
    {
      liveList.add(var);
    }
  }

  public void removeRedundant(String[] varNames,VVarRef.Local[] paramNames)
  {
    Vector<varLiveness> ll = new Vector<>(liveList);
    for(varLiveness vl : ll)
    {
      String name = vl.getName();
      //removes variables that have the same start and end line
      if(vl.getStart() == vl.getEnd())
      {
        // leave "this" in varRegMap
        if(!Objects.equals(name, "this")){
          liveList.remove(vl);
          continue;
        }
      }

      //removes redundadnt variable names
      boolean validName = false;
      for(String vName : varNames){
        if( Objects.equals(name, new String(vName)))
          validName = true;
      }
      if(!validName){
        liveList.remove(vl);
        continue;
      }

      //removes redundant parameter names
      for(VVarRef.Local pName : paramNames){
        if( Objects.equals(name, new String(pName.ident)) ) {
          // leave "this" in varRegMap
          if(!Objects.equals(name, "this")){
            liveList.remove(vl);
            break;
          }
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



  public String visit(String s, VAssign a) throws RuntimeException {
    varLiveness live = new varLiveness(a.source.toString(), lineNum);
    addLiveness(live);
    ++lineNum;
    return "VAssign";
  }

  public String visit(String s, VBranch b) throws RuntimeException {
    varLiveness live = new varLiveness(b.value.toString(), lineNum);
    varLiveness live2 = new varLiveness(b.target.toString(), lineNum);
    addLiveness(live);
    addLiveness(live2);
    ++lineNum;
    return "VBranch";
  }

  public String visit(String s, VBuiltIn c) throws RuntimeException {
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
    return "VBuiltIn";
  }

  public String visit(String s, VCall c) throws RuntimeException {
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
    return "VCall";
  }

  public String visit(String s, VGoto g) throws RuntimeException {
     
    varLiveness live = new varLiveness(g.target.toString(), lineNum);
    addLiveness(live);
    ++lineNum;
    return "VGoto";
  }

  public String visit(String s, VMemRead r) throws RuntimeException {
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
    return "VMemRead";
  }

  public String visit(String s, VMemWrite w) throws RuntimeException {
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
    return "VMemWrite";
  }

  public String visit(String s, VReturn r) throws RuntimeException {
    if(r.value != null) {
      varLiveness live = new varLiveness(r.value.toString(), lineNum);
      addLiveness(live);
    }
    ++lineNum;
    return "VReturn";
  }
}
