package org.jboss.management;

import java.util.Date;

import javax.management.j2ee.TimeStatistic;

/**
* Time Statisitic Container for JBoss.

* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
* @version $Revision$
**/
public class JBossTimeStatistic
   extends TimeStatistic
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private long mStart;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * Default (no-args) constructor
    **/
   public JBossTimeStatistic( String name, String unit, String description ) {
      super( name, unit, description );
      mStart = System.currentTimeMillis();
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
      count++;
      if( pTime == 0 )
      {
         minTime = 1;
      }
      if( minTime == 0 )
      {
         minTime = pTime;
      }
      minTime = pTime < minTime ? pTime : minTime;
      maxTime = pTime > maxTime ? pTime : maxTime;
      totalTime += pTime;
      requestRate = ( System.currentTimeMillis() - mStart ) / ( count * 1000 );
   }

   /**
    * Resets the statistics to the initial values
    **/
   public void reset() {
      count = 0;
      minTime = 0;
      maxTime = 0;
      totalTime = 0;
      requestRate = 0;
   }

   /**
    * @return Debug Information about this instance
    **/
   public String toString() {
      return "JBossTimeStatistic [ " + super.toString() + " ]";
   }
}
