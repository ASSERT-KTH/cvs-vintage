package test.jboss.jrmp;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemote extends Remote
{
    public IString copy(String arg) throws RemoteException;
}
