
public class Extends {
    public Extends() {
    }

    public String getName() {
      return this.getClass().getName();
    }

    public static void main(String[] args) {
        System.out.println((new Extends()).getName());
    }

}

