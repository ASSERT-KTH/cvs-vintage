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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.jboss.ejb.DeploymentException;

/**
 *   <description>
 *
 *   @see <related>
 *   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @version $Revision: 1.1 $
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
		
		System.out.println("Loading ejb-jar.xml : " + ejbjarUrl.toString());
		Document ejbjarDocument = getDocument(ejbjarUrl);
		
		// the url may be used to report errors
		metaData.setUrl(ejbjarUrl);
		metaData.importEjbJarXml(ejbjarDocument.getDocumentElement());
		
		// Load jbossdefault.xml from the default classLoader
		// we always load defaults first
		URL defaultJbossUrl = getClass().getResource("defaultjboss.xml");
		
		if (defaultJbossUrl == null) {
			throw new DeploymentException("no defaultjboss.xml found");
		}
		
		System.out.println("Loading defaultjboss.xml : " + defaultJbossUrl.toString());
		Document defaultJbossDocument = getDocument(defaultJbossUrl);
		
		metaData.setUrl(defaultJbossUrl);
		metaData.importJbossXml(defaultJbossDocument.getDocumentElement());
		
		// Load jboss.xml
		// if this file is provided, then we override the defaults
		URL jbossUrl = getClassLoader().getResource("META-INF/jboss.xml");
		
		if (jbossUrl != null) {
			System.out.println(jbossUrl.toString() + " found. Overriding defaults");
			Document jbossDocument = getDocument(jbossUrl);
			
			metaData.setUrl(jbossUrl);
			metaData.importJbossXml(jbossDocument.getDocumentElement());
		}	
		
		return metaData;
	}

   	// Package protected ---------------------------------------------

   	// Protected -----------------------------------------------------
	protected Document getDocument(URL url) throws IOException {
		Reader in = new InputStreamReader(url.openStream());
		com.sun.xml.tree.XmlDocumentBuilder xdb = new com.sun.xml.tree.XmlDocumentBuilder();
		Parser parser = new com.sun.xml.parser.Parser();
		xdb.setParser(parser);
		
		try {
		    parser.parse(new InputSource(in));
			return xdb.getDocument();
		} 
		catch (SAXException se) { 
			throw new IOException(se.getMessage()); 
		}
	}

	// Private -------------------------------------------------------

	// Inner classes -------------------------------------------------
}
