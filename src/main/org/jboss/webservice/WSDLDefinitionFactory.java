/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice;

// $Id: WSDLDefinitionFactory.java,v 1.11 2004/06/22 14:28:28 tdiesler Exp $

import org.jboss.logging.Logger;
import org.xml.sax.InputSource;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * A factory that creates a WSDL <code>Definition</code> from an URL.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-April-2004
 */
public class WSDLDefinitionFactory
{
   // provide logging
   private static final Logger log = Logger.getLogger(WSDLDefinitionFactory.class);

   // This feature is set by default in wsdl4j, it means the object structore contains the imported arguments
   public static final String FEATURE_IMPORT_DOCUMENTS = "javax.wsdl.importDocuments";
   // Set this feature for additional debugging output
   public static final String FEATURE_VERBOSE = "javax.wsdl.verbose";

   // The WSDLReader that is used by this factory
   private WSDLReader wsdlReader;

   // Hide constructor
   private WSDLDefinitionFactory() throws WSDLException
   {
      WSDLFactory wsdlFactory = WSDLFactory.newInstance();
      wsdlReader = wsdlFactory.newWSDLReader();
   }

   /** Create a new instance of a wsdl factory */
   public static WSDLDefinitionFactory newInstance() throws WSDLException
   {
      return new WSDLDefinitionFactory();
   }

   /** Set a feature on the underlying reader */
   public void setFeature(String name, boolean value) throws IllegalArgumentException
   {
      wsdlReader.setFeature(name, value);
   }

   /**
    * Read the wsdl document from the given URL
    */
   public Definition parse(URL wsdlLocation) throws WSDLException
   {
      if (wsdlLocation == null)
         throw new IllegalArgumentException("URL cannot be null");

      // wsdl4j is quite noisy on system out, we swallow the output
      PrintStream out = System.out;
      ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
      System.setOut(new PrintStream(baos));

      Definition wsdlDefinition = null;
      try
      {
         wsdlDefinition = wsdlReader.readWSDL(new WSDLLocatorImpl(wsdlLocation));
      }
      finally
      {
         System.setOut(out);
      }

      // write wsdl4j output as trace
      try
      {
         baos.close();
         BufferedReader br = new BufferedReader(new StringReader(new String(baos.toByteArray())));
         String line = br.readLine();
         while (line != null)
         {
            log.trace(line);
            line = br.readLine();
         }
      }
      catch (IOException ignore)
      {
         // do nothing
      }

      return wsdlDefinition;
   }

   /* A WSDLLocator that can handle wsdl imports
   */
   public static class WSDLLocatorImpl implements WSDLLocator
   {
      private URL wsdlURL;
      private String latestImportURI;

      public WSDLLocatorImpl(URL wsdlFile)
      {
         if (wsdlFile == null)
            throw new IllegalArgumentException("WSDL file argument cannot be null");

         this.wsdlURL = wsdlFile;
      }

      public InputSource getBaseInputSource()
      {
         try
         {
            InputStream is = wsdlURL.openStream();
            if (is == null)
               throw new IllegalArgumentException("Cannot obtain wsdl from: " + wsdlURL);

            return new InputSource(is);
         }
         catch (IOException e)
         {
            throw new RuntimeException("Cannot access wsdl from: " + wsdlURL);
         }
      }

      public String getBaseURI()
      {
         return wsdlURL.toExternalForm();
      }

      public InputSource getImportInputSource(String parent, String resource)
      {
         log.debug("getImportInputSource [parent=" + parent + ",resource=" + resource + "]");

         URL parentURL = null;
         try
         {
            parentURL = new URL(parent);
         }
         catch (MalformedURLException e)
         {
            log.error("Not a valid URL: " + parent);
            return null;
         }

         String wsdlImport = null;
         String external = parentURL.toExternalForm();

         // An external URL
         if (resource.startsWith("http://") || resource.startsWith("https://"))
         {
            wsdlImport = resource;
         }

         // Absolute path
         else if (resource.startsWith("/"))
         {
            String beforePath = external.substring(0, external.indexOf(parentURL.getPath()));
            wsdlImport = beforePath + resource;
         }

         // A relative path
         else
         {
            String parentDir = external.substring(0, external.lastIndexOf("/"));

            // remove references to current dir
            while (resource.startsWith("./"))
               resource = resource.substring(2);

            // remove references to parentdir
            while (resource.startsWith("../"))
            {
               parentDir = parentDir.substring(0, parentDir.lastIndexOf("/"));
               resource = resource.substring(3);
            }

            wsdlImport = parentDir + "/" + resource;
         }

         try
         {
            log.debug("Resolved to: " + wsdlImport);
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
