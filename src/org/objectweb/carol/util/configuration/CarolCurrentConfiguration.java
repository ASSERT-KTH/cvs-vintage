/**
 * Copyright (C) 2002,2004 - INRIA (www.inria.fr)
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
 * $Id: CarolCurrentConfiguration.java,v 1.4 2005/03/11 13:57:39 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.configuration;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.CORBA.PortableRemoteObjectDelegate;

/**
 * Class <code>CarolCurrentConfiguration</code> For handling active
 * configuration
 * @author Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @author Jacques Cayuela (Jacques.Cayuela@bull.net)
 * @version 1.0, 27/01/2004
 */

public class CarolCurrentConfiguration {

    /**
     * Protocols Portable Remote Object Delegate
     */
    private static Hashtable proDelegateTable = null;

    /**
     * Context Array for each protocol
     */
    private static Hashtable protocolsTable = null;

    /**
     * Protocol for default
     */
    private static String defaultRMI = null;

    /**
     * Thread Local for protocol context propagation
     */
    private static InheritableThreadLocal threadCtx = null;

    /**
     * private constructor for singleton
     */
    private static CarolCurrentConfiguration current = new CarolCurrentConfiguration();

    /**
     * private constructor for unicicity
     */
    private CarolCurrentConfiguration() {

        try {

            threadCtx = new InheritableThreadLocal();
            proDelegateTable = new Hashtable();
            protocolsTable = new Hashtable();
            //get rmi configuration hashtable
            Hashtable allRMIConfiguration = CarolConfiguration.getAllRMIConfiguration();
            //int nbProtocol = allRMIConfiguration.size();
            for (Enumeration e = allRMIConfiguration.elements(); e.hasMoreElements();) {
                RMIConfiguration currentConf = (RMIConfiguration) e.nextElement();
                String rmiName = currentConf.getName();
                // get the PRO
                proDelegateTable.put(rmiName, Class.forName(currentConf.getPro())
                        .newInstance());
                protocolsTable.put(rmiName, currentConf.getJndiProperties());
            }
            defaultRMI = CarolConfiguration.getDefaultProtocol().getName();
            // set the default protocol
            threadCtx.set(defaultRMI);

            // trace Protocol current
            if (TraceCarol.isDebugCarol()) {
                TraceCarol.debugCarol("CarolCurrentConfiguration.CarolCurrentConfiguration()");
                TraceCarol.debugCarol("Number of rmi:" + protocolsTable.size());
                TraceCarol.debugCarol("Default:" + defaultRMI);
            }

        } catch (Exception e) {
            if (TraceCarol.isDebugCarol()) {
                TraceCarol.debugCarol("CarolCurrentConfiguration.CarolCurrentConfiguration() Exception:" + e);
            }
        }
    }

    /**
     * Method getCurrent
     * @return CarolCurrentConfiguration return the current
     */
    public static CarolCurrentConfiguration getCurrent() {
        return current;
    }

    /**
     * This method if for setting one rmi context
     * @param s the rmi name
     */
    public void setRMI(String s) {
        threadCtx.set(s);
    }

    /**
     * set the default protocol
     */
    public void setDefault() {
        threadCtx.set(defaultRMI);
    }

    /**
     * Get the Portable Remote Object Hashtable
     * @return Hashtable the hashtable of PROD
     */
    public Hashtable getPortableRemoteObjectHashtable() {
        return proDelegateTable;
    }

    /**
     * Get the Context Hashtable
     * @param env the JNDI environment
     * @return Hashtable the hashtable of Context
     * @throws NamingException if InitialContext cannot be built
     */
    public Hashtable getNewContextHashtable(Hashtable env) throws NamingException {

        // build a new hashtable of context
        Hashtable result = new Hashtable();

        for (Enumeration e = protocolsTable.keys(); e.hasMoreElements();) {
            String k = (String) e.nextElement();

            // Get protocol env
            Hashtable protocolEnv = (Hashtable) protocolsTable.get(k);

            // Add properties which are not already defined (not JNDI env)
            for (Enumeration enu = env.keys(); enu.hasMoreElements();) {
                String key = (String) enu.nextElement();
                // not a java property defined, add it
                if (protocolEnv.get(key) == null || !key.startsWith("java")) {
                    protocolEnv.put(key, env.get(key));
                }
            }

            result.put(k, new InitialContext(protocolEnv));
        }

        return result;
    }

    /**
     * Get current protocol PROD
     * @return PortableRemoteObjectDelegate the portable remote object
     */
    public PortableRemoteObjectDelegate getCurrentPortableRemoteObject() {
        if (threadCtx.get() == null) {
            return (PortableRemoteObjectDelegate) proDelegateTable.get(defaultRMI);
        } else {
            return (PortableRemoteObjectDelegate) proDelegateTable.get(threadCtx.get());
        }
    }

    /**
     * Get current protocol Initial Context
     * @return InitialContext the initial Context
     * @throws NamingException if InitialContext cannot be built
     */
    public Context getCurrentInitialContext() throws NamingException {
        if (threadCtx.get() == null) {
            return new InitialContext((Properties) protocolsTable.get(defaultRMI));
        } else {
            return new InitialContext((Properties) protocolsTable.get(threadCtx.get()));
        }
    }

    /**
     * Get RMI properties
     * @param name protocol name
     * @return the corresponding RMI properties (null if RMI name not exists)
     */
    public Properties getRMIProperties(String name) {

        return (Properties) protocolsTable.get(name);
    }

    /**
     * Get current protocol RMI name
     * @return String the RMI name
     */
    public String getCurrentRMIName() {
        if (threadCtx.get() == null) {
            return defaultRMI;
        } else {
            return (String) threadCtx.get();
        }
    }

    /**
     * @return string representation of the object
     */
    public String toString() {
        return "\nnumber of rmi:" + protocolsTable.size() + "\ndefault:" + defaultRMI;
    }
}