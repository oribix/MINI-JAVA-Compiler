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
    
    for (int i = 0; i < ast.functions.length; i++) { //outputting parameters
      String line = "func " + ast.functions[i].ident + printStackArrays(
      // pass in different in, out, local values once we figure out how to calculate them
        ast.functions[i].stack.in, 
        ast.functions[i].stack.out, 
        ast.functions[i].stack.local);
      System.out.println(line);
      //outputting parameters of each function on its own line
      for (int j = 0; j < ast.functions[i].params.length; j++) 
      {
        System.out.println("  " + ast.functions[i].params[j].ident);
      }

      System.out.println();
    }
      
    System.out.println("ENDING TESTS");
  }
  
  String printStackArrays(int in, int out, int local)
  {
    String line = " [in " + in + 
                  ", out " + out +
                  ", local " + local + "]";
    return line;
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
}
