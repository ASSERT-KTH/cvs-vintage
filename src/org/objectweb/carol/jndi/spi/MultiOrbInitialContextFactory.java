/**
 * Copyright (C) 2002-2005 - INRIA (www.inria.fr)
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
 * $Id: MultiOrbInitialContextFactory.java,v 1.7 2005/03/08 17:24:29 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.spi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Class <code> MultiOrbInitialContextFactory </code> is the CAROL JNDI SPI
 * Context Factory for multi Context management.
 * @author Guillaume Riviere
 * @see javax.naming.spi.InitialContextFactory
 */
public class MultiOrbInitialContextFactory implements InitialContextFactory {

    /**
     * Creates an Initial Context for beginning name resolution. Special
     * requirements of this context are supplied using <code>environment</code>.
     * @param env The possibly null environment specifying information
     *        to be used in the creation of the initial context.
     * @return A non-null initial context object that implements the Context
     *         interface.
     * @exception NamingException If cannot create an initial context.
     */
    public Context getInitialContext(Hashtable env) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContextFactory.getInitialContext(Hashtable env)");
        }
        return new ContextWrapper(env);
    }

}