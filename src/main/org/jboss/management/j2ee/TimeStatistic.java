/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

/**
* Time Statisitic Container for JBoss.
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @author <a href="mailto:andreas@jboss.com">Andreas Schaefer</a>
* @version $Revision: 1.2 $
**/
public class TimeStatistic
   extends Statistic
   implements javax.management.j2ee.TimeStatistic
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   protected long mCount;
   protected long mMinTime;
   protected long mMaxTime;
   protected long mTotalTime;
   protected double mRequestRate;

   private long mStart;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
   * Default (no-args) constructor
   **/
   public TimeStatistic( String pName, String pUnit, String pDescription ) {
      super( pName, pUnit, pDescription );
      mStart = System.currentTimeMillis();
   }

   // -------------------------------------------------------------------------
   // CountStatistic Implementation
   // -------------------------------------------------------------------------  

   /**
   * @return The number of times a time measurements was added
   **/
   public long getCount() {
      return mCount;
   }

   /**
   * @return The minimum time added since start of the measurements
   **/
   public long getMinTime() {
      return mMinTime;
   }

   /**
   * @return The maximum time added since start of the measurements
   **/
   public long getMaxTime() {
      return mMaxTime;
   }

   /**
   * @return The sum of all the time added to the measurements since
   *         it started
   **/
   public long getTotalTime() {
      return mTotalTime;
   }

   /**
   * @return The request rate which is the number of counts divided by
   *         the time elapsed since the time measurements started
   **/
   public double getRequestRate() {
      return mRequestRate;
   }

   /**
   * @return Debug Information about this instance
   **/
   public String toString() {
      return "TimStatistic [ " +
         "Count: " + getCount() +
         ", Min. Time: " + getMinTime() +
         ", Max. Time: " + getMaxTime() +
         ", Total Time: " + getTotalTime() +
         ", Request Rate: " + getRequestRate() +
         ", " + super.toString() + " ]";
   }

   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------  

   /**
   * Adds a Statistic Information about the elapsed time an action
   * observed by this instance took.
   *
   * @param pTime Time elapsed to added to a statistics
   **/
   public void add( long pTime ) {
      mCount++;
      if( pTime == 0 )
      {
         mMinTime = 1;
      }
      if( mMinTime == 0 )
      {
         mMinTime = pTime;
      }
      mMinTime = pTime < mMinTime ? pTime : mMinTime;
      mMaxTime = pTime > mMaxTime ? pTime : mMaxTime;
      mTotalTime += pTime;
      mRequestRate = ( System.currentTimeMillis() - mStart ) / ( mCount * 1000 );
   }

   /**
   * Resets the statistics to the initial values
   **/
   public void reset() {
      mCount = 0;
      mMinTime = 0;
      mMaxTime = 0;
      mTotalTime = 0;
      mRequestRate = 0;
   }

}
