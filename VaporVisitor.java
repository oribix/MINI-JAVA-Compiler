import visitor.*;
import syntaxtree.*;
import symboltable.*;
import vaportools.*;
import java.util.*;

public class VaporVisitor extends DepthFirstVisitor {
  boolean debug_g = false;

  // Phase2 code
  VaporPrinter vaporPrinter;
  String synthTempVar;
  int tempVarIndex;
  int nullLabelIndex;
  int boundsLabelIndex;
  int labelnum;
  int ifElseCount;
  int whileCount;

  // Phase1 code
  SymbolTable symbolTable;
  SymbolType inheritedType;             // Basic type of object
  SymbolData deepInheritedType;         // "Deep" type refers to those with derived SymbolData objects
  Vector<String> synthFormalParamNames; // String names of formal params
  Vector<String> synthExprListNames;     // Used by MessageSend (synthesized from ExprList)
  Vector<MethodData> synthUnverifiedMethods;  // Used by Statement and ClassDecl (synthesize from MessageSend)
  boolean synthCalledMethod;            // Used in Statements and methodDec to backpatch method return types
  NodeToken currentClassName;

  //Class Constructor
  public VaporVisitor(){
    symbolTable = new SymbolTable();
    inheritedType = SymbolType.ST_NULL;
    deepInheritedType = null;
    synthFormalParamNames = new Vector<>();
    synthExprListNames = new Vector<>();
    synthUnverifiedMethods = new Vector<>();
    currentClassName = null;
    vaporPrinter = new VaporPrinter(symbolTable);
    synthTempVar = "";

    tempVarIndex = 0;
    nullLabelIndex = 0;
    boundsLabelIndex = 0;
    
    ifElseCount = 0;
    whileCount = 0;
  }

  //-----------------------------
  // Getters/Resetters of globals
  //-----------------------------

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
  Vector<String> getSynthFormalParamNames(){
	Vector<String> data = synthFormalParamNames;
    synthFormalParamNames = new Vector<>();
    return data;
  }

  //returns the synthesized Expression list
  Vector<String> getSynthExprListNames() {
    Vector<String> data = synthExprListNames;
    synthExprListNames = new Vector<>();
    return data;
  }

  //returns the Synthesized Unverified Methods
  Vector<MethodData> getSynthUnverifiedMethods() {
    Vector<MethodData> data = synthUnverifiedMethods;
    synthUnverifiedMethods = new Vector<>();
    return data;
  }

