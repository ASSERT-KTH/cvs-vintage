/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.metadata;

import java.net.URL;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.jboss.logging.Log;

import org.jboss.ejb.DeploymentException;

import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.XmlFileLoader;


/**
 *	<description>
 *
 *	@see <related>
 *	@author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *	@version $Revision: 1.6 $
 */
public class JawsXmlFileLoader {

	// Attributes ----------------------------------------------------
    private ApplicationMetaData application;
	private ClassLoader classLoader;
	private ClassLoader localClassLoader;
    private Log log;


	// Constructors --------------------------------------------------
	public JawsXmlFileLoader(ApplicationMetaData app, ClassLoader cl, ClassLoader localCl, Log l) {
		application = app;
		classLoader = cl;
		localClassLoader = localCl;
		log = l;
	}


	// Public --------------------------------------------------------
    public JawsApplicationMetaData load() throws DeploymentException {

		// first create the metadata
		JawsApplicationMetaData jamd = new JawsApplicationMetaData(application, classLoader);

		// Load standardjaws.xml from the default classLoader
		// we always load defaults first
		URL stdJawsUrl = classLoader.getResource("standardjaws.xml");

		if (stdJawsUrl == null) throw new DeploymentException("No standardjaws.xml found");

		log.debug("Loading standardjaws.xml : " + stdJawsUrl.toString());
		Document stdJawsDocument = XmlFileLoader.getDocument(stdJawsUrl);
		jamd.importXml(stdJawsDocument.getDocumentElement());

		// Load jaws.xml if provided
		URL jawsUrl = localClassLoader.getResource("META-INF/jaws.xml");

		if (jawsUrl != null) {
			log.debug(jawsUrl.toString() + " found. Overriding defaults");
			Document jawsDocument = XmlFileLoader.getDocument(jawsUrl);
			jamd.importXml(jawsDocument.getDocumentElement());
		}

		// this can only be done once all the beans are built
		jamd.init();

		return jamd;
   }
}
