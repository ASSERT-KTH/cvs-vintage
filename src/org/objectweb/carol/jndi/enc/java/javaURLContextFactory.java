/*
 *
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 1999 Bull S.A.
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
 * $Id: javaURLContextFactory.java,v 1.2 2003/04/10 15:38:40 riviereg Exp $
 * --------------------------------------------------------------------------
 */

package org.objectweb.carol.jndi.enc.java;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Context factory for javaURLContext objects.
 * This factory will be used for all "java:..." urls provided as Name objects
 * for all JNDI operations.
 *
 * @author Philippe Durieux
 * Contributor(s): 
 * Philippe Coq Monolog
 */
public class javaURLContextFactory implements ObjectFactory {

    /**
     * Returns an instance of javaURLContext for a java URL.
     *
     * If url is null, the result is a context for resolving java URLs.
     * If url is a URL, the result is a context named by the URL.
     *
     * @param url 	String with a "java:" prefix or null.
     * @param name	Name of context, relative to ctx, or null.
     * @param ctx	Context relative to which 'name' is named.
     * @param env	Environment to use when creating the context
     */
    public Object getObjectInstance(Object url, Name name, Context ctx, Hashtable env) throws Exception {

TraceCarol.debugJndiCarol( "url=" + url);

	if (url == null) {
	    // All naming operations with "java:..." comes here
	    // Users are encouraged to used intermediate contexts:
	    // ctx = ic.lookup("java:comp/env") called only once (perfs)
	    return new javaURLContext(env);
	}
	if (url instanceof String) {
	    // Don't know what to do here 
TraceCarol.debugJndiCarol( "javaURLContextFactory.getObjectInstance("+url+")");
	    return null;
	} else if (url instanceof String[]) {
	    // Don't know what to do here 
TraceCarol.debugJndiCarol( "javaURLContextFactory.getObjectInstance(String[])");
	    return null;
	} else {
	    // invalid argument
	    throw (new IllegalArgumentException("javaURLContextFactory"));
	}
    }
}
