all: clean Typecheck.class

run: clean Typecheck.class
	java Typecheck < tests/Phase1Tester/SelfTestCases/Basic.java

Typecheck.class: Typecheck.java visitor/DMVisitor.class
	javac Typecheck.java

visitor/DMVisitor.class: visitor/DMVisitor.java 
	javac visitor/DMVisitor.java

clean:
	rm -f *.class
	rm -f */*.class
