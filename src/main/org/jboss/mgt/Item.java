/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mgt;

import java.io.Serializable;

/**
 * Contains the management information about a Module
 * Item which could be a Servlet, JSP, EJB, EIX etc.
 *
 * @author Andreas "Mad" Schaefer (andreas.schaefer@madplanet.com)
 **/
public abstract class Item
   implements Serializable
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private String mName;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * Default (no-args) constructor
    **/
   public Item() {
   };
   
   /**
    * @param pName Name of the Item
    **/
   public Item(
      String pName
   ) {
      mName = pName;
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   /**
    * @return Name of these Item
    **/
   public String getName() {
      return mName;
   }
   
   /**
    * Sets the name of the Item
    *
    * @param pName Name to be set
    **/
   public void setName( String pName ) {
      mName = pName;
   }

   public String toString() {
      return "Item [ " + getName() + " ]";
   }
}
