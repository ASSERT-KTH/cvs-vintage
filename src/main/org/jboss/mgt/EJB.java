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

   public static final int STATELESS_SESSION = 1;
   public static final int STATEFUL_SESSION = 2;
   public static final int ENTITY_BMP = 3;
   public static final int ENTITY_CMP = 4;
   public static final int MESSAGE = 5;

   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private String mClass;
   private int mType;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * @param pName Name of the Servlet
    **/
   public EJB(
      String pName,
      int pType
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

   public String toString() {
      String lType = null;
      switch( getType() ) {
         case STATELESS_SESSION:
            lType = "Stateless Session";
            break;
         case STATEFUL_SESSION:
            lType = "Statefull Session";
            break;
         case ENTITY_BMP:
            lType = "Entity BMP";
            break;
         case ENTITY_CMP:
            lType = "Entity CMP";
            break;
         case MESSAGE:
            lType = "Message";
      }

      return "EJB [ " + getName() + 
         ", " + lType +
         " ]";
   }
}
