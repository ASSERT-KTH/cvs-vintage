/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import org.jboss.ejb.DeploymentException;


/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @version $Revision: 1.4 $
 */
public abstract class MetaData implements XmlLoadable {
    // Constants -----------------------------------------------------
	public static final byte TX_NOT_SUPPORTED  = 0;
    public static final byte TX_REQUIRED       = 1;
    public static final byte TX_SUPPORTS       = 2;
    public static final byte TX_REQUIRES_NEW   = 3;
    public static final byte TX_MANDATORY      = 4;
    public static final byte TX_NEVER          = 5;
    public static final byte TX_UNKNOWN        = 6;
    
    // Attributes ----------------------------------------------------
    
    // Static --------------------------------------------------------
    public static Iterator getChildrenByTagName(Element element, String tagName) {
		// getElementsByTagName gives the corresponding elements in the whole descendance.
	    // We want only children
		
		NodeList children = element.getChildNodes();
		ArrayList goodChildren = new ArrayList();
		for (int i=0; i<children.getLength(); i++) {
			Node currentChild = children.item(i);
			if (currentChild.getNodeType() == Node.ELEMENT_NODE && 
				((Element)currentChild).getTagName().equals(tagName)) {
				goodChildren.add((Element)currentChild);
			}
		}
		return goodChildren.iterator();
	}
	
	
	public static Element getUniqueChild(Element element, String tagName) throws DeploymentException {
		Iterator goodChildren = getChildrenByTagName(element, tagName);
		
		if (goodChildren.hasNext()) {
			Element child = (Element)goodChildren.next();
			if (goodChildren.hasNext()) {
				throw new DeploymentException("expected only one " + tagName + " tag");
			}
			return child;
		} else {
			throw new DeploymentException("expected one " + tagName + " tag");
		}
	}
	
	
	public static Element getOptionalChild(Element element, String tagName) throws DeploymentException {
		Iterator goodChildren = getChildrenByTagName(element, tagName);

		if (goodChildren.hasNext()) {
			Element child = (Element)goodChildren.next();
			if (goodChildren.hasNext()) {
				throw new DeploymentException("expected only one " + tagName + " tag");
			}
			return child;
		} else {
			return null;
		}
	}
	
	public static String getElementContent(Element element) throws DeploymentException {
		if (element == null) return null;
		
		NodeList children = element.getChildNodes();
		if ((children.getLength() == 1) && (children.item(0).getNodeType() == Node.TEXT_NODE)) {
		    return children.item(0).getNodeValue();
		} else {
			return null;
		}
	}
		
    
    // Constructors --------------------------------------------------
    
    // Public --------------------------------------------------------
    public void importXml (Element element) throws DeploymentException {
		String rootTag = element.getOwnerDocument().getDocumentElement().getTagName();
		
		if (rootTag.equals("jboss")) {
			// import jboss.xml
			importJbossXml(element);
		} else if (rootTag.equals("ejb-jar")) {
		    // import ejb-jar.xml
			importEjbJarXml(element);
		} else {
			throw new DeploymentException("Unrecognized root tag : "+ rootTag);
		}
	}
	
	public void importEjbJarXml (Element element) throws DeploymentException {}
	public void importJbossXml (Element element) throws DeploymentException {}
	
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
	protected boolean jdk13Enabled() {
		// should use "java.version" ?
		String javaVersion = System.getProperty("java.vm.version");
		if (javaVersion.startsWith("1.2.2")) {
			return false;
		} else {
		    // other versions should be ok (>=1.3) or crash (<1.2.2)
			return true;
		}
	}
	
	// Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
}
