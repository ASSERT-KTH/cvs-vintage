/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mgt;

import java.util.Collection;

import org.jboss.util.ServiceMBean;

/**
 * This interface defines the manageable interface for the JBoss Server
 * management object to be used as a JMX MBean.
 *
 * @author Marc Fleury
 **/
public interface ServerDataCollectorMBean
   extends ServiceMBean
{
   // -------------------------------------------------------------------------
   // Constants
   // -------------------------------------------------------------------------  

   public static final String OBJECT_NAME = "J2EEManagement:service=J2EEServer";

   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------

   /**
    * Returns an application if found with the given key.
    *
    * @param pApplicationId Id of the application to be retrieved
    *
    * @return Application if found or null if not
    **/
   public Application getApplication(
      String pApplicationId
   );

   /**
    * @return All the registered applications where the element is of type
    *         {@link org.jboss.mpg.Application Application}.
    **/
   public Collection getApplications();

   /**
    * Saves the given application either by registering as new application
    * or updating the registered application
    *
    * @param pApplication Application to be saved
    **/
   public void saveApplication(
      String pApplicationId,
      Application pApplication
   );

   /**
    * Removes the registered application if found
    *
    * @param pApplicationId Id of the application to be removed
    **/
   public void removeApplication(
      String pApplicationId
   );

   /**
    * Saves the given Module either by registering as new Module
    * or updating the registered Module
    *
    * @param pApplicationId Id of the Application the Module is part of
    * @param pModuleId Id of the Module to be saved
    * @param pModule Module to be saved
    **/
   public void saveModule(
      String pApplicationId,
      int pModuleId,
      Module pModule
   );

   /**
    * Removes the registered Module if found
    *
    * @param pApplicationId Id of the Application the Module is part of
    * @param pModuleId Id of the Module to be removed
    **/
   public void removeModule(
      String pApplicationId,
      int pModuleId
   );

}
