/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: BigIntegerTimerIdGenerator.java,v 1.1 2004/09/10 21:51:04 tdiesler Exp $

import java.math.BigInteger;

/**
 * A timerId generator that uses a BigInteger count.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 10-Sep-2004
 */
public class BigIntegerTimerIdGenerator implements TimerIdGenerator
{
   // The next timer identity
   private BigInteger nextTimerId = BigInteger.valueOf(0);

   /**
    * Get the next timer id
    */
   public synchronized String nextTimerId()
   {
      nextTimerId = nextTimerId.add(BigInteger.valueOf(1));
      return nextTimerId.toString();
   }
}
