package test.jboss.jrmp;

import java.rmi.Remote;
import java.rmi.RemoteException;

public class RemoteImpl implements IRemote
{
    public IString copy(String arg) throws RemoteException
    {
        return new AString(arg);
    }
}
