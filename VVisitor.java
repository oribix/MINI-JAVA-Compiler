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

public class VVisitor extends
  VisitorPR<String, String, RuntimeException> {
  public int lineNum = 1;
  Vector<varLiveness> liveList = new Vector<varLiveness>();


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
  
  public void sort(Vector<varLiveness> lList ) // insertion sort because im lazy
  {
    varLiveness temp;
    for (int i = 1; i < lList.size(); i++) {
      for(int j = i ; j > 0 ; j--){
        if(lList.get(j).end < lList.get(j-1).end){
          temp = lList.get(j);
          lList.set(j, lList.get(j-1));
          lList.set(j-1, temp);
        }
      }
    }
  }
  
  public void removeRedundant(String[] varNames,VVarRef.Local[] paramNames)
  {
    for(int i = 0; i < liveList.size(); i++)
    {
      int start = liveList.get(i).getStart();
      int end = liveList.get(i).getEnd();
      if(start == end)
      {
        liveList.remove(i);
        --i;
      }
    }
    for(int i = 0; i < liveList.size(); i++)
    {
      boolean validName = false;
      String name = liveList.get(i).getName();
      for(String vName : varNames){
        if( Objects.equals(name, new String(vName)) )
          validName = true;
      }
      if(!validName){
        liveList.remove(i);
        --i;
      }
    }
    for(int i = 0; i < liveList.size(); i++)
    {
      String name = liveList.get(i).getName();
      for(VVarRef.Local pName : paramNames){
        if( Objects.equals(name, new String(pName.ident)) ) {
          liveList.remove(i);
          --i;
        }
      }
    }
    sort(liveList);
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
    // FIXME: You can print a.dest, which contains the variable. For now, just print $t0.
    System.out.println(lineNum);
    varLiveness live = new varLiveness(a.source.toString(), lineNum);
    addLiveness(live);
    ++lineNum;
    System.out.println("$t0" + " = " + a.source);
    return "VAssign";
  }

  public String visit(String s, VBranch b) throws RuntimeException {
    String ifString = "";
    if(b.positive)
      ifString = "if";
    else
      ifString = "if0";

    System.out.println(lineNum);
    varLiveness live = new varLiveness(b.value.toString(), lineNum);
    varLiveness live2 = new varLiveness(b.target.toString(), lineNum);
    addLiveness(live);
    addLiveness(live2);
    ++lineNum;
    System.out.println(ifString + " " + b.value + " goto " + b.target);

    return "VBranch";
  }

  public String visit(String s, VBuiltIn c) throws RuntimeException {
    String code = new String();
    if(c.dest != null)
    {
      varLiveness live1 = new varLiveness(c.dest.toString(), lineNum);
      addLiveness(live1);
      code = c.dest + " = " + c.op.name + "(";
    }
    else
    {
      varLiveness live1 = new varLiveness(c.op.name, lineNum);
      addLiveness(live1);
      code = c.op.name + "(";
    }
    String argz = new String();
    for(VOperand arg : c.args) {
      argz = argz + " " + arg.toString();
      varLiveness live = new varLiveness(arg.toString(), lineNum);
      addLiveness(live);
    }
    argz = argz.substring(1);
    code += argz;
    code += ")";
    System.out.println(lineNum);
    ++lineNum;
    System.out.println(code);
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
      code = c.dest + " = call " + c.addr + "(";
    }
    else
    {
      varLiveness live = new varLiveness(c.addr.toString(), lineNum);
      addLiveness(live);
      code = c.addr + "(";
    }
    String argz = new String();
    for(VOperand arg : c.args) {
      argz = argz + " " + arg.toString();
      varLiveness live3 = new varLiveness(arg.toString(), lineNum);
      addLiveness(live3);
    }
    argz = argz.substring(1);
    code += argz;
    code += ")";
    System.out.println(lineNum);
    ++lineNum;
    System.out.println(code);
    return "VCall";
  }

  public String visit(String s, VGoto g) throws RuntimeException {
    System.out.println(lineNum);
    //varLiveness live = new varLiveness(g.target.toString(), lineNum);
    //addLiveness(live);
    ++lineNum;
    System.out.println("goto " + g.target);
    return "VGoto";
  }

  public String visit(String s, VMemRead r) throws RuntimeException {
    String base = ((VMemRef.Global)r.source).base.toString();
    int byteOffset = ((VMemRef.Global)r.source).byteOffset;
    varLiveness live = new varLiveness(r.dest.toString(), lineNum);
    addLiveness(live);
    if(base.equals("this") && byteOffset == 0)
    {
      System.out.println(lineNum);
      ++lineNum;
      System.out.println(r.dest + " = [this]");
    }
    else
    {
      System.out.println(lineNum);
      varLiveness live2 = new varLiveness(base, lineNum);
      addLiveness(live2);
      ++lineNum;
      System.out.println(r.dest + " = [" + base + " + " +  byteOffset + "]");
    }

    return "VMemRead";
  }

  public String visit(String s, VMemWrite w) throws RuntimeException {
    String base = ((VMemRef.Global)w.dest).base.toString();
    int byteOffset = ((VMemRef.Global)w.dest).byteOffset;

    String src = w.source.toString();
    varLiveness live = new varLiveness(base, lineNum);
    addLiveness(live);
    if(byteOffset == 0)
    {
      System.out.println(lineNum);
      ++lineNum;
      System.out.println("[" + base + "] = " + src);
    }
    else
    {
      System.out.println(lineNum);
      varLiveness live2 = new varLiveness(src, lineNum);
      addLiveness(live2);
      ++lineNum;
      System.out.println("[" + base + " + " + byteOffset + "] = " + src);
    }
    return "VMemWrite";
  }

  public String visit(String s, VReturn r) throws RuntimeException {
    if(r.value == null) {
      System.out.println(lineNum);
      ++lineNum;
      System.out.println("ret");
    }
    else {
      System.out.println(lineNum);
      varLiveness live = new varLiveness(r.value.toString(), lineNum);
      addLiveness(live);
      ++lineNum;
      System.out.println("$v0 = " + r.value);
      System.out.println(lineNum);
      ++lineNum;
      System.out.println("ret"); //store return value in $v0
    }
    return "VReturn";
  }
}
