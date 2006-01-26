/**
 * Copyright (C) 2002-2006 - INRIA (www.inria.fr)
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
 * $Id: CmiContextWrapperFactory.java,v 1.2 2006/01/26 16:28:55 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.spi;

import javax.naming.spi.InitialContextFactory;

import org.objectweb.carol.jndi.ns.CmiRegistry;
import org.objectweb.carol.util.configuration.CarolDefaultValues;

/**
 * Class <code> CmiContextWrapperFactory </code> is the CAROL
 * JNDI Context factory. This context factory build the cmi context for
 * reference wrapping to/from a remote object
 * @author Florent Benoit
 * @author Benoit Pelletier
 * @see javax.naming.spi.InitialContextFactory
 */
public class CmiContextWrapperFactory extends AbsInitialContextFactory implements InitialContextFactory {

    /**
     * Referencing factory
     */
    public static final String REFERENCING_FACTORY = "org.objectweb.carol.cmi.jndi.CmiInitialContextFactory";

    /**
     * @return the real factory of this wrapper
     */
    protected String getReferencingFactory() {
        return REFERENCING_FACTORY;
    }

    /**
     * @return class of the wrapper (to be instantiated + pool).
     */
    protected Class getWrapperClass() {
        boolean localreg = new Boolean(System.getProperty(CarolDefaultValues.LOCALREG_JRMP_PROPERTY, "false")).booleanValue();
        if ((localreg) && CmiRegistry.isLocal()) {
            return CmiLocalContext.class;
        } else {
            return CmiContext.class;
        }
    }

}