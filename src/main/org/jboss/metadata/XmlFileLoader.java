/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
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

import org.jboss.ejb.DeploymentException;

/**
 *   XmlFileLoader class is used to read ejb-jar.xml, standardjboss.xml, jboss.xml
 *   files, process them using DTDs and create ApplicationMetaData object for future using
 *
 *   @see <related>
 *   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @author <a href="mailto:WolfgangWerner@gmx.net">Wolfgang Werner</a>
 *   @author <a href="mailto:Darius.D@jbees.com">Darius Davidavicius</a>
 *   @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>
 *   @version $Revision: 1.14 $
 */
public class XmlFileLoader {
   	// Constants -----------------------------------------------------

   	// Attributes ----------------------------------------------------
    private static boolean defaultValidateDTDs = false;
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
	public ApplicationMetaData getMetaData() {
		return metaData;
    }

   /**
   Set the class loader
   @param ClassLoader cl - class loader
   */
	public void setClassLoader(ClassLoader cl) {
		classLoader = cl;
	}

   /**
   Gets the class loader
   @return ClassLoader - the class loader
   */
   public ClassLoader getClassLoader() {
		return classLoader;
	}

   /** Get the flag indicating that ejb-jar.dtd, jboss.dtd &
    jboss-web.dtd conforming documents should be validated
    against the DTD.
    */
   public boolean getValidateDTDs()
   {
       return validateDTDs;
   }
   /** Set the flag indicating that ejb-jar.dtd, jboss.dtd &
    jboss-web.dtd conforming documents should be validated
    against the DTD.
    */
   public void setValidateDTDs(boolean validate)
   {
       this.validateDTDs = validate;
   }

	/**
	* load()
	*
	* This method creates the ApplicationMetaData.
	* The configuration files are found in the classLoader.
	* The default jboss.xml and jaws.xml files are always read first, then we override
	* the defaults if the user provides them
	*
	*/
   	public ApplicationMetaData load() throws Exception {
      	// create the metadata
		metaData = new ApplicationMetaData();

		// Load ejb-jar.xml

		// we can always find the files in the classloader
		URL ejbjarUrl = getClassLoader().getResource("META-INF/ejb-jar.xml");

		if (ejbjarUrl == null) {
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

		if (defaultJbossUrl == null) {
			throw new DeploymentException("no standardjboss.xml found");
		}

		Document defaultJbossDocument = getDocumentFromURL(defaultJbossUrl);

		metaData.setUrl(defaultJbossUrl);
		metaData.importJbossXml(defaultJbossDocument.getDocumentElement());

		// Load jboss.xml
		// if this file is provided, then we override the defaults
		URL jbossUrl = getClassLoader().getResource("META-INF/jboss.xml");

		if (jbossUrl != null) {
//			Logger.debug(jbossUrl.toString() + " found. Overriding defaults");
			Document jbossDocument = getDocumentFromURL(jbossUrl);

			metaData.setUrl(jbossUrl);
			metaData.importJbossXml(jbossDocument.getDocumentElement());
		}

		return metaData;
	}

    /** Invokes getDocument(url, defaultValidateDTDs)
     */
    public static Document getDocument(URL url) throws DeploymentException
    {
        return getDocument(url, defaultValidateDTDs);
    }
    /** Get the xml file from the URL and parse it into a Document object.
     Calls new XmlFileLoader(validateDTDs).getDocumentFromURL(url);
     @param url, the URL from which the xml doc is to be obtained.
    @return Document
    */
    public static Document getDocument(URL url, boolean validateDTDs) throws DeploymentException
    {
        XmlFileLoader loader = new XmlFileLoader(validateDTDs);
        return loader.getDocumentFromURL(url);
    }

    /** Get the xml file from the URL and parse it into a Document object.
     Calls getDocument(url.openStream(), url.getPath());
     @param url, the URL from which the xml doc is to be obtained.
    @return Document
    */
    public Document getDocumentFromURL(URL url) throws DeploymentException
    {
        try
        {
            InputStream is = url.openStream();
            String docPath = url.getPath();
            return getDocument(is, docPath);
        } catch (IOException e) {
            throw new DeploymentException("Failed to obtain xml doc from URL", e);
        }
    }

    /** Parses the xml document in is and created the DOM Document. DTD validation
    is enabled if validateDTDs is true and we install an EntityResolver and
    ErrorHandler to resolve J2EE DTDs and handle errors.
    @param is, the InputStream container the xml descriptor to parse
    @param inPath, the path information for the xml doc. This is used for
    only for error reporting.
    @return Document
    */
    public Document getDocument(InputStream is, String inPath) throws DeploymentException 
    {
        try 
        {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            // Enable DTD validation based on our validateDTDs flag
            docBuilderFactory.setValidating(validateDTDs);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

            LocalResolver lr = new LocalResolver();
            ErrorHandler eh = new LocalErrorHandler( inPath, lr );
            docBuilder.setEntityResolver(lr);
            docBuilder.setErrorHandler(eh );

            Document doc = docBuilder.parse(is);
            return doc;
        } catch (SAXParseException e) {
        System.out.println(e.getMessage()+":"+e.getColumnNumber()+":"+e.getLineNumber());
            e.printStackTrace();
            throw new DeploymentException(e.getMessage(), e);
        } catch (SAXException e) {
            System.out.println(e.getException());
            throw new DeploymentException(e.getMessage(), e);
        } catch (Exception e) {
            throw new DeploymentException(e.getMessage(), e);
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
    * @author <a href="mailto:WolfgangWerner@gmx.net">Wolfgang Werner</a>
    * @author <a href="mailto:Darius.D@jbees.com">Darius Davidavicius</a>
    **/
	private static class LocalResolver implements EntityResolver
    {
        private Hashtable dtds = new Hashtable();
        private boolean hasDTD = false;

		public LocalResolver() {
            registerDTD("-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 1.1//EN", "ejb-jar.dtd");
            registerDTD("-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 2.0//EN", "ejb-jar_2_0.dtd");
            registerDTD("-//Sun Microsystems, Inc.//DTD J2EE Application 1.2//EN", "application_1_2.dtd");
            registerDTD("-//Sun Microsystems, Inc.//DTD Connector 1.0//EN", "connector_1_0.dtd");
            registerDTD("-//JBoss//DTD JAWS//EN", "jaws.dtd");
            registerDTD("-//JBoss//DTD JBOSS//EN","jboss.dtd");
		}

        /**
        Registers available DTDs
        @param String publicId    - Public ID of DTD
        @param String dtdFileName - the file name of DTD
        */
        public void registerDTD(String publicId, String dtdFileName) {
            dtds.put(publicId, dtdFileName);
        }

        /**
        Returns DTD inputSource. Is DTD was found in the hashtable and inputSource was created
        flad hasDTD is ser to true.
        @param String publicId    - Public ID of DTD
        @param String dtdFileName - the file name of DTD
        @return InputSource of DTD
        */
        public InputSource resolveEntity (String publicId, String systemId)
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
                } catch( Exception ex ) {
                    // ignore
                }
            }
            return null;
		}

      /**
      Returns the boolean value to inform id DTD was found in the XML file or not
      @return boolean - true if DTD was found in XML
      */
      public boolean hasDTD ()
      {
          return hasDTD;
      }

	}

