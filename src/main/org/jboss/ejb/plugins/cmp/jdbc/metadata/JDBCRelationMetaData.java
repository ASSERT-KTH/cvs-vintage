/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.util.Iterator;
import org.jboss.ejb.DeploymentException;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.RelationMetaData;
import org.jboss.metadata.RelationshipRoleMetaData;
import org.w3c.dom.Element;

/**
 * Represents one ejb-relation element found in the ejb-jar.xml
 * file's relationships elements.
 *    
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 *	@version $Revision: 1.1 $
 */
public class JDBCRelationMetaData extends MetaData {
	private final static int TABLE = 1;
	private final static int FOREIGN_KEY = 2;
	
	/** Name of the relation. Loaded from the ejb-relation-name element. */
	private String relationName;

	/** 
	 * The left jdbc relationship role. Loaded from an ejb-relationship-role.
	 * Left/right assignment is completely arbitrary.
	 */
	private JDBCRelationshipRoleMetaData left;

	/** 
	 * The right relationship role. Loaded from an ejb-relationship-role.
	 * Left/right assignment is completely arbitrary.
	 */
	private JDBCRelationshipRoleMetaData right;
	
	/**
	 * The mapping style for this relation (i.e., TABLE or FOREIGN_KEY).
	 */
	private int mappingStyle;
	
	// the name of the table to use for this bean
	private String tableName;
	
	// do we have to try and create the table on deployment?
	private boolean createTable;
	
	// do we have to drop the table on undeployment?
	private boolean removeTable;
	
	// do we use 'SELECT ... FOR UPDATE' syntax?
	private boolean selectForUpdate;
	
	// is the bean read-only?
	private boolean readOnly;
	
	// how long is read valid
	private int readTimeOut = -1;
	
	// should the table have a primary key constraint?
	private boolean primaryKeyConstraint;
		
	public JDBCRelationMetaData(
			RelationMetaData relationMetaData,
			JDBCApplicationMetaData applicationMetaData) {
		relationName = relationMetaData.getRelationName();
		
		RelationshipRoleMetaData leftRole = relationMetaData.getLeftRelationshipRole();
		RelationshipRoleMetaData rightRole = relationMetaData.getRightRelationshipRole();

		left = new JDBCRelationshipRoleMetaData(leftRole, this, applicationMetaData);
		right = new JDBCRelationshipRoleMetaData(rightRole, this, applicationMetaData);
		
		// give each role a referance to the other
		left.setRelatedRole(right);
		right.setRelatedRole(left);
	
		if(left.isMultiplicityMany() && right.isMultiplicityMany()) {
			mappingStyle = TABLE;
			tableName = left.getEntity().getName() + "_" + left.getCMRFieldName() +
					right.getEntity().getName() + "_" + right.getCMRFieldName();
		} else {
			mappingStyle = FOREIGN_KEY; 
		}		
	}

