/**
 * Copyright (C) 2005 - Bull S.A.
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
 * $Id: IRMIContextWrapperFactory.java,v 1.3 2005/09/15 13:04:16 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.spi;

import javax.naming.spi.InitialContextFactory;

import org.objectweb.carol.jndi.ns.IRMIRegistry;
import org.objectweb.carol.util.configuration.CarolDefaultValues;

/**
 * Class <code> IRMIContextWrapperFactory </code> is the CAROL
 * JNDI Context factory. This context factory build the irmi context for
 * reference wrapping to/from a remote object
 * @author Guillaume Riviere
 * @author Florent Benoit (refactoring)
 * @see javax.naming.spi.InitialContextFactory
 */
public class IRMIContextWrapperFactory extends AbsInitialContextFactory implements InitialContextFactory {

    /**
     * @return class of the wrapper (to be instantiated + pool).
     */
    protected Class getWrapperClass() {
        // use registry object when property is set to on and registry is in the same JVM.
        // TODO: Change the property name used by both JRMP and IRMI or always use this when registry is in the same JVM.
        boolean localO = new Boolean(System.getProperty(CarolDefaultValues.LOCAL_JRMP_PROPERTY, "false")).booleanValue();
        if (localO && IRMIRegistry.isLocal()) {
            return IRMILocalContext.class;
        } else {
            return IRMIContext.class;
        }
    }

}
