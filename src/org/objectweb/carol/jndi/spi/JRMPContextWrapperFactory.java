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
 * $Id: JRMPContextWrapperFactory.java,v 1.4 2004/09/01 11:02:41 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.spi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.objectweb.carol.jndi.ns.JRMPRegistry;
import org.objectweb.carol.util.configuration.CarolDefaultValues;

/**
 * @author riviereg To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class JRMPContextWrapperFactory implements InitialContextFactory {

    /**
     * Get/Build the JRMP Wrapper InitialContext
     * @param env the inital JRMP environement
     * @return a <code>Context</code> coresponding to the inital JRMP
     *         environement with JRMP Serializable ressource wrapping
     * @throws NamingException if a naming exception is encountered
     */
    public Context getInitialContext(Hashtable env) throws NamingException {

        boolean localO = new Boolean(System.getProperty(CarolDefaultValues.LOCAL_JRMP_PROPERTY, "false"))
                .booleanValue();

        if ((JRMPRegistry.isLocal()) && (localO)) {
            return JRMPLocalContext.getSingleInstance(JRMPRegistry.registry, env);
        } else {
            return JRMPContext.getSingleInstance(env);
        }
    }
}