/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import org.w3c.dom.Element;
import org.jboss.ejb.DeploymentException;

/** 
 * Represents one ejb-relationship-role element found in the ejb-jar.xml
 * file's ejb-relation elements.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.1 $
 */
public class RelationshipRoleMetaData extends MetaData {
	// one is one
	private static int ONE = 1;
	// and two is many :)
	private static int MANY = 2;
	
	/**
	 * Role name
	 */
	private String relationshipRoleName;
	
	/**
	 * Multiplicity of role, ONE or MANY.
	 */
	private int multiplicity;
	
	/**
	 * Should this entity be deleted when related entity is deleted.
	 */
	private boolean cascadeDelete;
	
	/**
	 * Name of the entity that has this role.
	 */
	private String entityName;
	
	/**
	 * Name of the entity's cmr field for this role.
	 */
	private String cmrFieldName;
	
	/**
	 * Type of the cmr field (i.e., collection or set)
	 */
	private String cmrFieldType;

	/**
	 * Gets the relationship role name
	 */
	public String getRelationshipRoleName() {
		return relationshipRoleName;
	}

	/**
	 * Checks if the multiplicity is one.
	 */
	public boolean isMultiplicityOne() {
		return multiplicity == ONE;
	}
	
	/**
	 * Checks if the multiplicity is many.
	 */
	public boolean isMultiplicityMany() {
		return multiplicity == MANY;
	}
	
	/**
	 * Should this entity be deleted when related entity is deleted.
	 */
	public boolean isCascadeDelete() {
		return cascadeDelete;
	}
	
	/**
	 * Gets the name of the entity that has this role.
	 */
	public String getEntityName() {
		return entityName;
	}
	
	/**
	 * Gets the name of the entity's cmr field for this role.
	 */
	public String getCMRFieldName() {
		return cmrFieldName;
	}
	
	/**
	 * Gets the type of the cmr field (i.e., collection or set)
	 */
	public String getCMRFieldType() {
		return cmrFieldType;
	}
	
   public void importEjbJarXml (Element element) throws DeploymentException {
	   // ejb-relationship-role-name?
		relationshipRoleName = getElementContent(getOptionalChild(element, "ejb-relationship-role-name"));
		
		// multiplicity
		String multiplicityString = getElementContent(getUniqueChild(element, "multiplicity"));
		if("One".equals(multiplicityString)) {
			multiplicity = ONE;
		} else if("Many".equals(multiplicityString)) {
			multiplicity = MANY;
		} else {
			throw new DeploymentException("multiplicity should be One or Many but is " + multiplicityString);
		}
		
		// cascade-delete? 
		Element cascadeDeleteElement = getOptionalChild(element, "cascade-delete");
		if(cascadeDeleteElement != null) {
			cascadeDelete = true;
		}
		
		// relationship-role-source
		Element relationshipRoleSourceElement = getUniqueChild(element, "relationship-role-source");
		entityName = getElementContent(getUniqueChild(relationshipRoleSourceElement, "ejb-name"));
		
		// cmr-field?
		Element cmrFieldElement = getOptionalChild(element, "cmr-field");
		if(cmrFieldElement != null) {
			// cmr-field-name
			cmrFieldName = getElementContent(getUniqueChild(cmrFieldElement, "cmr-field-name"));
			
		   // cmr-field-type?
			Element cmrFieldTypeElement = getOptionalChild(cmrFieldElement, "cmr-field-type");
			if(cmrFieldTypeElement != null) {
				cmrFieldType = getElementContent(cmrFieldTypeElement);
				if(cmrFieldType==null || 
						(!cmrFieldType.equals("java.util.Collection") && 
							!cmrFieldType.equals("java.util.Set"))) {
					throw new DeploymentException("multiplicity should be java.util.Collection or java.util.Set but is " + cmrFieldType);
				}
			}
		}
	}
}
