package org.jboss.management;

import java.io.Serializable;
import java.security.InvalidParameterException;

import javax.management.j2ee.J2EEManagedObject;
import javax.management.j2ee.StateManageable;
import javax.management.j2ee.StatisticsProvider;

/**
 * JBoss specific implementation.
 *
 * @author Marc Fleury
 **/
public abstract class JBossJ2EEManagedObject
   implements J2EEManagedObject, Serializable
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
   public JBossJ2EEManagedObject( String pName ) {
      setName( pName );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   /**
    * @return The actual Name which is never null.
    **/
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

   /**
    * @return True, indicates that operations and attributes specificed by the StateManageable
    * type are availble from the J2EEManagedObject.
    * False, the J2EEManagedObject does not support state management.
    **/
   public boolean isStateManageable() {
      return this instanceof StateManageable;
   }

   /**
    * @return True, indicates that the J2EEManagedObject supports performace statistics
    * and imcorporates the attributes and operations of the StatisticProvider type.
    * False, the J2EEMangedObject does not support performance statistics.
    **/
   public boolean hasStatistics() {
      return this instanceof StatisticsProvider;
   }

}
