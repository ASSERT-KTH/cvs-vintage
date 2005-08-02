/**
 * Copyright (C) 2002,2005 - INRIA (www.inria.fr)
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
 * --------------------------------------------------------------------------
 * $Id: IIOPContextWrapperFactory.java,v 1.6 2005/08/02 21:23:18 ashah Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.spi;

import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

import org.objectweb.carol.jndi.ns.IIOPCosNaming;
import org.objectweb.carol.rmi.exception.NamingExceptionHelper;
import org.objectweb.carol.util.configuration.CarolDefaultValues;

/**
 * Class <code> IIOPRemoteReferenceContextWrapperFactory </code> is the CAROL
 * JNDI Context factory. This context factory build the iiop context for
 * reference wrapping to/from a remote object
 * @author Guillaume Riviere
 * @author Florent Benoit (refactoring)
 * @see javax.naming.spi.InitialContextFactory
 */
public class IIOPContextWrapperFactory extends AbsInitialContextFactory implements InitialContextFactory {

    /**
     * @return class of the wrapper (to be instantiated + pool).
     */
    protected Class getWrapperClass() {
        return IIOPContext.class;
    }

    /**
     * Unique instance of the ORB running in the JVM
     */
    private static ORB orb = null;

    /**
     * The orb was started or not ?
     */
    private static boolean orbStarted = false;

    /**
     * Root POA used by Carol
     */
    private static POA rootPOA = null;

    /**
     * For some protocols, there are some initialization stuff to do
     * @throws NamingException if there is an exception
     */
    protected void init() throws NamingException {
        // Initialize ORB if null
        if (orb == null) {
            orb = IIOPCosNaming.getOrb();
        }

        if (!orbStarted && System.getProperty(CarolDefaultValues.SERVER_MODE, "false").equalsIgnoreCase("true")) {
            // Start ORB if it was not run and if we are in server mode
            new Thread(new Runnable() {

                public void run() {
                    orb.run();
                }
            }).start();
            orbStarted = true;
        }

        // activate root POA
        if (rootPOA == null) {
            try {
                rootPOA = org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
                rootPOA.the_POAManager().activate();
            } catch (Exception e) {
                throw NamingExceptionHelper.create("Cannot get a single instance of rootPOA : " + e.getMessage(), e);
            }
        }

    }

    /**
     * Store orb in the environment
     * @param environment hashtable containing the environment
     */
    protected void addExtraConfInEnvironment(Hashtable environment) {
        environment.put("java.naming.corba.orb", orb);
    }
}
