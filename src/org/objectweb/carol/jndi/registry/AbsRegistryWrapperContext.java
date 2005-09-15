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
 * $Id: AbsRegistryWrapperContext.java,v 1.1 2005/09/15 13:04:16 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.registry;


import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Hashtable;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.objectweb.carol.rmi.exception.NamingExceptionHelper;
import org.objectweb.carol.util.naming.LocalEnumeration;
import org.objectweb.carol.util.naming.SimpleNameParser;

/**
 * Wrapper on a Registry object and implementing Context
 * @author Florent Benoit (Refactoring)
 */
public abstract class AbsRegistryWrapperContext implements Context {

    /**
     * LocalRegistry for bindings and lookup
     */
    private Registry registry;

    /**
     * the JRMP JNDI environment
     */
    private static Hashtable environment = null;


    /**
     * Simple name parser
     */
    private static final NameParser NAME_PARSER = new SimpleNameParser();


    /**
     * Create a local context for the registry
     * @param env hashtable used
     * @param registry the registry to wrap
     * @param initialContextFactory the name of the initialContext factory to use
     */
    public AbsRegistryWrapperContext(Hashtable env, Registry registry, String initialContextFactory) {
        this.registry = registry;
        environment = env;
        environment.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
    }

    /**
     * Retrieves the named object.
     * @param name the name of the object to look up
     * @return the object bound to <tt>name</tt>
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookup(Name name) throws NamingException {
        if (name.isEmpty()) {
            return this;
        }
        Remote obj;
        try {
            obj = registry.lookup(name.get(0));
        } catch (NotBoundException e) {
            NameNotFoundException nnfe = new NameNotFoundException(name.get(0));
            nnfe.setRootCause(e);
            throw nnfe;
        } catch (Exception e) {
            throw NamingExceptionHelper.create("Cannot lookup name '" + name + "' : " + e.getMessage(), e);
        }
        return obj;
    }

    /**
     * Retrieves the named object.
     * @param name the name of the object to look up
     * @return the object bound to <tt>name</tt>
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookup(String name) throws NamingException {
        return lookup(new CompositeName(name));
    }

    /**
     * Binds a name to an object.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     */
    public void bind(Name name, Object obj) throws NamingException {
        if (name.isEmpty()) {
            throw new NamingException("Cannot bind empty name");
        }

        if (!(obj instanceof Remote)) {
            throw new NamingException(
                    "Can only bind object which implements Remote interface. This is not the case for object '" + obj
                            + "' with name '" + name + "'.");
        }

        try {
            registry.bind(name.get(0), (Remote) obj);
        } catch (AlreadyBoundException e) {
            NamingException ne = new NameAlreadyBoundException(name.get(0));
            ne.setRootCause(e);
            throw ne;
        } catch (Exception e) {
            NamingException ne = new NamingException();
            ne.setRootCause(e);
            throw ne;
        }
    }

    /**
     * Binds a name to an object.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     */
    public void bind(String name, Object obj) throws NamingException {
        bind(new CompositeName(name), obj);
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
        if (name.isEmpty()) {
            throw new NamingException("Cannot rebind empty name");
        }
        if (!(obj instanceof Remote)) {
            throw new NamingException(
                    "Can only rebind object which implements Remote interface. This is not the case for object '" + obj
                            + "' with name '" + name + "'.");
        }

        try {
            registry.rebind(name.get(0), (Remote) obj);
        } catch (Exception e) {
            NamingException ne = new NamingException();
            ne.setRootCause(e);
            throw ne;
        }
    }

    /**
     * Binds a name to an object, overwriting any existing binding.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     */
    public void rebind(String name, Object obj) throws NamingException {
        rebind(new CompositeName(name), obj);
    }

