/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.net.URL;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.jboss.logging.Log;

import org.jboss.ejb.DeploymentException;

import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.XmlFileLoader;


/**
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 *	@author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *	@version $Revision: 1.1 $
 */
public class JDBCXmlFileLoader {
	
	// Attributes ----------------------------------------------------
	private ApplicationMetaData application;
	private ClassLoader classLoader;
	private ClassLoader localClassLoader;
	private Log log;
	
	
	// Constructors --------------------------------------------------
	public JDBCXmlFileLoader(ApplicationMetaData app, ClassLoader cl, ClassLoader localCl, Log l) {
		application = app;
		classLoader = cl;
		localClassLoader = localCl;
		log = l;
	}


	// Public --------------------------------------------------------
	public JDBCApplicationMetaData load() throws DeploymentException {
		
		// first create the metadata
		JDBCApplicationMetaData jamd = new JDBCApplicationMetaData(application, classLoader);
		
		// Load standardjbosscmp-jdbc.xml from the default classLoader
		// we always load defaults first
		URL stdJDBCUrl = classLoader.getResource("standardjbosscmp-jdbc.xml");
		
		if(stdJDBCUrl == null) {
			throw new DeploymentException("No standardjbosscmp-jdbc.xml found");
		}
		
		log.debug("Loading standardjbosscmp-jdbc.xml : " + stdJDBCUrl.toString());
		Document stdJDBCDocument = XmlFileLoader.getDocument(stdJDBCUrl);
		jamd.importXml(stdJDBCDocument.getDocumentElement());
		
		// Load jbosscmp-jdbc.xml if provided
		URL jdbcUrl = localClassLoader.getResource("META-INF/jbosscmp-jdbc.xml");
		
		if (jdbcUrl != null) {
			log.debug(jdbcUrl.toString() + " found. Overriding defaults");
			Document jdbcDocument = XmlFileLoader.getDocument(jdbcUrl);
			jamd.importXml(jdbcDocument.getDocumentElement());
		}
		
		// this can only be done once all the beans are built
		jamd.init();
		
		return jamd;
	}
}
