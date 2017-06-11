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
  Vector<String> localStack;

  // Stack sizes for function header
  int inStackSize;
  int outStackSize;
  int localStackSize;

  // Index where t regs and spill vars start in local register
  int localT; 
  int localSpill;

  // CONSTRUCTORS
  public VaporTranslator(VaporProgram inAST){
    ast = inAST;
    registers = new Registers();
    varRegMap = new HashMap<>();
    localStack = new Vector<>();
    localT = 0;
  }

  // METHODS
  void translate(){
    printDataSegments();

    // Output vapor-m code for each vapor function:
    for (VFunction function : ast.functions) {
      mapParamsToRegs(function);          // map arguments, if any, to $a# and in stack locations
      liveList = calcLiveness(function);  // calculate liveness of vars
      linearScanRegisterAllocation();     // assign registers
      //todo: deleteme
      //System.out.println("varRegMap: " + varRegMap.toString());
      calcStackSizes(function);           // calculate size of in, out, and local stacks
      assignLocalStackLocations();        // takes variables in local stack and maps them to "local[#]"
      printCode(function);
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
      varRegMap.put(spill.getName(), "LOCAL TEMP"); // Delete this after debugging
      localStack.add(spill.getName());
      active.remove(spill);
      active.add(i);
    }
    else{
      varRegMap.put(i.getName(), "LOCAL TEMP"); // Delete this after debugging
      localStack.add(i.getName());
    }
    return;
  }

  void mapParamsToRegs(VFunction function) {
    // Map and store param values to $a registers/in-stack locations
    for(int i = 0; i < function.params.length; i++) {
      if (i < 4)
        varRegMap.put(function.params[i].toString(), "$a" + i);
      else
        varRegMap.put(function.params[i].toString(), "in[" + i + "]");
    }
  }

  //Liveness Intervals
  Vector<varLiveness> calcLiveness(VFunction function){
    LivenessVisitor liveVisitor = new LivenessVisitor(function);
    VInstr[] body = function.body;
    for(VInstr inst : body) {
      inst.accept(liveVisitor);
    }
    liveVisitor.removeRedundant(function.vars, function.params);
    //liveVisitor.printLiveness();
    return liveVisitor.getLiveList();
  }

  void printCode(VFunction function){
    //for(VCodeLabel l : function.labels)
    //  System.out.println(l.ident + ":" + l.instrIndex);

    VVisitor visitor = new VVisitor(varRegMap, liveList);

    printFunctionHeaders(function.ident);

    // Store s values into local
    for(int i = 0; i < registers.highestS; i++)
      System.out.println("local[" + i + "] = $s" + i);

    // print body of function
    VInstr[] body = function.body;
    VCodeLabel[] labels = function.labels;
    int currLabel = 0;

    //j is the line number
    for(int j = 0; j < body.length; j++) {
      //print one or more labels if label should be here
      while(currLabel < labels.length && j == labels[currLabel].instrIndex)
        System.out.println(labels[currLabel++].ident + ":");

      //print instruction using VVisitor
      VInstr inst = body[j];

      // If instruction is VCall, we need to back up values first
      // Can't really do in VVisitor because we need access to VFunction object
      if (inst instanceof VCall) {
        // back up $a registers into in stack
        int aRegCnt = 0;
        int backupLength = (function.params.length <= 4) ? function.params.length : 4;
        for(; aRegCnt < backupLength; aRegCnt++)
          System.out.println("in[" + aRegCnt + "] = $a" + aRegCnt);

        //// back up t registers into local stack
        //final int endLocalT = localT + registers.highestT;
        //for (int i = localT; i < endLocalT; i++)
        //  System.out.println("local[" + i + "] = $t" + (i - localT));

        // assign values to a regs, call function
        inst.accept(visitor);

        //// The "restore t registers from local stack" section
        //for (int i = localT; i < endLocalT; i++)
        //  System.out.println("$t" + (i - localT) + " = local[" + i + "]");

        // The "restore a registers with values from in" section
        --aRegCnt;
        for(; aRegCnt >= 0; --aRegCnt)
        {
          String stackName = "in[" + aRegCnt + ']';
          String regName = "$a" + aRegCnt;
          System.out.println(regName + " = " + stackName);
        }

        // Put returned value into dest register
        System.out.println(varRegMap.get(((VCall)inst).dest.toString()) + " = " + "$v0");
      }
      else
        inst.accept(visitor);
    }

    // Restore local values into s
    for(int i = 0; i < registers.highestS; i++)
    {
      System.out.println("$s" + i + " = local[" + i + ']');
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

  void calcStackSizes(VFunction f) {
    // Calculate in, out, and local
    StackInfoVisitor osv = new StackInfoVisitor();

    // In size is number of arguments (if greater than 4)
    inStackSize = f.params.length > 4 ? f.params.length : 0;

    // Out size is number of arguments of called function with most arguments (if > 4)
    for(VInstr inst : f.body) 
      outStackSize = Math.max(inst.accept(osv), outStackSize);  // StackSize of out stack

    if (outStackSize <= 4)
      outStackSize = 0;

    // Local size is the amount of S regs, amount of T regs, and amount of spills
    localStackSize = registers.highestS + registers.highestT + localStack.size();
    localT = registers.highestS;
    localSpill = registers.highestS + registers.highestT;

    // But...if no functions ever get called, then we don't back them up in local
    if (osv.getVCallIndices().isEmpty()) {
      localStackSize -= registers.highestT;
      localSpill = registers.highestS;
    }
  }

  void assignLocalStackLocations() {
    int i = localSpill;
    for (String var : localStack)
      varRegMap.put(var, "local[" + (i++) + "]");
  }

  void printFunctionHeaders(String ident)
  {
    // pass in different in, out, local values once we figure out how to calculate them
    System.out.println("func " + ident + printStackArrays(inStackSize, outStackSize, localStackSize));
  }

  String printStackArrays(int in, int out, int local)
  {
    return " [in " + in + ", out " + out + ", local " + local + "]";
  }
}
