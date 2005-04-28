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
 * $Id: ProtocolConfigurationImplMBean.java,v 1.3 2005/04/28 11:37:26 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.configuration;

import java.util.List;

import javax.naming.NamingException;

/**
 * Defines the MBean interface. Don't use Models MBeans as it will imply some
 * external libraries like commons-modeler (easier).
 * @author Florent Benoit
 */
public interface ProtocolConfigurationImplMBean {

    /**
     * Gets the provider URL of this configuration (Context.PROVIDER_URL)
     * @return the provider URL of this configuration
     */
    String getProviderURL();

    /**
     * Gets the InitialContextFactory classname
     * (Context.INITIAL_CONTEXT_FACTORY)
     * @return the InitialContextFactory classname
     */
    String getInitialContextFactoryClassName();

    /**
     * Gets JNDI names of the context with this configuration
     * @return JNDI names
     * @throws NamingException if the names cannot be listed
     */
    List getNames() throws NamingException;

    /**
     * @return the name of the configuration
     */
    String getName();


    /**
     * @return Object Name
     */
    String getobjectName();


    /**
     * Sets the object name of this mbean
     * @param name the Object Name
     */
    void setobjectName(String name);

    /**
     * @return true if it is an event provider
     */
    boolean iseventProvider();

    /**
     * @return true if this managed object implements J2EE State Management
     *         Model
     */
    boolean isstateManageable();

    /**
     * @return true if this managed object implements the J2EE StatisticProvider
     *         Model
     */
    boolean isstatisticsProvider();


}
