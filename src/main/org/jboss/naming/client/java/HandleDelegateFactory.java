/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming.client.java;

import javax.ejb.spi.HandleDelegate;

import org.jboss.proxy.ejb.handle.HandleDelegateImpl;

/**
 * A simple factory for the HandleDelegate implementation object
 * along the lines of ORBFactory
 *  
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 1.1 $
 */
public class HandleDelegateFactory
{
   /** The HandleDelegateImpl */
   private static HandleDelegate hd;
   
   public static HandleDelegate getHandleDelegateSingleton()
   {
      synchronized (HandleDelegateFactory.class)
      {
         if (hd == null)
         {
            // Create the singleton
            hd = new HandleDelegateImpl();
         }
         return hd;
      }
   }
}
