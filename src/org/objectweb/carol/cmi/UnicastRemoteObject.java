/*
 * Copyright (C) 2002-2003, Simon Nieuviarts
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 */
package org.objectweb.carol.cmi;

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * To export objects in the cluster.
 */
public class UnicastRemoteObject implements Remote {
    protected UnicastRemoteObject() throws RemoteException {
        exportObject((Remote)this);
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Not implemented");
    }

    /*
        private static ClusterSkel putObject(Remote obj) throws RemoteException {
            WeakRef wr = new WeakRef(obj, rQueue);
            boolean start = false;
            ClusterSkel cs;
            synchronized (exports) {
                cs = (ClusterSkel)exports.get(wr);
                if (cs == null) {
                    cs = new ClusterSkel(obj);
                    exports.put(wr, cs);
                    if (rQueueFlush == null) {
                        rQueueFlush = new RefQueueFlush();
                        start = true;
                    }
                }
            }
            if (start) rQueueFlush.start();
            return cs;
        }
    */

    public static void exportObject(Remote obj)
        throws RemoteException {
        /*
            	if (obj instanceof UnicastRemoteObject) {
            	    ClusterSkel cs = putObject(obj);
        	    LowerOrb.exportObject(cs, port);
        	    return cs; // XXX Should return the stub
        	} else
        	    throw new RemoteException("no support for servers which do not extend " + UnicastRemoteObject.class.getClass().getName());
        */
        //    	DistributedExports.exportObject(obj);
        LowerOrb.exportObject(obj);
    }

    public static void unexportObject(Remote obj)
        throws NoSuchObjectException {
        /*
        	ClusterSkel cs = (ClusterSkel)exports.get(obj);
        	if (cs == null)
        	    throw new NoSuchObjectException("Object not exported");
        	return LowerOrb.unexportObject(cs, force);
        */
        //    	DistributedExports.unexportObject(obj);
        LowerOrb.unexportObject(obj);
    }
}
