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

import org.jboss.management.JBossIpAddress;
import org.jboss.management.JBossJVM;
import org.jboss.management.JBossNode;
import org.jboss.management.JBossPort;

import management.IpAddress;
import management.JVM;
import management.Node;
import management.Port;

/**
 * Node Data Collector
 *
 * @author Marc Fleury
 **/
public class NodeDataCollector
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
   public NodeDataCollector() {
   }

   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------  

   public Collection refresh( MBeanServer pServer ) {
      Collection lReturn = new ArrayList();
      try {
         // Get the JVMs Info
         //AS Right now there is only one JVM at all
         Runtime lRuntime = Runtime.getRuntime();
         JVM[] lJVMs = new JVM[] {
            new JBossJVM(
               System.getProperty( "java.runtime.name" ),
               (int) ( lRuntime.totalMemory() - lRuntime.freeMemory() ),
               (int) lRuntime.totalMemory(),
               System.getProperty( "java.version" ),
               System.getProperty( "java.vendor" ),
               System.getProperty( "java.class.path" )
            )
         };
         //AS Right now I have no clue which ports should be
         //AS listed and how to get the Webserver port
         Port lWebPort = new JBossPort(
            "WebServer Port",
            8080,
            null
         );
         IpAddress lWebIpAddress = new JBossIpAddress(
            "WebServer IpAddress",
            InetAddress.getLocalHost().getHostAddress(),
            new Port[] { lWebPort }
         );
         lWebPort.setIpAddress( lWebIpAddress );
         IpAddress[] lIpAddresses = new IpAddress[] {
            new JBossIpAddress(
               "WebServer",
               InetAddress.getLocalHost().getHostAddress(),
               new Port[] { lWebPort }
            )
         };
         // Create Node Info
         lReturn.add(
            new JBossNode(
               InetAddress.getLocalHost().getHostName(),
               System.getProperty( "os.arch" ),
               System.getProperty( "os.name" ),
               lJVMs,
               lIpAddresses
            )
         );
      }
      catch( Exception e ) {
         e.printStackTrace();
      }
      return lReturn;
   }

}
