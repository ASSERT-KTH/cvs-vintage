//The foo remote interface 
//extends only the Remote interface
//and exposes the remote methods
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FooRemoteInterface extends Remote {

    //This method is remote
    public Integer myMethod() throws RemoteException; 

}
