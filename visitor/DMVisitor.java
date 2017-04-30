package visitor;
import syntaxtree.*;
import symboltable.*;
import java.util.*;

public class DMVisitor extends DepthFirstVisitor {

  SymbolTable symbolTable;
  ClassRefChecker classRefChecker;      // Checks unknown class declarations and method calls
  SymbolType inheritedType;             // Basic type of object
  SymbolData deepInheritedType;         // "Deep" type refers to those with derived SymbolData objects
  Vector<SymbolData> synthFormalParam;  // Used by MethodDeclaration (synthesized from FormalParam)
  Vector<SymbolData> synthExprList;     // Used by MessageSend (synthesized from ExprList)
  Vector<MethodData> synthUnverifiedMethods;  // Used by Statement and ClassDecl (synthesize from MessageSend)
  boolean synthCalledMethod;            // Used in Statements and methodDec to backpatch method return types
  NodeToken currentClassName;

  //Class Constructor
  public DMVisitor(){
    symbolTable = new SymbolTable();
    classRefChecker = new ClassRefChecker(symbolTable);
    inheritedType = SymbolType.ST_NULL;
    deepInheritedType = null;
    synthFormalParam = new Vector<>();
    synthExprList = new Vector<>();
    synthUnverifiedMethods = new Vector<>();
    currentClassName = null;
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

  //gets deep inherited type then resets it
  SymbolData getDeepInheritedType() {
    SymbolData data = deepInheritedType;
    deepInheritedType = null;
    return data;
  }

  //returns the synthesized formal parameters
  Vector<SymbolData> getSynthFormalParam() {
    Vector<SymbolData> data = synthFormalParam;
    synthFormalParam = new Vector<>();
    return data;
  }

  //returns the synthesized Expression list
  Vector<SymbolData> getSynthExprList() {
    Vector<SymbolData> data = synthExprList;
    synthExprList = new Vector<>();
    return data;
  }

  //returns the Synthesized Unverified Methods
  Vector<MethodData> getSynthUnverifiedMethods() {
    Vector<MethodData> data = synthUnverifiedMethods;
    synthUnverifiedMethods = new Vector<>();
    return data;
  }

  //--------------------------
  //suggested helper functions
  //--------------------------

  //returns class's name given a class node
  String classname(MainClass mc){
    return mc.f1.f0.toString();
  }

  String classname(ClassDeclaration cd){
    return cd.f1.f0.toString();
  }

  String classname(ClassExtendsDeclaration ced){
    return ced.f1.f0.toString();
  }
  //--------------------

  //returns a method's name given a method node
  String methodname(MethodDeclaration md){
    return md.f2.f0.toString();
  }

  //returns true if the strings in idList are pairwise distinct
  boolean distinct(ArrayList<String> idList){
    HashSet hs = new HashSet<String>(idList);
    if(hs.size() < idList.size()) return false;
    else return true;
  }

  //--------------------------
  //End suggested helper functions
  //--------------------------

  boolean getSynthCalledMethod() {
    if (synthCalledMethod) {
      synthCalledMethod = false;
      return true;
    }
    return false;
  }

  void printErrorMethod(String prepend, NodeToken methodToken, Vector<SymbolData> exprList) {
    int listSize = exprList.size();

    System.err.print(prepend + methodToken + "(");
    for (int i = 0; i < listSize; i++) {
      if (i != listSize - 1)
        System.err.print(exprList.get(i).getFormalType() + ", ");
      else
        System.err.print(exprList.get(i).getFormalType());
    }
    System.err.println(")");
  }

  void checkAndAddClassMethods(NodeToken classToken, NodeListOptional n) {
    ArrayList<String> methodNames = new ArrayList<String>();
    for(Node node : n.nodes){
      MethodDeclaration md = (MethodDeclaration)node;
      methodNames.add(methodname(md));
      symbolTable.addMethodToClass(classToken, readMethod(md));
    }

    System.out.println("\nmethod names:");
    for(String s : methodNames){
      System.out.println(s);
    }
    System.out.println();

    if(!distinct(methodNames)){
      System.out.println("Methods not distinct!");
      System.exit(-1);
    }
  }

  MethodData readMethod(MethodDeclaration md) {
    MethodDeclareVisitor mdVisitor = new MethodDeclareVisitor(symbolTable);

    // Get return type for method
    md.f1.accept(mdVisitor);
    SymbolType returnType = mdVisitor.getInheritedType();
    SymbolData returnData = mdVisitor.getDeepInheritedType();
    if (returnData == null)
      returnData = new SymbolData(returnType);

    // Get formal paramters
    md.f4.accept(mdVisitor);

    // Add unverified classes to ClassRefChecker
    for (NodeToken classToken : mdVisitor.unverifiedClasses)
      classRefChecker.verifyClassExists(classToken);

    return new MethodData(md.f2.f0, returnData, mdVisitor.getSynthFormalParam());
  }

  /**
   * f0 -> MainClass()
   * f1 -> ( TypeDeclaration() )*
   * f2 -> <EOF>
   */
  public void visit(Goal n) {
    // Verify distinct classes and methods, adds both to symbol table
    ArrayList<String> classnames = new ArrayList<String>();
    classnames.add(classname(n.f0));
    symbolTable.addSymbol(n.f0.f1.f0, new ClassData(n.f0.f1.f0));

    //add all classnames in TypeDeclaration* to list
    for(Node typeDeclaration : n.f1.nodes){
      TypeDeclaration td = (TypeDeclaration)typeDeclaration;
      Node choice = td.f0.choice;
      int which = td.f0.which;

      if(which == 0){//ClassDeclaration
        ClassDeclaration cd = (ClassDeclaration)choice;
        classnames.add(classname(cd));

        // Note: do we need to add main class's main method above? Probably not.
        symbolTable.addSymbol(cd.f1.f0, new ClassData(cd.f1.f0)); // Add class
        classRefChecker.notifyClassExists(cd.f1.f0);              // Notify class exists
        checkAndAddClassMethods(cd.f1.f0, cd.f4);                 // Add methods
      }
      else{//ClassExtendsDeclaration
        ClassExtendsDeclaration ced = (ClassExtendsDeclaration)choice;
        classnames.add(classname(ced));
        symbolTable.addSymbol(ced.f1.f0, SymbolType.ST_CLASS_EXTENDS);
      }
    }

    //prints class names for debugging
    System.out.println("Class Names:");
    for(String cn : classnames){
      System.out.println(cn);
    }
    System.out.println();

    //checks if classnames are distinct
    if(!distinct(classnames)){
      System.exit(-1);
    }

    classRefChecker.checkClassesExisted();

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
    currentClassName = n.f1.f0;

    //enter new scope for class
    n.f2.accept(this);
    symbolTable.newScope();

    n.f3.accept(this);
    n.f4.accept(this);
    n.f5.accept(this);
    n.f6.accept(this);
    //symbolTable.addMethodToClass(n.f1.f0, MethodData.mainInstance(n.f6));

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
    currentClassName = null;
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
    currentClassName = n.f1.f0;

    //enter class scope
    n.f2.accept(this);
    symbolTable.newScope();

    n.f3.accept(this);
    n.f4.accept(this);

    //exit class scope
    n.f5.accept(this);
    symbolTable.exitScope();
    currentClassName = null;
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
    currentClassName = n.f1.f0;

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
    currentClassName = null;
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
    if(st == SymbolType.ST_CLASS_VAR)
      symbolTable.addSymbol(n.f1.f0, getDeepInheritedType());
    else
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
    // enter method scope
    symbolTable.newScope();
    n.f0.accept(this);
    n.f1.accept(this);

    // Get return type for method
    SymbolType returnType = getInheritedType();
    SymbolData returnData = getDeepInheritedType();
    n.f2.accept(this);
    n.f1.accept(this);
    n.f3.accept(this);
    n.f4.accept(this);
    n.f5.accept(this);

    // Convert SymbolType to SymbolData if need be
    if (returnData == null)
      returnData = new SymbolData(returnType);

    // Put methodData into containing class
    MethodData methodData = new MethodData(n.f2.f0, returnData, getSynthFormalParam());

    n.f6.accept(this);
    n.f7.accept(this);
    n.f8.accept(this);
    n.f9.accept(this);
    n.f10.accept(this);
    n.f11.accept(this);
    n.f12.accept(this);

    // To do: Typecheck the return value

    // exit method scope
    symbolTable.exitScope();
  }

  ///**
  // * f0 -> FormalParameter()
  // * f1 -> ( FormalParameterRest() )*
  // */
  //public void visit(FormalParameterList n) {
  //  n.f0.accept(this);
  //  n.f1.accept(this);
  //}

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
    if(st != SymbolType.ST_CLASS_VAR) {
      symbolTable.addSymbol(n.f1.f0, st); 
      synthFormalParam.add(new SymbolData(st));
    } else {
      symbolTable.addSymbol(n.f1.f0, sd);
      synthFormalParam.add(sd);
    }
  }

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
      inheritedType = SymbolType.ST_CLASS_VAR;

