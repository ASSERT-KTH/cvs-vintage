/*
 * Copyright (c) 2000 Peter Antman DN <peter.antman@dn.se>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jboss.jms.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * A JMS provider adapter for <em>JBossMQ</em>.
 *
 * Created: Fri Dec 22 09:34:04 2000
 * 6/22/01 - hchirino - The queue/topic jndi references are now configed via JMX
 *
 * @author Peter Antman
 * @author  <a href="mailto:cojonudo14@hotmail.com">Hiram Chirino</a>
 * @version
 */
public class JBossMQProvider 
   extends AbstractJMSProviderAdapter
{


	public static final String INITIAL_CONTEXT_FACTORY = "org.jnp.interfaces.NamingContextFactory";
	public static final String URL_PKG_PREFIXES = "org.jboss.naming";
	private static final String SECURITY_MANAGER = "java.naming.rmi.security.manager";

	private String hasJndiSecurityManager = "yes";

	public JBossMQProvider() {
	}

	public Context getInitialContext() throws NamingException {
		Context ctx = null;
		if (providerURL == null) {
			// Use default
			ctx = new InitialContext(); // Only for JBoss embedded now
		} else {
			// Try another location
			Hashtable props = new Hashtable();
			props.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
			props.put(Context.PROVIDER_URL, providerURL);
			props.put(SECURITY_MANAGER, hasJndiSecurityManager);
			props.put(Context.URL_PKG_PREFIXES, URL_PKG_PREFIXES);
			ctx = new InitialContext(props);
		}
		return ctx;
	}

}