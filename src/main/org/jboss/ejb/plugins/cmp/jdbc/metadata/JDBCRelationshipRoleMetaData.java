/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import org.jboss.ejb.DeploymentException;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.RelationshipRoleMetaData;
import org.w3c.dom.Element;

/** 
 * Represents one ejb-relationship-role element found in the ejb-jar.xml
 * file's ejb-relation elements.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.1 $
 */
public class JDBCRelationshipRoleMetaData extends MetaData {
	/**
	 * Relation to which this role belongs.
	 */
	private JDBCRelationMetaData relationMetaData;

	/**
	 * Role name
	 */
	private String relationshipRoleName;
	
	/**
	 * is the multiplicity one?
	 */
	private boolean multiplicityOne;
	
	/**
	 * Should this entity be deleted when related entity is deleted.
	 */
	private boolean cascadeDelete;
	
	/**
	 * The entity that has this role.
	 */
	private JDBCEntityMetaData entity;
	
	/**
	 * Name of the entity's cmr field for this role.
	 */
	private String cmrFieldName;

		
	/**
	 * Type of the cmr field (i.e., collection or set)
	 */
	private String cmrFieldType;
	
	/**
	 * The related role's jdbc meta data.
	 */
	private JDBCRelationshipRoleMetaData relatedRole;

	public JDBCRelationshipRoleMetaData(
			RelationshipRoleMetaData relationshipRole,
			JDBCRelationMetaData relationMetaData,
			JDBCApplicationMetaData applicationMetaData) {
				
		this.relationMetaData = relationMetaData;
		
		relationshipRoleName = relationshipRole.getRelationshipRoleName();
		multiplicityOne = relationshipRole.isMultiplicityOne();
		cascadeDelete = relationshipRole.isCascadeDelete();
		
		cmrFieldName = relationshipRole.getCMRFieldName();
		cmrFieldType = relationshipRole.getCMRFieldType();

		String entityName = relationshipRole.getEntityName();
		
		entity = applicationMetaData.getBeanByEjbName(entityName);
		
		// inform the entity about this role
		entity.addRelationshipRole(this);
	}

	/**
	 * Gets the relation to which this role belongs.
	 */
	public JDBCRelationMetaData getRelationMetaData() {
		return relationMetaData;
	}
	
	/**
	 * Gets the name of this role.
	 */
	public String getRelationshipRoleName() {
		return relationshipRoleName;
	}
	
	/**
	 * Checks if the multiplicity is one.
	 */
	public boolean isMultiplicityOne() {
		return multiplicityOne;
	}
	
	/**
	 * Checks if the multiplicity is many.
	 */
	public boolean isMultiplicityMany() {
		return !multiplicityOne;
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
	public JDBCEntityMetaData getEntity() {
		return entity;
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

	/**
	 * Sets the related role's jdbc meta data.
	 */
	public void setRelatedRole(JDBCRelationshipRoleMetaData relatedRole) {
		this.relatedRole = relatedRole;
	}
	
	/**
	 * Gets the related role's jdbc meta data.
	 */
	public JDBCRelationshipRoleMetaData getRelatedRole() {
		return relatedRole;
	}

	public void importXml(Element element) throws DeploymentException {		
	}
}
