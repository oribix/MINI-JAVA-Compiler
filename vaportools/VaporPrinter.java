package vaportools;

import java.util.Vector;
import symboltable.ClassData;
import symboltable.MethodData;

public class VaporPrinter {
  static public void createVMT(ClassData cd) {
    Vector<MethodData> methods = cd.getMethods();
    if (!methods.isEmpty()) {
      System.out.println("const vmt_" + cd.getClassName());

      String prefix = "  :" + cd.getClassName() + ".";
      for (MethodData md : methods)
        System.out.println(prefix + md.getName());

      System.out.println();
    }
  }
}
