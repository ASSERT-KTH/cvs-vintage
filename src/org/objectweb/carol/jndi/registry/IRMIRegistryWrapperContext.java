/**
 * Copyright (C) 2005 - Bull S.A.
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
 * $Id: IRMIRegistryWrapperContext.java,v 1.1 2005/09/15 13:04:16 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.registry;

import java.util.Hashtable;

import javax.naming.Context;

import org.objectweb.carol.jndi.ns.IRMIRegistry;

/**
 * Wrapper on a Registry object and implementing Context
 * @author Guillaume Riviere
 * @author Florent Benoit (Refactoring)
 */
public class IRMIRegistryWrapperContext extends AbsRegistryWrapperContext implements Context {

    /**
     * Create a local context for the registry
     * @param env hashtable used
     */
    public IRMIRegistryWrapperContext(Hashtable env) {
        super(env, IRMIRegistry.getRegistry(), "org.objectweb.carol.jndi.spi.IRMIContextWrapperFactory");
    }

}