      // If class doesn't exist "yet", put it in backpatch list
      NodeToken classToken = ((Identifier) n.f0.choice).f0;
      deepInheritedType = new ClassVarData(classToken);
      if (!symbolTable.classExists(classToken))
        classRefChecker.verifyClassExists(classToken);
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

    if (getSynthCalledMethod()) {
      System.out.println("Statement: Method was called!");
      for (MethodData md : getSynthUnverifiedMethods()) {
        md.setReturnType(new SymbolData(SymbolType.ST_INT));
        System.out.println(md.getDeepType());
      }
    }
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
    SymbolData lhsSD = symbolTable.getSymbolData(n.f0.f0, SymbolType.ST_VARIABLE);
    if(lhsSD == null){
      System.out.println("Symbol " + n.f0.f0.toString() + " does not exist");
      System.exit(-1);
    }
    lhs = lhsSD.getType();

    n.f1.accept(this);

    //rhs
    n.f2.accept(this);
    rhs = inheritedType;

    n.f3.accept(this);
    if(lhs != rhs) System.exit(-1);

    //todo: check if rhs type is <= lhs type
    if(lhs == SymbolType.ST_CLASS_VAR){
      SymbolData rhsSD = getDeepInheritedType();
      //check if rhs <= lhs
    }
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

