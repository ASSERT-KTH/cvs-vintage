/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mgt;

import java.io.Serializable;
import java.util.Collection;

/**
 * Contains the management information about
 * a deployed applications.
 *
 * @author Marc Fleury
 **/
public class Module
   implements Serializable
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private String mModuleId;
   private String mDeploymentDescriptor;
   private Collection mItems;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * @param pModuleId Id of these module which must be unique within the application.
    * @param pDeploymentDescriptor Deployment Descriptor of this module
    * @param pItems Collection of Entities
    **/
   public Module(
      String pModuleId,
      String pDeploymentDescriptor,
      Collection pItems
   ) {
      mModuleId = pModuleId;
      setDeploymentDescriptor( pDeploymentDescriptor );
      mItems = pItems;
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   /**
    * @return Id of these Module
    **/
   public String getId() {
      return mModuleId;
   }
   
   /**
    * Returns the deployment descriptor
    *
    * @return Deployment Descriptor of this module.
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
    * Adds a new Item
    *
    * @param pItem Item to be added
    **/
   public void addItem( Item pItem ) {
      mItems.add( pItem );
   }
      
   /**
    * Removes a Item
    *
    * @param pItem Item to be removed
    **/
   public void removeItem( Item pItem ) {
      mItems.remove( pItem );
   }

   /**
    * @return Collection of Items deployed with this application. Each
    *         item is of type {@link org.jboss.mgt.Item Item}.
    **/
   public Collection getItems() {
      return mItems;
   }

   public String toString() {
      return "Module [ " + getId() +
         ", deployment descriptor : " + getDeploymentDescriptor() +
         ", items: " + getItems() + " ]";
   }
}
