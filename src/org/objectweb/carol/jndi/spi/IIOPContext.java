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
 * $Id: IIOPContext.java,v 1.5 2004/09/01 11:02:41 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.spi;

import java.io.Serializable;
import java.rmi.Remote;
import java.util.HashMap;
import java.util.Hashtable;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.spi.ObjectFactory;

import org.objectweb.carol.jndi.wrapping.JNDIReferenceWrapper;
import org.objectweb.carol.jndi.wrapping.JNDIRemoteResource;
import org.objectweb.carol.jndi.wrapping.JNDIResourceWrapper;
import org.objectweb.carol.util.configuration.CarolCurrentConfiguration;

import com.sun.jndi.rmi.registry.RemoteReference;

/**
 * @author riviereg To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class IIOPContext implements Context {

    /**
     * the IIOP JNDI context
     * @see #IIOPContext
     */
    private static Context iiopContext = null;

    /**
     * the IIOP Wrapper JNDI context
     * @see #IIOPContext
     */

    private static HashMap hashMap = new HashMap();

    /**
     * the Exported Wrapper Hashtable
     */
    private static Hashtable wrapperHash = new Hashtable();

    /**
     * Constructs an IIOP Wrapper context
     * @param IIOPContext the inital IIOP context
     * @throws NamingException if a naming exception is encountered
     */
    private IIOPContext(Context iiopCtx) throws NamingException {
        iiopContext = iiopCtx;

    }

    /**
     * @param o
     * @param name
     * @return @throws NamingException
     */
    public static Context getSingleInstance(Hashtable env) throws NamingException {
        String key = null;
        if (env != null) {
            key = (String) env.get(Context.PROVIDER_URL);
        }
        Context ctx = (Context) hashMap.get(key);
        if (ctx == null) {
            env.put("java.naming.factory.initial", "com.sun.jndi.cosnaming.CNCtxFactory");
            ctx = new IIOPContext(new InitialContext(env));
            hashMap.put(key, ctx);
        }
        return ctx;
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
                return objFact.getObjectInstance(objRef, name, this, iiopContext.getEnvironment());
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
                JNDIReferenceWrapper irw = new JNDIReferenceWrapper(((Referenceable) o).getReference());
                CarolCurrentConfiguration.getCurrent().getCurrentPortableRemoteObject().exportObject(irw);
                Remote oldObj = (Remote) wrapperHash.put(name, irw);
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
            } else if ((!(o instanceof Remote)) && (o instanceof Reference)) {
                JNDIReferenceWrapper irw = new JNDIReferenceWrapper((Reference) o);
                CarolCurrentConfiguration.getCurrent().getCurrentPortableRemoteObject().exportObject(irw);
                Remote oldObj = (Remote) wrapperHash.put(name, irw);
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
            } else if ((!(o instanceof Remote)) && (o instanceof Serializable)) {
                JNDIResourceWrapper irw = new JNDIResourceWrapper((Serializable) o);
                CarolCurrentConfiguration.getCurrent().getCurrentPortableRemoteObject().exportObject(irw);
                Remote oldObj = (Remote) wrapperHash.put(name, irw);
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

    // Context methods
    // The Javadoc is deferred to the Context interface.

    public Object lookup(Name name) throws NamingException {
        return unwrapObject(iiopContext.lookup(name), name);
    }

    public Object lookup(String name) throws NamingException {
        return lookup(new CompositeName(name));
    }

    public void bind(Name name, Object obj) throws NamingException {
        iiopContext.bind(name, wrapObject(obj, name, false));
    }

    public void bind(String name, Object obj) throws NamingException {
        bind(new CompositeName(name), obj);
    }

    public void rebind(Name name, Object obj) throws NamingException {
        iiopContext.rebind(name, wrapObject(obj, name, true));
    }

    public void rebind(String name, Object obj) throws NamingException {
        rebind(new CompositeName(name), obj);
    }

    public void unbind(Name name) throws NamingException {
        try {
            iiopContext.unbind(name);
            if (wrapperHash.containsKey(name)) {
                CarolCurrentConfiguration.getCurrent().getCurrentPortableRemoteObject().unexportObject(
                        (Remote) wrapperHash.remove(name));
            }
        } catch (Exception e) {
            throw new NamingException("" + e);
        }
    }

    public void unbind(String name) throws NamingException {
        unbind(new CompositeName(name));
    }

    public void rename(Name oldName, Name newName) throws NamingException {
        if (wrapperHash.containsKey(oldName)) {
            wrapperHash.put(newName, wrapperHash.remove(oldName));
        }
        iiopContext.rename(oldName, newName);
    }

    public void rename(String name, String newName) throws NamingException {
        rename(new CompositeName(name), new CompositeName(newName));
    }

    public NamingEnumeration list(Name name) throws NamingException {
        return iiopContext.list(name);
    }

    public NamingEnumeration list(String name) throws NamingException {
        return list(new CompositeName(name));
    }

    public NamingEnumeration listBindings(Name name) throws NamingException {
        return iiopContext.listBindings(name);
    }

    public NamingEnumeration listBindings(String name) throws NamingException {
        return listBindings(new CompositeName(name));
    }

    public void destroySubcontext(Name name) throws NamingException {
        iiopContext.destroySubcontext(name);
    }

    public void destroySubcontext(String name) throws NamingException {
        destroySubcontext(new CompositeName(name));
    }

    public Context createSubcontext(Name name) throws NamingException {
        return iiopContext.createSubcontext(name);
    }

    public Context createSubcontext(String name) throws NamingException {
        return createSubcontext(new CompositeName(name));
    }

    public Object lookupLink(Name name) throws NamingException {
        return iiopContext.lookupLink(name);
    }

    public Object lookupLink(String name) throws NamingException {
        return lookupLink(new CompositeName(name));
    }

    public NameParser getNameParser(Name name) throws NamingException {
        return iiopContext.getNameParser(name);
    }

    public NameParser getNameParser(String name) throws NamingException {
        return getNameParser(new CompositeName(name));
    }

    public String composeName(String name, String prefix) throws NamingException {
        return name;
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        return (Name) name.clone();
    }

    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        return iiopContext.addToEnvironment(propName, propVal);
    }

    public Object removeFromEnvironment(String propName) throws NamingException {
        return iiopContext.removeFromEnvironment(propName);
    }

    public Hashtable getEnvironment() throws NamingException {
        return iiopContext.getEnvironment();
    }

    public void close() throws NamingException {
        // do nothing for the moment
    }

    public String getNameInNamespace() throws NamingException {
        return iiopContext.getNameInNamespace();
    }

}