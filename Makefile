all: clean Typecheck.class

run: clean Typecheck.class
	java Typecheck < tests/phase1-tests/Basic.java

Typecheck.class: Typecheck.java
	javac Typecheck.java

clean:
	rm -f *.class
	rm -f */*.class
