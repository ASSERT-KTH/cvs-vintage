package org.objectweb.carol.jndi.wrapping;

import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.naming.Reference;

public interface RemoteReference extends Remote {
    public Reference getReference() throws RemoteException;
}
