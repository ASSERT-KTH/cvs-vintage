/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.invocation.trunk.client;

/**
 * This interface should be implemented by object that wants to receive TrunkRequests.
 *
 * @author <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 */
public interface ITrunkListener
{
   void requestEvent(ICommTrunk trunk, TrunkRequest request);
   void exceptionEvent(ICommTrunk trunk, Exception e);
}
