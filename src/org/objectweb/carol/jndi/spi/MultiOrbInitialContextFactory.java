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
 * $Id: MultiOrbInitialContextFactory.java,v 1.6 2005/02/11 10:12:59 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.spi;

//java import
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.ObjectFactory;

import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Class <code> MultiOrbInitialContextFactory </code> is the CAROL JNDI SPI
 * Context Factory for multi Context management.
 * @author Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @see javax.naming.spi.ObjectFactory
 * @see javax.naming.spi.InitialContextFactory
 */
public class MultiOrbInitialContextFactory implements ObjectFactory, InitialContextFactory {

    /**
     * Get a new multi initial context
     * @return the new Multi Initial Context
     */
    public Context getInitialContext(Hashtable env) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContextFactory.getInitialContext(Hashtable env)");
        }
        return new ContextWrapper(env);
    }

    /**
     * never use for the moment
     */
    public Object getObjectInstance(Object ref, Name name, Context nameCtx, Hashtable env) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol
                    .debugJndiCarol("MultiOrbInitialContextFactory.getObjectInstance(Object ref, Name name, Context nameCtx, Hashtable env)");
        }
        // never use
        return null;
    }
}