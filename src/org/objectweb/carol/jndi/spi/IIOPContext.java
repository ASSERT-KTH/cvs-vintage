/**
 * Copyright (C) 2002,2005 - INRIA (www.inria.fr)
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
 * $Id: IIOPContext.java,v 1.9 2005/08/02 22:02:54 ashah Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.spi;

import java.io.Serializable;
import java.rmi.Remote;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.spi.ObjectFactory;
import javax.rmi.CORBA.PortableRemoteObjectDelegate;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

import org.objectweb.carol.jndi.ns.IIOPCosNaming;
import org.objectweb.carol.jndi.wrapping.JNDIReferenceWrapper;
import org.objectweb.carol.jndi.wrapping.JNDIRemoteResource;
import org.objectweb.carol.jndi.wrapping.JNDIResourceWrapper;
import org.objectweb.carol.jndi.wrapping.RemoteReference;
import org.objectweb.carol.rmi.exception.NamingExceptionHelper;
import org.objectweb.carol.util.configuration.ConfigurationRepository;

/**
 * @author Guillaume Riviere
 * @author Florent Benoit (POA model, Inheritance refactoring)
 */
public class IIOPContext extends AbsContext implements Context {

    /**
     * Root POA used by Carol
     */
    private static POA rootPOA = null;

    /**
     * Constructs an IIOP Wrapper context
     * @param iiopCtx the inital IIOP context
     * @throws NamingException if POA cannot be activated
     */
    public IIOPContext(Context iiopCtx) throws NamingException {
        super(iiopCtx);

        // Initialize ORB if null
        ORB orb = IIOPCosNaming.getOrb();
        if (rootPOA == null) {
            try {
                rootPOA = org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            } catch (Exception e) {
                throw NamingExceptionHelper.create("Cannot get a single instance" + e.getMessage(), e);
            }
        }
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
            // Unwrap object if required.
            if (o instanceof RemoteReference) {
                // build of the Referenceable object with is Reference
                Reference objRef = ((RemoteReference) o).getReference();
                ObjectFactory objFact = (ObjectFactory) (Class.forName(objRef.getFactoryClassName())).newInstance();
                return objFact.getObjectInstance(objRef, name, this, getEnvironment());
            } else if (o instanceof JNDIRemoteResource) {
                return ((JNDIRemoteResource) o).getResource();
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

}
