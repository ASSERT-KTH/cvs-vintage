/**
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 2004 Bull S.A.
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
 * $Id: JacORBIIOPContext.java,v 1.2 2004/12/15 15:18:18 benoitf Exp $
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
import org.objectweb.carol.util.csiv2.SasComponent;
import org.objectweb.carol.util.csiv2.SasPolicy;

import com.sun.jndi.rmi.registry.RemoteReference;

/**
 * @author Guillaume Riviere
 * @author Florent Benoit (make it working for JacORB IIOP)
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
     * Root POA used by Carol
     */
    public static POA rootPOA = null;

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

        if (JacORBCosNaming.getOrb() != null) {
            orb = JacORBCosNaming.getOrb();
            if (rootPOA == null) {
                try {
                    rootPOA = org.omg.PortableServer.POAHelper.narrow(JacORBCosNaming.getOrb().resolve_initial_references("RootPOA"));
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

        } else if (orb == null) {
            orb = ORB.init(new String[0], null);
            try {
                rootPOA = org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            } catch (Exception e) {
                throw new NamingException("Cannot get a single instance" + e.getMessage());
            }
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
            env.put("java.naming.factory.initial", "com.sun.jndi.cosnaming.CNCtxFactory");
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
            //TODO: Is it a remote reference object ?
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
                JNDIRemoteResource jndiRemoteResource = (JNDIRemoteResource) PortableRemoteObject.narrow(o, JNDIRemoteResource.class);
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

    // Context methods
    // The Javadoc is deferred to the Context interface.

    /**
     * Retrieves the named object.
     * @param name the name of the object to look up
     * @return the object bound to <tt>name</tt>
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookup(Name name) throws NamingException {
        return unwrapObject(iiopContext.lookup(name), name);
    }

    public Object lookup(String name) throws NamingException {
        try {
            return lookup(new CompositeName(name));
        } catch (Exception e) {
            throw new NamingException("Cannot lookup object : '" +  e.getMessage() + "'");
        }
    }

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

    public void bind(String name, Object obj) throws NamingException {
        bind(new CompositeName(name), obj);
    }

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

    public void rebind(String name, Object obj) throws NamingException {
        rebind(new CompositeName(name), obj);
    }

    public void unbind(Name name) throws NamingException {
        try {
            iiopContext.unbind(name);
        } catch (Exception e) {
            throw new NamingException("" + e);
        }
    }

    public void unbind(String name) throws NamingException {
        unbind(new CompositeName(name));
    }

    public void rename(Name oldName, Name newName) throws NamingException {
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

    /**
     * @return the orb.
     */
    public static ORB getOrb() {
        return orb;
    }


    private void rebindWithSpecificPoa(Name name, Remote r) throws Exception {
        POA securedPOA = createSecurePOA(name.toString());
        org.omg.PortableServer.Servant servant = (org.omg.PortableServer.Servant) Util.getTie(r);
        securedPOA.activate_object_with_id(name.toString().getBytes(), servant);
        iiopContext.rebind(name, securedPOA.servant_to_reference(servant));
    }

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

       //TODO : Detect if a POA with this name already exists and avoid to create it.
       // use random for now

       // Create policies
       org.omg.CORBA.Policy[] policies = new org.omg.CORBA.Policy[3];
       policies[0] = rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID);
       policies[1] = rootPOA.create_lifespan_policy(LifespanPolicyValue.TRANSIENT);
       policies[2] = new SasPolicy(sasComponent);
       return rootPOA.create_POA(nameId + Math.random(), rootPOA.the_POAManager(), policies);
   }




}