class Main {
	public static void main(String[] s){
    A a;
	  D d;
		System.out.println(new B().run());
    a = d;
	}
}

class A {
  A a;
  B b;
  C c;
  D d;
  int horn;

	public int run() {
		int x;
		boolean y;
		int[] z;
		x = 2;
    
    if (x < 3) {
      x = 2;
    }
    else {
      x = 1;
    }
    while (true) {
      x = 2;
    }
		return d.jump(x, 1);
		//return this.jump(x);
	}

	public boolean debugger() {return b;}

	public int jump(int a, int b) {
	  return a;
	}
}

class B extends A{
  boolean zimbabwe;
  public int africa(int cape) {
    return horn;
  }
}
class C extends B{
  boolean zambia;
  boolean westernSahara;
  public int africa(int cape) {
    return westernSahara;
  }
}
class D extends C{
  boolean uganda;
  boolean tunisia;
  boolean togo;
  public int africa(int cape) {
    return uganda;
  }
  public int jump(int a, B b) {
    b = new B();
    return 1 + 1;
  }
  public D test() {
    return d;
  }
}
class taco {}
