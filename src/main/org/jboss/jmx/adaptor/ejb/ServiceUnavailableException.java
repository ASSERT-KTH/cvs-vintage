// ----------------------------------------------------------------------------
// File: ServiceUnavailableException
// Copyright ( c ) 2001 eBuilt, Inc.  All rights reserved.
// Version: $Revision: 1.1 $
// Last Checked In: $Date: 2001/09/12 01:49:06 $
// Last Checked In By: $Author: schaefera $
// ----------------------------------------------------------------------------

package org.jboss.jmx.adaptor.ejb;

/**
 * An instance of this class is thrown when a Value Object
 * contains an invalid value.
 *
 * @author Andreas Schaefer
 * @version $Revision: 1.1 $
 **/
public class ServiceUnavailableException
   extends Exception
{
   
   // -------------------------------------------------------------------------
   // Static
   // -------------------------------------------------------------------------
	 
   // -------------------------------------------------------------------------
   // Members 
   // -------------------------------------------------------------------------  

   // -------------------------------------------------------------------------
   // Constructor
   // -------------------------------------------------------------------------
	 
   /**
    * Constructor with a message of the exception
    *
    * @param pMessage Message to further explain the exception
    **/
   public ServiceUnavailableException( String pMessage )
   {
      super( pMessage );
   }


   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------  

   /**
    * Describes the instance and its content for debugging purpose
    *
    * @return Using the one from the super class
    **/
   public String toString()
   {
      return super.toString();
   }

   /**
    * Determines if the given instance is the same as this instance
    * based on its content. This means that it has to be of the same
    * class ( or subclass ) and it has to have the same content
    *
    * @return Returns the equals value from the super class
    **/
   public boolean equals( Object pTest )
   {
      return super.equals( pTest );
   }

   /**
    * Returns the hashcode of this instance
    *
    * @return Hashcode of the super class
    **/
   public int hashCode()
   {
      return super.hashCode();
   }

}

// ----------------------------------------------------------------------------
// EOF
