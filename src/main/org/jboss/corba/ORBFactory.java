/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.corba;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import java.util.Properties;

import org.jboss.logging.Logger;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

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
   
   public static ORB getORB()
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
               log.trace("Unable to retrieve system properties", ignored);
               properties = null;
            }

            // Create the singleton ORB
            orb = ORB.init(new String[0], properties);

            // Activate the root POA
            try 
            {
                POA rootPOA = (POA) orb.resolve_initial_references("RootPOA");
                rootPOA.the_POAManager().activate();
            }
            catch (Throwable t)
            {
                log.warn("Unable to activate POA", t);
            }
         }
         return orb;
      }
   }
   
   public static void setORB(ORB orb)
   {
      if (ORBFactory.orb != null)
         throw new IllegalStateException("ORB has already been set");
      ORBFactory.orb = orb;
   }
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
