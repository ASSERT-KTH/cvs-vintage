package jsp;


public class TestBean {

  public TestBean () {
  }

  public String getName () {
    return this.name;
  }
  
  public void setName (String nm) {
    this.name = nm;
  }

  public String getAge () {
    return this.age;
  }

  public void setAge (String age) {
    this.age = age;
  }

  private String name = "TestBean";
  String age = "10";
}






