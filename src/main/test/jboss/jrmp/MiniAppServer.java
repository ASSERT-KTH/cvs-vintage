package test.jboss.jrmp;

import java.io.File;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.MarshalledObject;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/** A test server for dynamic class loading.
 *
 * @author Scott_Stark@displayscape.com
 * @version $Revision: 1.1 $
 */
public class MiniAppServer
{
    private Registry registry;
    private Remote remoteImpl;
    private Remote stub;
    private URLClassLoader loader;

    public MiniAppServer() throws RemoteException
    {
        registry = LocateRegistry.createRegistry(1099);
    }

    private void loadObjects(String jarPath) throws Exception
    {
        File jarFile = new File(jarPath);
        URL[] urls = {jarFile.toURL()};
        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        loader = new URLClassLoader(urls, parent);
        Thread.currentThread().setContextClassLoader(loader);
        // This causes the java.rmi.server.codebase value to be used for any classes loaded by loader
        //sun.rmi.server.LoaderHandler.registerCodebaseLoader(loader);
        remoteImpl = (Remote) loader.loadClass("test.jboss.jrmp.RemoteImpl").newInstance();
        stub = UnicastRemoteObject.exportObject(remoteImpl);
        System.out.println("Exported: "+stub);
    }
    private void bindToRegistry() throws Exception
    {
        registry.bind("IRemote", stub);
        System.out.println("Bound object under IRemote");
    }
    private void exportAsProxy() throws Exception
    {
        Handler handler = new Handler(stub);
        Class[] interfaces = {loader.loadClass("test.jboss.jrmp.IRemote")};
        Remote proxy = (Remote) Proxy.newProxyInstance(loader, interfaces, handler);
        MarshalledObject marshalledProxy = new MarshalledObject(proxy);
        // Wait for a client request for the proxy
        ServerSocket ss = new ServerSocket(1100);
        System.out.println("Waiting for client on port 1100...");
        Socket cs = ss.accept();
        System.out.println("Accepted client connection: "+cs);
        OutputStream os = cs.getOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(marshalledProxy);
        oos.close();
        cs.close();
        ss.close();
        System.out.println("Sent remote proxy");
    }

    public static void main(String[] args) throws Exception
    {
        System.setProperty("java.rmi.server.codebase", "http://siren:8080/jboss/remote.jar");
        System.setProperty("java.rmi.server.hostname", "succubus");
        MiniAppServer appServer = new MiniAppServer();
        String jarPath = args.length > 0 ? args[0] : "remote.jar";
        appServer.loadObjects(jarPath);
        //appServer.bindToRegistry();
        appServer.exportAsProxy();
    }
}
