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
 * $Id: MultiContext.java,v 1.7 2005/03/10 10:05:01 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.spi;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.objectweb.carol.util.configuration.CarolCurrentConfiguration;
import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Class <code> MultiContext </code> is the CAROL JNDI SPI Context for multi
 * Context management.
 * <ul>
 * <li>For void methods, it call method on every protocols</li>
 * <li>For other methods (returning objects), it call method on the current protocol</li>
 * </ul>
 * @author Guillaume Riviere
 * @author Florent Benoit (refactoring)
 * @see javax.naming.Context
 * @see javax.naming.InitialContext
 * @see org.objectweb.util.multi.ProtocolCurrent
 */
public class MultiContext implements Context {

    /**
     * The ProtocolCurrent for management of active Context
     */
    private CarolCurrentConfiguration currentConfig = null;

    /**
     * Active Contexts, this variable is just a cache of the protocol current
     * context array
     */
    private Hashtable activesInitialsContexts = null;

    /**
     * String for rmi name
     */
    private String rmiName = null;

    /**
     * Constructor, load communication framework and instaciate initial contexts
     * @param env the environment of the InitialContext
     * @throws NamingException if cccf.getNewContextHashtable(env) fails
     */
    public MultiContext(Hashtable env) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.MultiContext(env), env = " + env);
        }
        currentConfig = CarolCurrentConfiguration.getCurrent();
        activesInitialsContexts = currentConfig.getNewContextHashtable(env);
    }

    /**
     * Retrieves the named object.
     * @param name the name of the object to look up
     * @return the object bound to <tt>name</tt>
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookup(String name) throws NamingException {
        return currentConfig.getCurrentInitialContext().lookup(name);
    }

    /**
     * Retrieves the named object.
     * @param name the name of the object to look up
     * @return the object bound to <tt>name</tt>
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookup(Name name) throws NamingException {
        return currentConfig.getCurrentInitialContext().lookup(name);
    }

    /**
     * Binds a name to an object.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     */
    public void bind(String name, Object obj) throws NamingException {
        for (Enumeration e = activesInitialsContexts.keys(); e.hasMoreElements();) {
            rmiName = (String) e.nextElement();
            currentConfig.setRMI(rmiName);
            ((Context) activesInitialsContexts.get(rmiName)).bind(name, obj);
            currentConfig.setDefault();
        }
    }

    /**
     * Binds a name to an object.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     */
    public void bind(Name name, Object obj) throws NamingException {
        for (Enumeration e = activesInitialsContexts.keys(); e.hasMoreElements();) {
            rmiName = (String) e.nextElement();
            currentConfig.setRMI(rmiName);
            ((Context) activesInitialsContexts.get(rmiName)).bind(name, obj);
            currentConfig.setDefault();
        }
    }

    /**
     * Binds a name to an object, overwriting any existing binding.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     */
    public void rebind(String name, Object obj) throws NamingException {
        for (Enumeration e = activesInitialsContexts.keys(); e.hasMoreElements();) {
            rmiName = (String) e.nextElement();
            currentConfig.setRMI(rmiName);
            ((Context) activesInitialsContexts.get(rmiName)).rebind(name, obj);
            currentConfig.setDefault();
        }
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
        for (Enumeration e = activesInitialsContexts.keys(); e.hasMoreElements();) {
            rmiName = (String) e.nextElement();
            currentConfig.setRMI(rmiName);
            ((Context) activesInitialsContexts.get(rmiName)).rebind(name, obj);
            currentConfig.setDefault();
        }
    }

    /**
     * Unbinds the named object.
     * @param name the name to unbind; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void unbind(String name) throws NamingException {
        for (Enumeration e = activesInitialsContexts.keys(); e.hasMoreElements();) {
            rmiName = (String) e.nextElement();
            currentConfig.setRMI(rmiName);
            ((Context) activesInitialsContexts.get(rmiName)).unbind(name);
            currentConfig.setDefault();
        }
    }

    /**
     * Unbinds the named object. Removes the terminal atomic name in
     * <code>name</code> from the target context--that named by all but the
     * terminal atomic part of <code>name</code>.
     * @param name the name to unbind; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void unbind(Name name) throws NamingException {
        for (Enumeration e = activesInitialsContexts.keys(); e.hasMoreElements();) {
            rmiName = (String) e.nextElement();
            currentConfig.setRMI(rmiName);
            ((Context) activesInitialsContexts.get(rmiName)).unbind(name);
            currentConfig.setDefault();
        }
    }

    /**
     * Binds a new name to the object bound to an old name, and unbinds the old
     * name.
     * @param oldName the name of the existing binding; may not be empty
     * @param newName the name of the new binding; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void rename(String oldName, String newName) throws NamingException {
        for (Enumeration e = activesInitialsContexts.keys(); e.hasMoreElements();) {
            rmiName = (String) e.nextElement();
            currentConfig.setRMI(rmiName);
            ((Context) activesInitialsContexts.get(rmiName)).rename(oldName, newName);
            currentConfig.setDefault();
        }
    }

    /**
     * Binds a new name to the object bound to an old name, and unbinds the old
     * name. Both names are relative to this context. Any attributes associated
     * with the old name become associated with the new name.
     * @param oldName the name of the existing binding; may not be empty
     * @param newName the name of the new binding; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void rename(Name oldName, Name newName) throws NamingException {
        for (Enumeration e = activesInitialsContexts.keys(); e.hasMoreElements();) {
            rmiName = (String) e.nextElement();
            currentConfig.setRMI(rmiName);
            ((Context) activesInitialsContexts.get(rmiName)).rename(oldName, newName);
            currentConfig.setDefault();
        }
    }

    /**
     * Enumerates the names bound in the named context, along with the class
     * names of objects bound to them.
     * @param name the name of the context to list
     * @return an enumeration of the names and class names of the bindings in
     *         this context. Each element of the enumeration is of type
     *         <tt>NameClassPair</tt>.
     * @throws NamingException if a naming exception is encountered
     */
    public NamingEnumeration list(String name) throws NamingException {
        return currentConfig.getCurrentInitialContext().list(name);
    }

    /**
     * Enumerates the names bound in the named context, along with the class
     * names of objects bound to them. The contents of any subcontexts are not
     * included.
     * @param name the name of the context to list
     * @return an enumeration of the names and class names of the bindings in
     *         this context. Each element of the enumeration is of type
     *         <tt>NameClassPair</tt>.
     * @throws NamingException if a naming exception is encountered
     */
    public NamingEnumeration list(Name name) throws NamingException {
        return currentConfig.getCurrentInitialContext().list(name);
    }

    /**
     * Enumerates the names bound in the named context, along with the objects
     * bound to them.
     * @param name the name of the context to list
     * @return an enumeration of the bindings in this context. Each element of
     *         the enumeration is of type <tt>Binding</tt>.
     * @throws NamingException if a naming exception is encountered
     */
    public NamingEnumeration listBindings(String name) throws NamingException {
        return currentConfig.getCurrentInitialContext().listBindings(name);
    }

    /**
     * Enumerates the names bound in the named context, along with the objects
     * bound to them. The contents of any subcontexts are not included.
     * @param name the name of the context to list
     * @return an enumeration of the bindings in this context. Each element of
     *         the enumeration is of type <tt>Binding</tt>.
     * @throws NamingException if a naming exception is encountered
     */
    public NamingEnumeration listBindings(Name name) throws NamingException {
        return currentConfig.getCurrentInitialContext().listBindings(name);
    }

    /**
     * Destroys the named context and removes it from the namespace.
     * @param name the name of the context to be destroyed; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void destroySubcontext(String name) throws NamingException {
        for (Enumeration e = activesInitialsContexts.keys(); e.hasMoreElements();) {
            rmiName = (String) e.nextElement();
            currentConfig.setRMI(rmiName);
            ((Context) activesInitialsContexts.get(rmiName)).destroySubcontext(name);
            currentConfig.setDefault();
        }
    }

    /**
     * Destroys the named context and removes it from the namespace. Any
     * attributes associated with the name are also removed. Intermediate
     * contexts are not destroyed.
     * @param name the name of the context to be destroyed; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void destroySubcontext(Name name) throws NamingException {
        for (Enumeration e = activesInitialsContexts.keys(); e.hasMoreElements();) {
            rmiName = (String) e.nextElement();
            currentConfig.setRMI(rmiName);
            ((Context) activesInitialsContexts.get(rmiName)).destroySubcontext(name);
            currentConfig.setDefault();
        }
    }

    /**
     * Creates and binds a new context.
     * @param name the name of the context to create; may not be empty
     * @return the newly created context
     * @throws NamingException if a naming exception is encountered
     */
    public Context createSubcontext(String name) throws NamingException {
        return currentConfig.getCurrentInitialContext().createSubcontext(name);
    }

    /**
     * Creates and binds a new context.
     * @param name the name of the context to create; may not be empty
     * @return the newly created context
     * @throws NamingException if a naming exception is encountered
     */
    public Context createSubcontext(Name name) throws NamingException {
        return currentConfig.getCurrentInitialContext().createSubcontext(name);
    }

    /**
     * Retrieves the named object, following links except for the terminal
     * atomic component of the name.
     * @param name the name of the object to look up
     * @return the object bound to <tt>name</tt>, not following the terminal
     *         link (if any)
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookupLink(String name) throws NamingException {
        return currentConfig.getCurrentInitialContext().lookupLink(name);
    }

    /**
     * Retrieves the named object, following links except for the terminal
     * atomic component of the name.
     * @param name the name of the object to look up
     * @return the object bound to <tt>name</tt>, not following the terminal
     *         link (if any).
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookupLink(Name name) throws NamingException {
        return currentConfig.getCurrentInitialContext().lookupLink(name);
    }

    /**
     * Retrieves the parser associated with the named context.
     * @param name the name of the context from which to get the parser
     * @return a name parser that can parse compound names into their atomic
     *         components
     * @throws NamingException if a naming exception is encountered
     */
    public NameParser getNameParser(String name) throws NamingException {
        return currentConfig.getCurrentInitialContext().getNameParser(name);
    }

    /**
     * Retrieves the parser associated with the named context.
     * @param name the name of the context from which to get the parser
     * @return a name parser that can parse compound names into their atomic
     *         components
     * @throws NamingException if a naming exception is encountered
     */
    public NameParser getNameParser(Name name) throws NamingException {
        return currentConfig.getCurrentInitialContext().getNameParser(name);
    }

    /**
     * Composes the name of this context with a name relative to this context.
     * @param name a name relative to this context
     * @param prefix the name of this context relative to one of its ancestors
     * @return the composition of <code>prefix</code> and <code>name</code>
     * @throws NamingException if a naming exception is encountered
     */
    public String composeName(String name, String prefix) throws NamingException {
        return name;
    }

    /**
     * Composes the name of this context with a name relative to this context.
     * @param name a name relative to this context
     * @param prefix the name of this context relative to one of its ancestors
     * @return the composition of <code>prefix</code> and <code>name</code>
     * @throws NamingException if a naming exception is encountered
     */
    public Name composeName(Name name, Name prefix) throws NamingException {
        return currentConfig.getCurrentInitialContext().composeName(name, prefix);
    }

    /**
     * Adds a new environment property to the environment of this context. If
     * the property already exists, its value is overwritten. See class
     * description for more details on environment properties.
     * @param propName the name of the environment property to add; may not be
     *        null
     * @param propVal the value of the property to add; may not be null
     * @return the previous value of the property, or null if the property was
     *         not in the environment before
     * @throws NamingException if a naming exception is encountered
     */
    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        return currentConfig.getCurrentInitialContext().addToEnvironment(propName, propVal);
    }

    /**
     * Removes an environment property from the environment of this context. See
     * class description for more details on environment properties.
     * @param propName the name of the environment property to remove; may not
     *        be null
     * @return the previous value of the property, or null if the property was
     *         not in the environment
     * @throws NamingException if a naming exception is encountered
     */
    public Object removeFromEnvironment(String propName) throws NamingException {
        return currentConfig.getCurrentInitialContext().removeFromEnvironment(propName);
    }

    /**
     * Retrieves the environment in effect for this context. See class
     * description for more details on environment properties.
     * @return the environment of this context; never null
     * @throws NamingException if a naming exception is encountered
     */
    public Hashtable getEnvironment() throws NamingException {
        return currentConfig.getCurrentInitialContext().getEnvironment();
    }

    /**
     * Closes this context. This method releases this context's resources
     * immediately, instead of waiting for them to be released automatically by
     * the garbage collector.
     * @throws NamingException if a naming exception is encountered
     */
    public void close() throws NamingException {
        for (Enumeration e = activesInitialsContexts.keys(); e.hasMoreElements();) {
            rmiName = (String) e.nextElement();
            currentConfig.setRMI(rmiName);
            ((Context) activesInitialsContexts.get(rmiName)).close();
            currentConfig.setDefault();
        }
    }

    /**
     * Retrieves the full name of this context within its own namespace.
     * @return this context's name in its own namespace; never null
     * @throws NamingException if a naming exception is encountered
     */
    public String getNameInNamespace() throws NamingException {
        return currentConfig.getCurrentInitialContext().getNameInNamespace();
    }

}