	public void importXml(Element element) throws DeploymentException {		
		// mapping style
		String mappingStyleString = getElementContent(getOptionalChild(element, "mapping-style"));
		if(mappingStyleString != null) {
			if(mappingStyleString.equals("table")) {
				mappingStyle = TABLE;
			} else if(mappingStyleString.equals("foreign-key")) {
				mappingStyle = FOREIGN_KEY;
				if(left.isMultiplicityMany() && right.isMultiplicityMany()) {
					throw new DeploymentException("Foreign key mapping-style is not allowed for many-to-many relationsips.");
				}
			} else {
				throw new DeploymentException("Unknown mapping-style: " + mappingStyleString);
			}
		}			
		
		// get table name
		String tableString = getElementContent(getOptionalChild(element, "table-name"));
		if(tableString != null) {
			tableName = tableString;
		}
			
		// create table?  If not provided, keep default.
		String createString = getElementContent(getOptionalChild(element, "create-table"));
		if(createString != null) {
	   	createTable = Boolean.valueOf(createString).booleanValue();
		}
			
    	// remove table?  If not provided, keep default.
		String removeString = getElementContent(getOptionalChild(element, "remove-table"));
		if(removeString != null) {
			removeTable = Boolean.valueOf(removeString).booleanValue();
		}
    	
		// read-only
		String readOnlyString = getElementContent(getOptionalChild(element, "read-only"));
		if(readOnlyString != null) {
			readOnly = Boolean.valueOf(readOnlyString).booleanValue();
		}

		// read-time-out
		if(isReadOnly()) {
			// read-time-out
			String readTimeOutString = getElementContent(getOptionalChild(element, "read-time-out"));
		   if(readTimeOutString != null) {
				readTimeOut = Integer.parseInt(readTimeOutString);
			}
		}		

		String sForUpString = getElementContent(getOptionalChild(element, "select-for-update"));
		if(sForUpString != null) {
	   	selectForUpdate = (Boolean.valueOf(sForUpString).booleanValue());
			selectForUpdate = selectForUpdate && !isReadOnly();
		}

		// primary key constraint?  If not provided, keep default.
		String pkString = getElementContent(getOptionalChild(element, "pk-constraint"));
		if(pkString != null) {
			primaryKeyConstraint = Boolean.valueOf(pkString).booleanValue();
		}
		
		JDBCRelationshipRoleMetaData other = null;
		Iterator iter = getChildrenByTagName(element, "ejb-relationship-role");
		if(iter.hasNext()) {
			Element relationshipRoleElement = (Element)iter.next();
			String relationshipRoleName = getElementContent(getOptionalChild(relationshipRoleElement, "ejb-relationship-role-name"));
			if(left.getRelationshipRoleName().equals(relationshipRoleName)) {
				left.importXml(relationshipRoleElement);
				other = right;
			} else if(right.getRelationshipRoleName().equals(relationshipRoleName)) {
				right.importXml(relationshipRoleElement);
				other = left;
			} else {
				throw new DeploymentException("Found ejb-relationship-role '" + relationshipRoleName + "' in jboss-cmp.xml, but no matching role exits in ejb-jar.xml");
			}
		}
		
		if(iter.hasNext()) {
			Element relationshipRoleElement = (Element)iter.next();
			String relationshipRoleName = getElementContent(getOptionalChild(relationshipRoleElement, "ejb-relationship-role-name"));
			if(other.getRelationshipRoleName().equals(relationshipRoleName)) {
				other.importXml(relationshipRoleElement);
			} else {
				throw new DeploymentException("Found ejb-relationship-role '" + relationshipRoleName + "' in jboss-cmp.xml, but no matching role exits in ejb-jar.xml");
			}
		}

		if(iter.hasNext()) {
			throw new DeploymentException("Expected only 2 ejb-relationship-role but found more then 2");
		}
	}

	/** 
	 * Gets the relation name. 
	 * Relation name is loaded from the ejb-relation-name element.
	 */
	public String getRelationName() {
	   return relationName;
	}
	
	/** 
	 * Gets the left jdbc relationship role. 
	 * The relationship role is loaded from an ejb-relationship-role.
	 * Left/right assignment is completely arbitrary.
	 */
	public JDBCRelationshipRoleMetaData getLeftRelationshipRole() {
		return left;
	}

	/** 
	 * Gets the right jdbc relationship role.
	 * The relationship role is loaded from an ejb-relationship-role.
	 * Left/right assignment is completely arbitrary.
	 */
	public JDBCRelationshipRoleMetaData getRightRelationshipRole() {
		return right;
	}
	
	/**
	 * Should this relation be mapped to a relation table.
	 */
	public boolean isTableMappingStyle() {
		return mappingStyle == TABLE;
	}
	
	/**
	 * Should this relation use foreign keys for storage.
	 */
	public boolean isForeignKeyMappingStyle() {
		return mappingStyle == FOREIGN_KEY;
	}
	
	/**
	 * Gets the name of the relation table.
	 */
	public String getTableName() {
		return tableName;
	}
	
	/**
	 * Should the relation table be created on startup.
	 */
	public boolean getCreateTable() {
		return createTable;
	}
	
	/**
	 * Should the relation table be removed on shutdown.
	 */
	public boolean getRemoveTable() {
		return removeTable;
	}
	
	/**
	 * When the relation table is created, should it have a primary key constraint.
	 */
	public boolean hasPrimaryKeyConstraint() {
		return primaryKeyConstraint;
	}
	
	/**
	 * Is this relation read-only?
	 */
	public boolean isReadOnly() {
		return readOnly;
	}
	
	/**
	 * Gets the read time out length.
	 */
	public int getReadTimeOut() {
		return readTimeOut;
	}
	
	/**
	 * Should select queries use the for update clause.
	 */
	public boolean hasSelectForUpdate() {
		return selectForUpdate;
	}
}
