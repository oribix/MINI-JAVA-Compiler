package visitor;
import syntaxtree.*;
import symboltable.*;
import java.util.*;

public class DMVisitor extends DepthFirstVisitor {

  SymbolTable symbolTable;
  SymbolType inheritedType;
  SymbolData deepInheritedType;

  boolean declaringType;

  public DMVisitor(){
    symbolTable = new SymbolTable();
    inheritedType = SymbolType.ST_NULL;
    deepInheritedType = null;

    declaringType = false;
  }

  ///**
  // * f0 -> MainClass()
  // * f1 -> ( TypeDeclaration() )*
  // * f2 -> <EOF>
  // */
  //public void visit(Goal n) {
  //  n.f0.accept(this);
  //  n.f1.accept(this);
  //  n.f2.accept(this);
  //}

  /**
   * f0 -> "class"
   * f1 -> Identifier()
   * f2 -> "{"
   * f3 -> "public"
   * f4 -> "static"
   * f5 -> "void"
   * f6 -> "main"
   * f7 -> "("
   * f8 -> "String"
   * f9 -> "["
   * f10 -> "]"
   * f11 -> Identifier()
   * f12 -> ")"
   * f13 -> "{"
   * f14 -> ( VarDeclaration() )*
   * f15 -> ( Statement() )*
   * f16 -> "}"
   * f17 -> "}"
   */
  public void visit(MainClass n) {
    n.f0.accept(this);
    inheritedType = SymbolType.ST_CLASS;  // Pass type "class" to identifier
    n.f1.accept(this);
    //inheritedType = SymbolType.ST_NULL;   // Remove inherited value
    n.f2.accept(this);
    symbolTable.newScope(); //scope: class
    n.f3.accept(this);
    n.f4.accept(this);
    n.f5.accept(this);
    n.f6.accept(this);
    symbolTable.newScope();//scope: main
    n.f7.accept(this);
    n.f8.accept(this);
    n.f9.accept(this);
    n.f10.accept(this);
    inheritedType = SymbolType.ST_STRING_ARR; // Pass type "String[]" to identifier
    n.f11.accept(this);
    //inheritedType = SymbolType.ST_NULL;       // Remove inherited value
    n.f12.accept(this);
    n.f13.accept(this);
    n.f14.accept(this);
    n.f15.accept(this);
    n.f16.accept(this);
    symbolTable.exitScope();
    n.f17.accept(this);
    symbolTable.exitScope();
  }

  ///**
  // * f0 -> ClassDeclaration()
  // *       | ClassExtendsDeclaration()
  // */
  //public void visit(TypeDeclaration n) {
  //  n.f0.accept(this);
  //}

  /**
   * f0 -> "class"
   * f1 -> Identifier()
   * f2 -> "{"
   * f3 -> ( VarDeclaration() )*
   * f4 -> ( MethodDeclaration() )*
   * f5 -> "}"
   */
  public void visit(ClassDeclaration n) {
    n.f0.accept(this);
    inheritedType = SymbolType.ST_CLASS;  // Pass type "class" to identifier
    n.f1.accept(this);
    symbolTable.newScope();
    //inheritedType = SymbolType.ST_NULL;   // Remove inherited value
    n.f2.accept(this);
    n.f3.accept(this);
    n.f4.accept(this);
    n.f5.accept(this);
    symbolTable.exitScope();
  }

  /**
   * f0 -> "class"
   * f1 -> Identifier()
   * f2 -> "extends"
   * f3 -> Identifier()
   * f4 -> "{"
   * f5 -> ( VarDeclaration() )*
   * f6 -> ( MethodDeclaration() )*
   * f7 -> "}"
   */
  public void visit(ClassExtendsDeclaration n) {
    n.f0.accept(this);
    inheritedType = SymbolType.ST_CLASS_EXTENDS;  // Pass type "class extends" to identifier
    n.f1.accept(this);
    //inheritedType = SymbolType.ST_NULL;           // Remove inherited value
    n.f2.accept(this);
    n.f3.accept(this);
    symbolTable.newScope();
    n.f4.accept(this);
    n.f5.accept(this);
    n.f6.accept(this);
    n.f7.accept(this);
    symbolTable.exitScope();
  }

  /**
   * f0 -> Type()
   * f1 -> Identifier()
   * f2 -> ";"
   */
  public void visit(VarDeclaration n) {
    n.f0.accept(this);  // inheritedValue is set by Type()
    n.f1.accept(this);
    //inheritedType = SymbolType.ST_NULL; // Remove inherited value
    n.f2.accept(this);
  }

