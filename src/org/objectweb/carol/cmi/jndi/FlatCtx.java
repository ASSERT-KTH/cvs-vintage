/**
 * Copyright (C) 2002-2005 - Bull S.A.
 *
 * CMI : Cluster Method Invocation
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
 * $Id: FlatCtx.java,v 1.6 2005/07/27 11:49:23 pelletib Exp $
 * --------------------------------------------------------------------------
 */
/*
 * @(#)FlatCtx.java	1.4 99/10/15
 *
 * Copyright 1997, 1998, 1999 Sun Microsystems, Inc. All Rights
 * Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free,
 * license to use, modify and redistribute this software in source and
 * binary code form, provided that i) this copyright notice and license
 * appear on all copies of the software; and ii) Licensee does not
 * utilize the software in a manner which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE
 * HEREBY EXCLUDED.  SUN AND ITS LICENSORS SHALL NOT BE LIABLE
 * FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN
 * NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT
 * OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line
 * control of aircraft, air traffic, aircraft navigation or aircraft
 * communications; or in the design, construction, operation or
 * maintenance of any nuclear facility. Licensee represents and warrants
 * that it will not use or redistribute the Software for such purposes.
 */
package org.objectweb.carol.cmi.jndi;

import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.util.Hashtable;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.spi.NamingManager;

import org.objectweb.carol.cmi.Registry;
import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Class <code> FlatCtx </code> is an implementation of the Context interface
 * @author Simon Nieuviarts
 */
class FlatCtx implements Context {

    /**
     * Environment
     */
    private Hashtable myEnv = null;

    /**
     * Provider
     */
    private String provider = null;

    /**
     * JNDI Parser
     */
    private static NameParser myParser = new FlatNameParser();

    /**
     * Registry
     */
    private Registry reg;

    /**
     * New context creation
     * @param environment environment
     * @throws NamingException context can't be created
     */
    FlatCtx(Hashtable environment) throws NamingException {
        if (environment != null) {
            myEnv = (Hashtable) (environment.clone());
            provider = (String) myEnv.get(Context.PROVIDER_URL);
        }
        if (provider == null) {
            provider = "cmi:";
        }
        org.objectweb.carol.cmi.NamingContext nc;
        try {
            nc = new org.objectweb.carol.cmi.NamingContext(provider);
            reg = org.objectweb.carol.cmi.Naming.getRegistry(nc.hp);
        } catch (java.net.MalformedURLException e) {
            NamingException ex = new NamingException();
            ex.setRootCause(e);
            throw ex;
        } catch (java.rmi.RemoteException e) {
            NamingException ex = new NamingException();
            ex.setRootCause(e);
            throw ex;
        }
    }

