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
 * $Id: JEREMIEContextWrapperFactory.java,v 1.3 2005/03/10 10:05:02 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.spi;

import javax.naming.spi.InitialContextFactory;

/**
 * Class <code> JEREMIEContextWrapperFactory </code> is the CAROL
 * JNDI Context factory. This context factory build the jeremie context for
 * reference wrapping to/from a remote object
 * @author Guillaume Riviere
 * @author Florent Benoit (refactoring)
 * @see javax.naming.spi.InitialContextFactory
 */
public class JEREMIEContextWrapperFactory extends AbsInitialContextFactory implements InitialContextFactory {

    /**
     * Referencing factory
     */
    public static final String REFERENCING_FACTORY = "org.objectweb.jeremie.services.registry.jndi.JRMIInitialContextFactory";

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
        return JEREMIEContext.class;
    }

}

