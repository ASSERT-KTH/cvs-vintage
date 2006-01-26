/**
 * Copyright (C) 2006 - Bull S.A.S.
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
 * $Id: CmiRegistryWrapperContext.java,v 1.1 2006/01/26 16:28:55 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.registry;

import java.util.Hashtable;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;

import org.objectweb.carol.jndi.ns.CmiRegistry;

/**
 * Wrapper on a Registry object and implementing Context
 * @author Benoit Pelletier
 */
public class CmiRegistryWrapperContext extends AbsRegistryWrapperContext implements Context {

    /**
     * Create a local context for the registry
     * @param env hashtable used
     */
    public CmiRegistryWrapperContext(Hashtable env) {
        super(env, CmiRegistry.getRegistry(), "org.objectweb.carol.jndi.spi.CmiContextWrapperFactory");
    }

    /**
     * Remove the unneeded characters within a jndi name
     * @param name jndi name to scan
     * @return cleaned name
     */
    private String cleanName(String name) {
        String n = null;
        if (name.startsWith("\"") && (name.endsWith("\""))) {
            n = name.substring(1, name.length() - 1);
        } else {
            n = name;
        }
        return n;
    }
    /**
     * Checks whether the name contains a url
     * @param name jndi name to check
     * @return true if the name contains a url
     */
    private boolean isAUrl(String name) {
        return name.startsWith("cmi://") || name.startsWith("\"cmi://");
    }

    /**
     * Get the url part of the name
     * @param name jndi name
     * @return url part of the name
     */
    private String getUrl(String name) {
        String myName = cleanName(name);
        if (isAUrl(myName)) {
            String url = myName.substring(0, myName.indexOf('/', "cmi://".length() + 1));
            return url;
        } else {
            return "";
        }
    }

    /**
     * Get the name without any url
     * @param name jndi name to process
     * @return name without any url part
     */
    private String getName(String name) {
        String myName = cleanName(name);
        if (isAUrl(myName)) {
            String url = getUrl(myName);
            String n = myName.substring(url.length() + 1);
            return n;

        } else {
            return myName;
        }
    }

    /**
     * Retrieves the named object.
     * @param name the name of the object to look up
     * @return the object bound to <tt>name</tt>
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookup(Name name) throws NamingException {
        // If required, the call is redirected to the targeted context
        if (isAUrl(name.get(0))) {
            String url = getUrl(name.get(0));
            String n = getName(name.get(0));
            Hashtable h = new Hashtable();
            h.put(Context.PROVIDER_URL, url);
            h.put(Context.INITIAL_CONTEXT_FACTORY, "org.objectweb.carol.cmi.jndi.CmiInitialContextFactory");
            return new InitialContext(h).lookup(n);
        }
        // else default behavior
        return super.lookup(name);
    }

    /**
     * Binds a name to an object.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     */
    public void bind(Name name, Object obj) throws NamingException {

        // Clean the jndi name from extra info/characters
        String n = getName(name.get(0));

        super.bind(new CompositeName(n), obj);
    }

    /**
     * Binds a name to an object, overwriting any existing binding. All
     * intermediate contexts and the target context (that named by all but
     * terminal atomic component of the name) must already exist.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     */
    public void rebind(Name name, Object obj) throws NamingException {
        // Clean the jndi name from extra info/characters
        String n = getName(name.get(0));

        super.rebind(new CompositeName(n), obj);
    }

    /**
     * Unbinds the named object. Removes the terminal atomic name in
     * <code>name</code> from the target context--that named by all but the
     * terminal atomic part of <code>name</code>.
     * @param name the name to unbind; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void unbind(Name name) throws NamingException {
        // Clean the jndi name from extra info/characters
        String n = getName(name.get(0));

        super.unbind(new CompositeName(n));
    }
}

