import java.util.HashMap;

public class Registers {
  public String[] t, s, a, v;
  //boolean[] tFree, sFree, aFree, vFree;
  //HashMap<String, String> varMap;

  public Registers() {
    t = new String[9];
    s = new String[8];
    a = new String[4];
    v = new String[2];

    //tFree = new boolean[9];
    //sFree = new boolean[8];
    //aFree = new boolean[4];
    //vFree = new boolean[2];

    //varMap = new HashMap<>();
  }

  public void setValue(char regLetter, int i, String var) {
    switch (regLetter) {
      case 't':
        if (i < 9) {
          t[i] = var;
          //tFree[i] = false;
        }
        break;
      case 's':
        if (i < 8) {
          s[i] = var;
          //sFree[i] = false;
        }
        break;
      case 'a':
        if (i < 4) {
          a[i] = var;
          //aFree[i] = false;
        }
        break;
      case 'v':
        if (i < 2) {
          v[i] = var;
          //vFree[i] = false;
        }
        break;
      default:
        System.err.println("Error: unrecognized register in register class.");
        System.exit(-1);
    }
  }
}
