package vaportools;

import java.util.Vector;
import symboltable.ClassData;

import symboltable.SymbolTable;
import symboltable.MethodData;

public class VaporPrinter {
	SymbolTable symbolTable;
	public VaporPrinter(SymbolTable st){
		symbolTable = st;
	}
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
	public void print(String s){
		for(int i = 0; i < symbolTable.Size() - 1; i++){
			System.out.printf("  ");
		}
		System.out.printf(s);
		System.out.printf("\n");
	}
	public void print(int n, String s){
		for(int i = 0; i < n; i++){
			System.out.printf("  ");
		}
		System.out.printf(s);
		System.out.printf("\n");
	}
}
