//The foo remote object server
import java.rmi.RemoteException;
import javax.rmi.PortableRemoteObject;
import org.objectweb.carol.util.configuration.CarolConfiguration;

public class Server {

    //The main method of this server
    public static void main(String [] args)  {
	try {
	    //initialize carol
	    CarolConfiguration.init();

	    FooRemoteInterface myFoo = new Foo();
	    //The object is explicitly exported on RMI IIOP:
	    PortableRemoteObject.exportObject(myFoo);
	} catch (RemoteException e) {
	    //Foo construction problem
	}
	    
    }
}
