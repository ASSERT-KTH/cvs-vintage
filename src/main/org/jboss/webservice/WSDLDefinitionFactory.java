/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice;

// $Id: WSDLDefinitionFactory.java,v 1.7 2004/06/09 13:41:40 tdiesler Exp $

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

   /**
    * Read the wsdl document from the given URL
    */
   public static Definition parse(URL wsdlLocation) throws WSDLException
   {
      // wsdl4j is quite noisy on system out, we swallow the output
      PrintStream out = System.out;
      ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
      System.setOut(new PrintStream(baos));

      Definition wsdlDefinition = null;
      try
      {
         WSDLFactory wsdlFactory = WSDLFactory.newInstance();
         WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
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
         while(line != null)
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

      public InputSource getImportInputSource(String parent, String relative)
      {
         log.trace("getImportInputSource [parent=" + parent + ",relative=" + relative + "]");

         String parentDir = parent.substring(0, parent.lastIndexOf("/"));

         // remove references to current dir
         while (relative.startsWith("./"))
            relative = relative.substring(2);

         // remove references to parentdir
         while (relative.startsWith("../"))
         {
            parentDir = parentDir.substring(0, parentDir.lastIndexOf("/"));
            relative = relative.substring(3);
         }

         String wsdlImport = parentDir + "/" + relative;

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
