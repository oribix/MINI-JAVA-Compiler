import cs132.vapor.ast.VInstr.VisitorR;
import cs132.vapor.ast.VCall;
import cs132.vapor.ast.VAssign;
import cs132.vapor.ast.VBranch;
import cs132.vapor.ast.VBuiltIn;
import cs132.vapor.ast.VCall;
import cs132.vapor.ast.VGoto;
import cs132.vapor.ast.VMemRead;
import cs132.vapor.ast.VMemRef;
import cs132.vapor.ast.VMemWrite;
import cs132.vapor.ast.VReturn;
import cs132.vapor.ast.VOperand;
import cs132.vapor.ast.VMemRef;


public class OutStackVisitor extends VisitorR<Integer, RuntimeException> {
  public Integer visit(VCall c) throws RuntimeException {
    return c.args.length;
  }

  public Integer visit(VAssign a) throws RuntimeException {
    return 0;
  }
  
  public Integer visit(VBranch b) throws RuntimeException {
    return 0;
  }
  
  public Integer visit(VBuiltIn c) throws RuntimeException {
    return 0;
  }
  
  public Integer visit(VGoto g) throws RuntimeException {
    return 0;
  }
  
  public Integer visit(VMemRead r) throws RuntimeException {
    return 0;
  }

  public Integer visit(VMemWrite w) throws RuntimeException {
    return 0;
  }
  
  public Integer visit(VReturn r) throws RuntimeException {
    return 0;
  }
} 
