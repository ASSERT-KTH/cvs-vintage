/**
 * Copyright (C) 2002-2005 - Bull S.A.
 *
 * CMI : Cluster Method Invocation
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
 * $Id: CmiInitialContextFactory.java,v 1.3 2005/07/27 11:49:23 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.spi.InitialContextFactory;

/**
 * Class <code> CmiInitialContextFactory </code> is the implementation of the InitialContextFactory
 * interface for the CMI protocol
 *
 * @see javax.naming.spi.InitialContextFactory
 *
 * @author Simon Nieuviarts
 */
public class CmiInitialContextFactory implements InitialContextFactory {

    /**
     * @param env environment
     * @return a new initial context
     * @throws NamingException if an exception is encountered
     */
    public Context getInitialContext(Hashtable env)
        throws javax.naming.NamingException {
        return new FlatCtx(env);
    }
}
