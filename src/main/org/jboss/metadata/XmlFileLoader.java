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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.EntityResolver;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.jboss.ejb.DeploymentException;
//import org.jboss.logging.Logger;

/**
 *   <description>
 *
 *   @see <related>
 *   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @author <a href="mailto:WolfgangWerner@gmx.net">Wolfgang Werner</a>
 *   @version $Revision: 1.13 $
 */
public class XmlFileLoader {
   	// Constants -----------------------------------------------------

   	// Attributes ----------------------------------------------------
	ClassLoader classLoader;

	ApplicationMetaData metaData;

	// Static --------------------------------------------------------

	// Constructors --------------------------------------------------
   	public XmlFileLoader() {
	}

	// Public --------------------------------------------------------
	public ApplicationMetaData getMetaData() {
		return metaData;
    }

	public void setClassLoader(ClassLoader cl) {
		classLoader = cl;
	}

   	public ClassLoader getClassLoader() {
		return classLoader;
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

//		Logger.debug("Loading ejb-jar.xml : " + ejbjarUrl.toString());
		Document ejbjarDocument = getDocument(ejbjarUrl);

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

//		Logger.debug("Loading standardjboss.xml : " + defaultJbossUrl.toString());
		Document defaultJbossDocument = getDocument(defaultJbossUrl);

		metaData.setUrl(defaultJbossUrl);
		metaData.importJbossXml(defaultJbossDocument.getDocumentElement());

		// Load jboss.xml
		// if this file is provided, then we override the defaults
		URL jbossUrl = getClassLoader().getResource("META-INF/jboss.xml");

		if (jbossUrl != null) {
//			Logger.debug(jbossUrl.toString() + " found. Overriding defaults");
			Document jbossDocument = getDocument(jbossUrl);

			metaData.setUrl(jbossUrl);
			metaData.importJbossXml(jbossDocument.getDocumentElement());
		}

		return metaData;
	}

   	// Package protected ---------------------------------------------

   	// Protected -----------------------------------------------------
	public static Document getDocument(URL url) throws DeploymentException {

      try {
         return getDocument (url.openStream());
      } catch (IOException _ioe) {
			throw new DeploymentException(_ioe.getMessage());
      }
	}

	public static Document getDocument(InputStream _in) throws DeploymentException
   {
		try
      {
			Reader in = new InputStreamReader(_in);

         DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
         EntityResolver er = new LocalResolver();
         docBuilder.setEntityResolver(er);
         Document doc = docBuilder.parse(_in);
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

	// Private -------------------------------------------------------

	// Inner classes -------------------------------------------------
	/**
	 * Local entity resolver to handle EJB 1.1 DTD. With this a http connection
	 * to sun is not needed during deployment.
	 * @author <a href="mailto:WolfgangWerner@gmx.net">Wolfgang Werner</a>
	 **/
	private static class LocalResolver implements EntityResolver {
		private Hashtable dtds = new Hashtable();

		public LocalResolver() {
			registerDTD("-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 1.1//EN", "ejb-jar.dtd");
            registerDTD("-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 2.0//EN", "ejb-jar_2_0.dtd");
			registerDTD("-//Sun Microsystems, Inc.//DTD J2EE Application 1.2//EN", "application_1_2.dtd");
			registerDTD("-//Sun Microsystems, Inc.//DTD Connector 1.0//EN", "connector_1_0.dtd");
   		registerDTD("-//JBoss//DTD JAWS//EN", "jaws.dtd");
		}

		public void registerDTD(String publicId, String dtdFileName) {
			dtds.put(publicId, dtdFileName);
		}

		public InputSource resolveEntity (String publicId, String systemId) {
			String dtd = (String)dtds.get(publicId);

			if (dtd != null) {
				try {
					InputStream dtdStream = getClass().getResourceAsStream(dtd);
					return new InputSource(dtdStream);
				} catch( Exception ex ) {
					// ignore
				}
			}
			return null;
		}
	}

}
