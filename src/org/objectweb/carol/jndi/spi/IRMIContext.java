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
 * $Id: IRMIContext.java,v 1.2 2005/05/16 08:51:12 benoitf Exp $
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
import org.objectweb.carol.rmi.exception.NamingExceptionHelper;
import org.objectweb.carol.rmi.util.PortNumber;
import org.objectweb.carol.util.configuration.CarolDefaultValues;
import org.objectweb.carol.util.configuration.ConfigurationRepository;


/**
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 */

public class IRMIContext extends AbsContext implements Context {

    /**
     * Constructs a IRMI Wrapper context
     *
     * @param context the inital context
     */
    public IRMIContext(Context context) {
        super(context);
    }

    /**
     * @return the object port used for exporting object
     */
    protected int getObjectPort() {
        Properties prop = ConfigurationRepository.getProperties();
        if (prop != null) {
            String propertyName = CarolDefaultValues.SERVER_IRMI_PORT;
            return PortNumber.strToint(prop.getProperty(propertyName, "0"), propertyName);
        }
        return 0;
    }


    /**
     * If this object is a reference wrapper return the reference If
     * this object is a resource wrapper return the resource
     *
     * @param o the object to resolve
     * @param name name of the object to unwrap
     * @return the unwrapped object
     * @throws NamingException if the object cannot be unwraped
     */
    protected Object unwrapObject(Object o, Name name) throws NamingException {
        Object result = super.defaultUnwrapObject(o, name);
        if (result instanceof Reference) {
            try {
                return javax.naming.spi.NamingManager.getObjectInstance
                    (result, null, null, getEnvironment());
            } catch (Exception e) {
                throw NamingExceptionHelper.create
                    ("Cannot resolve reference", e);
            }
        } else {
            return result;
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
            javax.rmi.CORBA.PortableRemoteObjectDelegate pro =
                ConfigurationRepository.getCurrentConfiguration().getProtocol()
                .getPortableRemoteObject();
            if (!(o instanceof Remote)) {
                if (o instanceof Referenceable) {
                    o = ((Referenceable) o).getReference();
                }
                o = new JNDIResourceWrapper((Serializable) o);
                pro.exportObject((Remote) o);
                Remote old = (Remote) addToExported(name, o);
                if (old != null) {
                    if (replace) {
                        pro.unexportObject(old);
                    } else {
                        pro.unexportObject((Remote) o);
                        addToExported(name, old);
                        throw new NamingException("Object '" + o + "' with name '" + name + "' is already bind");
                    }
                }
            }

            if (o instanceof Remote) {
                o = pro.toStub((Remote) o);
                return o;
            } else {
                return o;
            }
        } catch (java.rmi.RemoteException e) {
            throw (NamingException) new NamingException().initCause(e);
        }
    }
}
