/**
 * Copyright (C) 2005 - Redhat
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
 * $Id: URLInitialContextFactory.java,v 1.2 2005/09/15 12:52:01 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.spi;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.NamingManager;

/**
 * @author Archit Shah
 */
public class URLInitialContextFactory implements InitialContextFactory {

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
        String provider = (String) env.get(Context.PROVIDER_URL);
        String protocol = provider.substring(0, provider.indexOf(':'));
        Context ctx = NamingManager.getURLContext(protocol, env);
        return (Context) ctx.lookup(provider);
    }
}
