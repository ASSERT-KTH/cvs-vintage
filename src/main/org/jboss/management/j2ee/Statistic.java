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
public abstract class Statistic
   implements javax.management.j2ee.Statistic
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   protected String mName;
   protected String mUnit;
   protected String mDescription;
   protected long mStartTime;
   protected long mLastSampleTime;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   /**
   * Default constructor
   *
   * @param pName Name of the statistic
   * @param pUnit Unit description used in this statistic
   * @param pDescription Human description of the statistic
   **/
   public Statistic( String pName, String pUnit, String pDescription ) {
      mName = pName;
      mUnit = pUnit;
      mDescription = pDescription;
   }
   
   // Public --------------------------------------------------------
   
   // javax.management.j2ee.Statistics implementation ---------------
   
   public String getName() {
      return mName;
   }

   public String getUnit() {
      return mUnit;
   }

   public String getDescription() {
      return mDescription;
   }
   
   public long getStartTime() {
      return mStartTime;
   }
   
   public long getLastSampleTime() {
      return mLastSampleTime;
   }
   
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
