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
 * $Id: JRMPContext.java,v 1.8 2005/03/15 09:57:03 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.spi;

import java.io.Serializable;
import java.rmi.Remote;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;

import org.objectweb.carol.jndi.wrapping.JNDIResourceWrapper;
import org.objectweb.carol.jndi.wrapping.UnicastJNDIReferenceWrapper;
import org.objectweb.carol.rmi.exception.NamingExceptionHelper;
import org.objectweb.carol.rmi.util.PortNumber;
import org.objectweb.carol.util.configuration.CarolConfiguration;
import org.objectweb.carol.util.configuration.CarolCurrentConfiguration;
import org.objectweb.carol.util.configuration.CarolDefaultValues;
import org.objectweb.carol.util.configuration.RMIConfiguration;

/**
 * @author Guillaume Riviere
 * @author Florent Benoit
 */
public class JRMPContext extends AbsContext implements Context {

    /**
     * Constructs an JRMP Wrapper context
     * @param jrmpContext the inital JRMP context
     */
    public JRMPContext(Context jrmpContext) {
        super(jrmpContext);
    }

    /**
     * @return the object port used for exporting object
     * @throws NamingException if the port cannot be retrieved
     */
    protected int getObjectPort() throws NamingException {
        try {
            RMIConfiguration rmiConfig = CarolConfiguration.getRMIConfiguration("jrmp");
            String propertyName = CarolDefaultValues.SERVER_JRMP_PORT;
            Properties p = rmiConfig.getConfigProperties();
            if (p != null) {
                return PortNumber.strToint(p.getProperty(propertyName, "0"), propertyName);
            }
        } catch (Exception e) {
            throw NamingExceptionHelper.create("Cannot get object port", e);
        }
        return 0;
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
                // Add wrapper for the two first cases. Then it will use PortableRemoteObject instead of UnicastRemoteObject
                // and when fixing JRMP exported objects port, it use JRMPProdelegate which is OK.
                if ((!(o instanceof Remote)) && (o instanceof Referenceable)) {
                    return new UnicastJNDIReferenceWrapper(((Referenceable) o).getReference(), getObjectPort());
                } else if ((!(o instanceof Remote)) && (o instanceof Reference)) {
                    return new UnicastJNDIReferenceWrapper((Reference) o, getObjectPort());
                } else if ((!(o instanceof Remote)) && (!(o instanceof Referenceable)) && (!(o instanceof Reference))
                        && (o instanceof Serializable)) {
                    // Only Serializable (not implementing Remote or Referenceable or
                    // Reference)
                    JNDIResourceWrapper irw = new JNDIResourceWrapper((Serializable) o);
                    CarolCurrentConfiguration.getCurrent().getCurrentPortableRemoteObject().exportObject(irw);
                    Remote oldObj = (Remote) addToExported(name, irw);
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
                throw NamingExceptionHelper.create("Cannot wrap object '" + o + "' with name '" + name + "' : "
                        + e.getMessage(), e);
            }
    }
}