  /**
   * f0 -> "public"
   * f1 -> Type()
   * f2 -> Identifier()
   * f3 -> "("
   * f4 -> ( FormalParameterList() )?
   * f5 -> ")"
   * f6 -> "{"
   * f7 -> ( VarDeclaration() )*
   * f8 -> ( Statement() )*
   * f9 -> "return"
   * f10 -> Expression()
   * f11 -> ";"
   * f12 -> "}"
   */
  public void visit(MethodDeclaration n) {
    n.f0.accept(this);
    inheritedType = SymbolType.ST_METHOD; // Do not use type Type() for symbol table
    n.f1.accept(this);
    n.f2.accept(this);
    //inheritedType = SymbolType.ST_NULL;   // Reset type
    n.f1.accept(this);
    n.f3.accept(this);
    n.f4.accept(this);
    n.f5.accept(this);
    n.f6.accept(this);
    n.f7.accept(this);
    n.f8.accept(this);
    n.f9.accept(this);
    n.f10.accept(this);
    n.f11.accept(this);
    n.f12.accept(this);
  }

  ///**
  // * f0 -> FormalParameter()
  // * f1 -> ( FormalParameterRest() )*
  // */
  //public void visit(FormalParameterList n) {
  //  n.f0.accept(this);
  //  n.f1.accept(this);
  //}

  ///**
  // * f0 -> Type()
  // * f1 -> Identifier()
  // */
  //public void visit(FormalParameter n) {
  //  n.f0.accept(this);
  //  n.f1.accept(this);
  //}

  ///**
  // * f0 -> ","
  // * f1 -> FormalParameter()
  // */
  //public void visit(FormalParameterRest n) {
  //  n.f0.accept(this);
  //  n.f1.accept(this);
  //}

  /**
   * f0 -> ArrayType()
   *       | BooleanType()
   *       | IntegerType()
   *       | Identifier()
   */
  public void visit(Type n) {
    declaringType = true; // Used by Identifier if declaration is type CLASS_VAR
    n.f0.accept(this);
    declaringType = false;
  }

  /**
   * f0 -> "int"
   * f1 -> "["
   * f2 -> "]"
   */
  public void visit(ArrayType n) {
    n.f0.accept(this);
    n.f1.accept(this);
    n.f2.accept(this);

    // If type is method, then array is return type of method
    if (inheritedType != SymbolType.ST_METHOD)
      inheritedType = SymbolType.ST_INT_ARR;
  }

  /**
   * f0 -> "boolean"
   */
  public void visit(BooleanType n) {
    n.f0.accept(this);

    // If type is method, then boolean is return type of method
    if (inheritedType != SymbolType.ST_METHOD)
      inheritedType = SymbolType.ST_BOOLEAN;
  }

  /**
   * f0 -> "int"
   */
  public void visit(IntegerType n) {
    //System.out.println(n.f0);
    n.f0.accept(this);

    // If type is method, then integer is return type of method
    if (inheritedType != SymbolType.ST_METHOD)
      inheritedType = SymbolType.ST_INT;
  }

  ///**
  // * f0 -> Block()
  // *       | AssignmentStatement()
  // *       | ArrayAssignmentStatement()
  // *       | IfStatement()
  // *       | WhileStatement()
  // *       | PrintStatement()
  // */
  //public void visit(Statement n) {
  //  n.f0.accept(this);
  //}

  ///**
  // * f0 -> "{"
  // * f1 -> ( Statement() )*
  // * f2 -> "}"
  // */
  //public void visit(Block n) {
  //  n.f0.accept(this);
  //  n.f1.accept(this);
  //  n.f2.accept(this);
  //}

  ///**
  // * f0 -> Identifier()
  // * f1 -> "="
  // * f2 -> Expression()
  // * f3 -> ";"
  // */
  //public void visit(AssignmentStatement n) {
  //  n.f0.accept(this);
  //  n.f1.accept(this);
  //  n.f2.accept(this);
  //  n.f3.accept(this);
  //}

  ///**
  // * f0 -> Identifier()
  // * f1 -> "["
  // * f2 -> Expression()
  // * f3 -> "]"
  // * f4 -> "="
  // * f5 -> Expression()
  // * f6 -> ";"
  // */
  //public void visit(ArrayAssignmentStatement n) {
  //  n.f0.accept(this);
  //  n.f1.accept(this);
  //  n.f2.accept(this);
  //  n.f3.accept(this);
  //  n.f4.accept(this);
  //  n.f5.accept(this);
  //  n.f6.accept(this);
  //}

