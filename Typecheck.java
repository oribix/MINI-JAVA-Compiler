import syntaxtree.*;
import visitor.*;

class Typecheck{
  public static void main(String args[]){
    //taken from JTB tutorial
    MiniJavaParser parser = new MiniJavaParser(System.in);

    try {
      Node root = parser.Goal();
      root.accept(new DMVisitor());
      System.err.println("Java program parsed successfully.");
    }
    catch (ParseException e) {
      System.err.println("Encountered errors during parse.");
    }

  }
}
