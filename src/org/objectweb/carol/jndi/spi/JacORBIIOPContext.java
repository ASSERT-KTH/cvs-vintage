/**
 * Copyright (C) 2004-2005 - Bull S.A.
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
 * $Id: JacORBIIOPContext.java,v 1.9 2005/08/02 22:02:54 ashah Exp $
 * --------------------------------------------------------------------------
 */

package org.objectweb.carol.jndi.spi;

import java.io.Serializable;
import java.rmi.Remote;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.spi.ObjectFactory;
import javax.rmi.PortableRemoteObject;
import javax.rmi.CORBA.PortableRemoteObjectDelegate;
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
import org.objectweb.carol.jndi.wrapping.RemoteReference;
import org.objectweb.carol.rmi.exception.NamingExceptionHelper;
import org.objectweb.carol.util.configuration.ConfigurationRepository;
import org.objectweb.carol.util.csiv2.SasComponent;
import org.objectweb.carol.util.csiv2.SasPolicy;

/**
 * @author Florent Benoit
 */
public class JacORBIIOPContext extends AbsContext implements Context {

    /**
     * Object to use (specific POA) when using csiv2
     */
    public static final String SAS_COMPONENT = "org.objectweb.carol.util.csiv2.SasComponent";

    /**
     * Number of POA policies
     */
    private static final int POA_POLICIES_NUMBER = 3;

    /**
     * Root POA used by Carol
     */
    private static POA rootPOA = null;

    /**
     * SAS parameters (use for policy)
     */
    private SasComponent sasComponent = null;

    /**
     * Constructs an IIOP Wrapper context for JacORB
     * @param iiopCtx the inital IIOP context
     * @throws NamingException if POA cannot be activated
     */
    public JacORBIIOPContext(Context iiopCtx) throws NamingException {
        super(iiopCtx);
        this.sasComponent = (SasComponent) iiopCtx.getEnvironment().get(SAS_COMPONENT);

        ORB orb = JacORBCosNaming.getOrb();
        if (rootPOA == null) {
            try {
                rootPOA = org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            } catch (Exception e) {
                throw NamingExceptionHelper.create("Cannot get a single instance of rootPOA : " + e.getMessage(), e);
            }
        }

    }

    /**
     * @return the rootPOA.
     */
    public static POA getRootPOA() {
        return rootPOA;
    }

    /**
     * If this object is a reference wrapper return the reference If this object
     * is a resource wrapper return the resource
     * @param o the object to resolve
     * @param name name of the object to unwrap
     * @return the unwrapped object
     * @throws NamingException if the object cannot be unwraped
     */
    protected Object unwrapObject(Object o, Name name) throws NamingException {
        try {
            // Is it a remote reference object ?
            ObjectImpl objImpl = (ObjectImpl) PortableRemoteObject.narrow(o, ObjectImpl.class);
            String[] ids = objImpl._ids();
            // first item
            String itf = ids[0];

            if (itf.indexOf(":org.objectweb.carol.jndi.wrapping.RemoteReference:") != -1) {
                // build of the Referenceable object with is Reference
                Reference objRef = ((RemoteReference) PortableRemoteObject.narrow(o, RemoteReference.class))
                        .getReference();
                ObjectFactory objFact = (ObjectFactory) (Class.forName(objRef.getFactoryClassName())).newInstance();
                return objFact.getObjectInstance(objRef, name, this, getEnvironment());
            } else if (itf.indexOf("RMI:org.objectweb.carol.jndi.wrapping.JNDIRemoteResource:") != -1) {
                // Cast
                JNDIRemoteResource jndiRemoteResource = (JNDIRemoteResource) PortableRemoteObject.narrow(o,
                        JNDIRemoteResource.class);
                return jndiRemoteResource.getResource();
            } else {
                return o;
            }
        } catch (Exception e) {
            throw NamingExceptionHelper.create("Cannot unwrap object '" + o + "' with name '" + name + "' :" + e.getMessage(), e);
        }
    }

    /**
     * Wrap an Object : If the object is a reference wrap it into a Reference
     * Wrapper Object here the good way is to contact the carol configuration to
     * get the portable remote object
     * @param o the object to encode
     * @param name of the object
     * @param replace if the object need to be replaced
     * @return a <code>Remote JNDIRemoteReference Object</code> if o is a
     *         resource o if else
     * @throws NamingException if object cannot be wrapped
     */
    protected Object wrapObject(Object o, Name name, boolean replace) throws NamingException {
        try {
            Remote wrappedObject = null;

            // Wrap object if required
            if ((!(o instanceof Remote)) && (o instanceof Referenceable)) {
                wrappedObject = new JNDIReferenceWrapper(((Referenceable) o).getReference());
            } else if ((!(o instanceof Remote)) && (o instanceof Reference)) {
                wrappedObject = new JNDIReferenceWrapper((Reference) o);
            } else if ((!(o instanceof Remote)) && (o instanceof Serializable)) {
                wrappedObject = new JNDIResourceWrapper((Serializable) o);
            } else {
                // return object directly as it is not wrapped.
                return o;
            }

            // Object has been wrapped, need to export it
            PortableRemoteObjectDelegate proDelegate = ConfigurationRepository.getCurrentConfiguration().getProtocol().getPortableRemoteObject();
            proDelegate.exportObject(wrappedObject);

            Remote oldObj = (Remote) addToExported(name, wrappedObject);
            if (oldObj != null) {
                if (replace) {
                    proDelegate.unexportObject(oldObj);
                } else {
                    proDelegate.unexportObject(wrappedObject);
                    addToExported(name, oldObj);
                    throw new NamingException("Object '" + o + "' with name '" + name + "' is already bind");
                }
            }
            return wrappedObject;
        } catch (Exception e) {
            throw NamingExceptionHelper.create("Cannot wrap object '" + o + "' with name '" + name + "' : " + e.getMessage(), e);
        }
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
        } catch (RuntimeException re) {
            throw NamingExceptionHelper.create("Cannot lookup object with name '" + name + " : " + re.getMessage() , re);
        } catch (NamingException e) {
            // Seems that message with JacORB is null many times
            // So to avoid 'null' as message, use toString() method.
            // Also, print the stacktrace with traces enabled.
            String msg = e.getMessage();
            if (msg == null) {
                msg = e.toString();
            }
            throw NamingExceptionHelper.create("Cannot lookup object with name '" + name + " : " + msg , e);
        }
    }

    /**
     * Binds a name to an object.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     */
    public void bind(Name name, Object obj) throws NamingException {
        try {
            if (sasComponent != null) {
                Remote r = (Remote) wrapObject(obj, name, false);
                bindWithSpecificPoa(name, r);
            } else {
                super.bind(name, obj);
            }
        } catch (Exception e) {
            throw NamingExceptionHelper.create("Cannot bind object '" + obj + "' with name '" + name + "' :" + e.getMessage(), e);
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
        try {
            if (sasComponent != null) {
                Remote r = (Remote) wrapObject(obj, name, true);
                rebindWithSpecificPoa(name, r);
            } else {
                super.rebind(name, obj);
            }
        } catch (Exception e) {
            throw NamingExceptionHelper.create("Cannot rebind object '" + obj + "' with name '" + name + "' :" + e.getMessage(), e);
        }
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
        getWrappedContext().rebind(name, securedPOA.servant_to_reference(servant));
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
        getWrappedContext().bind(name, securedPOA.servant_to_reference(servant));
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
