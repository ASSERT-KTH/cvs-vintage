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
 * $Id: MultiOrbInitialContextFactory.java,v 1.8 2005/03/10 10:05:02 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.spi;

import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.objectweb.carol.util.configuration.CarolCurrentConfiguration;
import org.objectweb.carol.util.configuration.CarolDefaultValues;
import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Class <code> MultiOrbInitialContextFactory </code> is the CAROL JNDI SPI
 * Context Factory for multi Context management.
 * @author Guillaume Riviere
 * @author Florent Benoit (Refactoring)
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
        // Need to know if we want a multiprotocol context or the context for the given PROVIDER_URL
        String providerURL = null;
        if (env != null) {
            providerURL = (String) env.get(Context.PROVIDER_URL);
        }

        //  No provider URL, do multi context
        if (providerURL == null) {
            if (TraceCarol.isDebugJndiCarol()) {
                TraceCarol.debugJndiCarol("No provider URL, use multiprotocol context");
            }
            return new MultiContext(env);
        } else {
            //if user has provided a PROVIDER_URL, don't do a multiprocol context
            if (TraceCarol.isDebugJndiCarol()) {
                TraceCarol.debugJndiCarol("Use provider URL of the environment '" + providerURL + "'.");
            }
            // Need to know the factory to use if it is not provided
            String initFactory = (String) env.get(Context.INITIAL_CONTEXT_FACTORY);
            if (initFactory == null) {
                String protocolName = CarolDefaultValues.getRMIProtocol(providerURL);
                Properties protocolConfig = CarolCurrentConfiguration.getCurrent().getRMIProperties(protocolName);
                if (protocolConfig == null) {
                    throw new IllegalArgumentException("No configuration in carol for protocol '" + protocolName + "'.");
                }
                initFactory = (String) protocolConfig.get(Context.INITIAL_CONTEXT_FACTORY);
                env.put(Context.INITIAL_CONTEXT_FACTORY, initFactory);
                if (TraceCarol.isDebugJndiCarol()) {
                    TraceCarol.debugJndiCarol("No INITIAL_CONTEXT_FACTORY, use '" + initFactory + "' as InitialContex factory.");
                }
            }
            return new InitialContext(env);
        }
    }

}