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
 * $Id: AbsInitialContextFactory.java,v 1.2 2005/08/02 21:23:18 ashah Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.spi;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.objectweb.carol.rmi.exception.NamingExceptionHelper;

/**
 * Instantiate the class of the given SPI and Handle a cache mechanism for
 * storing contexts. It avoids to build new Context.
 * When wanting to integrate a new protocol, extends this class and define getReferencingFactory() and getWrapperClass() method.
 * Also, there are some others methods that you could redefine like init() or addExtraConfInEnvironment() to your specific needs.
 * @author Florent Benoit
 */
public abstract class AbsInitialContextFactory implements InitialContextFactory {


    /**
     * Map containing context (used as cache mechanism)
     */
    private static HashMap contexts = new HashMap();


    /**
     * @return the real factory of this wrapper. The default is to use the
     * URLInitialContextFactory which will pass to the NamingManager the
     * protocol specified in the context url in order to retrieve a context
     * factory.
     */
    protected String getReferencingFactory() {
        return URLInitialContextFactory.class.getName();
    }

    /**
     * @return class of the wrapper (to be instantiated + pool).
     */
    protected abstract Class getWrapperClass();

    /**
     * Creates an Initial Context for beginning name resolution. Special
     * requirements of this context are supplied using <code>environment</code>.
     * @param environment The possibly null environment specifying information
     *        to be used in the creation of the initial context.
     * @return A non-null initial context object that implements the Context
     *         interface.
     * @exception NamingException If cannot create an initial context.
     */
    public Context getInitialContext(Hashtable environment) throws NamingException {

        // Look into cache if key is not null
        Context ctx = null;
        String key = getKey(environment);
        if (key != null) {
            ctx = (Context) contexts.get(key);
        }

        // value found, return it
        if (ctx != null) {
            return ctx;
        }

        // Factory need to initialize some specific settings ?
        init();

        // else, need to build a new one and add it in cache
        environment.put(Context.INITIAL_CONTEXT_FACTORY, getReferencingFactory());

        // Get class of the wrapper
        Class clazz = getWrapperClass();

        // Load the constructor
        Constructor ctr = null;
        try {
            ctr = clazz.getConstructor(getClassConstructor());
        } catch (Exception e) {
            throw NamingExceptionHelper.create("Cannot find the constructor with Context class as argument in class '" + clazz.getName() + "' : " + e.getMessage(), e);
        }

        // The environment may be completed by extra values ?
        addExtraConfInEnvironment(environment);

        // Build a new context
        try {
            ctx = (Context) ctr.newInstance(getClassArgs(environment));
        } catch (Exception e) {
            throw NamingExceptionHelper.create("Cannot build an instance of the class '" + clazz.getName() + "' : " + e.getMessage(), e);
        }

        // add in cache if there is a key
        if (key != null) {
            contexts.put(key, ctx);
        }

        // return context
        return ctx;
    }

    /**
     * @param environment env to determine the key
     * @return the key or null if we don't want to cache it
     */
    protected String getKey(Hashtable environment) {
        String key = null;
        if (environment != null) {
            key = (String) environment.get(Context.PROVIDER_URL);
        }
        return key;
    }

    /**
     * Gets the values to give to the constructor of the given class (provided by getWrapperClass())
     * @param environment given environment
     * @return array of objects for the constructor
     * @throws NamingException if the new InitialContext fails
     */
    protected Object[] getClassArgs(Hashtable environment) throws NamingException {
        return new Object[] {new InitialContext(environment)};
    }

    /**
     * Gets the arguments of the constructor of the given class (provided by getWrapperClass())
     * @return array of objects for the constructor
     */
    protected Class[] getClassConstructor() {
        return new Class[] {Context.class};
    }


    /**
     * This method should be redefined by subclasses
     * For some protocols, there is a need to populate environment with some settings
     * @param environment hashtable containing the environment
     */
    protected void addExtraConfInEnvironment(Hashtable environment) {

    }

    /**
     * This method should be redefined by subclasses
     * For some protocols, there are some initialization stuff to do
     * @throws NamingException if there is an exception
     */
    protected void init() throws NamingException {
    }

}
