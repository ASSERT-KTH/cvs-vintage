/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: EngineConfigurationFinder.java,v 1.1 2004/04/30 16:24:46 tdiesler Exp $
package org.jboss.webservice;

// $Id: EngineConfigurationFinder.java,v 1.1 2004/04/30 16:24:46 tdiesler Exp $

import org.apache.axis.EngineConfiguration;
import org.apache.axis.configuration.FileProvider;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Discover the Axis EngineConfiguration.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 30-April-2004
 */
public final class EngineConfigurationFinder
{
   // provide logging
   private static final Logger log = Logger.getLogger(EngineConfigurationFinder.class);

   /**
    * Get the AxisClient EngineConfiguration.
    * 
    * 1. Read the config location from the system property {@link Constants.WS4EE_CLIENT_CONFIG}
    * 2. If not set, fall back to 'META-INF/axis-client-config.xml'  
    * 3. Try to access the config location as URL 
    * 4. Try to access the config location as File 
    * 5. Try to access the config location as Resource
    *
    * @return The client EngineConfiguration, or null
    */
   public static EngineConfiguration getClientEngineConfiguration()
   {

      String configLocation = System.getProperty(Constants.WS4EE_CLIENT_CONFIG);
      if (configLocation == null)
         configLocation = "META-INF/axis-client-config.xml";

      return getEngineConfiguration(configLocation);
   }

   /**
    * Get the AxisClient EngineConfiguration.
    *
    * 1. Read the config location from the system property {@link Constants.WS4EE_SERVER_CONFIG}
    * 2. If not set, fall back to 'META-INF/axis-server-config.xml'
    * 3. Try to access the config location as URL
    * 4. Try to access the config location as File
    * 5. Try to access the config location as Resource
    *
    * @return The client EngineConfiguration, or null
    */
   public static EngineConfiguration getServerEngineConfiguration()
   {
      String configLocation = System.getProperty(Constants.WS4EE_SERVER_CONFIG);
      if (configLocation == null)
         configLocation = "META-INF/axis-server-config.xml";

      return getEngineConfiguration(configLocation);


   }

   /** Do the discovery in the way described above. */
   private static EngineConfiguration getEngineConfiguration(String configLocation)
   {
      EngineConfiguration config = null;

      // Try to load it from a URL
      try
      {
         URL url = new URL(configLocation);
         InputStream is = url.openStream();
         if (is != null)
            config = new FileProvider(is);
      }
      catch (MalformedURLException e)
      {
         // its not a url
      }
      catch (IOException e)
      {
         // there is nothing at that url
      }

      // Try to load it from a file
      if (config == null && new File(configLocation).exists())
      {
         config = new FileProvider(configLocation);
      }


      // Try to load it from the class loader that loaded this class
      if (config == null)
      {
         ClassLoader cl = EngineConfigurationFinder.class.getClassLoader();
         InputStream is = cl.getResourceAsStream(configLocation);
         if (is != null)
            config = new FileProvider(is);
      }

      if (config == null)
         log.warn("Cannot find engine configuration at: " + configLocation);

      return config;
   }
}
