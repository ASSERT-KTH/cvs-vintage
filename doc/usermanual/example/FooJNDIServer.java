//The foo remote object server
import java.rmi.RemoteException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.objectweb.carol.util.configuration.CarolConfiguration;

public class Server {

    //The main method of this server
    public static void main(String [] args)  {
	try {
	    //initialize carol
	    CarolConfiguration.init();

	    // the object is automatically 
	    // exported on RMI IIOP
	    FooRemoteInterface myFoo = new Foo();
	    
	    // now the server bind this object trough JNDI
	    // with the name myobjectname
	    InitialContext ic = new InitialContext();
	    ic.rebind("myobjectname", myFoo)

	} catch (RemoteException e) {
	    //Foo construction problem
	}catch (NamingException ne) {
	    //Foo binding problem
	}
	    
    }
}
