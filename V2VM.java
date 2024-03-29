//import syntaxtree.*;
//import visitor.*;

import cs132.util.ProblemException;
import cs132.vapor.parser.VaporParser;
import cs132.vapor.ast.VaporProgram;
import cs132.vapor.ast.VBuiltIn.Op;
import cs132.vapor.ast.VDataSegment;

import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;

import java.io.InputStream;
import java.text.ParseException;

class V2VM{
  public static void main(String args[]){
    VaporProgram ast;
    try {
      ast = parseVapor(System.in,System.out);

      VaporTranslator translator = new VaporTranslator(ast);
      translator.translate();
    }

    catch (IOException e) {
      System.err.println(e.getMessage());
    }

  }

  public static VaporProgram parseVapor(InputStream in, PrintStream err) throws IOException {
    Op[] ops = {
      Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS,
      Op.PrintIntS, Op.HeapAllocZ, Op.Error,
    };
    boolean allowLocals = true;
    String[] registers = null;
    boolean allowStack = false;

    VaporProgram tree;
    try {
      tree = VaporParser.run(new InputStreamReader(in), 1, 1,
                             java.util.Arrays.asList(ops),
                             allowLocals, registers, allowStack);
    }
    catch (ProblemException ex) {
      err.println(ex.getMessage());
      return null;
    }

    return tree;
  }

}