  /**
   * f0 -> "if"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   * f4 -> Statement()
   * f5 -> "else"
   * f6 -> Statement()
   */
  public void visit(IfStatement n) {
    n.f0.accept(this);

    //Expression should evaluate to boolean
    n.f1.accept(this);
    n.f2.accept(this);
    n.f3.accept(this);
    typeCheck(SymbolType.ST_BOOLEAN);

    n.f4.accept(this);
    n.f5.accept(this);
    n.f6.accept(this);
  }

  /**
   * f0 -> "while"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   * f4 -> Statement()
   */
  public void visit(WhileStatement n) {
    n.f0.accept(this);

    //Expression should evaluate to boolean
    n.f1.accept(this);
    n.f2.accept(this);
    n.f3.accept(this);
    typeCheck(SymbolType.ST_BOOLEAN);

    n.f4.accept(this);
  }

  /**
   * f0 -> "System.out.println"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   * f4 -> ";"
   */
  public void visit(PrintStatement n) {
    n.f0.accept(this);

    //Expression should Evaluate to int
    n.f1.accept(this);
    n.f2.accept(this);
    n.f3.accept(this);
    typeCheck(SymbolType.ST_INT);

    n.f4.accept(this);
  }

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

  /** 
   * MessageSend is a function call. Check that the method exists.
   * If the class doesn't have that method (wrong name, wrong args),
   * throw an error. If the class doesn't exist yet, throw the method
   * call into the ClassRefChecker list.
   *
   * f0 -> PrimaryExpression()
   * f1 -> "."
   * f2 -> Identifier()
   * f3 -> "("
   * f4 -> ( ExpressionList() )?
   * f5 -> ")"
   */
  public void visit(MessageSend n) {
    // In sec: consider case where deepInheritedType isn't null, but variable is backpatched
    // method should be backpatched too then.
    // Insertion into backpatching list has to occur at parent of messageSend so we know what
    // return type we need for the MethodData we put in classRefChecker for backpatching later.

    synthCalledMethod = true;

    //Primary Expression
    n.f0.accept(this);
    SymbolData callerData = getDeepInheritedType();
    if (callerData == null){
      System.out.println("Error: Message Send case not handled yet.");
      System.exit(-1);
    }

    n.f1.accept(this);

    //methodname
    n.f2.accept(this);
    System.out.println("Method: " + n.f2.f0);

    //Expression List
    n.f3.accept(this);
    n.f4.accept(this);
    Vector<SymbolData> givenArgs = getSynthExprList();
    n.f5.accept(this);

    // Get class type of caller from primary expression
    if (!symbolTable.classExists(new NodeToken(callerData.getDeepType()))) {
      System.out.println("MessageSend: type that doesn't exist");
      MethodData unknownMethod = 
          new MethodData(n.f2.f0, new SymbolData(SymbolType.ST_UNKNOWN), givenArgs);
      synthUnverifiedMethods.add(unknownMethod);
    }
    else {
      MethodData methodData = symbolTable
        .getMethodFromClass(new NodeToken(callerData.getDeepType()), n.f2.f0);

      // Error check for method existing
      if (methodData == null) {
        System.err.println("Error: " + n.f2.f0 + " doesn't exist");
        System.exit(-1);
      }

      // Error check for correct argument types
      Vector<SymbolData> methodParams = methodData.getParameterTypes();
      if (!methodParams.equals(givenArgs)) {
        System.err.println("Error: mismatched method signatures.");
        printErrorMethod("Method should be: ", n.f2.f0, methodParams);
        printErrorMethod("Method given: ", n.f2.f0, givenArgs);
        System.exit(-1);
      }

      // Set return type in inheritedType and deepInheritedType
      SymbolData returnType = methodData.getReturnType();
      inheritedType = returnType.getType();
      if (returnType.getType() == SymbolType.ST_CLASS_VAR)
        deepInheritedType = returnType;

      System.out.println(methodData.getDeepType() + " was called");
    }
  }

