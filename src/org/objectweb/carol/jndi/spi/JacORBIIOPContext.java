/**
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 2004,2005 Bull S.A.
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
 * $Id: JacORBIIOPContext.java,v 1.5 2005/03/03 16:23:46 benoitf Exp $
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
import javax.rmi.PortableRemoteObject;
import javax.rmi.CORBA.Util;

import org.omg.CORBA.ORB;
import org.omg.CORBA.portable.ObjectImpl;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;

import org.objectweb.carol.jndi.ns.JacORBCosNaming;
import org.objectweb.carol.jndi.wrapping.JNDIReferenceWrapper;
import org.objectweb.carol.jndi.wrapping.JNDIRemoteResource;
import org.objectweb.carol.jndi.wrapping.JNDIResourceWrapper;
import org.objectweb.carol.util.configuration.CarolCurrentConfiguration;
import org.objectweb.carol.util.configuration.TraceCarol;
import org.objectweb.carol.util.csiv2.SasComponent;
import org.objectweb.carol.util.csiv2.SasPolicy;

import com.sun.jndi.rmi.registry.RemoteReference;

/**
 * @author Florent Benoit
 */
public class JacORBIIOPContext implements Context {

    /**
     * Object to use (specific POA) when using csiv2
     */
    public static final String SAS_COMPONENT = "org.objectweb.carol.util.csiv2.SasComponent";

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
     * Number of POA policies
     */
    private static final int POA_POLICIES_NUMBER = 3;

    /**
     * Root POA used by Carol
     */
    private static POA rootPOA = null;

    /**
     * Unique instance of the ORB running in the JVM
     */
    private static ORB orb = null;

    /**
     * The orb was started or not ?
     */
    private static boolean orbStarted = false;

    /**
     * SAS parameters (use for policy)
     */
    private SasComponent sasComponent = null;

    /**
     * Constructs an IIOP Wrapper context for JacORB
     * @param iiopCtx the inital IIOP context
     * @param sasComponent object containing csiv2 information
     */
    private JacORBIIOPContext(Context iiopCtx, SasComponent sasComponent) {
        iiopContext = iiopCtx;
        this.sasComponent = sasComponent;
    }

    /**
     * @return the rootPOA.
     */
    public static POA getRootPOA() {
        return rootPOA;
    }

    /**
     * @param env the Environment for the initial context
     * @return the IIOP context for JacORB
     * @throws NamingException if the instance cannot be get
     */
    public static Context getSingleInstance(Hashtable env) throws NamingException {
        String key = null;
        SasComponent envSasComponent = null;
        if (env != null) {
            key = (String) env.get(Context.PROVIDER_URL);
            envSasComponent = (SasComponent) env.get(SAS_COMPONENT);
        }

        orb = JacORBCosNaming.getOrb();
        if (rootPOA == null) {
            try {
                rootPOA = org.omg.PortableServer.POAHelper.narrow(JacORBCosNaming.getOrb()
                        .resolve_initial_references("RootPOA"));
                rootPOA.the_POAManager().activate();
            } catch (Exception e) {
                throw new NamingException("Cannot get a single instance" + e.getMessage());
            }
        }
        if (!orbStarted) {
            // Start it
            new Thread(new Runnable() {

                public void run() {
                    JacORBCosNaming.getOrb().run();
                }
            }).start();
            orbStarted = true;

        }

        // Look in cache only if there are no specific policies
        Context ctx = null;
        if (envSasComponent == null) {
            ctx = (Context) hashMap.get(key);
        }
        if (ctx == null) {
            if (orb != null) {
                env.put("java.naming.corba.orb", orb);
            }
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory");
            ctx = new JacORBIIOPContext(new InitialContext(env), envSasComponent);
            // Add in cache in no specific policies
            if (envSasComponent == null) {
                hashMap.put(key, ctx);
            }
        }
        return ctx;
    }