  ///**
  // * f0 -> "if"
  // * f1 -> "("
  // * f2 -> Expression()
  // * f3 -> ")"
  // * f4 -> Statement()
  // * f5 -> "else"
  // * f6 -> Statement()
  // */
  //public void visit(IfStatement n) {
  //  n.f0.accept(this);
  //  n.f1.accept(this);
  //  n.f2.accept(this);
  //  n.f3.accept(this);
  //  n.f4.accept(this);
  //  n.f5.accept(this);
  //  n.f6.accept(this);
  //}

  ///**
  // * f0 -> "while"
  // * f1 -> "("
  // * f2 -> Expression()
  // * f3 -> ")"
  // * f4 -> Statement()
  // */
  //public void visit(WhileStatement n) {
  //  n.f0.accept(this);
  //  n.f1.accept(this);
  //  n.f2.accept(this);
  //  n.f3.accept(this);
  //  n.f4.accept(this);
  //}

  ///**
  // * f0 -> "System.out.println"
  // * f1 -> "("
  // * f2 -> Expression()
  // * f3 -> ")"
  // * f4 -> ";"
  // */
  //public void visit(PrintStatement n) {
  //  n.f0.accept(this);
  //  n.f1.accept(this);
  //  n.f2.accept(this);
  //  n.f3.accept(this);
  //  n.f4.accept(this);
  //}

  ///**
  // * f0 -> AndExpression()
  // *       | CompareExpression()
  // *       | PlusExpression()
  // *       | MinusExpression()
  // *       | TimesExpression()
  // *       | ArrayLookup()
  // *       | ArrayLength()
  // *       | MessageSend()
  // *       | PrimaryExpression()
  // */
  //public void visit(Expression n) {
  //  n.f0.accept(this);
  //}

  ///**
  // * f0 -> PrimaryExpression()
  // * f1 -> "&&"
  // * f2 -> PrimaryExpression()
  // */
  //public void visit(AndExpression n) {
  //  n.f0.accept(this);
  //  n.f1.accept(this);
  //  n.f2.accept(this);
  //}

  ///**
  // * f0 -> PrimaryExpression()
  // * f1 -> "<"
  // * f2 -> PrimaryExpression()
  // */
  //public void visit(CompareExpression n) {
  //  n.f0.accept(this);
  //  n.f1.accept(this);
  //  n.f2.accept(this);
  //}

  ///**
  // * f0 -> PrimaryExpression()
  // * f1 -> "+"
  // * f2 -> PrimaryExpression()
  // */
  //public void visit(PlusExpression n) {
  //  n.f0.accept(this);
  //  n.f1.accept(this);
  //  n.f2.accept(this);
  //}

  ///**
  // * f0 -> PrimaryExpression()
  // * f1 -> "-"
  // * f2 -> PrimaryExpression()
  // */
  //public void visit(MinusExpression n) {
  //  n.f0.accept(this);
  //  n.f1.accept(this);
  //  n.f2.accept(this);
  //}

  ///**
  // * f0 -> PrimaryExpression()
  // * f1 -> "*"
  // * f2 -> PrimaryExpression()
  // */
  //public void visit(TimesExpression n) {
  //  n.f0.accept(this);
  //  n.f1.accept(this);
  //  n.f2.accept(this);
  //}

  ///**
  // * f0 -> PrimaryExpression()
  // * f1 -> "["
  // * f2 -> PrimaryExpression()
  // * f3 -> "]"
  // */
  //public void visit(ArrayLookup n) {
  //  n.f0.accept(this);
  //  n.f1.accept(this);
  //  n.f2.accept(this);
  //  n.f3.accept(this);
  //}

  ///**
  // * f0 -> PrimaryExpression()
  // * f1 -> "."
  // * f2 -> "length"
  // */
  //public void visit(ArrayLength n) {
  //  n.f0.accept(this);
  //  n.f1.accept(this);
  //  n.f2.accept(this);
  //}

  ///**
  // * f0 -> PrimaryExpression()
  // * f1 -> "."
  // * f2 -> Identifier()
  // * f3 -> "("
  // * f4 -> ( ExpressionList() )?
  // * f5 -> ")"
  // */
  //public void visit(MessageSend n) {
  //  n.f0.accept(this);
  //  n.f1.accept(this);
  //  n.f2.accept(this);
  //  n.f3.accept(this);
  //  n.f4.accept(this);
  //  n.f5.accept(this);
  //}

  ///**
  // * f0 -> Expression()
  // * f1 -> ( ExpressionRest() )*
  // */
  //public void visit(ExpressionList n) {
  //  n.f0.accept(this);
  //  n.f1.accept(this);
  //}

