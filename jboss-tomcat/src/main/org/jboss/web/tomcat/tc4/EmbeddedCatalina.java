/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.web.tomcat.tc4;

import java.util.ArrayList;

import org.apache.catalina.*;
import org.apache.catalina.startup.Embedded;

import org.jboss.logging.Logger;

/** A subclass of Embedded that accepts Services and locating virtual
 host instances.

 TOMCAT 4.1.12 UPDATE

@author Scott.Stark@jboss.org
 */
public class EmbeddedCatalina extends Embedded
{
   Logger log;
   ArrayList services = new ArrayList();

   /** Creates a new instance of EmbeddedCatalina */
   public EmbeddedCatalina(org.apache.catalina.Logger logger, Realm realm)
   {
      super(logger, realm);
      if( logger instanceof Logger )
         log = (Logger) logger;
      else
         log = Logger.getLogger(EmbeddedCatalina.class);
   }

    public void addService(Service service)
    {
        synchronized (services)
        {
            services.add(service);
            if( super.started == true )
            {
                try
                {
                    service.initialize();
                }
                catch (LifecycleException e)
                {
                    e.printStackTrace();
                }
            }

            if( super.started && (service instanceof Lifecycle) )
            {
                try
                {
                    ((Lifecycle) service).start();
                }
                catch (LifecycleException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

   public Connector[] findConnectors()
   {
      return super.connectors;
   }

   public Host findHost(String hostName)
   {
      log.trace("findHost, hostName="+hostName);
      Host host = null;
      Host defaultHost = null;
      for(int e = 0; e < engines.length; e ++)
      {
         Engine engine = engines[e];
         Container child = engine.findChild(hostName);
         if( (child instanceof Host) == true )
         {
            host = (Host) child;
         }
      }

      // Search all Hosts for matching names and aliases
      if( host == null )
      {
         log.trace("No child found matching hostName="+hostName);
         for(int e = 0; e < engines.length; e ++)
         {
            Engine engine = engines[e];
            String defaultHostName = engine.getDefaultHost();
            log.trace("Checking Engine: "+engine+", defaultHost="+defaultHostName);
            Container[] children = engine.findChildren();
            for(int c = 0; c < children.length && host == null; c ++)
            {
               Container child = children[c];
               if( (child instanceof Host) == true )
               {
                  Host tmpHost = (Host) child;
                  log.trace("Checking Host: "+tmpHost);
                  String tmpHostName = tmpHost.getName();
                  if( tmpHostName.equalsIgnoreCase(hostName) == false )
                  {
                     // Check the aliases
                     String[] aliases = tmpHost.findAliases();
                     for(int a = 0; a < aliases.length; a ++)
                     {
                        String alias = aliases[a];
                        if( alias.equalsIgnoreCase(hostName) == true )
                        {
                           host = tmpHost;
                           break;
                        }
                     }
                     // If there still is no match compare defaultHost
                     if( host == null && defaultHostName != null )
                     {
                        log.trace("Checking if defaultHost matches: "+tmpHostName);
                        if( defaultHostName.equalsIgnoreCase(tmpHostName) )
                        {
                           log.trace("May use host based on defaultHost name="+defaultHostName);
                           defaultHost = tmpHost;
                        }
                     }
                  }
               }
            }
            // If there is still no host use any defaultHost match
            if( host == null )
               host = defaultHost;
         }
      }

      return host;
   }


}
