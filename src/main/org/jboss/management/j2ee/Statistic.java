/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

/**
* JBoss Implementation of the base Model for a Statistic Information
*
* @author Marc Fleury
**/
public class Statistic
   implements javax.management.j2ee.Statistic
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   protected String mName;
   protected String mUnit;
   protected String mDescription;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
   * Default (no-args) constructor
   **/
   public Statistic( String pName, String pUnit, String pDescription ) {
      mName = pName;
      mUnit = pUnit;
      mDescription = pDescription;
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   /**
   * @return Name of the Statistics
   **/
   public String getName() {
      return mName;
   }

   /**
   * @return Unit of Measurement. For TimeStatistics valid values are "HOUR",
   *         "MINUTE", "SECOND", "MILLISECOND", "MICROSECOND", "NANOSECOND"
   **/
   public String getUnit() {
      return mUnit;
   }

   /**
   * @return A human-readable description
   **/
   public String getDescription() {
      return mDescription;
   }

}