    /**
     * If this object is a reference wrapper return the reference If this object
     * is a resource wrapper return the resource
     * @param o the object to resolve
     * @param name name of the object to unwrap
     * @return the unwrapped object
     * @throws NamingException if the object cannot be unwraped
     */
    private Object unwrapObject(Object o, Name name) throws NamingException {
        try {
            // Is it a remote reference object ?
            ObjectImpl objImpl = (ObjectImpl) PortableRemoteObject.narrow(o, ObjectImpl.class);
            String[] ids = objImpl._ids();
            // first item
            String itf = ids[0];

            if (itf.indexOf(":com.sun.jndi.rmi.registry.RemoteReference:") != -1) {
                // build of the Referenceable object with is Reference
                Reference objRef = ((RemoteReference) PortableRemoteObject.narrow(o, RemoteReference.class))
                        .getReference();
                ObjectFactory objFact = (ObjectFactory) (Class.forName(objRef.getFactoryClassName())).newInstance();
                return objFact.getObjectInstance(objRef, name, this, iiopContext.getEnvironment());
            } else if (itf.indexOf("RMI:org.objectweb.carol.jndi.wrapping.JNDIRemoteResource:") != -1) {
                // Cast
                JNDIRemoteResource jndiRemoteResource = (JNDIRemoteResource) PortableRemoteObject.narrow(o,
                        JNDIRemoteResource.class);
                return jndiRemoteResource.getResource();
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
     *         resource o if else
     * @throws NamingException if object cannot be wrapped
     */
    private Remote wrapObject(Object o) throws NamingException {
        try {
            if ((!(o instanceof Remote)) && (o instanceof Referenceable)) {
                JNDIReferenceWrapper irw = new JNDIReferenceWrapper(((Referenceable) o).getReference());
                CarolCurrentConfiguration.getCurrent().getCurrentPortableRemoteObject().exportObject(irw);
                return irw;
            } else if ((!(o instanceof Remote)) && (o instanceof Reference)) {
                JNDIReferenceWrapper irw = new JNDIReferenceWrapper((Reference) o);
                CarolCurrentConfiguration.getCurrent().getCurrentPortableRemoteObject().exportObject(irw);
                return irw;
            } else if ((!(o instanceof Remote)) && (o instanceof Serializable)) {
                JNDIResourceWrapper irw = new JNDIResourceWrapper((Serializable) o);
                CarolCurrentConfiguration.getCurrent().getCurrentPortableRemoteObject().exportObject(irw);
                return irw;
            } else {
                return (Remote) o;
            }
        } catch (Exception e) {
            throw new NamingException("" + e);
        }
    }

    /**
     * Retrieves the named object.
     * @param name the name of the object to look up
     * @return the object bound to <tt>name</tt>
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookup(Name name) throws NamingException {
        return unwrapObject(iiopContext.lookup(name), name);
    }

    /**
     * Retrieves the named object.
     * @param name the name of the object to look up
     * @return the object bound to <tt>name</tt>
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookup(String name) throws NamingException {
        try {
            return lookup(new CompositeName(name));
        } catch (Exception e) {
            // Seems that message with JacORB is null many times
            // So to avoid 'null' as message, use toString() method.
            // Also, print the stacktrace with traces enabled.
            String msg = e.getMessage();
            if (msg == null) {
                msg = e.toString();
            }
            if (TraceCarol.isDebugJndiCarol()) {
                e.printStackTrace();
            }
            throw new NamingException("Cannot lookup object : '" + msg + "'");
        }
    }

    /**
     * Binds a name to an object.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     */
    public void bind(Name name, Object obj) throws NamingException {
        Remote r = wrapObject(obj);
        try {
            if (sasComponent != null) {
                bindWithSpecificPoa(name, r);
            } else {
                iiopContext.bind(name, r);
            }
        } catch (Exception e) {
            throw new NamingException("Cannot bind :" + e.getMessage());
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
        Remote r = wrapObject(obj);
        try {
            if (sasComponent != null) {
                rebindWithSpecificPoa(name, r);
            } else {
                iiopContext.rebind(name, r);
            }
        } catch (Exception e) {
            throw new NamingException("Cannot bind :" + e.getMessage());
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
        try {
            iiopContext.unbind(name);
        } catch (Exception e) {
            throw new NamingException("" + e);
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
        iiopContext.rename(oldName, newName);
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
        return iiopContext.list(name);
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
        return iiopContext.listBindings(name);
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
        iiopContext.destroySubcontext(name);
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
        return iiopContext.createSubcontext(name);
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
        return iiopContext.lookupLink(name);
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
        return lookupLink(new CompositeName(name));
    }

    /**
     * Retrieves the parser associated with the named context.
     * @param name the name of the context from which to get the parser
     * @return a name parser that can parse compound names into their atomic
     *         components
     * @throws NamingException if a naming exception is encountered
     */
    public NameParser getNameParser(Name name) throws NamingException {
        return iiopContext.getNameParser(name);
    }

    /**
     * Retrieves the parser associated with the named context.
     * @param name the name of the context from which to get the parser
     * @return a name parser that can parse compound names into their atomic
     *         components
     * @throws NamingException if a naming exception is encountered
     */
    public NameParser getNameParser(String name) throws NamingException {
        return getNameParser(new CompositeName(name));
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
        return (Name) name.clone();
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
        return iiopContext.addToEnvironment(propName, propVal);
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
        return iiopContext.removeFromEnvironment(propName);
    }

    /**
     * Retrieves the environment in effect for this context. See class
     * description for more details on environment properties.
     * @return the environment of this context; never null
     * @throws NamingException if a naming exception is encountered
     */
    public Hashtable getEnvironment() throws NamingException {
        return iiopContext.getEnvironment();
    }

    /**
     * Closes this context. This method releases this context's resources
     * immediately, instead of waiting for them to be released automatically by
     * the garbage collector.
     * @throws NamingException if a naming exception is encountered
     */
    public void close() throws NamingException {
        // do nothing for the moment
    }

    /**
     * Retrieves the full name of this context within its own namespace.
     * @return this context's name in its own namespace; never null
     * @throws NamingException if a naming exception is encountered
     */
    public String getNameInNamespace() throws NamingException {
        return iiopContext.getNameInNamespace();
    }

    /**
     * @return the orb.
     */
    public static ORB getOrb() {
        return orb;
    }

    /**
     * Rebind an object by using a secure POA (csiv2)
     * @param name name of the object
     * @param r remote object to bind
     * @throws Exception if the object cannot be bound
     */
    private void rebindWithSpecificPoa(Name name, Remote r) throws Exception {
        POA securedPOA = createSecurePOA(name.toString());
        org.omg.PortableServer.Servant servant = (org.omg.PortableServer.Servant) Util.getTie(r);
        securedPOA.activate_object_with_id(name.toString().getBytes(), servant);
        iiopContext.rebind(name, securedPOA.servant_to_reference(servant));
    }

    /**
     * Bind an object by using a secure POA (csiv2)
     * @param name name of the object
     * @param r remote object to bind
     * @throws Exception if the object cannot be bound
     */
    private void bindWithSpecificPoa(Name name, Remote r) throws Exception {
        POA securedPOA = createSecurePOA(name.toString());
        org.omg.PortableServer.Servant servant = (org.omg.PortableServer.Servant) Util.getTie(r);
        securedPOA.activate_object_with_id(name.toString().getBytes(), servant);
        iiopContext.bind(name, securedPOA.servant_to_reference(servant));
    }

    /**
     * Build a new POA (with csiv2 policy)
     * @param nameId name for POA
     * @return a POA
     * @throws Exception if the POA cannot be created
     */
    private POA createSecurePOA(String nameId) throws Exception {

        //TODO : Detect if a POA with this name already exists and avoid to
        // create it.
        // use random for now

        // Create policies
        org.omg.CORBA.Policy[] policies = new org.omg.CORBA.Policy[POA_POLICIES_NUMBER];
        policies[0] = rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID);
        policies[1] = rootPOA.create_lifespan_policy(LifespanPolicyValue.TRANSIENT);
        policies[2] = new SasPolicy(sasComponent);
        return rootPOA.create_POA(nameId + Math.random(), rootPOA.the_POAManager(), policies);
    }

}