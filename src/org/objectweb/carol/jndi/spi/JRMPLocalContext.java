/**
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 2005 Bull S.A.
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
 * $Id: JRMPLocalContext.java,v 1.8 2005/03/10 10:05:02 benoitf Exp $
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

import org.objectweb.carol.jndi.registry.RegistryWrapperContext;
import org.objectweb.carol.jndi.wrapping.JNDIResourceWrapper;
import org.objectweb.carol.rmi.exception.NamingExceptionHelper;
import org.objectweb.carol.util.configuration.CarolCurrentConfiguration;

import com.sun.jndi.rmi.registry.ReferenceWrapper;

/**
 * Use the wrapper on registry object defined by RegistryWrapperContext class.
 * This class has been refactored to split : <ul>
 * <li> - wrapper on registry object</li>
 * <li> - Single instance</li>
 * <li> - Wrapping of Serializable/Referenceable/... objects</li>
 * </ul>
 * @author Florent Benoit
 */
public class JRMPLocalContext extends AbsContext implements Context {


    /**
     * Constructs an JRMP local Wrapper context
     * @param jrmpLocalContext the inital Local JRMP context
     * @throws NamingException if the registry wrapper cannot be build
     */
    public JRMPLocalContext(Context jrmpLocalContext) throws NamingException {
        super(new RegistryWrapperContext(jrmpLocalContext.getEnvironment()));
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
        return super.defaultUnwrapObject(o, name);
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
            if ((!(o instanceof Remote)) && (o instanceof Referenceable)) {
                return new ReferenceWrapper(((Referenceable) o).getReference());
            } else if ((!(o instanceof Remote)) && (o instanceof Reference)) {
                return new ReferenceWrapper((Reference) o);
            } else if ((!(o instanceof Remote)) && (o instanceof Serializable)) {
                JNDIResourceWrapper irw = new JNDIResourceWrapper((Serializable) o);
                CarolCurrentConfiguration.getCurrent().getCurrentPortableRemoteObject().exportObject(irw);
                JNDIResourceWrapper oldObj = (JNDIResourceWrapper) addToExported(name, irw);
                if (oldObj != null) {
                    if (replace) {
                        CarolCurrentConfiguration.getCurrent().getCurrentPortableRemoteObject().unexportObject(oldObj);
                    } else {
                        CarolCurrentConfiguration.getCurrent().getCurrentPortableRemoteObject().unexportObject(irw);
                        addToExported(name, oldObj);
                        throw new NamingException("Object '" + o + "' with name '" + name + "' is already bind");
                    }
                }
                return irw;
            } else {
                return o;
            }
        } catch (Exception e) {
            throw NamingExceptionHelper.create("Cannot wrap object '" + o + "' with name '" + name + "' : " + e.getMessage(), e);
        }
    }

}