  ///**
  // * f0 -> ","
  // * f1 -> Expression()
  // */
  //public void visit(ExpressionRest n) {
  //  n.f0.accept(this);
  //  n.f1.accept(this);
  //}

  ///**
  // * f0 -> IntegerLiteral()
  // *       | TrueLiteral()
  // *       | FalseLiteral()
  // *       | Identifier()
  // *       | ThisExpression()
  // *       | ArrayAllocationExpression()
  // *       | AllocationExpression()
  // *       | NotExpression()
  // *       | BracketExpression()
  // */
  //public void visit(PrimaryExpression n) {
  //  n.f0.accept(this);
  //}

  ///**
  // * f0 -> <INTEGER_LITERAL>
  // */
  //public void visit(IntegerLiteral n) {
  //  n.f0.accept(this);
  //}

  ///**
  // * f0 -> "true"
  // */
  //public void visit(TrueLiteral n) {
  //  n.f0.accept(this);
  //}

  ///**
  // * f0 -> "false"
  // */
  //public void visit(FalseLiteral n) {
  //  n.f0.accept(this);
  //}

  /**
   * f0 -> <IDENTIFIER>
   */
  public void visit(Identifier n) {
    n.f0.accept(this);

    /* Add to symbol table if identifier is a declaration
     * Each case should include special symbolData (maybe derived classes?)
     *
     * If not a declaration, identifier is a variable access?? (Check later)
     */
    switch (inheritedType) {

      // If inheritedType is null but we're declaring a type, it must be a special class declaration
      case ST_NULL:
        if (declaringType) {
          // NEXT TIME: Be sure to check that declared class exists (or backpatch), put in symbol table, do with methods

          // To typecheck Class variables
          // Put below code into special function for type checking (that maintains backpatch list?)
          Scope currScope = symbolTable.getGlobalScope();
          currScope.PrintAll();

          inheritedType = SymbolType.ST_CLASS_VAR;
          deepInheritedType = new ClassVarData(n.f0);
        }
        break;

      // Used in methods, parameters, or variables. Same action as String[]
      case ST_BOOLEAN:
      case ST_INT:
      case ST_INT_ARR:

      // Called only as parameter of function main
      case ST_STRING_ARR:
        symbolTable.addSymbol(n.f0, inheritedType);
        break;

      // Used in methods, parameters, or variables. Needs derived SymbolData
      case ST_CLASS_VAR:
        System.out.println(n.f0 + " is class var of type " + deepInheritedType.getDeepType());
        deepInheritedType = null; // Reset deep type
        break;

      // ClassDeclaration or MainClass
      case ST_CLASS:
        symbolTable.addSymbol(n.f0, inheritedType);

        break;

      // ClassExtendsDeclaration
      case ST_CLASS_EXTENDS:
        symbolTable.addSymbol(n.f0, inheritedType);
        break;

      default:
        System.err.println("error: unexpected case in Identifier for " + n.f0);
    }

    if(inheritedType != SymbolType.ST_NULL && !declaringType)
      inheritedType = SymbolType.ST_NULL;
  }

  ///**
  // * f0 -> "this"
  // */
  //public void visit(ThisExpression n) {
  //  n.f0.accept(this);
  //}

  ///**
  // * f0 -> "new"
  // * f1 -> "int"
  // * f2 -> "["
  // * f3 -> Expression()
  // * f4 -> "]"
  // */
  //public void visit(ArrayAllocationExpression n) {
  //  n.f0.accept(this);
  //  n.f1.accept(this);
  //  n.f2.accept(this);
  //  n.f3.accept(this);
  //  n.f4.accept(this);
  //}

  ///**
  // * f0 -> "new"
  // * f1 -> Identifier()
  // * f2 -> "("
  // * f3 -> ")"
  // */
  //public void visit(AllocationExpression n) {
  //  n.f0.accept(this);
  //  n.f1.accept(this);
  //  n.f2.accept(this);
  //  n.f3.accept(this);
  //}

  ///**
  // * f0 -> "!"
  // * f1 -> Expression()
  // */
  //public void visit(NotExpression n) {
  //  n.f0.accept(this);
  //  n.f1.accept(this);
  //}

  ///**
  // * f0 -> "("
  // * f1 -> Expression()
  // * f2 -> ")"
  // */
  //public void visit(BracketExpression n) {
  //  n.f0.accept(this);
  //  n.f1.accept(this);
  //  n.f2.accept(this);
  //}

}
