/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Element;
import org.jboss.ejb.DeploymentException;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @version $Revision: 1.2 $
 */
public class EntityMetaData extends BeanMetaData {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
	private boolean cmp;
    private String primaryKeyClass;
    private boolean reentrant;
    private ArrayList cmpFields = new ArrayList();
	private String primKeyField;
	
    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
	public EntityMetaData(ApplicationMetaData app) {
		super(app);
		session = false;
	}

    // Public --------------------------------------------------------
	public boolean isCMP() { return cmp; }
	public boolean isBMP() { return !cmp; }
	public boolean isReentrant() { return reentrant; }
	public String getPrimaryKeyClass() { return primaryKeyClass; }
	public String[] getCMPFields() { return (String[]) cmpFields.toArray(); }
	public String getPrimKeyField() { return primKeyField; }
	
	public String getDefaultConfigurationName() {
		if (isCMP()) {
			return jdk13Enabled() ? ConfigurationMetaData.CMP_13 : ConfigurationMetaData.CMP_12;
		} else {
			return jdk13Enabled() ? ConfigurationMetaData.BMP_13 : ConfigurationMetaData.BMP_12;
		}
	}
	
	public void importEjbJarXml(Element element) throws DeploymentException {
		super.importEjbJarXml(element);
		
		// set persistence type
		String persistenceType = getElementContent(getUniqueChild(element, "persistence-type"));
		if (persistenceType.equals("Bean")) {
			cmp = false;
		} else if (persistenceType.equals("Container")) {
			cmp = true;
		} else {
			throw new DeploymentException("persistence-type should be 'Bean' or 'Container'");
		}
		
		// set primary key class
		primaryKeyClass = getElementContent(getUniqueChild(element, "prim-key-class"));
    
	 	// set reentrant
		reentrant = Boolean.valueOf(getElementContent(getUniqueChild(element, "reentrant"))).booleanValue();
		
		// set the cmp fields
		if (isCMP()) {
			Iterator iterator = getChildrenByTagName(element, "cmp-fields");			
			while (iterator.hasNext()) {
				Element field = (Element)iterator.next();
				cmpFields.add(getElementContent(getUniqueChild(field, "field-name")));
			}
		}
		
		// set the primary key field
		if (isCMP()) {
			primKeyField = getElementContent(getOptionalChild(element, "primkey-field"));
		}
	}


	
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
}
