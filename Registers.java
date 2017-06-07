import java.util.HashMap;

public class Registers {
  // Note: Just realized that boolean arrays, the most EXPECTED part of a class like this, 
  // are actually kinda useless here due to lowestT/S. I only added them to the boolean checks as 
  // a sanity check and an excuse to use them. I'd remove them, but...I feel bad.

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
      String reg = "t" + lowestT;
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
      String reg = "s" + lowestS;
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
    if (reg.length() != 2 || 
        !Character.isAlphabetic(reg.charAt(0)) || 
        !Character.isDigit(reg.charAt(1))) {
      System.err.println("Error: Passed invalid string in Registers.returnFreeReg()");
      System.exit(-1);
    }

    // Second char is the reg number
    int i = reg.charAt(1) - '0';

    // First char is t or s
    if (reg.charAt(0) == 't') {
      tUsed[i] = false;
      lowestT = Math.min(i, lowestT);
    } else if (reg.charAt(0) == 's') {
      sUsed[i] = false;
      lowestS = Math.min(i, lowestS);
    } else {
      System.err.println("Error: bad character in Registers.returnFreeReg()");
      System.exit(-1);
    }
  }
}
