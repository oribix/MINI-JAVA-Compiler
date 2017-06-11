public class varLiveness implements Comparable<varLiveness>{
  String name;
  int start;
  int end;
  
  public varLiveness(String n, int lineNo){
    name = n;
    start = lineNo;
    end = lineNo;
  }
  
  public void updateEnd(int e){
    if(e > end)
      end = e;
  }

  public String getName(){
    return name;
  }

  public int getStart(){
    return start;
  }

  public int getEnd(){
    return end;
  }

  //compares by end time
  public int compareTo(varLiveness v){
    if(this.end == v.end) return 0;
    else if(this.end < v.end) return -1;
    else return 1;
  }

  @Override
  public boolean equals(Object o){
    if(o == null) return false;
    if(!(o instanceof varLiveness)) return false;
    else{
      varLiveness vl = (varLiveness)o;
      return this.getName().equals(vl.getName());
    }
  }

  @Override
  public int hashCode(){
    return this.getName().hashCode();
  }
}