    /**
     * Unbinds the named object. Removes the terminal atomic name in
     * <code>name</code> from the target context--that named by all but the
     * terminal atomic part of <code>name</code>.
     * @param name the name to unbind; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void unbind(Name name) throws NamingException {
        if (name.isEmpty()) {
            throw new NamingException("Cannot unbind empty name");
        }
        try {
            registry.unbind(name.get(0));
        } catch (Exception e) {
            NamingException ne = new NamingException();
            ne.setRootCause(e);
            throw ne;
        }
    }

    /**
     * Unbinds the named object.
     * @param name the name to unbind; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void unbind(String name) throws NamingException {
        unbind(new CompositeName(name));
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
        bind(newName, lookup(oldName));
        unbind(oldName);
    }

    /**
     * Binds a new name to the object bound to an old name, and unbinds the old
     * name.
     * @param oldName the name of the existing binding; may not be empty
     * @param newName the name of the new binding; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void rename(String oldName, String newName) throws NamingException {
        rename(new CompositeName(oldName), new CompositeName(newName));
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
        if (!name.isEmpty()) {
            throw new NamingException("Cannot list with a given empty name");
        }
        try {
            String[] names = registry.list();
            return new LocalEnumeration(this, names);
        } catch (Exception e) {
            NamingException ne = new NamingException();
            ne.setRootCause(e);
            throw ne;
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
        return list(new CompositeName(name));
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
        if (!name.isEmpty()) {
            throw new NamingException("can not list");
        }
        try {
            String[] names = registry.list();
            return new LocalEnumeration(this, names);
        } catch (RemoteException e) {
            NamingException ne = new NamingException();
            ne.setRootCause(e);
            throw ne;
        }
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
        return listBindings(new CompositeName(name));
    }

    /**
     * Destroys the named context and removes it from the namespace. Any
     * attributes associated with the name are also removed. Intermediate
     * contexts are not destroyed.
     * @param name the name of the context to be destroyed; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void destroySubcontext(Name name) throws NamingException {
        throw new NamingException("destroySubcontext() method not implemented");
    }

    /**
     * Destroys the named context and removes it from the namespace.
     * @param name the name of the context to be destroyed; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void destroySubcontext(String name) throws NamingException {
        destroySubcontext(new CompositeName(name));
    }

    /**
     * Creates and binds a new context.
     * @param name the name of the context to create; may not be empty
     * @return the newly created context
     * @throws NamingException if a naming exception is encountered
     */
    public Context createSubcontext(Name name) throws NamingException {
        throw new NamingException("createSubcontext() method not implemented");
    }

    /**
     * Creates and binds a new context.
     * @param name the name of the context to create; may not be empty
     * @return the newly created context
     * @throws NamingException if a naming exception is encountered
     */
    public Context createSubcontext(String name) throws NamingException {
        return createSubcontext(new CompositeName(name));
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
        return lookup(name);
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
        return lookup(name);
    }

    /**
     * Retrieves the parser associated with the named context.
     * @param name the name of the context from which to get the parser
     * @return a name parser that can parse compound names into their atomic
     *         components
     * @throws NamingException if a naming exception is encountered
     */
    public NameParser getNameParser(Name name) throws NamingException {
        return NAME_PARSER;
    }

    /**
     * Retrieves the parser associated with the named context.
     * @param name the name of the context from which to get the parser
     * @return a name parser that can parse compound names into their atomic
     *         components
     * @throws NamingException if a naming exception is encountered
     */
    public NameParser getNameParser(String name) throws NamingException {
        return NAME_PARSER;
    }

    /**
     * Composes the name of this context with a name relative to this context.
     * @param name a name relative to this context
     * @param prefix the name of this context relative to one of its ancestors
     * @return the composition of <code>prefix</code> and <code>name</code>
     * @throws NamingException if a naming exception is encountered
     */
    public Name composeName(Name name, Name prefix) throws NamingException {
        Name result = (Name) prefix.clone();
        return result.addAll(name);
    }

    /**
     * Composes the name of this context with a name relative to this context.
     * @param name a name relative to this context
     * @param prefix the name of this context relative to one of its ancestors
     * @return the composition of <code>prefix</code> and <code>name</code>
     * @throws NamingException if a naming exception is encountered
     */
    public String composeName(String name, String prefix) throws NamingException {
        return composeName(new CompositeName(name), new CompositeName(prefix)).toString();
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
        return environment.put(propName, propVal);
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
        return environment.remove(propName);
    }

    /**
     * Retrieves the environment in effect for this context. See class
     * description for more details on environment properties.
     * @return the environment of this context; never null
     * @throws NamingException if a naming exception is encountered
     */
    public Hashtable getEnvironment() throws NamingException {
        return (Hashtable) environment.clone();
    }

    /**
     * Closes this context. This method releases this context's resources
     * immediately, instead of waiting for them to be released automatically by
     * the garbage collector.
     */
    public void close() {
    }

    /**
     * Retrieves the full name of this context within its own namespace.
     * @return this context's name in its own namespace; never null
     */
    public String getNameInNamespace() {
        return "";
    }

}