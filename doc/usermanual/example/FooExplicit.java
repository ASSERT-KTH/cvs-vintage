//The Foo object is a remote object
import java.rmi.RemoteException;

//The class foo implement 
//only the FooRemoteInterface interface 
public class Foo implement FooRemoteInterface {

    //The constructor
    public Foo() throws RemoteException {
	super();
    }
    
    //This method is remote
    public Integer myMethod() throws RemoteException {
	return new Integer(0);
    }
}
