/*
 * @(#) LmiRegistry.java	1.0 02/07/15
 *
 * Copyright (C) 2002 - INRIA (www.inria.fr)
 *
 * CAROL: Common Architecture for RMI ObjectWeb Layer
 *
 * This library is developed inside the ObjectWeb Consortium,
 * http://www.objectweb.org
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
 *
 */
package org.objectweb.carol.jndi.ns;

import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import org.objectweb.carol.util.configuration.TraceCarol;


/*
 * Class <code>LMIRegistry</code> is a fake registry service
 *
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/01/2003
 */
public class LmiRegistry implements NameService {

    /**
     * port number (0 for default)
     */
    public int port=0;

    /**
     * start Method, Start a new NameService or do nothing if the name service is all ready start
     * @param int port is port number
     * @throws NameServiceException if a problem occure 
     */
    public void start() throws NameServiceException {	
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("LMIRegistry.start() on port:" + port);
        }
	// do nothing
    }

    /**
     * stop Method, Stop a NameService or do nothing if the name service is all ready stop
     * @throws NameServiceException if a problem occure 
     */
    public void stop() throws NameServiceException {
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("LMIRegistry.stop()");
        }
	// do nothing
    }

    /**
     * isStarted Method, check if a name service is started
     * @return alway return true 
     */
    public boolean isStarted() {
	return true;
    }

    /**
     * set port method, set the port for the name service
     * @param int port number
     */
    public void setPort(int p) {
    }
}
