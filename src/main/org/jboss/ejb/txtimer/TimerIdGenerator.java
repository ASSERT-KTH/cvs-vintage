/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: TimerIdGenerator.java,v 1.1 2004/09/10 21:51:04 tdiesler Exp $


/**
 * An implemenation of the interface provides a timer ids.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 10-Sep-2004
 */
public interface TimerIdGenerator
{
   /**
    * Get the next timer id
    */
   String nextTimerId();
}
