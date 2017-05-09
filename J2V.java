import syntaxtree.*;
import visitor.*;

public class J2V {
  public static void main(String args[]){
    //taken from JTB tutorial
    MiniJavaParser parser = new MiniJavaParser(System.in);

    try {
      Node root = parser.Goal();
      root.accept(new VaporVisitor());
      System.out.println("Vapor code generated successfully.");
    }
    catch (ParseException e) {
      System.err.println("Encountered errors during parse.");
    }

  }
}
