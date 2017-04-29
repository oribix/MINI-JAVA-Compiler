package visitor;

import java.util.Vector;

import symboltable.*;
import syntaxtree.*;

// Class that just vists methods to declare them
class MethodDeclareVisitor extends DMVisitor {
  SymbolTable symbolTable;      // Passed from DMVisitor
  public Vector<NodeToken> unverifiedClasses; // Pass this back to DMVisitor

  public MethodDeclareVisitor(SymbolTable st) {
    symbolTable = new SymbolTable();
    synthFormalParam = new Vector<>();
    unverifiedClasses = new Vector<>();
  }

  /**
   * f0 -> ArrayType()
   *       | BooleanType()
   *       | IntegerType()
   *       | Identifier()
   */
  public void visit(Type n) {
    n.f0.accept(this);
    if(n.f0.which == 3){//if we chose Identifier()
      inheritedType = SymbolType.ST_CLASS_VAR;

      // If class doesn't exist "yet", put it in backpatch list
      NodeToken classToken = ((Identifier) n.f0.choice).f0;
      deepInheritedType = new ClassVarData(classToken);
      if (!symbolTable.classExists(classToken))
        unverifiedClasses.add(classToken);
    }
  }

  /**
   * Note: FormalParameter is ONLY used by MethodDeclaration
   * 
   * f0 -> Type()
   * f1 -> Identifier()
   */
  public void visit(FormalParameter n) {
    n.f0.accept(this);
    n.f1.accept(this);

    // Push variables into method's scope
    // Push variable types for methodData information
    SymbolType st = getInheritedType();
    SymbolData sd = getDeepInheritedType();
    if(st != SymbolType.ST_CLASS_VAR)
      synthFormalParam.add(new SymbolData(st));
    else
      synthFormalParam.add(sd);
  }
}
