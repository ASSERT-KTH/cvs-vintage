/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
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
import org.xml.sax.EntityResolver;

import org.jboss.ejb.DeploymentException;
import org.jboss.logging.Logger;

/**
 *   <description>
 *
 *   @see <related>
 *   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @author <a href="mailto:WolfgangWerner@gmx.net">Wolfgang Werner</a>
 *   @version $Revision: 1.4 $
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
		
		Logger.log("Loading ejb-jar.xml : " + ejbjarUrl.toString());
		Document ejbjarDocument = getDocument(ejbjarUrl);
		
		// the url may be used to report errors
		metaData.setUrl(ejbjarUrl);
		metaData.importEjbJarXml(ejbjarDocument.getDocumentElement());
		
		// Load jbossdefault.xml from the default classLoader
		// we always load defaults first
		URL defaultJbossUrl = getClass().getResource("standardjboss.xml");
		
		if (defaultJbossUrl == null) {
			throw new DeploymentException("no standardjboss.xml found");
		}
		
		Logger.log("Loading standardjboss.xml : " + defaultJbossUrl.toString());
		Document defaultJbossDocument = getDocument(defaultJbossUrl);
		
		metaData.setUrl(defaultJbossUrl);
		metaData.importJbossXml(defaultJbossDocument.getDocumentElement());
		
		// Load jboss.xml
		// if this file is provided, then we override the defaults
		URL jbossUrl = getClassLoader().getResource("META-INF/jboss.xml");
		
		if (jbossUrl != null) {
			Logger.log(jbossUrl.toString() + " found. Overriding defaults");
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
			Reader in = new InputStreamReader(url.openStream());
			com.sun.xml.tree.XmlDocumentBuilder xdb = new com.sun.xml.tree.XmlDocumentBuilder();
		
			Parser parser = new com.sun.xml.parser.Parser();
		
			// Use a local entity resolver to get rid of the DTD loading via internet
			EntityResolver er = new LocalResolver();
			parser.setEntityResolver(er);
			xdb.setParser(parser);
			
			parser.parse(new InputSource(in));
			return xdb.getDocument();
		} catch (Exception e) { 
			throw new DeploymentException(e.getMessage()); 
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
