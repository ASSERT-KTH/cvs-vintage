/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management;

import java.util.Collection;

import org.jboss.util.ServiceMBean;

import management.J2EEApplication;

/**
 * This interface defines the manageable interface for the JBoss Server
 * management object to be used as a JMX MBean.
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 * @version $Revision: 1.2 $
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

   /** @return Sleep period in milliseconds between to refresh cycles **/
   public int getRefreshSleep();
   /**
    * Sets the Sleep time (in milliseconds) between two refresh cycles
    *
    * @param pSleep Sleep period in milliseconds
    **/
   public void setRefreshSleep( int pSleep );
   
   /**
    * Informs this intance the environment changes and he should update its
    * data
    **/
   public void refresh();

   /**
    * Informs this intance the environment changes and he should update its
    * data right NOW
    **/
   public void refreshNow();

   /**
    * Returns an application if found with the given key.
    *
    * @param pApplicationId Id of the application to be retrieved
    *
    * @return Application if found or null if not
    **/
   public J2EEApplication getApplication(
      String pApplicationId
   );

   /**
    * @return All the registered applications where the element is of type
    *         {@link org.jboss.mpg.Application Application}.
    **/
   public Collection getApplications();
   
   /**
    * @return All the registered resources of this server. All are of type
    *         {@link management.J2EEResource J2EEResource}.
    **/
   public Collection getResources();
   
   /**
    * @return All the nodes of this server running. All are of type
    *         {@link management.Node Node}.
    **/
   public Collection getNodes();
   
   /**
    * @return Information about the JVM this application server is running on
    **/
//   public void getJVM(); 

/* AS Is not used anymore
   /**
    * Saves the given application either by registering as new application
    * or updating the registered application
    *
    * @param pApplication Application to be saved
    ** /
   public void saveApplication(
      String pApplicationId,
      Application pApplication
   );

   /**
    * Removes the registered application if found
    *
    * @param pApplicationId Id of the application to be removed
    ** /
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
    ** /
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
    ** /
   public void removeModule(
      String pApplicationId,
      int pModuleId
   );
*/
}
