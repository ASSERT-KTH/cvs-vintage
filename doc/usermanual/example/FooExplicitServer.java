//The Foo remote object server
import java.rmi.RemoteException;
import javax.rmi.PortableRemoteObject;

public class Server {

    //The main method of this server
    public static void main(String [] args)  {
	try {
	    FooRemoteInterface myFoo = new Foo();
	    // the object is explicitly exported on RMI IIOP:
	    PortableRemoteObject.exportObject(myFoo);
	} catch (RemoteException e) {
	    //Foo construction problem
	}
	    
    }
}
