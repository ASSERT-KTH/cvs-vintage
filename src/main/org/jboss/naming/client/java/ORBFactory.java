/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming.client.java;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import java.util.Properties;

import org.jboss.logging.Logger;
import org.omg.CORBA.ORB;

/** 
 * An object factory that creates an ORB on the client
 *  
 * @author Adrian Brock (adrian@jboss.com)
 * @version $Revision: 1.4 $
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
            Properties properties;
            try
            {
               properties = (Properties) AccessController.doPrivileged(new PrivilegedAction()
               {
                  public Object run()
                  {
                     return System.getProperties();
                  }
               });
            }
            catch (SecurityException ignored)
            {
               properties = null;
            }

            // Create the singleton ORB
            orb = ORB.init(new String[0], properties);
            
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
