package visitor;
import syntaxtree.*;
import symboltable.*;
import java.util.*;

public class DMVisitor extends DepthFirstVisitor {

  SymbolTable symbolTable;
  SymbolType inheritedType;
  SymbolData deepInheritedType;

  private NodeToken cn;

  public DMVisitor(){
    symbolTable = new SymbolTable();
    inheritedType = SymbolType.ST_NULL;
    deepInheritedType = null;

  }

  //makes sure inheritedType is of type st
  //then resets inheretedType
  void typeCheck(SymbolType st){
    if(inheritedType != st){
      System.out.println("Error: Unexpected Type" + inheritedType);
      System.out.println("Expecting " + st);
      System.exit(-1);
    }
    inheritedType = SymbolType.ST_NULL;
  }

  //gets the inherited type then resets it
  SymbolType getInheritedType(){
    SymbolType it = inheritedType;
    inheritedType = SymbolType.ST_NULL;
    return it;
  }

  //suggested helper functions

  //returns class's name
  String classname(MainClass mc){
    return mc.f1.f0.toString();
  }

  String classname(ClassDeclaration cd){
    return cd.f1.f0.toString();
  }

  String classname(ClassExtendsDeclaration ced){
    return ced.f1.f0.toString();
  }

  //returns a method's name
  String methodname(MethodDeclaration md){
    return md.f2.f0.toString();
  }

  boolean distinct(ArrayList<String> idList){
    HashSet hs = new HashSet<String>(idList);
    if(hs.size() < idList.size()){
      System.out.println("Error: non-Distinct");
      return false;
    }
    else{
      System.out.println("Distinct :D");
      return true;
    }
  }

  /**
   * f0 -> MainClass()
   * f1 -> ( TypeDeclaration() )*
   * f2 -> <EOF>
   */
  public void visit(Goal n) {
    ArrayList<String> classnames = new ArrayList<String>();
    classnames.add(classname(n.f0));
    //add all classnames in TypeDeclaration* to list
    for(Node typeDeclaration : n.f1.nodes){
      TypeDeclaration td = (TypeDeclaration)typeDeclaration;
      Node choice = td.f0.choice;
      int which = td.f0.which;
      if(which == 0){//ClassDeclaration
        classnames.add(classname((ClassDeclaration)choice));
      }
      else{//ClassExtendsDeclaration
        classnames.add(classname((ClassExtendsDeclaration)choice));
      }
    }

    System.out.println("Class Names:");
    for(String cn : classnames){
      System.out.println(cn);
    }
    System.out.println();

    if(!distinct(classnames)){
      System.exit(-1);
    }

    n.f0.accept(this);
    n.f1.accept(this);
    n.f2.accept(this);
  }

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
    //add class to symbol table
    n.f0.accept(this);
    n.f1.accept(this);
    cn = n.f1.f0;
    symbolTable.addSymbol(n.f1.f0, SymbolType.ST_CLASS);

    //enter new scope for class
    n.f2.accept(this);
    symbolTable.newScope();

    //todo: add main to function scope?
    n.f3.accept(this);
    n.f4.accept(this);
    n.f5.accept(this);
    n.f6.accept(this);

    //enter new scope for main
    n.f7.accept(this);
    symbolTable.newScope();//scope: main

    //add String[] id to symbol table
    n.f8.accept(this);
    n.f9.accept(this);
    n.f10.accept(this);
    n.f11.accept(this);
    symbolTable.addSymbol(n.f11.f0, SymbolType.ST_STRING_ARR);

    n.f12.accept(this);
    n.f13.accept(this);
    n.f14.accept(this);
    n.f15.accept(this);

    //exit main scope
    n.f16.accept(this);
    symbolTable.exitScope();

    //exit class scope
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
    //add class to symbol table
    n.f0.accept(this);
    n.f1.accept(this);
    symbolTable.addSymbol(n.f1.f0, SymbolType.ST_CLASS);

    //enter class scope
    n.f2.accept(this);
    symbolTable.newScope();

    n.f3.accept(this);

    ArrayList<String> methodNames = new ArrayList<String>();
    for(Node node : n.f4.nodes){
      methodNames.add(methodname((MethodDeclaration)node));
    }

    System.out.println("method names:");
    for(String s : methodNames){
      System.out.println(s);
    }
    System.out.println();

    n.f4.accept(this);

    //exit class scope
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
    //add class to symbol table
    n.f0.accept(this);
    n.f1.accept(this);
    symbolTable.addSymbol(n.f1.f0, SymbolType.ST_CLASS_EXTENDS);

