/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mgt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Contains the management information about
 * a deployed applications.
 *
 * @author Marc Fleury
 **/
public class Application
   implements Serializable
{
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
   public Application(
      String pApplicationId,
      String pDeploymentDescriptor,
      Collection pModules
   ) {
      mApplicationId = pApplicationId;
      setDeploymentDescriptor( pDeploymentDescriptor );
      setModules( pModules );
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
    * @return Collection of Modules deployed with this application. Each
    *         item is of type {@link org.jboss.mgt.JBossModule JBossModule}.
    **/
   public Collection getModules() {
      return mModules;
   }
   
   /**
    * Sets a new list of modules
    *
    * @param pModules New list of modules to be set
    **/
   public void setModules( Collection pModules ) {
      if( pModules == null ) {
         // If null is passed then keep the list
         mModules = new ArrayList();
      }
      else {
         mModules = pModules;
      }
   }
   
   public String toString() {
      return "Application [ " + getId() +
         ", deployment descriptor : " + getDeploymentDescriptor() +
         ", modules: " + getModules() + " ]";
   }
}
