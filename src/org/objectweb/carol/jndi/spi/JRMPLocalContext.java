/**
 * Copyright (C) 2002,2004 - INRIA (www.inria.fr)
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
 * $Id: JRMPLocalContext.java,v 1.6 2004/09/01 11:02:41 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.spi;

import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.CompoundName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.spi.ObjectFactory;

import org.objectweb.carol.jndi.wrapping.JNDIRemoteResource;
import org.objectweb.carol.jndi.wrapping.JNDIResourceWrapper;
import org.objectweb.carol.util.configuration.CarolCurrentConfiguration;

import com.sun.jndi.rmi.registry.ReferenceWrapper;
import com.sun.jndi.rmi.registry.RemoteReference;

/**
 * @author riviereg To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class JRMPLocalContext implements Context {

    /**
     * LocalRegistry for bindings and lookup
     */
    private Registry registry;

    /**
     * the JRMP JNDI environment
     * @see #JRMPLocalContext
     */
    private static Hashtable environment = null;

    /**
     * Simple name parser
     */
    private static final NameParser nameParser = new SimpleNameParser();

    /**
     * The Local JRMP Wrapper JNDI context
     * @see #JRMPLocalContext
     */
    private static Context single = null;

    /**
     * The Exported Wrapper Hashtable
     */
    private static Hashtable wrapperHash = null;

    /**
     * Create a local context for the registry
     */
    private JRMPLocalContext(Registry reg, Hashtable env) throws NamingException {
        registry = reg;
        wrapperHash = new Hashtable();
        environment = env;
        environment.put("java.naming.factory.initial", "org.objectweb.carol.jndi.spi.JRMPContextWrapperFactory");
    }

    /**
     * @param o
     * @param name
     * @return @throws NamingException
     */
    public static Context getSingleInstance(Registry reg, Hashtable env) throws NamingException {
        if (single == null) {
            single = new JRMPLocalContext(reg, env);
        }
        return single;
    }

    /**
     * If this object is a reference wrapper return the reference If this object
     * is a resource wrapper return the resource
     * @param o the object to resolve
     * @return the unwrapped object
     */
    private Object unwrapObject(Object o, Name name) throws NamingException {
        try {
            //TODO: May we can do a narrow ?
            if (o instanceof RemoteReference) {
                // build of the Referenceable object with is Reference
                Reference objRef = ((RemoteReference) o).getReference();
                ObjectFactory objFact = (ObjectFactory) (Class.forName(objRef.getFactoryClassName())).newInstance();
                return objFact.getObjectInstance(objRef, name, this, environment);
            } else if (o instanceof JNDIRemoteResource) {
                return ((JNDIRemoteResource) o).getResource();
            } else {
                return o;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NamingException("" + e);
        }
    }

    /**
     * Wrap an Object : If the object is a reference wrap it into a Reference
     * Wrapper Object here the good way is to contact the carol configuration to
     * get the portable remote object
     * @param o the object to encode
     * @return a <code>Remote JNDIRemoteReference Object</code> if o is a
     *         ressource o if else
     */
    private Object wrapObject(Object o, Name name, boolean replace) throws NamingException {
        try {
            if ((!(o instanceof Remote)) && (o instanceof Referenceable)) {
                return new ReferenceWrapper(((Referenceable) o).getReference());
            } else if ((!(o instanceof Remote)) && (o instanceof Reference)) {
                return new ReferenceWrapper((Reference) o);
            } else if ((!(o instanceof Remote)) && (o instanceof Serializable)) {
                JNDIResourceWrapper irw = new JNDIResourceWrapper((Serializable) o);
                CarolCurrentConfiguration.getCurrent().getCurrentPortableRemoteObject().exportObject(irw);
                JNDIResourceWrapper oldObj = (JNDIResourceWrapper) wrapperHash.put(name, irw);
                if (oldObj != null) {
                    if (replace) {
                        CarolCurrentConfiguration.getCurrent().getCurrentPortableRemoteObject().unexportObject(oldObj);
                    } else {
                        CarolCurrentConfiguration.getCurrent().getCurrentPortableRemoteObject().unexportObject(irw);
                        wrapperHash.put(name, oldObj);
                        throw new NamingException("Object already bind");
                    }
                }
                return irw;
            } else {
                return o;
            }
        } catch (Exception e) {
            throw new NamingException("" + e);
        }
    }

    // context methods
    public Object lookup(Name name) throws NamingException {
        if (name.isEmpty()) {
            return this;
        }
        Remote obj;
        try {
            obj = registry.lookup(name.get(0));
        } catch (NotBoundException e) {
            throw (new NameNotFoundException(name.get(0)));
        } catch (Exception e) {
            NamingException ne = new NamingException();
            ne.setRootCause(e);
            throw ne;
        }
        return (unwrapObject(obj, name));
    }

    public Object lookup(String name) throws NamingException {
        return lookup(new CompositeName(name));
    }

    /**
     * If the object to be bound is both Remote and Referenceable, binds the
     * object itself, not its Reference.
     */
    public void bind(Name name, Object obj) throws NamingException {
        if (name.isEmpty()) {
            throw new NamingException("Cannot bind empty name");
        }
        try {
            registry.bind(name.get(0), (Remote) wrapObject(obj, name, false));
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

    public void bind(String name, Object obj) throws NamingException {
        bind(new CompositeName(name), obj);
    }

    public void rebind(Name name, Object obj) throws NamingException {
        if (name.isEmpty()) {
            throw new NamingException("Cannot rebind empty name");
        }
        try {
            registry.rebind(name.get(0), (Remote) wrapObject(obj, name, true));
        } catch (Exception e) {
            NamingException ne = new NamingException();
            ne.setRootCause(e);
            throw ne;
        }
    }

    public void rebind(String name, Object obj) throws NamingException {
        rebind(new CompositeName(name), obj);
    }

    public void unbind(Name name) throws NamingException {
        if (name.isEmpty()) {
            throw new NamingException("Cannot unbind empty name");
        }
        try {
            registry.unbind(name.get(0));
            if (wrapperHash.containsKey(name)) {
                CarolCurrentConfiguration.getCurrent().getCurrentPortableRemoteObject().unexportObject(
                        (Remote) wrapperHash.remove(name));
            }
        } catch (Exception e) {
            NamingException ne = new NamingException();
            ne.setRootCause(e);
            throw ne;
        }
    }

    public void unbind(String name) throws NamingException {
        unbind(new CompositeName(name));
    }

    public void rename(Name oldName, Name newName) throws NamingException {
        if (wrapperHash.containsKey(oldName)) {
            wrapperHash.put(newName, wrapperHash.remove(oldName));
        }
        bind(newName, lookup(oldName));
        unbind(oldName);
    }

    public void rename(String name, String newName) throws NamingException {
        rename(new CompositeName(name), new CompositeName(newName));
    }

    public NamingEnumeration list(Name name) throws NamingException {
        if (!name.isEmpty()) {
            throw new NamingException("can not list");
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

    public NamingEnumeration list(String name) throws NamingException {
        return list(new CompositeName(name));
    }

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

    public NamingEnumeration listBindings(String name) throws NamingException {
        return listBindings(new CompositeName(name));
    }

    public void destroySubcontext(Name name) throws NamingException {
        throw new NamingException("local operation not implemented");
    }

    public void destroySubcontext(String name) throws NamingException {
        throw new NamingException("local operation not implemented");
    }

    public Context createSubcontext(Name name) throws NamingException {
        throw new NamingException("local operation not implemented");
    }

    public Context createSubcontext(String name) throws NamingException {
        throw new NamingException("local operation not implemented");
    }

    public Object lookupLink(Name name) throws NamingException {
        return lookup(name);
    }

    public Object lookupLink(String name) throws NamingException {
        return lookup(name);
    }

    public NameParser getNameParser(Name name) throws NamingException {
        return nameParser;
    }

    public NameParser getNameParser(String name) throws NamingException {
        return nameParser;
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        Name result = (Name) prefix.clone();
        return result.addAll(name);
    }

    public String composeName(String name, String prefix) throws NamingException {
        return composeName(new CompositeName(name), new CompositeName(prefix)).toString();
    }

    public Object removeFromEnvironment(String propName) throws NamingException {
        return environment.remove(propName);
    }

    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        return environment.put(propName, propVal);
    }

    public Hashtable getEnvironment() throws NamingException {
        return (Hashtable) environment.clone();
    }

    public void close() {
    }

    public String getNameInNamespace() {
        return "";
    }

}

/**
 * A very simple Compound Name parser
 */

class SimpleNameParser implements NameParser {

    private static final Properties syntax = new Properties();

    public Name parse(String name) throws NamingException {
        return (new CompoundName(name, syntax));
    }
}

/**
 * Local enumaration for local context
 */

class LocalEnumeration implements NamingEnumeration {

    private Context localContext;

    private final String[] names;

    private int nextName;

    LocalEnumeration(Context ctx, String[] names) {
        this.localContext = ctx;
        this.names = names;
        nextName = 0;
    }

    public boolean hasMore() {
        return (nextName < names.length);
    }

    public Object next() throws NamingException {
        if (!hasMore()) {
            throw (new java.util.NoSuchElementException());
        }
        String name = names[nextName++];
        Name cname = (new CompositeName()).add(name);

        Object obj = localContext.lookup(cname);
        return (new Binding(cname.toString(), obj));
    }

    public boolean hasMoreElements() {
        return hasMore();
    }

    public Object nextElement() {
        try {
            return next();
        } catch (NamingException e) {
            throw new java.util.NoSuchElementException(e.toString());
        }
    }

    public void close() {
    }

}