    n.f2.accept(this);
    n.f3.accept(this);

    //enter class scope
    n.f4.accept(this);
    symbolTable.newScope();

    n.f5.accept(this);
    n.f6.accept(this);

    //exit class scope
    n.f7.accept(this);
    symbolTable.exitScope();
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

    SymbolType st = getInheritedType();
    if(st == SymbolType.ST_CLASS)
      st = SymbolType.ST_CLASS_VAR;
    symbolTable.addSymbol(n.f1.f0, st);
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
    //add method to symbol table
    n.f0.accept(this);
    n.f1.accept(this);
    n.f2.accept(this);
    symbolTable.addSymbol(n.f2.f0, SymbolType.ST_METHOD);

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
    n.f0.accept(this);
    if(n.f0.which == 3){//if we chose Identifier()
      inheritedType = SymbolType.ST_CLASS;
    }
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
    inheritedType = SymbolType.ST_INT_ARR;
  }

  /**
   * f0 -> "boolean"
   */
  public void visit(BooleanType n) {
    n.f0.accept(this);
    inheritedType = SymbolType.ST_BOOLEAN;
  }

  /**
   * f0 -> "int"
   */
  public void visit(IntegerType n) {
    n.f0.accept(this);
    inheritedType = SymbolType.ST_INT;
  }

  /**
   * f0 -> Block()
   *       | AssignmentStatement()
   *       | ArrayAssignmentStatement()
   *       | IfStatement()
   *       | WhileStatement()
   *       | PrintStatement()
   */
  public void visit(Statement n) {
    n.f0.accept(this);
  }

  /**
   * f0 -> "{"
   * f1 -> ( Statement() )*
   * f2 -> "}"
   */
  public void visit(Block n) {
    n.f0.accept(this);
    symbolTable.newScope();
    n.f1.accept(this);
    n.f2.accept(this);
    symbolTable.exitScope();
  }

  /**
   * f0 -> Identifier()
   * f1 -> "="
   * f2 -> Expression()
   * f3 -> ";"
   */
  public void visit(AssignmentStatement n) {
    SymbolType lhs, rhs;

    //lhs
    n.f0.accept(this);
    SymbolData sd = symbolTable.getSymbolData(n.f0.f0, SymbolType.ST_VARIABLE);
    if(sd == null){
      System.out.println("Symbol " + n.f0.f0.toString() + " does not exist");
      System.exit(-1);
    }

    n.f1.accept(this);

    //rhs
    n.f2.accept(this);
    rhs = inheritedType;

    n.f3.accept(this);
    //todo: check if rhs type is <= lhs type
  }

  /**
   * f0 -> Identifier()
   * f1 -> "["
   * f2 -> Expression()
   * f3 -> "]"
   * f4 -> "="
   * f5 -> Expression()
   * f6 -> ";"
   */
  public void visit(ArrayAssignmentStatement n) {
    //array id
    n.f0.accept(this);
    inheritedType = symbolTable.getSymbolData(n.f0.f0, SymbolType.ST_VARIABLE).getType();
    typeCheck(SymbolType.ST_INT_ARR);

    //array index
    n.f1.accept(this);
    n.f2.accept(this);
    n.f3.accept(this);
    typeCheck(SymbolType.ST_INT);

    n.f4.accept(this);

    //rhs
    n.f5.accept(this);
    typeCheck(SymbolType.ST_INT);

    n.f6.accept(this);
  }

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

  /**
   * f0 -> AndExpression()
   *       | CompareExpression()
   *       | PlusExpression()
   *       | MinusExpression()
   *       | TimesExpression()
   *       | ArrayLookup()
   *       | ArrayLength()
   *       | MessageSend()
   *       | PrimaryExpression()
   */
  public void visit(Expression n) {
    n.f0.accept(this);
    System.out.println("Expression Evaluated");
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "&&"
   * f2 -> PrimaryExpression()
   */
  public void visit(AndExpression n) {
    //lhs
    n.f0.accept(this);
    typeCheck(SymbolType.ST_BOOLEAN);

    n.f1.accept(this);

    //rhs
    n.f2.accept(this);
    typeCheck(SymbolType.ST_BOOLEAN);

    //<AndExpression> = ST_Boolean
    inheritedType = SymbolType.ST_BOOLEAN;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "<"
   * f2 -> PrimaryExpression()
   */
  public void visit(CompareExpression n) {
    //lhs
    n.f0.accept(this);
    typeCheck(SymbolType.ST_INT);

    n.f1.accept(this);

    //rhs
    n.f2.accept(this);
    typeCheck(SymbolType.ST_INT);

    //<CompareExpression> = ST_BOOLEAN
    inheritedType = SymbolType.ST_BOOLEAN;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "+"
   * f2 -> PrimaryExpression()
   */
  public void visit(PlusExpression n) {
    //lhs
    n.f0.accept(this);
    typeCheck(SymbolType.ST_INT);

    n.f1.accept(this);

    //rhs
    n.f2.accept(this);
    typeCheck(SymbolType.ST_INT);

    //<PlusExpression> = ST_INT
    inheritedType = SymbolType.ST_INT;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "-"
   * f2 -> PrimaryExpression()
   */
  public void visit(MinusExpression n) {
    //lhs
    n.f0.accept(this);
    typeCheck(SymbolType.ST_INT);

    n.f1.accept(this);

    //rhs
    n.f2.accept(this);
    typeCheck(SymbolType.ST_INT);

    //<MinusExpression> = ST_INT
    inheritedType = SymbolType.ST_INT;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "*"
   * f2 -> PrimaryExpression()
   */
  public void visit(TimesExpression n) {
    //lhs
    n.f0.accept(this);
    typeCheck(SymbolType.ST_INT);

    n.f1.accept(this);

    //rhs
    n.f2.accept(this);
    typeCheck(SymbolType.ST_INT);

    //<TimesExpression> = ST_INT
    inheritedType = SymbolType.ST_INT;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "["
   * f2 -> PrimaryExpression()
   * f3 -> "]"
   */
  public void visit(ArrayLookup n) {
    n.f0.accept(this);
    typeCheck(SymbolType.ST_INT_ARR);

    n.f1.accept(this);
    n.f2.accept(this);
    n.f3.accept(this);
    typeCheck(SymbolType.ST_INT);

    //<ArrayLookup> = ST_INT
    inheritedType = SymbolType.ST_INT;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "."
   * f2 -> "length"
   */
  public void visit(ArrayLength n) {
    n.f0.accept(this);
    n.f1.accept(this);
    n.f2.accept(this);
    typeCheck(SymbolType.ST_INT_ARR);

    //<ArrayLength> = ST_INT
    inheritedType = SymbolType.ST_INT;
  }

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

  /**
   * f0 -> <INTEGER_LITERAL>
   */
  public void visit(IntegerLiteral n) {
    n.f0.accept(this);

    //<IntegerLiteral> = ST_INT
    inheritedType = SymbolType.ST_INT;
  }

  /**
   * f0 -> "true"
   */
  public void visit(TrueLiteral n) {
    n.f0.accept(this);

    //<TrueLiteral> = ST_BOOLEAN
    inheritedType = SymbolType.ST_BOOLEAN;
  }

  /**
   * f0 -> "false"
   */
  public void visit(FalseLiteral n) {
    n.f0.accept(this);

    //<FalseLiteral> = ST_BOOLEAN
    inheritedType = SymbolType.ST_BOOLEAN;
  }

  /**
   * f0 -> <IDENTIFIER>
   */
  public void visit(Identifier n) {
    n.f0.accept(this);

    //inheritedType = SymbolType.ST_CLASS_VAR;
    //deepInheritedType = new ClassVarData(n.f0);

    //ST_CLASS_VAR
    //System.out.println(n.f0 + " is class var of type " + deepInheritedType.getDeepType());
    //deepInheritedType = null; // Reset deep type


    //inheritedType = SymbolType.ST_NULL;
  }

  ///**
  // * f0 -> "this"
  // */
  //public void visit(ThisExpression n) {
  //  n.f0.accept(this);
  //}

  /**
   * f0 -> "new"
   * f1 -> "int"
   * f2 -> "["
   * f3 -> Expression()
   * f4 -> "]"
   */
  public void visit(ArrayAllocationExpression n) {
    n.f0.accept(this);
    n.f1.accept(this);
    n.f2.accept(this);
    n.f3.accept(this);
    n.f4.accept(this);
    typeCheck(SymbolType.ST_INT);

    //<ArrayAllocationExpression> = ST_INT
    inheritedType = SymbolType.ST_INT_ARR;
  }

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

  /**
   * f0 -> "!"
   * f1 -> Expression()
   */
  public void visit(NotExpression n) {
    n.f0.accept(this);
    n.f1.accept(this);
    typeCheck(SymbolType.ST_BOOLEAN);

    //<NotExpression> = ST_BOOLEAN
    inheritedType = SymbolType.ST_BOOLEAN;
  }

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
