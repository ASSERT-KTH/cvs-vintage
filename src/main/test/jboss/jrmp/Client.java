package test.jboss.jrmp;

import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.MarshalledObject;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 *
 * @author  <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
 * @version 
 */
public class Client
{
    public static void main(String[] args) throws Exception
    {
        IRemote remote = null;
        if( args.length == 1 )
        {
            Registry registry = LocateRegistry.getRegistry(args[0]);
            remote = (IRemote) registry.lookup("IRemote");
        }
        else
        {
            Socket cs = new Socket(args[0], 1100);
            InputStream is = cs.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);
            MarshalledObject marshalledProxy = (MarshalledObject) ois.readObject();
            remote = (IRemote) marshalledProxy.get();
        }
        System.out.println("Found IRemote: "+remote);
        IString str = remote.copy("Hello");
        System.out.println("IRemote.copy -> "+str);
    }
}
