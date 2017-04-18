all: Typecheck

run: Typecheck
	java Typecheck < tests/Phase1Tester/SelfTestCases/Basic.java

Typecheck:
	javac Typecheck.java

clean:
	rm -f *.class
	rm -f */*.class
