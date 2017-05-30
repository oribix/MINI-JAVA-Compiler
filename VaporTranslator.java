import cs132.util.ProblemException;
import cs132.vapor.parser.VaporParser;
import cs132.vapor.ast.VaporProgram;
import cs132.vapor.ast.VBuiltIn.Op;
import cs132.vapor.ast.VDataSegment;
import cs132.vapor.ast.VOperand.Static;

import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;

import java.io.InputStream;
import java.text.ParseException;


public class VaporTranslator{
  //FIELDS
  VaporProgram ast;
  
  //CONSTRUCTOR
  public VaporTranslator(VaporProgram inAST){
    ast = inAST;
  }
  //METHODS
  void test(){
    System.out.println("RUNNING TESTS");
    for(VDataSegment VDS : ast.dataSegments)
    {
      String line = "const " + VDS.ident;
      System.out.println(line);
      for(Static VOp : VDS.values)
      {
        System.out.println("  " + VOp);
      }
      System.out.println("");
      //System.out.println(java.util.Arrays.toString(VDS.values));
    }
    System.out.println("ENDING TESTS");
  }
}
