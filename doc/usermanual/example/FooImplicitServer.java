//The Foo remote object server
import java.rmi.RemoteException;

public class Server {

    //The main method of this server
    public static void main(String [] args)  {
	try {
	    FooRemoteInterface myFoo = new Foo();
	    // the object is automatically 
	    // exported on RMI IIOP
	} catch (RemoteException e) {
	    //Foo construction problem
	}
	    
    }
}
