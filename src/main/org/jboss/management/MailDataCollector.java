/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.jboss.management.JBossJavaMail;

import javax.management.j2ee.JavaMail;
import javax.management.j2ee.StatisticsProvider;
import javax.management.j2ee.Stats;

/**
 * Mail Data Collector
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 * @version $Revision: 1.5 $
 **/
public class MailDataCollector
   implements DataCollector
{

   // -------------------------------------------------------------------------
   // Constants
   // -------------------------------------------------------------------------  

   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------  

   /**
    * Default (no-args) Constructor
    **/
   public MailDataCollector() {
   }

   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------  

   public Collection refresh( MBeanServer pServer ) {
      Collection lReturn = new ArrayList();
      try {
         Iterator i = pServer.queryNames(
            new ObjectName( "DefaultDomain", "service", "Mail" ),
            null
         ).iterator();
         while( i.hasNext() ) {
            ObjectName lBean = (ObjectName) i.next();
            lReturn.add(
               new JBossJavaMail(
                  (String) pServer.getAttribute(
                     lBean,
                     "Name"
                  )
               )
            );
         }
      }
      catch( Exception e ) {
         e.printStackTrace();
      }
      return lReturn;
   }

  public Stats getStatistics( StatisticsProvider pProvider, MBeanServer pServer )
  {
    return null;
  }
  
  public void resetStatistics( StatisticsProvider pProvider, MBeanServer pServer )
  {
  }
}
