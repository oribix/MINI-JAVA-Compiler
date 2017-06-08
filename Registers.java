import java.util.HashMap;

public class Registers {
  public static final int R = 17;

  private boolean[] tUsed, sUsed;
  private int lowestT, lowestS; // tracks lowest free index for t and s

  public Registers() {
    tUsed = new boolean[9]; // default: initialized to false
    sUsed = new boolean[8];

    lowestT = 0;
    lowestS = 0;
  }

  // Returns string like "s1" or "t5". Uses lowestT/S as an optimization to avoid
  // searching the boolean array for free reg.
  public String getFreeReg() {
    if (lowestT < 9 && !tUsed[lowestT]) {
      String reg = "$t" + lowestT;
      int i = lowestT;
      tUsed[lowestT] = true;

      for (; i < 9; i++) {
        if (!tUsed[i]) {
          lowestT = i;
          break;
        }
      }
      if (i == 9)
        lowestT = 9;

      return reg;
    } else if (lowestS < 8 && !sUsed[lowestS]) {
      String reg = "$s" + lowestS;
      int i = lowestS;
      sUsed[lowestS] = true;

      for (; i < 8; i++) {
        if (!sUsed[i]) {
          lowestS = i;
          break;
        }
      }
      if (i == 8)
        lowestS = 8;

      return reg;
    } else {
      return null;
    }
  }

  // Takes argument like "s0" or "t7"
  public void returnFreeReg(String reg) {
    // Second char is the reg number
    int regNum = reg.charAt(2) - '0';

    // First char is t or s
    char regType = reg.charAt(1);
    if (regType == 't') {
      tUsed[regNum] = false;
      lowestT = Math.min(regNum, lowestT);
    } else if (regType == 's') {
      sUsed[regNum] = false;
      lowestS = Math.min(regNum, lowestS);
    } else {
      System.err.println("Error: bad character in Registers.returnFreeReg()");
      System.exit(-1);
    }
  }
}
