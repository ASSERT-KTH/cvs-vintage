//The Foo object is a remote object
import java.rmi.RemoteException;
import javax.rmi.PortableRemoteObject;

//The class foo extends PortableRemoteObject 
//and implement the FooRemoteInterface interface 
public class Foo extends PortableRemoteObject 
		 implements FooRemoteInterface {

    //The constructor
    public Foo() throws RemoteException {
	super();
    }
    
    //This method is remote
    public Integer myMethod() throws RemoteException {
	return new Integer(0);
    }
}