  /**
   * Note: ExpressionList is only used by MessageSend
   * 
   * f0 -> Expression()
   * f1 -> ( ExpressionRest() )*
   */
  public void visit(ExpressionList n) {
    n.f0.accept(this);

    // record variable types for method type check
    SymbolType st = getInheritedType();
    SymbolData sd = getDeepInheritedType();
    if(st != SymbolType.ST_CLASS_VAR)
      synthExprList.add(new SymbolData(st));
    else
      synthExprList.add(sd);

    n.f1.accept(this);
  }

  /**
   * Note: ExpressionRest is only used by ExpressionList
   * 
   * f0 -> ","
   * f1 -> Expression()
   */
  public void visit(ExpressionRest n) {
    n.f0.accept(this);
    n.f1.accept(this);

    // record variable types for method type check
    SymbolType st = getInheritedType();
    SymbolData sd = getDeepInheritedType();
    if(st != SymbolType.ST_CLASS_VAR)
      synthExprList.add(new SymbolData(st));
    else
      synthExprList.add(sd);
  }

  /**
   * f0 -> IntegerLiteral()
   *       | TrueLiteral()
   *       | FalseLiteral()
   *       | Identifier()
   *       | ThisExpression()
   *       | ArrayAllocationExpression()
   *       | AllocationExpression()
   *       | NotExpression()
   *       | BracketExpression()
   */
  public void visit(PrimaryExpression n) {
    n.f0.accept(this);

    // To check if variable exists (identifiers). If so, grab its type.
    if (n.f0.which == 3) {
      NodeToken varName = ((Identifier) n.f0.choice).f0;
      SymbolData data = symbolTable.getSymbolData(varName, SymbolType.ST_VARIABLE);

      if (data == null) {
        System.err.println("Error: variable " + varName + " not in scope");
        System.exit(-1);
      }

      inheritedType = data.getType();
      if (data.getType() == SymbolType.ST_CLASS_VAR)
        deepInheritedType = data;

      System.out.println("PrimaryExpression: " + varName + ", " + data.getFormalType());
    }
  }

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

  ///**
  // * f0 -> <IDENTIFIER>
  // */
  //public void visit(Identifier n) {
  //  n.f0.accept(this);
  //}

  /**
   * f0 -> "this"
   */
  public void visit(ThisExpression n) {
    n.f0.accept(this);
    inheritedType = SymbolType.ST_CLASS_VAR;
    deepInheritedType = new ClassVarData(currentClassName);
  }

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

  /**
   * f0 -> "new"
   * f1 -> Identifier()
   * f2 -> "("
   * f3 -> ")"
   */
  public void visit(AllocationExpression n) {
    n.f0.accept(this);
    n.f1.accept(this);
    n.f2.accept(this);
    n.f3.accept(this);

    inheritedType = SymbolType.ST_CLASS_VAR;
    deepInheritedType = new ClassVarData(n.f1.f0);
  }

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
