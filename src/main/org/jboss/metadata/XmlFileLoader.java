/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org
 */
package org.jboss.metadata;

import java.net.URL;
import java.io.IOException;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.jboss.deployment.DeploymentException;
import org.jboss.logging.Logger;

/**
 * XmlFileLoader class is used to read ejb-jar.xml, standardjboss.xml,
 * jboss.xml files, process them using DTDs and create ApplicationMetaData
 * object for future using
 *
 * @see <related>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:WolfgangWerner@gmx.net">Wolfgang Werner</a>
 * @author <a href="mailto:Darius.D@jbees.com">Darius Davidavicius</a>
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 *
 * @version $Revision: 1.35 $
 *
 * Revisions:
 *
 *   20010620 Bill Burke: Print an error message when failing to load
 *                        standardjboss.xml or jboss.xml. It was a pain
 *                        to debug a standardjboss.xml syntax error.
 *
 */
public class XmlFileLoader
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   private static boolean defaultValidateDTDs = false;
   private static Logger log = Logger.getLogger(XmlFileLoader.class);
   private ClassLoader classLoader;
   private ApplicationMetaData metaData;
   private boolean validateDTDs;

   // Static --------------------------------------------------------
   public static boolean getDefaultValidateDTDs()
   {
      return defaultValidateDTDs;
   }

   public static void setDefaultValidateDTDs(boolean validate)
   {
      defaultValidateDTDs = validate;
   }


   // Constructors --------------------------------------------------
   public XmlFileLoader()
   {
      this(defaultValidateDTDs);
   }

   public XmlFileLoader(boolean validateDTDs)
   {
      this.validateDTDs = validateDTDs;
   }

   // Public --------------------------------------------------------
   public ApplicationMetaData getMetaData()
   {
      return metaData;
   }

   /**
    * Set the class loader
    *
    * @param ClassLoader cl - class loader
    */
   public void setClassLoader(ClassLoader cl)
   {
      classLoader = cl;
   }

   /**
    * Gets the class loader
    *
    * @return ClassLoader - the class loader
    */
   public ClassLoader getClassLoader()
   {
      return classLoader;
   }


   /**
    * Get the flag indicating that ejb-jar.dtd, jboss.dtd &amp;
    * jboss-web.dtd conforming documents should be validated
    * against the DTD.
    */
   public boolean getValidateDTDs()
   {
      return validateDTDs;
   }

   /** Set the flag indicating that ejb-jar.dtd, jboss.dtd &amp;
    * jboss-web.dtd conforming documents should be validated
    * against the DTD.
    */
   public void setValidateDTDs(boolean validate)
   {
      this.validateDTDs = validate;
   }

   /**
    * This method creates the ApplicationMetaData.
    *
    * The configuration files are found in the classLoader.
    * The default jboss.xml and jaws.xml files are always read first,
    * then we override the defaults if the user provides them
    *
    * @return The ApplicationMetaData loaded from ejb-jar.xml merged with
    *         the settings from standardjboss.xml and jboss.xml
    */
   public ApplicationMetaData load()
      throws Exception
   {
      // create the metadata
      metaData = new ApplicationMetaData();
      // Load ejb-jar.xml
      // we can always find the files in the classloader
      URL ejbjarUrl = getClassLoader().getResource("META-INF/ejb-jar.xml");
      if (ejbjarUrl == null)
      {
         throw new DeploymentException("no ejb-jar.xml found");
      }

      Document ejbjarDocument = getDocumentFromURL(ejbjarUrl);

      // the url may be used to report errors
      metaData.setUrl(ejbjarUrl);
      metaData.importEjbJarXml(ejbjarDocument.getDocumentElement());

      // Load jbossdefault.xml from the default classLoader
      // we always load defaults first
      // we use the context classloader, because this guy has to know where
      // this file is
      URL defaultJbossUrl = Thread.currentThread().getContextClassLoader().getResource("standardjboss.xml");
      if (defaultJbossUrl == null)
      {
         throw new DeploymentException("no standardjboss.xml found");
      }

      Document defaultJbossDocument = null;
      try
      {
         defaultJbossDocument = getDocumentFromURL(defaultJbossUrl);
         metaData.setUrl(defaultJbossUrl);
         metaData.importJbossXml(defaultJbossDocument.getDocumentElement());
      }
      catch (Exception ex)
      {
         log.error("failed to load standardjboss.xml.  There could be a " +
            "syntax error.", ex);
         throw ex;
      }

      // Load jboss.xml
      // if this file is provided, then we override the defaults
      try
      {
         URL jbossUrl = getClassLoader().getResource( "META-INF/jboss.xml" );
         if (jbossUrl != null)
         {
            Document jbossDocument = getDocumentFromURL(jbossUrl);
            metaData.setUrl(jbossUrl);
            metaData.importJbossXml(jbossDocument.getDocumentElement());
         }
      }
      catch (Exception ex) // FIXME: Make this more concrete
      {
         log.error("failed to load jboss.xml.  There could be a syntax " +
            "error.", ex);
         throw ex;
      }

      return metaData;
   }

   /** Invokes getDocument(url, defaultValidateDTDs)
    *
    */
   public static Document getDocument(URL url) throws DeploymentException
   {
      return getDocument(url, defaultValidateDTDs);
   }

   /**
    * Get the xml file from the URL and parse it into a Document object.
    * Calls new XmlFileLoader(validateDTDs).getDocumentFromURL(url);
    *
    * @param url, the URL from which the xml doc is to be obtained.
    *
    * @return Document
    */
   public static Document getDocument(URL url, boolean validateDTDs)
      throws DeploymentException
   {
      XmlFileLoader loader = new XmlFileLoader(validateDTDs);
      return loader.getDocumentFromURL(url);
   }

   /**
    * Get the xml file from the URL and parse it into a Document object.
    * Calls getDocument(url.openStream(), url.getPath());
    *
    * @param url, the URL from which the xml doc is to be obtained.
    *
    * @return Document
    */
   public Document getDocumentFromURL(URL url) throws DeploymentException
   {
      try
      {
         InputStream is = url.openStream();
         String docPath = url.getPath();

         return getDocument(is, docPath);
      }
      catch (IOException e)
      {
         throw new DeploymentException("Failed to obtain xml doc from URL", e);
      }
   }

   /**
    * Parses the xml document in is and created the DOM Document. DTD
    * validation is enabled if validateDTDs is true and we install an
    * EntityResolver and ErrorHandler to resolve J2EE DTDs and handle
    * errors.
    *
    * @param is the InputStream container the xml descriptor to parse
    * @param inPath the path information for the xml doc. This is used for
    *               only for error reporting.
    *
    * @return Document
    */
   public Document getDocument(InputStream is, String inPath)
      throws DeploymentException
   {
      try
      {
         DocumentBuilderFactory docBuilderFactory =
            DocumentBuilderFactory.newInstance();

         // Enable DTD validation based on our validateDTDs flag
         docBuilderFactory.setValidating(validateDTDs);
         DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
         LocalResolver lr = new LocalResolver();
         LocalErrorHandler eh = new LocalErrorHandler( inPath, lr );
         docBuilder.setEntityResolver(lr);
         docBuilder.setErrorHandler(eh );

         Document doc = docBuilder.parse(is);
         if(validateDTDs && eh.hadError())
         {
            throw new DeploymentException("Invalid XML: file=" + inPath);
         }
         return doc;

      }
      catch( DeploymentException e )
      {
         // rethrow
         throw e;
      }
      catch( SAXParseException e )
      {
         log.error( e.getMessage() + ":" +e.getColumnNumber() + ":" +
            e.getLineNumber(), e );
         throw new DeploymentException( "Invalid XML: file='" + inPath +
            "' - " + e.getMessage(), e);
      }
      catch( SAXException e )
      {
         log.error( e.getException() );
         throw new DeploymentException( "Invalid XML: file='" + inPath +
            "' - " + e.getMessage(), e);
      }
      catch( Exception e )
      {
         throw new DeploymentException(e.getMessage(), e);
      }
      finally {
         // get around "too many open files" errors
         try {
            is.close();
         }
         catch (Exception e)
         {
            // ignore
         }
      }
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

   /**
    * Local entity resolver to handle J2EE DTDs. With this a http connection
    * to sun is not needed during deployment.
    * Function boolean hadDTD() is here to avoid validation errors in
    * descriptors that do not have a DOCTYPE declaration.
    **/
   private static class LocalResolver implements EntityResolver
   {
      private Hashtable dtds = new Hashtable();
      private boolean hasDTD = false;

      /** Register the mapping of the DOCTYPE public ID names to the DTD file
       */
      public LocalResolver()
      {
         registerDTD("-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 1.1//EN", "ejb-jar.dtd");
         registerDTD("-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 2.0//EN", "ejb-jar_2_0.dtd");
         registerDTD("-//Sun Microsystems, Inc.//DTD J2EE Application 1.2//EN", "application_1_2.dtd");
         registerDTD("-//Sun Microsystems, Inc.//DTD J2EE Application 1.3//EN", "application_1_3.dtd");
         registerDTD("-//Sun Microsystems, Inc.//DTD Connector 1.0//EN", "connector_1_0.dtd");
         registerDTD("-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN", "web-app_2_2.dtd");
         registerDTD("-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN", "web-app_2_3.dtd");
         registerDTD("-//JBoss//DTD JAWS//EN", "jaws.dtd");
         registerDTD("-//JBoss//DTD JAWS 2.4//EN", "jaws_2_4.dtd");
         registerDTD("-//JBoss//DTD JAWS 3.0//EN", "jaws_3_0.dtd");
         registerDTD("-//JBoss//DTD JBOSS//EN","jboss.dtd");
         registerDTD("-//JBoss//DTD JBOSS 2.4//EN","jboss_2_4.dtd");
         registerDTD("-//JBoss//DTD JBOSS 3.0//EN","jboss_3_0.dtd");
         registerDTD("-//JBoss//DTD JBOSS 3.1//EN","jboss_3_1.dtd");
         registerDTD("-//JBoss//DTD JBOSS 3.2//EN","jboss_3_2.dtd");
         registerDTD("-//JBoss//DTD JBOSSCMP-JDBC 3.0//EN", "jbosscmp-jdbc_3_0.dtd");
         registerDTD("-//JBoss//DTD JBOSSCMP-JDBC 3.2//EN", "jbosscmp-jdbc_3_2.dtd");
         registerDTD("-//JBoss//DTD JBOSSCMP-JDBC 4.0//EN", "jbosscmp-jdbc_4_0.dtd");
         registerDTD("-//JBoss//DTD Web Application 2.2//EN", "jboss-web.dtd");
         registerDTD("-//JBoss//DTD Web Application 2.3//EN", "jboss-web_3_0.dtd");
      }

      /**
       * Registers available DTDs
       *
       * @param String publicId    - Public ID of DTD
       * @param String dtdFileName - the file name of DTD
       */
      public void registerDTD(String publicId, String dtdFileName)
      {
         dtds.put(publicId, dtdFileName);
      }

      /**
       * Returns DTD inputSource. Is DTD was found in the hashtable and
       * inputSource was created flag hasDTD is set to true.
       *
       * @param publicId Public ID of DTD
       * @param dtdFileName the file name of DTD
       *
       * @return InputSource of DTD
       */
      public InputSource resolveEntity(String publicId, String systemId)
      {
         hasDTD = false;
         String dtd = (String)dtds.get(publicId);

         if (dtd != null)
         {
            hasDTD = true;
            try
            {
               InputStream dtdStream = getClass().getResourceAsStream(dtd);
               InputSource aInputSource = new InputSource(dtdStream);
               return aInputSource;
            }
            catch( Exception ignore )
            {
            }
         }
         return null;
      }

      /**
       * Returns the boolean value to inform id DTD was found in the XML
       * file or not.
       *
       * @return true if DTD was found in XML, false otherwise
       */
      public boolean hasDTD()
      {
         return hasDTD;
      }
   }

   /**
    * Local error handler for entity resolver to DocumentBuilder parser.
    * Error is printed to output just if DTD was detected in the XML file.
    * If DTD was not found in XML file it is assumed that the EJB builder
    * doesn't want to use DTD validation. Validation may have been enabled via
    * via validateDTDs flag so we look to the hasDTD() function in the
    * LocalResolver and reject errors if DTD not used.
    */
   private static class LocalErrorHandler implements ErrorHandler
   {
      // The xml file being parsed
      private String theFileName;
      private LocalResolver localResolver;
      private boolean error;

      public LocalErrorHandler( String inFileName, LocalResolver localResolver )
      {
         this.theFileName = inFileName;
         this.localResolver = localResolver;
         this.error = false;
      }

      public void error(SAXParseException exception)
      {
         if ( localResolver.hasDTD() )
         {
            this.error = true;
            log.error("XmlFileLoader: File "
               + theFileName
               + " process error. Line: "
               + String.valueOf(exception.getLineNumber())
               + ". Error message: "
               + exception.getMessage()
            );
         }
      }

      public void fatalError(SAXParseException exception)
      {
         if ( localResolver.hasDTD() )
         {
            this.error = true;
            log.error("XmlFileLoader: File "
               + theFileName
               + " process fatal error. Line: "
               + String.valueOf(exception.getLineNumber())
               + ". Error message: "
               + exception.getMessage()
            );
         }
      }

      public void warning(SAXParseException exception)
      {
         if ( localResolver.hasDTD() )
         {
            this.error = true;
            log.error("XmlFileLoader: File "
               + theFileName
               + " process warning. Line: "
               + String.valueOf(exception.getLineNumber())
               + ". Error message: "
               + exception.getMessage()
            );
         }
      }

      public boolean hadError() {
         return error;
      }
   }// end class LocalErrorHandler
}
/*
vim:ts=3:sw=3:et
*/
