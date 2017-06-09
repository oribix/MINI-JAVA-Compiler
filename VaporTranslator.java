//project imports
import cs132.vapor.ast.VaporProgram;
import cs132.vapor.ast.VDataSegment;
import cs132.vapor.ast.VOperand.Static;
import cs132.vapor.ast.VInstr;
import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VCodeLabel;
import cs132.vapor.ast.VVarRef;
import cs132.vapor.ast.VCall;
import cs132.vapor.ast.VBuiltIn;

//java library imports
import java.util.HashMap;
import java.util.Vector;
import java.util.TreeSet;
import java.util.Collections;

public class VaporTranslator{
  // FIELDS
  VaporProgram ast;
  HashMap<String, String> varRegMap;//(var name, register/stack loc)
  Vector<varLiveness> liveList;
  Registers registers;
  int localStackCnt;

  // CONSTRUCTORS
  public VaporTranslator(VaporProgram inAST){
    ast = inAST;
    registers = new Registers();
    varRegMap = new HashMap<>();
    localStackCnt = 0;
  }

  // METHODS
  void translate(){
    printDataSegments();
    int s = 0;
    for (VFunction function : ast.functions) {
      localStackCnt = 8;
      liveList = calcLiveness(function);
      linearScanRegisterAllocation();
      s = registers.highestS; 
      System.out.println("varRegMap: " + varRegMap.toString());
      printCode(function, s);
      registers.clear();
      varRegMap.clear();
    }
  }

  // Goes throught the list of intervals from a function
  // By end of this function, all variables should be appropriately 
  // mapped to either a register or a local stack position in varRegMap
  void linearScanRegisterAllocation() {
    TreeSet<varLiveness> active = new TreeSet<>();
    for (varLiveness i : liveList) {
      expireOldIntervals(active, i);
      if (active.size() == Registers.R)
        spillAtInterval(active, i);
      else {
        varRegMap.put(i.getName(), registers.getFreeReg());
        active.add(i);
      }
    }
  }

  void expireOldIntervals(TreeSet<varLiveness> active, varLiveness i)
  {
    TreeSet<varLiveness> newActive = new TreeSet<>(active);
    for(varLiveness j : newActive) {
      if(j.getEnd() >= i.getStart())
        return;
      active.remove(j);
      registers.returnFreeReg(varRegMap.get(j.getName()));
    }
  }

  void spillAtInterval(TreeSet<varLiveness> active, varLiveness i){
    varLiveness spill = active.last();
    if (spill.getEnd() > i.getEnd()){
      varRegMap.put(i.getName(), varRegMap.get(spill.getName()));
      varRegMap.put(spill.getName(), "local[" + localStackCnt++ + "]");
      active.remove(spill);
      active.add(i);
    }
    else{
      varRegMap.put(i.getName(), "local[" + localStackCnt++ + "]");
    }
    return;
  }

  //Liveness Intervals
  Vector<varLiveness> calcLiveness(VFunction function){
    LivenessVisitor liveVisitor = new LivenessVisitor();
    VInstr[] body = function.body;
    //j is the line number
    for(int j = 0; j < body.length; j++) {
      //print instruction
      VInstr inst = body[j];
      String TEST2 = inst.accept(new String("test"), liveVisitor);
    }
    liveVisitor.removeRedundant(function.vars, function.params);
    return liveVisitor.getLiveList();
  }

  void printCode(VFunction function, int s){
    VVisitor visitor = new VVisitor(varRegMap, liveList);
    //System.out.println(varRegMap.size());
    // Print function headers
    System.out.println(getFunctionHeaders(function));
    // Store s values into local
    for(int i = 0; i < s; i++)
    {
      System.out.println("local[" + i + "] = $s" + i);
    }
    // Map and store param values to $a registers

    VInstr[] body = function.body;
    VCodeLabel[] labels = function.labels;
    int currLabel = 0;
    //j is the line number
    for(int j = 0; j < body.length; j++) {
      //print label if there is one
      if(currLabel < labels.length && j+currLabel == labels[currLabel].instrIndex){
        System.out.println(labels[currLabel++].ident + ":");
      }

      //print instruction
      VInstr inst = body[j];
      
      //If instruction is vCall, back up and restore $a registers into in stack
      if (inst instanceof VCall || inst instanceof VBuiltIn) {
        int aRegCnt = 0;
        for(VVarRef.Local param : function.params)
        {
          String stackName = "in[" + aRegCnt + ']';
          String regName = "$a" + aRegCnt;
          System.out.println(stackName + " = " + regName);
          ++aRegCnt;
        }
        inst.accept(visitor);
        --aRegCnt;
        for(; aRegCnt >= 0; --aRegCnt)
        {
          String stackName = "in[" + aRegCnt + ']';
          String regName = "$a" + aRegCnt;
          System.out.println(regName + " = " + stackName);
        }
      }
      else
        inst.accept(visitor);
    }
    for(int i = 0; i < s; i++)
    {
      System.out.print("$s" + i + " = local[" + i + ']');
    }
    System.out.println("ret"); //store return value in $v0
    System.out.println();
    return;
  }

  void printDataSegments()
  {
    for(VDataSegment VDS : ast.dataSegments)
    {
      String line = "const " + VDS.ident;
      System.out.println(line);
      for(Static VOp : VDS.values)
      {
        System.out.println("  " + VOp);
      }
      System.out.println("");
    }
  }

  String getFunctionHeaders(VFunction f)
  {
    // Calculate in, out, and local
    OutStackVisitor osv = new OutStackVisitor();
    int outSize = 0, inSize = 0, localSize = 0;

    // In size is number of arguments (if greater than 4)
    inSize = f.params.length > 4 ? f.params.length : 0;

    // Out size is number of arguments of called function with most arguments (if > 4)
    for(VInstr inst : f.body) 
      outSize = Math.max(inst.accept(osv), outSize);  // Size of out stack

    if (outSize <= 4)
      outSize = 0;

    // Local size is the amount of S registers ever used + number of spilled intervals
    localSize = registers.highestS;
    if (localStackCnt > 8)
      localSize = localStackCnt;

    // pass in different in, out, local values once we figure out how to calculate them
    return "func " + f.ident + printStackArrays(inSize, outSize, localSize);
  }

  String printStackArrays(int in, int out, int local)
  {
    return " [in " + in + ", out " + out + ", local " + local + "]";
  }
}
