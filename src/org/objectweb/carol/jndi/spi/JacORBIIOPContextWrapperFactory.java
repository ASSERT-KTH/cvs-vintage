/**
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 2004,2005 Bull S.A.
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
 * $Id: JacORBIIOPContextWrapperFactory.java,v 1.2 2005/02/08 09:45:57 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.spi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

/**
 * Class <code> JacORBIIOPContextWrapperFactory </code> is the CAROL JNDI
 * Context factory for JacORB. This context factory build the iiop context for
 * reference wrapping to/from a remote object
 * @author Florent BENOIT
 * @see javax.naming.spi.InitialContextFactory
 */
public class JacORBIIOPContextWrapperFactory implements InitialContextFactory {

    /**
     * Get/Build the IIOP Wrapper InitialContext
     * @param env the inital IIOP environement
     * @return a <code>Context</code> coresponding to the inital IIOP
     *         environement with IIOP Serializable ressource wrapping
     * @throws NamingException if a naming exception is encountered
     */
    public Context getInitialContext(Hashtable env) throws NamingException {
        return JacORBIIOPContext.getSingleInstance(env);
    }

}