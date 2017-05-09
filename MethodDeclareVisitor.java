import visitor.*;

import java.util.Vector;

import symboltable.*;
import syntaxtree.*;

// Class that just vists methods to declare them
class MethodDeclareVisitor extends DMVisitor {
  SymbolTable symbolTable;      // Passed from DMVisitor
  Vector<SymbolData> synthFormalParam;  // Used by MethodDeclaration (synthesized from FormalParam)
  public Vector<NodeToken> unverifiedClasses; // Pass this back to DMVisitor

  public MethodDeclareVisitor(SymbolTable st) {
    symbolTable = st;
    synthFormalParam = new Vector<>();
    unverifiedClasses = new Vector<>();
  }

  //returns the synthesized formal parameters
  public Vector<SymbolData> getSynthFormalParam() {
    Vector<SymbolData> data = synthFormalParam;
    synthFormalParam = new Vector<>();
    return data;
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

  /**
   * f0 -> Type()
   * f1 -> Identifier()
   * f2 -> ";"
   */
  public void visit(VarDeclaration n) {
    n.f0.accept(this);
    n.f1.accept(this);
    n.f2.accept(this);

    //SymbolType st = getInheritedType();
    //if(st == SymbolType.ST_CLASS_VAR)
    //  symbolTable.addSymbol(n.f1.f0, getDeepInheritedType());
    //else
    //  symbolTable.addSymbol(n.f1.f0, st);
  }
}
