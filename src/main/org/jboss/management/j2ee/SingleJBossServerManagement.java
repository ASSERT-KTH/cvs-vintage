/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.management.j2ee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.logging.Logger;

/**
* Represents the single JBoss server management domain
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.10 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011126 Andreas Schaefer:</b>
 * <ul>
 * <li> Adjustments to the JBoss Guidelines
 * </ul>
 **/
public class SingleJBossServerManagement
   extends J2EEDomain
{
   /** Class logger. */
   private static final Logger log =
      Logger.getLogger(SingleJBossServerManagement.class);

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------
   
   public SingleJBossServerManagement()
      throws MalformedObjectNameException, InvalidParentException
   {
      super( "jboss.management.single" );
   }
   
   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------
   
   public void postCreation() {
      // Create Server Component
      log.debug("getObjectName(), name: " + getObjectName() );
      try {
         log.debug("getObjectName(), create J2EEServer instance" );
         // Create single JBoss server
         ObjectName lServer = getServer().createMBean(
            "org.jboss.management.j2ee.J2EEServer",
            null,
            new Object[] {
               "Single",
               getObjectName(),
               "jboss.org",
               "3.0.0Beta2"
            },
            new String[] {
               String.class.getName(),
               ObjectName.class.getName(),
               String.class.getName(),
               String.class.getName()
            }
         ).getObjectName();
         // Create its node
         ObjectName lNode = getServer().createMBean(
            "org.jboss.management.j2ee.JVM",
            null,
            new Object[] {
               "localhost",
               lServer,
               "1.3.1_02",
               "Sun",
               "localhost"
            },
            new String[] {
               String.class.getName(),
               ObjectName.class.getName(),
               String.class.getName(),
               String.class.getName(),
               String.class.getName()
            }
         ).getObjectName();
      }
      catch( JMException jme ) {
         log.error("unexpected exception", jme);
      }
      catch( Exception e ) {
         log.error("unexpected exception", e);
      }
   }
   
   public String toString() {
      return "SingleJBossServerManagement { " + super.toString() + " } []";
   }
}
