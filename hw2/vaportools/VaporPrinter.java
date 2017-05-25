package vaportools;

import java.util.Vector;
import symboltable.ClassData;

import symboltable.SymbolTable;
import symboltable.MethodData;

public class VaporPrinter {
	SymbolTable symbolTable;
  int scopeNum;
	public VaporPrinter(SymbolTable st){
		symbolTable = st;
    scopeNum = 0;
	}
  public void addScope(){
    scopeNum++;
  }
  public void removeScope(){
    scopeNum--;
  }
	public void print(String s){
		// for(int i = 0; i < symbolTable.Size() - 3; i++){
    for(int i = 0; i < scopeNum; i++) {
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
