/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming.client.java;

import java.util.Hashtable;

import org.jboss.logging.Logger;
import org.omg.CORBA.ORB;

/** 
 * An object factory that creates an ORB on the client
 *  
 * @author Adrian Brock (adrian@jboss.com)
 * @version $Revision: 1.1 $
 */
public class ORBFactory
{
   // Constants -----------------------------------------------------

   /** The logger */
   private static final Logger log = Logger.getLogger(ORBFactory.class);
   
   /** The orb */
   private static ORB orb;
   
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------
   
   public static ORB getORBSingleton()
   {
      synchronized (ORBFactory.class)
      {
         if (orb == null)
         {
            // Create the singleton ORB
            orb = ORB.init(new String[0], null);
            
            // Start the orb
            new Thread
            (
               new Runnable()
               {
                  public void run()
                  {
                     try
                     {
                        orb.run();
                     }
                     catch (Throwable t)
                     {
                        log.warn("Error running ORB", t);
                     }
                  }
               }, 
               "ORB thread"
            ).start(); 
         }
         return orb;
      }
   }
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
