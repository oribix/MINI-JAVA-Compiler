import cs132.vapor.ast.VaporProgram;
import cs132.vapor.ast.VDataSegment;
import cs132.vapor.ast.VOperand.Static;
import cs132.vapor.ast.VInstr;
import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VCodeLabel;
import cs132.vapor.ast.VVarRef;
import java.util.HashMap;
import java.util.Vector;

public class VaporTranslator{
  // FIELDS
  VaporProgram ast;
  Vector<varLiveness> active;
  HashMap<String, String> varRegMap;
  Registers registers;

  // CONSTRUCTORS
  public VaporTranslator(VaporProgram inAST){
    ast = inAST;
    registers = new Registers();
  }

  // METHODS
  void translate(){
    printDataSegments();
    for (VFunction function : ast.functions) {
      Vector<varLiveness> liveList = calcLiveness(function);
      printCode(function);
    }
  }

  // Goes throught the list of intervals from a function
  // By end of this function, all variables should be appropriately 
  // mapped to either a register or a local stack position
  void linearScanRegisterAllocation(Vector<varLiveness> liveList) {
    active = new Vector<>();
    for (varLiveness i : liveList) {
      expireOldIntervals(i);
      if (active.size() == Registers.R)
        spillAtInterval(i);
      else {
        varRegMap.put(i.getName(), registers.getFreeReg());
        active.add(i);
      }
      sortByEndPoint(active); // Keep active sorted by end point
    }
  }

  void expireOldIntervals(varLiveness i)
  {
    for(int j = 0; j < active.size(); j++) {
      if(active.get(j).getEnd() >= i.getStart())
        return;
      varLiveness reg = active.remove(j);
      --j;
      registers.returnFreeReg(varRegMap.get(reg.getName()));
    }
  }

  void spillAtInterval(varLiveness i){
    varLiveness spill = active.lastElement();
    if (spill.getEnd() > i.getEnd()){
      //register[i] = register[spill]
      //location[spill] = new stack location
      active.remove(spill);
      //add i to active, sorted by increasing end point
      active.add(i);
      //todo: sort by endpoint

    }
    else{
      //location[i] = new stack location
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

    System.out.println(function.ident);
    System.out.println();
    liveVisitor.removeRedundant(function.vars, function.params);
    liveVisitor.printLiveness(); //remove eventually
    liveVisitor.resetLineNum();
    System.out.println();
    return liveVisitor.getLiveList();
  }

  void printCode(VFunction function){
    VVisitor visitor = new VVisitor();
    // Print function headers
    System.out.println(getFunctionHeaders(function));

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
      String TESTER = inst.accept(new String("test"), visitor);
    }

    System.out.println();
    return;
  }
  public void sortByEndPoint(Vector<varLiveness> lList ) // insertion sort because im lazy
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
    // Calculate in, out, and local (local NOT done yet)
    OutStackVisitor osv = new OutStackVisitor();
    int outSize = 0, inSize = 0, localSize = 0;

    if (f.params.length > 4) 
      inSize = f.params.length; // Size of in stack
    else
      inSize = 0;

    for(VInstr inst : f.body) 
      outSize = Math.max(inst.accept(osv), outSize);  // Size of out stack

    if (outSize <= 4)
      outSize = 0;

    // pass in different in, out, local values once we figure out how to calculate them
    return "func " + f.ident + printStackArrays(inSize, outSize, localSize);
  }

  String printStackArrays(int in, int out, int local)
  {
    return " [in " + in + ", out " + out + ", local " + local + "]";
  }
}
