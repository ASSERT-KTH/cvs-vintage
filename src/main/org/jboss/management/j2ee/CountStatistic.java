/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.management.j2ee;

/**
* This class is the JBoss specific Counter Statistics class allowing
* just to increase and reset the instance.
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @author <a href="mailto:andreas@jboss.com">Andreas Schaefer</a>
* @version $Revision: 1.1 $
*/
public class CountStatistic
   extends Statistic
   implements javax.management.j2ee.CountStatistic
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   protected long mCount;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * Default (no-args) constructor
    **/
   public CountStatistic( String pName, String pUnit, String pDescription ) {
      super( pName, pUnit, pDescription );
   }

   // -------------------------------------------------------------------------
   // CountStatistic Implementation
   // -------------------------------------------------------------------------  

   /**
    * @return The value of Count
    **/
   public long getCount() {
      return mCount;
   }

   /**
   * @return Debug Information about this Instance
   **/
   public String toString() {
      return "CountStatistics[ " + getCount() + ", " + super.toString() + " ]";
   }

   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------  

   /**
    * Adds a hit to this counter
    **/
   public void add() {
      mCount++;
   }
   
   /**
    * Removes a hit to this counter
    **/
   public void remove() {
      mCount = mCount > 0 ? mCount--: 0;
   }
   
   /**
    * Resets the statistics to the initial values
    **/
   public void reset() {
      mCount = 0;
   }
}
