/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice;

// $Id: WSDLDefinitionFactory.java,v 1.1 2004/05/12 22:09:56 tdiesler Exp $

import org.jboss.logging.Logger;
import org.xml.sax.InputSource;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * A factory that creates a WSDL <code>Definition</code> from an URL.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-April-2004
 */
public final class WSDLDefinitionFactory
{
   // provide logging
   private static final Logger log = Logger.getLogger(WSDLDefinitionFactory.class);

   // hide constructor
   private WSDLDefinitionFactory()
   {
   }

   public static Definition readWSDL(URL wsdlLocation) throws WSDLException
   {
      WSDLFactory wsdlFactory = WSDLFactory.newInstance();
      WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
      Definition wsdlDefinition = wsdlReader.readWSDL(new WSDLLocatorImpl(wsdlLocation));
      return wsdlDefinition;
   }

   /* A WSDLLocator that can handle wsdl imports
   */
   public static class WSDLLocatorImpl implements WSDLLocator
   {
      private URL wsdlFile;
      private String latestImportURI;

      public WSDLLocatorImpl(URL wsdlFile)
      {
         this.wsdlFile = wsdlFile;
      }

      public InputSource getBaseInputSource()
      {
         try
         {
            InputStream is = wsdlFile.openStream();
            if (is == null)
               throw new IllegalArgumentException("Cannot obtain wsdl from: " + wsdlFile);

            return new InputSource(is);
         }
         catch (IOException e)
         {
            throw new RuntimeException("Cannot access wsdl from: " + wsdlFile);
         }
      }

      public String getBaseURI()
      {
         return wsdlFile.toExternalForm();
      }

      public InputSource getImportInputSource(String parent, String relative)
      {
         log.debug("getImportInputSource [parent=" + parent + ",relative=" + relative + "]");

         int index = wsdlFile.toExternalForm().lastIndexOf("/");
         String wsdlImport = wsdlFile.toExternalForm().substring(0, index) + "/" + relative;

         try
         {
            InputStream is = new URL(wsdlImport).openStream();
            if (is == null)
               throw new IllegalArgumentException("Cannot import wsdl from: " + wsdlImport);

            latestImportURI = wsdlImport;
            return new InputSource(is);
         }
         catch (IOException e)
         {
            throw new RuntimeException("Cannot access imported wsdl from: " + wsdlImport);
         }
      }

      public String getLatestImportURI()
      {
         return latestImportURI;
      }
   }
}
