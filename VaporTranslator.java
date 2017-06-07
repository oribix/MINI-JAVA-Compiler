import cs132.vapor.ast.VaporProgram;
import cs132.vapor.ast.VDataSegment;
import cs132.vapor.ast.VOperand.Static;
import cs132.vapor.ast.VInstr;
import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VCodeLabel;
import cs132.vapor.ast.VVarRef;
import java.util.Vector;

public class VaporTranslator{
  // FIELDS
  VaporProgram ast;

  // CONSTRUCTORS
  public VaporTranslator(VaporProgram inAST){
    ast = inAST;
  }

  // METHODS
  void translate(){
    Vector<varLiveness> liveList = calcLiveness();
    printCode();
  }

  //Liveness Intervals
  Vector<varLiveness> calcLiveness(){
    LivenessVisitor liveVisitor = new LivenessVisitor();
    for (VFunction function : ast.functions) {
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
    }
    return liveVisitor.getLiveList();
  }

  void printCode(){
    VVisitor visitor = new VVisitor();
    printDataSegments();
    for (VFunction function : ast.functions) {
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
    }
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