  boolean getSynthCalledMethod() {
    if (synthCalledMethod) {
      synthCalledMethod = false;
      return true;
    }
    return false;
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

  //returns a method's name given a method node
  String varname(VarDeclaration vd){
    return vd.f1.f0.toString();
  }

  //returns true if the strings in idList are pairwise distinct
  boolean distinct(ArrayList<String> idList){
    HashSet hs = new HashSet<String>(idList);
    if(hs.size() < idList.size()) return false;
    else return true;
  }

  boolean noOverloading(ArrayList<NodeToken> classTokens) {
    // Check all classes in scope
    for (NodeToken classToken : classTokens) {
      ClassData cd = (ClassData) symbolTable.getSymbolData(
          classToken, SymbolType.ST_CLASS);

      // If class has parent, look at parent methods
      if (cd.getParent() != null) {
        ClassData parent = (ClassData) symbolTable.getSymbolData(
            cd.getParent(), SymbolType.ST_CLASS);

        // Put class' methods in hashmap for easy access
        HashMap<String, MethodData> cdMethodMap = new HashMap<>();
        for (MethodData md : cd.getMethods())
          cdMethodMap.put(md.getName().toString(), md);

        // Cycle up the chain of parents
        NodeToken parentClassToken = cd.getParent();
        while (parent != null) {
          // Compare methodData objects of parent and child
          for (MethodData md : parent.getMethods()) {
            MethodData parentMd = cdMethodMap.get(md.getName().toString());

            if (parentMd != null) {
              if (!md.equals(parentMd)) {
                DebugErr("Error: " + classToken + "." 
                    + md.getName().toString() + "() overloads function " 
                    + parentClassToken + "." + parentMd.getName().toString() + "()");
                return false;
              }
            }
          }

          // Get higher up parent
          parentClassToken = parent.getParent();
          if (parentClassToken != null) {
            parent = (ClassData) symbolTable.getSymbolData(
                parentClassToken, SymbolType.ST_CLASS);
          } else
            parent = null;
        }
      }
    }
    return true;
  }

  //--------------------------
  //End suggested helper functions
  //Start typechecking overview functions
  //--------------------------

  void printErrorMethod(String prepend, NodeToken methodToken, Vector<SymbolData> exprList) {
    int listSize = exprList.size();

    DebugErr(prepend + methodToken + "(");
    for (int i = 0; i < listSize; i++) {
      if (i != listSize - 1)
        DebugErr(exprList.get(i).getFormalType() + ", ");
      else
        DebugErr(exprList.get(i).getFormalType());
    }
    DebugErr(")");
  }

  void checkAndAddClassMethods(NodeToken classToken, NodeListOptional n) {
    // Get method names and add methods to symboltable
    ArrayList<String> methodNames = new ArrayList<String>();
    for(Node node : n.nodes){
      MethodDeclaration md = (MethodDeclaration)node;
      methodNames.add(methodname(md));
      symbolTable.addMethodToClass(classToken, readMethod(md));
    }

    // Output method names
    DebugOut("\nmethod names:");
    for(String s : methodNames){
      DebugOut(s);
    }
    DebugOut();

    // Test for distinct method names
    if(!distinct(methodNames)){
      DebugErr("Methods not distinct!");
      System.exit(-1);
    }
  }

  void checkAndAddFields(NodeToken classToken, NodeListOptional n) {
    // Get method names and add methods to symboltable
    ArrayList<String> varNames = new ArrayList<String>();
    for(Node node : n.nodes){
      VarDeclaration vd = (VarDeclaration)node;
      varNames.add(varname(vd));
      VarPair vp  = readVar(vd);
      symbolTable.addFieldVarToClass(classToken, vp.nameToken, vp.varData);
    }

    // Output method names
    DebugOut("\nvar names:");
    for(String s : varNames){
      DebugOut(s);
    }
    DebugOut();

    // Test for distinct method names
    if(!distinct(varNames)){
      DebugErr("Class fields not distinct!");
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

    return new MethodData(md.f2.f0, returnData, mdVisitor.getSynthFormalParam());
  }

  VarPair readVar(VarDeclaration vd) {
    MethodDeclareVisitor vdVisitor = new MethodDeclareVisitor(symbolTable);

    // Get type of variable
    vd.accept(vdVisitor);
    SymbolType type = vdVisitor.getInheritedType();
    SymbolData deepType = vdVisitor.getDeepInheritedType();
    if (deepType == null)
      deepType = new SymbolData(type);

    return new VarPair(vd.f1.f0, deepType);
  }

  //-----------------------------------
  //End typechecking overview functions
  //Start Vapor specific functions
  //-----------------------------------

  String newTempVar() {
    return "t." + tempVarIndex++;
  }

  String newLabel() {
    return "label" + labelnum++;
  }

  void resetTempVar() {
    tempVarIndex = 0;
  }

  // null labels are especially used for new class variables
  String newNullLabel() {
    return "null" + ++nullLabelIndex;
  }

  String newBoundsLabel() {
    return "bounds" + ++boundsLabelIndex;
  }

  String printClassVar(ClassVarData cd) {
    String thisVar = newTempVar();
    String nullLabel = newNullLabel();
    int fieldSize = symbolTable.getClassFieldSize(new NodeToken(cd.getDeepType()));

    // size of (fields + pointer to v-table) * 4
    vaporPrinter.print(thisVar + " = HeapAllocZ(" + (fieldSize * 4 + 4) + ")");
    vaporPrinter.print("[" + thisVar + "] = :vmt_" + cd.getDeepType());
    vaporPrinter.print("if " + thisVar + " goto :" + nullLabel);
    vaporPrinter.print("Error(\"null pointer\")");
    vaporPrinter.print(nullLabel + ":");
    
    return thisVar;
  }

	void createVMT(ClassData cd) {
		vaporPrinter.print("const vmt_" + cd.getClassName());

    if (!createVMTHelper(cd))
      vaporPrinter.print("  -1");
		System.out.println();
		//Vector<MethodData> methods = cd.getMethods();
		//if (!methods.isEmpty()) {
		//	String prefix = "  :" + cd.getClassName() + ".";

		//	for (MethodData md : methods)
		//	  System.out.println(prefix + md.getName());
		//  System.out.println();
		//}
	}

  // function that outputs parents' methods w/o the first vmt line
  // returns bool if function printed any methods
	boolean createVMTHelper(ClassData cd) {
	  if (cd == null)
	    return false;

    // Print parent methods first. Track if the parents printed methods
    boolean parentsPrinted = false;
    if (cd.getParent() != null) {
      ClassData parentCD = (ClassData) symbolTable.getSymbolData(cd.getParent(), SymbolType.ST_CLASS);
      parentsPrinted = createVMTHelper(parentCD);
    }

    // print methods
		Vector<MethodData> methods = cd.getMethods();
		if (!methods.isEmpty()) {
			String prefix = "  :" + cd.getClassName() + ".";

			for (MethodData md : methods)
			  System.out.println(prefix + md.getName());

		  return true;
		}
    
    return parentsPrinted;
	}

  //----------------------------
  //End Vapor specific functions
  //Start grammar rules
  //----------------------------

  /**
   * f0 -> MainClass()
   * f1 -> ( TypeDeclaration() )*
   * f2 -> <EOF>
   */
  public void visit(Goal n) {
    // Verify distinct classes and methods, adds both to symbol table
    symbolTable.addSymbol(n.f0.f1.f0, new ClassData(n.f0.f1.f0));

    //add all classnames in TypeDeclaration* to list
    for(Node typeDeclaration : n.f1.nodes){
      TypeDeclaration td = (TypeDeclaration)typeDeclaration;
      Node choice = td.f0.choice;
      int which = td.f0.which;
      ClassData data = null;

      if(which == 0){//ClassDeclaration
        ClassDeclaration cd = (ClassDeclaration)choice;
        data = new ClassData(cd.f1.f0);

        symbolTable.addSymbol(cd.f1.f0, data);    // Add class
        checkAndAddClassMethods(cd.f1.f0, cd.f4); // Add methods
        checkAndAddFields(cd.f1.f0, cd.f3);       // Add fields

      }
      else{//ClassExtendsDeclaration
        ClassExtendsDeclaration ced = (ClassExtendsDeclaration)choice;
        data = new ClassData(ced.f1.f0, ced.f3.f0);

        symbolTable.addSymbol(ced.f1.f0, data);     // Add class
        checkAndAddClassMethods(ced.f1.f0, ced.f6); // Add methods
        checkAndAddFields(ced.f1.f0, ced.f5);       // Add fields
      }

      createVMT(data); // Print Vapor Code for v-table (methods)
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
    currentClassName = n.f1.f0;

    //enter new scope for class
    vaporPrinter.print("func Main()");
    vaporPrinter.addScope();
    n.f2.accept(this);
    symbolTable.newScope();

    n.f3.accept(this);
    n.f4.accept(this);
    n.f5.accept(this);
    n.f6.accept(this);

    //enter new scope for main
    n.f7.accept(this);
    symbolTable.newScope();

    //add String[] id to symbol table
    n.f8.accept(this);
    n.f9.accept(this);
    n.f10.accept(this);
    n.f11.accept(this);
    symbolTable.addSymbol(n.f11.f0, SymbolType.ST_STRING_ARR);

    // new scope for variables inside main
    symbolTable.newScope();
    n.f12.accept(this);
    n.f13.accept(this);
    n.f14.accept(this);
    n.f15.accept(this);

    // return, exit inside main and main scope
    n.f16.accept(this);
    vaporPrinter.print("ret");
    symbolTable.exitScope();
    symbolTable.exitScope();

    //exit class scope
    n.f17.accept(this);
    vaporPrinter.removeScope();
    symbolTable.exitScope();
    currentClassName = null;

    //reset temporary variable counter
    resetTempVar();
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
    symbolTable.newScope(); // formal params scope
    n.f0.accept(this);
    n.f1.accept(this);
    n.f2.accept(this);
    n.f1.accept(this);
    n.f3.accept(this);
    n.f4.accept(this);
    Vector<String> fpn = getSynthFormalParamNames();	
    //output looks like func Class.ClassFunc(this num1 num2 num3)
    String s = "func " + currentClassName + "." + n.f2.f0 + "(this";
    for(int i = 0; i < fpn.size(); i++)
    {
      s = s + " " + fpn.get(i);
	  }
	  s = s + ')';

    vaporPrinter.print(0, s);
    vaporPrinter.addScope();
    symbolTable.newScope(); // declared variables scope
    n.f5.accept(this);
    n.f6.accept(this);
    n.f7.accept(this);
    n.f8.accept(this);

    //return statement
    n.f9.accept(this);
    n.f10.accept(this);
    s = "ret " + synthTempVar;
    vaporPrinter.print(1, s);
    vaporPrinter.removeScope();

    n.f11.accept(this);
    n.f12.accept(this);

    symbolTable.exitScope();  // varDec
    symbolTable.exitScope();  // formalParam
    resetTempVar();           // reset temp variables
  }

  ///** Does not need to be changed
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
    SymbolType st = getInheritedType();
    SymbolData sd = getDeepInheritedType();
    if(st != SymbolType.ST_CLASS_VAR)
      symbolTable.addSymbol(n.f1.f0, st); 
    else
      symbolTable.addSymbol(n.f1.f0, sd);

    // Get identifier names for vapor code
    synthFormalParamNames.add(n.f1.f0.toString());
  }

  ///** Does not need to be changed
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
      NodeToken classToken = ((Identifier) n.f0.choice).f0;
      deepInheritedType = new ClassVarData(classToken);
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
      for (MethodData md : getSynthUnverifiedMethods()) {
        md.setReturnType(new SymbolData(SymbolType.ST_INT));
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

  ///** Set the identifier equal to the tempVar that the expresion is stored in
  // * f0 -> Identifier()
  // * f1 -> "="
  // * f2 -> Expression()
  // * f3 -> ";"
  // */
  public void visit(AssignmentStatement n) {
    n.f0.accept(this);
    String identifierName = synthTempVar;
    n.f1.accept(this);
    n.f2.accept(this);
    vaporPrinter.print(identifierName + " = " + synthTempVar);
    n.f3.accept(this);
  }

  ///** Formatted according to the example in "Hints"
  ///** Stores the value of synthTempVar(f5) into the array index
  // * f0 -> Identifier()
  // * f1 -> "["
  // * f2 -> Expression()
  // * f3 -> "]"
  // * f4 -> "="
  // * f5 -> Expression()
  // * f6 -> ";"
  // */
  public void visit(ArrayAssignmentStatement n) {
    n.f0.accept(this);
    String identifierName = synthTempVar;
    n.f1.accept(this);
    n.f2.accept(this);
    String indexNum = synthTempVar;
    // checking if the index is in range
    // if not, output an error message
    vaporPrinter.print("s = [" + identifierName + "]");
    vaporPrinter.print("ok = LtS(" + indexNum + " s)");
    String boundsLabel = newBoundsLabel();
    vaporPrinter.print("if ok goto :" + boundsLabel);
    vaporPrinter.print("Error(\"Array index out of bounds\")");
    //vaporPrinter.print("l" + nullLabelIndex + ": ok = LtS(-1 " + indexNum + ")");
    //++nullLabelIndex;
    //vaporPrinter.print("if ok goto :l" + nullLabelIndex);
    //vaporPrinter.print("Error(\"Array index out of bounds\")");
    vaporPrinter.print(boundsLabel + ":");
    vaporPrinter.print("o = MulS(" + indexNum + " 4)");
    ++nullLabelIndex;
    vaporPrinter.print("d = Add(" + identifierName + " o)");
    n.f3.accept(this);
    n.f4.accept(this);
    n.f5.accept(this);
    // store the expression in synthTempVar into the address in [d+4]
    vaporPrinter.print("[d+4] = " + synthTempVar);
    n.f6.accept(this);
  }

  ///** Generates an if else statement using labels and goto instructions
  // * f0 -> "if"
  // * f1 -> "("
  // * f2 -> Expression()
  // * f3 -> ")"
  // * f4 -> Statement()
  // * f5 -> "else"
  // * f6 -> Statement()
  // */
  public void visit(IfStatement n) {
	++ifElseCount;
    n.f0.accept(this);
    n.f1.accept(this);
    n.f2.accept(this);
    String ifElseLabel1 = "if" + ifElseCount + "_else";
    vaporPrinter.print("if0 " + synthTempVar + " goto :" + ifElseLabel1);
    // remove scope before each label, add scope after each label
    vaporPrinter.addScope();
    n.f3.accept(this);
    n.f4.accept(this);
    String ifElseLabel2 = "if" + ifElseCount + "_end";
    vaporPrinter.print("goto :" + ifElseLabel2);
    n.f5.accept(this);
    vaporPrinter.removeScope();
    vaporPrinter.print(ifElseLabel1 + ":");
    vaporPrinter.addScope();
    n.f6.accept(this);
    vaporPrinter.removeScope();
    vaporPrinter.print(ifElseLabel2 + ":");
  }

  ///** Generates a while loop using labels and goto instructions
  // * f0 -> "while"
  // * f1 -> "("
  // * f2 -> Expression()
  // * f3 -> ")"
  // * f4 -> Statement()
  // */
  public void visit(WhileStatement n) {
	++whileCount;
    n.f0.accept(this);
    n.f1.accept(this);
    vaporPrinter.print("while" + whileCount + ":");
    n.f2.accept(this);
    String whileLabel = "while" + whileCount + "_end";
    vaporPrinter.print("if0 " + synthTempVar + " goto :" + whileLabel);
    // remove scope before each label, add scope after each label
    vaporPrinter.addScope();
    n.f3.accept(this);
    n.f4.accept(this);
    vaporPrinter.print("goto :while" + whileCount);
    vaporPrinter.removeScope();
    vaporPrinter.print(whileLabel + ":");
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
    n.f1.accept(this);
    n.f2.accept(this);
    String arg = synthTempVar;
    n.f3.accept(this);
    n.f4.accept(this);

    vaporPrinter.print("PrintIntS(" + arg + ")");
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
  public void visit(Expression n) {
    n.f0.accept(this);
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "&&"
   * f2 -> PrimaryExpression()
   */
  public void visit(AndExpression n) {
    n.f0.accept(this);
    String arg1 = synthTempVar;
    n.f1.accept(this);
    n.f2.accept(this);
    String arg2 = synthTempVar;

    //<AndExpression> = ST_Boolean
    inheritedType = SymbolType.ST_BOOLEAN;

    String temp = newTempVar();
    vaporPrinter.print(temp + " = And(" + arg1 + " " + arg2 + ")");

    synthTempVar = temp;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "<"
   * f2 -> PrimaryExpression()
   */
  public void visit(CompareExpression n) {
    n.f0.accept(this);
    String arg1 = synthTempVar;
    n.f1.accept(this);
    n.f2.accept(this);
    String arg2 = synthTempVar;

    //<CompareExpression> = ST_BOOLEAN
    inheritedType = SymbolType.ST_BOOLEAN;

    String temp = newTempVar();
    vaporPrinter.print(temp + " = LtS(" + arg1 + " " + arg2 + ")");

    synthTempVar = temp;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "+"
   * f2 -> PrimaryExpression()
   */
  public void visit(PlusExpression n) {
    n.f0.accept(this);
    String arg1 = synthTempVar;
    n.f1.accept(this);
    n.f2.accept(this);
    String arg2 = synthTempVar;

    //<PlusExpression> = ST_INT
    inheritedType = SymbolType.ST_INT;

    String temp = newTempVar();
    vaporPrinter.print(temp + " = Add(" + arg1 + " " + arg2 + ")");

    synthTempVar = temp;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "-"
   * f2 -> PrimaryExpression()
   */
  public void visit(MinusExpression n) {
    n.f0.accept(this);
    String arg1 = synthTempVar;
    n.f1.accept(this);
    n.f2.accept(this);
    String arg2 = synthTempVar;

    String temp = newTempVar();

    //<MinusExpression> = ST_INT
    inheritedType = SymbolType.ST_INT;

    vaporPrinter.print(temp + " = Sub(" + arg1 + " " + arg2 + ")");

    synthTempVar = temp;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "*"
   * f2 -> PrimaryExpression()
   */
  public void visit(TimesExpression n) {

    n.f0.accept(this);
    String arg1 = synthTempVar;
    n.f1.accept(this);
    n.f2.accept(this);
    String arg2 = synthTempVar;

    String temp = newTempVar();
    //<TimesExpression> = ST_INT
    inheritedType = SymbolType.ST_INT;
    vaporPrinter.print(temp + " = MulS(" + arg1 + " " + arg2 + ")");

    synthTempVar = temp;
  }

  /** Formatted according to the example in "Hints"
  /** Stores the value of synthTempVar(f5) into the array index
   * f0 -> PrimaryExpression()
   * f1 -> "["
   * f2 -> PrimaryExpression()
   * f3 -> "]"
   */
  public void visit(ArrayLookup n) {
    n.f0.accept(this);
    String arrayName = synthTempVar;
    n.f1.accept(this);
    n.f2.accept(this);
    String indexNum = synthTempVar;
    // checking if the index is in range
    // if not, output an error message
    vaporPrinter.print("s = [" + arrayName + "]");
    vaporPrinter.print("ok = LtS(" + indexNum + " s)");
    String boundsLabel = newBoundsLabel();
    vaporPrinter.print("if ok goto :" + boundsLabel);
    vaporPrinter.print("Error(\"Array index out of bounds\")");
    //vaporPrinter.print("l" + nullLabelIndex + ": ok = LtS(-1 " + indexNum + ")");
    //vaporPrinter.print("if ok goto :l" + nullLabelIndex);
    //vaporPrinter.print("Error(\"Array index out of bounds\")");
    vaporPrinter.print(boundsLabel + ":");
    vaporPrinter.print("o = MulS(" + indexNum + " 4)");
    vaporPrinter.print("d = Add(" + arrayName + " o)");
    String temp = newTempVar();
    // store the value in [d+4] into a tempVar
    vaporPrinter.print(temp + " = [d+4]");
    synthTempVar = temp;
    n.f3.accept(this);
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
    synthCalledMethod = true;

    //Primary Expression
    n.f0.accept(this);
    SymbolData callerData = getDeepInheritedType();
    String callerVar = synthTempVar;
    n.f1.accept(this);

    //methodname
    n.f2.accept(this);

    //Expression List
    n.f3.accept(this);
    n.f4.accept(this);
    Vector<String> givenArgs = getSynthExprListNames();
    n.f5.accept(this);

    // Set return type in inheritedType and deepInheritedType
    MethodData methodData = symbolTable
      .getMethodFromClass(new NodeToken(callerData.getDeepType()), n.f2.f0);
    SymbolData returnType = methodData.getReturnType();

    inheritedType = returnType.getType();
    if (returnType.getType() == SymbolType.ST_CLASS_VAR)
      deepInheritedType = returnType;

    // Vapor code for function call
    String temp0 = newTempVar(), 
        temp1 = newTempVar();
    int methodOffset = symbolTable.getMethodIndexFromClass(
        new NodeToken(callerData.getDeepType()), n.f2.f0);
    String callMethodLine = temp1 + " = call " + temp0 + "(" + callerVar;
    for (String s : givenArgs)
      callMethodLine += " " + s;
    callMethodLine += ")";

    vaporPrinter.print(temp0 + " = [" + callerVar + "]");
    vaporPrinter.print(temp0 + " = [" + temp0 + "+" + (methodOffset * 4) + "]");
    vaporPrinter.print(callMethodLine);
    synthTempVar = temp1;
  }

  /**
   * Note: ExpressionList is only used by MessageSend
   * 
   * f0 -> Expression()
   * f1 -> ( ExpressionRest() )*
   */
  public void visit(ExpressionList n) {
    n.f0.accept(this);
    synthExprListNames.add(synthTempVar); // Add arg name
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
    synthExprListNames.add(synthTempVar); // Add arg name
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

    if (n.f0.which == 3) {
      // To check if variable exists (identifiers). If so, grab its type.
      NodeToken varName = ((Identifier) n.f0.choice).f0;
      SymbolData data = symbolTable.getSymbolData(varName, SymbolType.ST_VARIABLE, currentClassName);

      inheritedType = data.getType();
      if (data.getType() == SymbolType.ST_CLASS_VAR)
        deepInheritedType = data;

      // For vapor: To check if variable was a field member of a class. If so, "load it" from "this"
      int fieldVarIndex = symbolTable.getFieldVarIndex(varName, currentClassName);
      if (fieldVarIndex != -1) {
        // Need offset index of field member. Will code new function for this.
        synthTempVar = newTempVar();
        vaporPrinter.print(synthTempVar + " = [this + " + (fieldVarIndex * 4 + 4) + "]");
      }
    }
  }

  /**
   * f0 -> <INTEGER_LITERAL>
   */
  public void visit(IntegerLiteral n) {
    n.f0.accept(this);
    synthTempVar = n.f0.toString();

    //<IntegerLiteral> = ST_INT
    inheritedType = SymbolType.ST_INT;
  }

  /**
   * f0 -> "true"
   */
  public void visit(TrueLiteral n) {
    n.f0.accept(this);
    synthTempVar = "1";
    //<TrueLiteral> = ST_BOOLEAN
    inheritedType = SymbolType.ST_BOOLEAN;
  }

  /**
   * f0 -> "false"
   */
  public void visit(FalseLiteral n) {
    n.f0.accept(this);
    synthTempVar = "0";
    //<FalseLiteral> = ST_BOOLEAN
    inheritedType = SymbolType.ST_BOOLEAN;
  }

  /**
   * f0 -> <IDENTIFIER>
   */
  public void visit(Identifier n) {
    n.f0.accept(this);
    synthTempVar = n.f0.toString();
  }

  /**
   * f0 -> "this"
   */
  public void visit(ThisExpression n) {
    n.f0.accept(this);
    synthTempVar = "this";
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
    String expr = synthTempVar;
    n.f4.accept(this);

    String result = newTempVar();

    //todo: keep track of array size for array.length!
    vaporPrinter.print("bytes = MulS(" + expr + " 4)");
    vaporPrinter.print("bytes = Add(bytes 4)");
    vaporPrinter.print(result + " = HeapAllocZ(bytes)");
    vaporPrinter.print("[" + result + "] = " + expr);

    //<ArrayAllocationExpression> = ST_INT
    inheritedType = SymbolType.ST_INT_ARR;
    synthTempVar = result;
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
    ClassVarData varData = new ClassVarData(n.f1.f0);
    deepInheritedType = varData;
    synthTempVar = printClassVar(varData);
  }

  /**
   * f0 -> "!"
   * f1 -> Expression()
   */
  public void visit(NotExpression n) {
    n.f0.accept(this);
    n.f1.accept(this);
    String arg1 = synthTempVar;

    String result  = newTempVar();
    vaporPrinter.print(result + " = eq(" + arg1 + " 0)");

    //<NotExpression> = ST_BOOLEAN
    inheritedType = SymbolType.ST_BOOLEAN;

    synthTempVar = result;
  }

  //doesnt need work...
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

  private void DebugOut(String s) {
    if (debug_g)
      System.out.println(s);
  }

  private void DebugOut() {
    if (debug_g)
      System.out.println();
  }

  private void DebugErr(String s) {
    //System.err.println(s);
    System.err.println("Type error");
  }

  private void DebugErr() {
    System.err.println();
  }

  // Just to return two values in readVar
  class VarPair {
    NodeToken nameToken; 
    SymbolData varData;
    VarPair(NodeToken nameToken, SymbolData varData) {
      this.nameToken = nameToken;
      this.varData = varData;
    }
  }
}
