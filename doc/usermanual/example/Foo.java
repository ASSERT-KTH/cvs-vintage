//The foo object is a remote object
import java.rmi.RemoteException;

//The class foo implements only 
//the FooRemoteInterface interface 
public class Foo implement FooRemoteInterface {
    
    //This method is remote
    public Integer myMethod() throws RemoteException{
	return new Integer(0);
    }
}
