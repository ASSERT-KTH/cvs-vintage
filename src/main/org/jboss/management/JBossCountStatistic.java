/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.management;

import javax.management.j2ee.CountStatistic;

/**
* This class is the JBoss specific Counter Statistics class allowing
* just to increase and reset the instance.
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
* @version $Revision$
*/
public class JBossCountStatistic
   extends CountStatistic
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * Default (no-args) constructor
    **/
   public JBossCountStatistic( String pName, String pUnit, String pDescription ) {
      super( pName, pUnit, pDescription );
   }

   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------  

   /**
    * Adds a hit to this counter
    **/
   public void add() {
      count++;
   }
   
   /**
    * Removes a hit to this counter
    **/
   public void remove() {
      count = count > 0 ? count--: 0;
   }
   
   /**
    * Resets the statistics to the initial values
    **/
   public void reset() {
      count = 0;
   }
}
