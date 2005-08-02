/**
 * Copyright (C) 2004-2005 - Bull S.A.
 *
 * CAROL: Common Architecture for RMI ObjectWeb Layer
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
 * $Id: JacORBIIOPContextWrapperFactory.java,v 1.5 2005/08/02 21:23:19 ashah Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.spi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

import org.objectweb.carol.jndi.ns.JacORBCosNaming;
import org.objectweb.carol.rmi.exception.NamingExceptionHelper;
import org.objectweb.carol.util.configuration.CarolDefaultValues;
import org.objectweb.carol.util.csiv2.SasComponent;

/**
 * Class <code> JacORBIIOPContextWrapperFactory </code> is the CAROL JNDI
 * Context factory for JacORB. This context factory build the iiop context for
 * reference wrapping to/from a remote object
 * @author Florent BENOIT
 * @see javax.naming.spi.InitialContextFactory
 */
public class JacORBIIOPContextWrapperFactory extends AbsInitialContextFactory implements InitialContextFactory {

    /**
     * Object to use (specific POA) when using csiv2
     */
    public static final String SAS_COMPONENT = "org.objectweb.carol.util.csiv2.SasComponent";


    /**
     * @return class of the wrapper (to be instantiated + pool).
     */
    protected Class getWrapperClass() {
        return JacORBIIOPContext.class;
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
     * Store orb in the environment
     * @param environment hashtable containing the environment
     */
    protected void addExtraConfInEnvironment(Hashtable environment) {
        environment.put("java.naming.corba.orb", orb);
    }

    /**
     * For some protocols, there are some initialization stuff to do
     * @throws NamingException if there is an exception
     */
    protected void init() throws NamingException {
        // Initialize ORB if null
        if (orb == null) {
            orb = JacORBCosNaming.getOrb();
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
     * @param environment env to determine the key
     * @return the key or null if we don't want to cache it
     */
    protected String getKey(Hashtable environment) {
        String key = null;
        SasComponent sasComponent = null;
        if (environment != null) {
            key = (String) environment.get(Context.PROVIDER_URL);
            sasComponent = (SasComponent) environment.get(SAS_COMPONENT);
        }
        // there is a key (and need to cache) only if sas component is not present.
        if (sasComponent == null) {
            return key;
        } else {
            return null;
        }
    }
}
