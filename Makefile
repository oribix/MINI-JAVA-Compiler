all: clean J2V.class

run:
	./Phase1Tester/run ./Phase1Tester/SelfTestCases/ ./hw1.tgz

tar: clean
	mkdir -p hw1/symboltable
	cp Typecheck.java hw1/
	cp DMVisitor.java hw1/
	cp MethodDeclareVisitor.java hw1/
	cp symboltable/* hw1/symboltable/
	tar -czf hw1.tgz hw1/

test: clean Typecheck.class
	java Typecheck < Phase1Tests/Basic.java

J2V.class: J2V.java
	javac J2V.java

Typecheck.class: Typecheck.java
	javac Typecheck.java

clean:
	rm -f *.class
	rm -f */*.class
	rm -rf hw1*
	rm -rf Phase1Tester/Output
