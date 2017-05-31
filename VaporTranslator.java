import cs132.vapor.ast.VaporProgram;
import cs132.vapor.ast.VDataSegment;
import cs132.vapor.ast.VOperand.Static;

public class VaporTranslator{
  // FIELDS
  VaporProgram ast;
  
  // CONSTRUCTORS
  public VaporTranslator(VaporProgram inAST){
    ast = inAST;
  }
  
  // METHODS
  void test(){ // possibly rename to translate()
    System.out.println("RUNNING TESTS");
    
    printDataSegments();
    for (int i = 0; i < ast.functions.length; i++) {
      System.out.println(getFunctionHeaders(i));
      System.out.println();
    }
      
    System.out.println("ENDING TESTS");
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
  String getFunctionHeaders(int i)
  {
    String line = "func " + ast.functions[i].ident + printStackArrays(
      // pass in different in, out, local values once we figure out how to calculate them
        ast.functions[i].stack.in, 
        ast.functions[i].stack.out, 
        ast.functions[i].stack.local);
    return line;
  }
  String printStackArrays(int in, int out, int local)
  {
    String line = " [in " + in + 
                  ", out " + out +
                  ", local " + local + "]";
    return line;
  }
}
