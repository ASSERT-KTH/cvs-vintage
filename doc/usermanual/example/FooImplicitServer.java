//The foo remote object server
import java.rmi.RemoteException;
import org.objectweb.carol.util.configuration.CarolConfiguration;

public class Server {

    //The main method of this server
    public static void main(String [] args)  {
	try {
	    //initialize carol
	    CarolConfiguration.init();

	    FooRemoteInterface myFoo = new Foo();
	    // the object is automatically 
	    // exported on RMI IIOP
	} catch (RemoteException e) {
	    //Foo construction problem
	}
	    
    }
}