    /**
     * Retrieves the named object
     * @param name string to lookup
     * @return object found
     * @throws NamingException context can't be found
     */
    public Object lookup(String name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("lookup(" + name + ")");
        }
        if (name.equals("")) {
            // Asking to look up this context itself.  Create and return
            // a new instance with its own independent environment.
            return (new FlatCtx(myEnv));
        }
        try {
            Object obj = reg.lookup(name);
            if (obj instanceof RemoteReference) {
                return NamingManager.getObjectInstance(
                    ((RemoteReference) obj).getReference(),
                    null,
                    this,
                    this.myEnv);
            }
            if (TraceCarol.isDebugJndiCarol()) {
                TraceCarol.debugJndiCarol("lookup(" + name + ") returned");
            }
            return obj;
        } catch (java.rmi.NotBoundException e) {
            NameNotFoundException ex = new NameNotFoundException();
            ex.setRootCause(e);
            throw ex;
        } catch (Exception e) {
            NamingException ex = new NamingException();
            ex.setRootCause(e);
            throw ex;
        }
    }

    /**
     * Retrieves the named object
     * @param name string to lookup
     * @return object found
     * @throws NamingException context can't be found
     */
    public Object lookup(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return lookup(name.toString());
    }

    /**
     * Add an entry in the registry
     * @param name name to associate with the bound object
     * @param obj added object
     * @throws NamingException object can't be bound
     */
    public void bind(String name, Object obj) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("bind(" + name + ")");
        }
        if (name.equals("")) {
            throw new InvalidNameException("Cannot bind empty name");
        }
        Object o = NamingManager.getStateToBind(obj, null, this, myEnv);
        try {
            //XXX Should support Serializable objects ?
            if (o instanceof java.rmi.Remote) {
                reg.bind(name, (java.rmi.Remote) o);
            } else if (o instanceof Reference) {
                reg.bind(name, new ReferenceImpl((Reference) o));
            } else if (o instanceof Referenceable) {
                reg.bind(
                    name,
                    new ReferenceImpl(((Referenceable) o).getReference()));
            } else {
                throw new NamingException(
                    "object to bind must be Remote : "
                        + obj.getClass().getName());
            }
        } catch (java.rmi.RemoteException e) {
            NamingException ex = new NamingException();
            ex.setRootCause(e);
            throw ex;
        } catch (java.rmi.AlreadyBoundException e) {
            NamingException ex = new NameAlreadyBoundException();
            ex.setRootCause(e);
            throw ex;
        }
    }

    /**
     * Add an entry in the registry
     * @param name name to associate with the bound object
     * @param obj updated object
     * @throws NamingException object can't be bound
     */
    public void bind(Name name, Object obj) throws NamingException {
        // Flat namespace; no federation; just call string version
        bind(name.toString(), obj);
    }

    /**
     * Update an entry in the registry
     * @param name name to update in the registry
     * @param obj added object
     * @throws NamingException object can't be updated
     */
    public void rebind(String name, Object obj) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("rebind(" + name + ")");
        }
        if (name.equals("")) {
            throw new InvalidNameException("Cannot bind empty name");
        }
        Object o = NamingManager.getStateToBind(obj, null, this, myEnv);
        try {
            if (o instanceof java.rmi.Remote) {
                reg.rebind(name, (java.rmi.Remote) o);
            } else if (o instanceof Reference) {
                reg.rebind(name, new ReferenceImpl((Reference) o));
            } else if (o instanceof Referenceable) {
                reg.rebind(
                    name,
                    new ReferenceImpl(((Referenceable) o).getReference()));
            } else {
                throw new NamingException(
                    "object to bind must be Remote : "
                        + obj.getClass().getName());
            }
        } catch (java.rmi.RemoteException e) {
            NamingException ex = new NamingException();
            ex.setRootCause(e);
            throw ex;
        }
    }

    /**
     * Update an entry in the registry
     * @param name name to update in the registry
     * @param obj updated object
     * @throws NamingException object can't be updated
     */
    public void rebind(Name name, Object obj) throws NamingException {
        // Flat namespace; no federation; just call string version
        rebind(name.toString(), obj);
    }

    /**
     * Remove an entry in the registry
     * @param name name to remove
     * @throws NamingException entry can't be removed
     */
    public void unbind(String name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("unbind(" + name + ")");
        }
        if (name.equals("")) {
            throw new InvalidNameException("Cannot unbind empty name");
        }
        try {
            reg.unbind(name);
        } catch (java.rmi.RemoteException e) {
            NamingException ex = new NamingException();
            ex.setRootCause(e);
            throw ex;
        } catch (java.rmi.NotBoundException e) {
            NameNotFoundException ex = new NameNotFoundException();
            ex.setRootCause(e);
            throw ex;
        }
    }

    /**
     * Remove an entry in the registry
     * @param name name to remove
     * @throws NamingException entry can't be removed
     */
    public void unbind(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        unbind(name.toString());
    }

    /**
     * Update a name in the registry
     * @param oldname name to update
     * @param newname new name to update
     * @throws NamingException name can't be updated
     */
    public void rename(String oldname, String newname) throws NamingException {
        throw new NamingException("not supported");
    }

    /**
     * Update a name in the registry
     * @param oldname name to update
     * @param newname new name to update
     * @throws NamingException name can't be updated
     */
   public void rename(Name oldname, Name newname) throws NamingException {
        // Flat namespace; no federation; just call string version
        rename(oldname.toString(), newname.toString());
    }

   /**
    * Enumerates the names bound in the named context, along with the
    * class names of objects bound to them
    * @param name name to search in the registry
    * @return list of [name-class]
    * @throws NamingException name not found
    */
    public NamingEnumeration list(String name) throws NamingException {
        try {
            return new CmiNames(reg.list());
        } catch (AccessException e) {
            NamingException ex = new NamingException();
            ex.setRootCause(e);
            throw ex;
        } catch (RemoteException e) {
            NamingException ex = new NamingException();
            ex.setRootCause(e);
            throw ex;
        }
    }

    /**
    * Enumerates the names bound in the named context, along with the
    * class names of objects bound to them
     * @param name name to search in the registry
     * @return list of [name-class]
     * @throws NamingException name not found
     */
    public NamingEnumeration list(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return list(name.toString());
    }

    /**
     * Enumerates the names bound in the named context, along with the
     * objects bound to them.
     * @param name name to search in the registry
     * @return list of [name-object]
     * @throws NamingException name not found
     */
    public NamingEnumeration listBindings(String name) throws NamingException {
        throw new NamingException("not supported");
    }

    /**
     * Enumerates the names bound in the named context, along with the
     * objects bound to them.
     * @param name name to search in the registry
     * @return list of [name-object]
     * @throws NamingException name not found
     */
     public NamingEnumeration listBindings(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return listBindings(name.toString());
    }

     /**
      * Destroys the named context and removes it from the namespace.
      * @param name entry to destroy
      * @throws NamingException name not found
      */
    public void destroySubcontext(String name) throws NamingException {
        throw new OperationNotSupportedException("FlatCtx does not support subcontexts");
    }

    /**
     * Destroys the named context and removes it from the namespace.
     * @param name entry to destroy
     * @throws NamingException name not found
     */
    public void destroySubcontext(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        destroySubcontext(name.toString());
    }

    /**
     * Creates and binds a new context.
     * @param name name to create
     * @return new context
     * @throws NamingException unable to create the context
     */
     public Context createSubcontext(String name) throws NamingException {
        throw new OperationNotSupportedException("FlatCtx does not support subcontexts");
    }

     /**
      * Creates and binds a new context.
      * @param name name to create
      * @return new context
      * @throws NamingException unable to create the context
      */
    public Context createSubcontext(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return createSubcontext(name.toString());
    }

    /**
     * Retrieves the named object, following links except
     * for the terminal atomic component of the name.
     * @param name name to search in the registry
     * @return object associated with the name
     * @throws NamingException unable to find the object
     */
    public Object lookupLink(String name) throws NamingException {
        // This flat context does not treat links specially
        return lookup(name);
    }

    /**
     * Retrieves the named object, following links except
     * for the terminal atomic component of the name.
     * @param name name to search in the registry
     * @return object associated with the name
     * @throws NamingException unable to find the object
     */
    public Object lookupLink(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return lookupLink(name.toString());
    }

    /**
     * Retrieves the parser associated with the named context.
     * @param name the name of the context from which to get the parser
     * @return  a name parser
     * @throws  NamingException if a naming exception is encountered
     */
    public NameParser getNameParser(String name) throws NamingException {
        return myParser;
    }

    /**
     * Retrieves the parser associated with the named context.
     * @param name the name of the context from which to get the parser
     * @return  a name parser
     * @throws  NamingException if a naming exception is encountered
     */
    public NameParser getNameParser(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return getNameParser(name.toString());
    }

    /**
     * Composes the name of this context with a name relative to
     * this context.
     * @param name a name relative to this context
     * @param prefix the name of this context relative to one of its ancestors
     * @return  the composition of <code>prefix</code> and <code>name</code>
     * @throws  NamingException if a naming exception is encountered
     */
     public String composeName(String name, String prefix)
        throws NamingException {
        Name result =
            composeName(new CompositeName(name), new CompositeName(prefix));
        return result.toString();
    }

     /**
      * Composes the name of this context with a name relative to
      * this context.
      * @param name a name relative to this context
      * @param prefix the name of this context relative to one of its ancestors
      * @return  the composition of <code>prefix</code> and <code>name</code>
      * @throws  NamingException if a naming exception is encountered
      */
    public Name composeName(Name name, Name prefix) throws NamingException {
        Name result = (Name) (prefix.clone());
        result.addAll(name);
        return result;
    }

    /**
     * Adds a new environment property to the environment of this
     * context.
     * @param propName the name of the environment property to add; may not be null
     * @param propVal  the value of the property to add; may not be null
     * @return  the previous value of the property
     * @throws  NamingException if a naming exception is encountered
     */
    public Object addToEnvironment(String propName, Object propVal)
        throws NamingException {
        if (myEnv == null) {
            myEnv = new Hashtable(5, 0.75f);
        }
        return myEnv.put(propName, propVal);
    }

    /**
     * Removes an environment property from the environment of this
     * context.
     * @param propName the name of the environment property to remove
     * @return  the previous value of the property
     * @throws  NamingException if a naming exception is encountered
     */
    public Object removeFromEnvironment(String propName)
        throws NamingException {
        if (myEnv == null) {
            return null;
        }

        return myEnv.remove(propName);
    }

    /**
     * Retrieves the environment in effect for this context.
     * @return  the environment of this context
     * @throws  NamingException if a naming exception is encountered
     */
    public Hashtable getEnvironment() throws NamingException {
        if (myEnv == null) {
            // Must return non-null
            return new Hashtable(3, 0.75f);
        } else {
            return (Hashtable) myEnv.clone();
        }
    }

    /**
     * Retrieves the full name of this context within its own namespace.
     *
     * @return  this context's name in its own namespace
     * @throws  NamingException if a naming exception is encountered
     */
     public String getNameInNamespace() throws NamingException {
        return "";
    }

     /**
      * Closes this context.
      * @throws NamingException if a naming exception is encountered
      */
    public void close() throws NamingException {
        myEnv = null;
        reg = null;
    }

    /**
     * Class for enumerating name/class pairs
     */
    class CmiNames implements NamingEnumeration {

        /**
         * names list
         */
        String[] names;

        /**
         * index
         */
        int index = 0;

        /**
         * constructs an enumeration list for an initial names list
         * @param names names list
         */
        CmiNames (String[] names) {
            this.names = names;
        }

        /**
         * test if the list contains some elements
         * @return true if the list is not empty
         */
        public boolean hasMoreElements() {
            return index < names.length;
        }

        /**
         * test if the list contains some elements
         * @return true if the list is not empty
         * @throws NamingException if a NamingException is encountered
         */
        public boolean hasMore() throws NamingException {
            return hasMoreElements();
        }

        /**
         * get the next element
         * @return NameClassPair element
         */
        public Object nextElement() {
            String name = names[index++];
            String className = "java.lang.Object";
            return new NameClassPair(name, className);
        }

        /**
         * get the next element
         * @return NameClassPair element
         * @throws NamingException if a NamingException is encountered
         */
        public Object next() throws NamingException {
            return nextElement();
        }

        /**
         * close the list
         * @throws NamingException if a NamingException is encountered
         */
        public void close() throws NamingException {
            names=null;
        }
    }
}
