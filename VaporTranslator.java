import cs132.vapor.ast.VaporProgram;
import cs132.vapor.ast.VDataSegment;
import cs132.vapor.ast.VOperand.Static;
import cs132.vapor.ast.VInstr;
import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VCodeLabel;


public class VaporTranslator{
  // FIELDS
  VaporProgram ast;
  VVisitor visitor;
  
  // CONSTRUCTORS
  public VaporTranslator(VaporProgram inAST){
    ast = inAST;
    visitor = new VVisitor();
  }
  
  // METHODS
  void translate(){ // possibly rename to translate()
    printDataSegments();
    for (VFunction function : ast.functions) {
      System.out.println(getFunctionHeaders(function));
      VInstr[] body = function.body;
      VCodeLabel[] labels = function.labels;
      int currLabel = 0;
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
    // pass in different in, out, local values once we figure out how to calculate them
    return "func " + f.ident + printStackArrays(f.stack.in, f.stack.out, f.stack.local);
  }

  String printStackArrays(int in, int out, int local)
  {
    return " [in " + in + ", out " + out + ", local " + local + "]";
  }
}
