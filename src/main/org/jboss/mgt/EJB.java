/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mgt;

/**
 * Contains the management information about a EJB.
 *
 * @author Andreas "Mad" Schaefer (andreas.schaefer@madplanet.com)
 **/
public class EJB
   extends Item
{
   // -------------------------------------------------------------------------
   // Constants
   // -------------------------------------------------------------------------  

   public static final int SESSION = 1;
   public static final int ENTITY = 2;
   public static final int MESSAGE_DRIVEN = 3;

   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private String mClass;
   private int mType;
   private boolean mDeployed;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * Default (no-args) constructor
    **/
   public EJB() {
   }
   
   /**
    * @param pName Name of the Servlet
    * @param pType Type of the EJB
    * @param pDeployed True if the EJB is deployed now
    **/
   public EJB(
      String pName,
      int pType,
      boolean pDeployed
   ) {
      super( pName );
      mType = pType;
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   /**
    * @return Type of the EJB
    **/
   public int getType() {
      return mType;
   }

   /**
    * Sets the type of the Item
    *
    * @param pType Type to be set
    **/
   public void setType( int pType ) {
      mType = pType;
   }

   /**
    * @return True if the EJB is deployed now
    **/
   public boolean isDeployed() {
      return mDeployed;
   }

   /**
    * Defines if the given EJB is deployed or not
    *
    * @param pDeployed True if the EJB is delployed
    **/
   public void setDeployed( boolean pDeployed ) {
      mDeployed = pDeployed;
   }

   public String toString() {
      String lType = null;
      switch( getType() ) {
         case SESSION:
            lType = "Session";
            break;
         case ENTITY:
            lType = "Entity";
            break;
         case MESSAGE_DRIVEN:
            lType = "MessageDriven";
      }

      return "EJB [ " + getName() + 
         ", " + lType +
         ", is deployed " + isDeployed() +
         " ]";
   }
}
