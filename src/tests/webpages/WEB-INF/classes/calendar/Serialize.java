package calendar;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class Serialize {

    // Serialize a bean.
    // It must have a no-arg constructor.

    public static void main(String[] args) {

	if (args.length != 2) {
	    System.out.println(" java serialize output-file classname");
	    return;
	}

	try {

	    FileOutputStream fos = new FileOutputStream(args[0]);
	    ObjectOutputStream objout = new ObjectOutputStream(fos);
	
	    Class clazz = Class.forName(args[1]);
	    objout.writeObject(clazz.newInstance());
	    objout.flush();

	    System.out.println("Serialized bean is in file " + args[0]);

	} catch (Exception ex) {
	    System.out.println("Sorry the follwing exception occured");
	    ex.printStackTrace();
	}
    }
}
