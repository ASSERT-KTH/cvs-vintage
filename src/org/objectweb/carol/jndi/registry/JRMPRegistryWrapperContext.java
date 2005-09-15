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
 * $Id: JRMPRegistryWrapperContext.java,v 1.1 2005/09/15 13:04:16 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.registry;

import java.util.Hashtable;

import javax.naming.Context;

import org.objectweb.carol.jndi.ns.JRMPRegistry;

/**
 * Wrapper on a Registry object and implementing Context
 * @author Guillaume Riviere
 * @author Florent Benoit (Refactoring)
 */
public class JRMPRegistryWrapperContext extends AbsRegistryWrapperContext implements Context {

    /**
     * Create a local context for the registry
     * @param env hashtable used
     */
    public JRMPRegistryWrapperContext(Hashtable env) {
        super(env, JRMPRegistry.getRegistry(), "org.objectweb.carol.jndi.spi.JRMPContextWrapperFactory");
    }
}

