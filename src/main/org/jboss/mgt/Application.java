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
import java.util.Hashtable;
import java.util.Map;

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
   // Constants
   // -------------------------------------------------------------------------  

   public static final int EJBS = 1;
   public static final int SERVLETS = 2;
   public static final int RARS = 3;

   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private String mApplicationId;
   private String mDeploymentDescriptor;
   private Map mModules = new Hashtable();

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * Creates an empty application
    *
    * @param pApplicationId Id of these Application which must be unique within
    *                       the node/server.
    * @param pDeploymentDescriptor Deployment Descriptor of this application
    *                              which maybe is not set.
    **/
   public Application(
      String pApplicationId,
      String pDeploymentDescriptor
   ) {
      mApplicationId = pApplicationId;
      setDeploymentDescriptor( pDeploymentDescriptor );
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
    * Returns the requested module if found
    *
    * @param pModuleId Id of the Module to be saved. Please use the
    *                  constants provided in here.
    * @return Module if found and otherwise null
    **/
   public Module getModule( int pModuleId ) {
      return (Module) mModules.get( new Integer( pModuleId ) );
   }
   
   /**
    * @return Collection of Modules deployed with this application. Each
    *         item is of type {@link org.jboss.mgt.Module Module}.
    **/
   public Collection getModules() {
      return new ArrayList( mModules.values() );
   }
   
   /**
    * Saves the given Module either by registering as new Module
    * or updating the registered Module
    *
    * @param pModuleId Id of the Module to be saved. Please use the
    *                  constants provided in here.
    * @param pModule Module to be saved
    **/
   public void saveModule(
      int pModuleId,
      Module pModule
   ) {
      mModules.put( new Integer( pModuleId ), pModule );
   }

   /**
    * Removes the registered Module if found
    *
    * @param pModuleId Id of the Module to be removed. Please use the
    *                  constants provided in here.
    **/
   public void removeModule(
      int pModuleId
   ) {
      mModules.remove( new Integer( pModuleId ) );
   }

   public String toString() {
      return "Application [ " + getId() +
         ", deployment descriptor : " + getDeploymentDescriptor() +
         ", modules: " + getModules() + " ]";
   }
}
