/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.management.j2ee;

import java.io.Serializable;
import java.security.InvalidParameterException;

import javax.management.j2ee.EventProvider;
import javax.management.j2ee.StateManageable;
import javax.management.j2ee.StatisticsProvider;

/**
* JBoss specific implementation.
*
* @author Marc Fleury
**/
public abstract class J2EEManagedObject
   implements javax.management.j2ee.J2EEManagedObject, Serializable
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private String name = null;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
   * Constructor taking the Name of this Object
   *
   * @param pName Name to be set which must not be null
   *
   * @throws InvalidParameterException If the given Name is null
   **/
   public J2EEManagedObject( String pName ) {
      setName( pName );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public String getName() {
      return name;
   }

   /**
   * Set the name of the J2EEManagedObject. All objects of this type must have a
   * name. The name must not be null.
   *
   * @param pName New Name to be set
   *
   * @throws InvalidParameterException If the given Name is null
   **/
   public void setName( String pName )
      throws
         InvalidParameterException
   {
      name = pName;
   }

   public boolean isStateManageable() {
      return this instanceof StateManageable;
   }

   public boolean isStatisticsProvider() {
      return this instanceof StatisticsProvider;
   }
   
   public boolean isEventProvider() {
      return this instanceof EventProvider;
   }

}
