all: Typecheck.java
	javac Typecheck.java

clean:
	rm -f *.class
	rm -f */*.class
