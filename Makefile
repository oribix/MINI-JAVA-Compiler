all: clean J2V.class

run:
	./Phase1Tester/run ./Phase1Tester/SelfTestCases/ ./hw1.tgz

hw1: clean
	mkdir -p hw1/symboltable
	cp Typecheck.java hw1/
	cp DMVisitor.java hw1/
	cp MethodDeclareVisitor.java hw1/
	cp symboltable/* hw1/symboltable/
	tar -czf hw1.tgz hw1/

hw2: clean
	mkdir -p hw2/symboltable
	mkdir -p hw2/vaportools
	cp DMVisitor.java hw2/
	cp VaporVisitor.java hw2/
	cp J2V.java hw2/
	cp MethodDeclareVisitor.java hw2/
	cp symboltable/* hw2/symboltable/
	cp vaportools/* hw2/vaportools/
	tar -czf hw2.tgz hw2/

test: clean V2VM.class
	java V2VM < Phase3Tests/Factorial.vapor 
	
V2VM.class: V2VM.java
	javac	V2VM.java -classpath vapor-parser.jar

J2V.class: J2V.java
	javac J2V.java

Typecheck.class: Typecheck.java
	javac Typecheck.java

clean:
	rm -f *.class
	rm -f */*.class
	rm -rf hw1*
	rm -rf hw2*
	rm -rf hw3*
	rm -rf Phase1Tester/Output
	rm -rf Phase2Tester/Output
	rm -rf Phase3Tester/Output