    /** Local error handler for entity resolver to DocumentBuilder parser.
    Error is printed to output just if DTD was detected in the XML file. 
    If DTD was not found in XML file it is assumed that the EJB builder
    doesn't want to use DTD validation. Validation may have been enabled via
    validateDTDs flag so we look to the hasDTD() function in the LocalResolver
    and reject errors if DTD not used.
    @author <a href="mailto:WolfgangWerner@gmx.net">Wolfgang Werner</a>
    @author <a href="mailto:Darius.D@jbees.com">Darius Davidavicius</a>
    **/
    private static class LocalErrorHandler implements ErrorHandler
    {
        // The xml file being parsed
        private String theFileName;
        private LocalResolver localResolver;
        public LocalErrorHandler( String inFileName, LocalResolver localResolver )
        {
            this.theFileName = inFileName;
            this.localResolver = localResolver;
        }

        public void error(SAXParseException exception) 
        {
            if ( localResolver.hasDTD() )
            {
                System.out.println("XmlFileLoader: File "
                                  + theFileName
                                  + " process error. Line: "
                                  + String.valueOf(exception.getLineNumber())
                                  + ". Error message: "
                                  + exception.getMessage()
                                 );
            }//end if
        }
        public void fatalError(SAXParseException exception) 
        {
            if ( localResolver.hasDTD() )
            {
                System.out.println("XmlFileLoader: File "
                                  + theFileName
                                  + " process fatal error. Line: "
                                  + String.valueOf(exception.getLineNumber())
                                  + ". Error message: "
                                  + exception.getMessage()
                                );
            }//end if
        }
        public void warning(SAXParseException exception) 
        {
            if ( localResolver.hasDTD() )
            {
                System.out.println("XmlFileLoader: File "
                                  + theFileName
                                  + " process warning. Line: "
                                  + String.valueOf(exception.getLineNumber())
                                  + ". Error message: "
                                  + exception.getMessage()
                                );
            }//end if
        }
    }// end class LocalErrorHandler

}
/* Change log:

 * Author: starksm, Date: Thu Jun 14 17:14:14  2001 GMT
 Incorporated Darius Davidavicius changes to support DTD validation.
 */
