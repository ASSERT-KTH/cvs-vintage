/**
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 2004 Bull S.A.
 * Contact: jonas-team@objectweb.org
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
 * $Id: SasHelper.java,v 1.1 2004/12/13 16:24:13 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.csiv2;

import java.rmi.Remote;
import java.util.Hashtable;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.objectweb.carol.jndi.spi.JacORBIIOPContext;

/**
 * Utility class for binding object with Csiv2 policy
 * @author Florent Benoit
 */
public class SasHelper {

    /**
     * Utility class, no public constructor
     */
    private SasHelper() {

    }

    /**
     * Allow to bind object to registry with a given SAS component.
     * <p> Use for CsiV2 policy binding
     * @param jndiName the name to bind for registry
     * @param remoteObj the object to bind
     * @param sasComponent the object containing CsiV2 compound sec mech configuration
     * @throws NamingException if the bound cannot be done
     */
    public static void rebind(String jndiName, Remote remoteObj, SasComponent sasComponent) throws NamingException {
        if (sasComponent != null) {
            Hashtable env = new Hashtable();
            env.put(JacORBIIOPContext.SAS_COMPONENT, sasComponent);
            new InitialContext(env).rebind(jndiName, remoteObj);
        } else {
            new InitialContext().rebind(jndiName, remoteObj);
        }
    }

}