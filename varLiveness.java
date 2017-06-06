public class varLiveness{
  String name;
  int start;
  int end;
  
  public varLiveness(String n, int s){
    name = n;
    start = s;
    end = 0;
  }
  
  public void updateEnd(int e){
    if(e > end)
      end = e;
  }
  
  
}
