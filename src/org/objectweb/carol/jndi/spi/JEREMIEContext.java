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
 * $Id: JEREMIEContext.java,v 1.11 2005/04/07 15:07:08 benoitf Exp $
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
import javax.rmi.CORBA.PortableRemoteObjectDelegate;

import org.objectweb.carol.jndi.wrapping.JNDIResourceWrapper;
import org.objectweb.carol.rmi.exception.NamingExceptionHelper;
import org.objectweb.carol.util.configuration.ConfigurationRepository;

/**
 * @author Guillaume Riviere
 * @author Florent Benoit
 */
public class JEREMIEContext extends AbsContext implements Context {

    /**
     * Constructs an JEREMIE Wrapper context
     * @param jeremieCtx the inital JEREMIE context
     */
    public JEREMIEContext(Context jeremieCtx) {
        super(jeremieCtx);
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
            if ((!(o instanceof Remote)) && (!(o instanceof Referenceable)) && (!(o instanceof Reference))
                    && (o instanceof Serializable)) {
                // Only Serializable (not implementing Remote or Referenceable or
                // Reference)
                JNDIResourceWrapper irw = new JNDIResourceWrapper((Serializable) o);
                PortableRemoteObjectDelegate proDelegate = ConfigurationRepository.getCurrentConfiguration().getProtocol().getPortableRemoteObject();
                proDelegate.exportObject(irw);

                Remote oldObj = (Remote) addToExported(name, irw);
                if (oldObj != null) {
                    if (replace) {
                        proDelegate.unexportObject(oldObj);
                    } else {
                        proDelegate.unexportObject(irw);
                        addToExported(name, oldObj);
                        throw new NamingException("Object '" + o + "' with name '" + name + "' is already bind");
                    }
                }
                return irw;
            } else {
                return o;
            }
        } catch (Exception e) {
            throw NamingExceptionHelper.create("Cannot wrap object '" + o + "' with name '" + name + "' : "
                    + e.getMessage(), e);
        }

    }
}