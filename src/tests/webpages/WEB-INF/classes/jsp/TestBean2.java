package jsp;


public class TestBean2 {

  public TestBean2 () {
  }

  public String getName () {
    return this.name;
  }
  
  public void setName (String nm) {
    this.name = nm;
  }

  private String getAge () {
    return this.age;
  }

  private void setAge (String age) {
    this.age = age;
  }

  private String name = "TestBean2";
  String age = "10";
}






