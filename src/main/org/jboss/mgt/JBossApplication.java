/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mgt;

import java.util.Collection;

/**
 * Contains the management information about
 * a deployed applications.
 *
 * @author Marc Fleury
 **/
public class JBossApplication {
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private String mApplicationId;
   private String mDeploymentDescriptor;
   private Collection mModules;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * @param pApplicationId Id of these Application which must be unique within
    *                       the node/server.
    * @param pDeploymentDescriptor Deployment Descriptor of this application
    *                              which maybe is not set.
    * @param pModules Collection of modules deployed with the given application
    *                 each item is of type {@link org.jboss.mgt.JBossModule
    *                 JBossModule}.
    **/
   public JBossApplication(
      String pApplicationId,
      String pDeploymentDescriptor,
      Collection pModules
   ) {
      mApplicationId = pApplicationId;
      setDeploymentDescriptor( pDeploymentDescriptor );
      mModules = pModules;
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   /**
    * @return Id of these Application
    **/
   public String getId() {
      return mApplicationId;
   }
   
   /**
    * Returns the deployment descriptor
    *
    * @return Deployment Descriptor of this application which maybe is not set.
    **/
   public String getDeploymentDescriptor() {
      return mDeploymentDescriptor;
   }
   
   /**
    * Sets the deployment descriptor
    *
    * @param pDeploymentDescriptor Deployment Descriptor of this application
    *                              which maybe is not set.
    **/
   public void setDeploymentDescriptor( String pDeploymentDescriptor ) {
      mDeploymentDescriptor = pDeploymentDescriptor;
   }
   
   /**
    * Adds a new Module
    *
    * @param pModule Module to be added
    **/
   public void addModule( JBossModule pModule ) {
      mModules.add( pModule );
   }
      
   /**
    * Removes a Module
    *
    * @param pModule Module to be removed
    **/
   public void removeModule( JBossModule pModule ) {
      mModules.remove( pModule );
   }

   /**
    * @return Collection of Modules deployed with this application. Each
    *         item is of type {@link org.jboss.mgt.JBossModule JBossModule}.
    **/
   public Collection getModules() {
      return mModules;
   }
}
