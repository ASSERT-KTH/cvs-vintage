package test.jboss.jrmp;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author  Scott_Stark@displayscape.com
 * @version 
 */
public class Handler implements InvocationHandler, Serializable
{
    Remote remote;
    Handler(Remote remote)
    {
        this.remote = remote;
    }
    public Object invoke(Object proxy, Method method, Object[] args)      
          throws Throwable
    {
        return method.invoke(remote, args);
    }
}
