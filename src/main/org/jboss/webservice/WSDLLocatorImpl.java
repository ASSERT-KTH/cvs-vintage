/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice;

// $Id: WSDLLocatorImpl.java,v 1.1 2004/05/10 16:26:30 tdiesler Exp $

import org.jboss.logging.Logger;
import org.xml.sax.InputSource;

import javax.wsdl.xml.WSDLLocator;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;

/**
 * A WSDLLocator that can handle wsdl imports
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 15-April-2004
 */
public class WSDLLocatorImpl implements WSDLLocator
{
   // provide logging
   private static final Logger log = Logger.getLogger(WSDLLocatorImpl.class);

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
