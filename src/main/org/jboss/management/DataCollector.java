/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management;

import java.util.Collection;

import javax.management.MBeanServer;

import management.J2EEManagedObject;

/**
 * Collector Interface which must be implemented by
 * any collector to lookup the management data
 *
 * @author Marc Fleury
 **/
public interface DataCollector {

   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------  

   /**
    * Is called when the data must be refreshed
    *
    * @param pServer MBean Server is used to get the information about the server
    *
    * @return Collection of elements found (must be of type J2EEManagedObject)
    **/
   public Collection refresh( MBeanServer pServer );

}
