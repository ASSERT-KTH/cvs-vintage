/*
 * JBoss, the OpenSource EJB server
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

import management.JavaMail;

/**
 * Mail Data Collector
 *
 * @author Marc Fleury
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

}
