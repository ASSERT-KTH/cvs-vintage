package org.jboss.ejb.plugins.cmp.jdbc.ejbql;

import java.util.Map;
import org.jboss.ejb.plugins.cmp.jdbc.SQLUtil;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;

public class CMRField  extends PathElement {
	private final JDBCCMRFieldBridge cmrFieldBridge;

	public CMRField(JDBCCMRFieldBridge cmrFieldBridge, PathElement parent) {
		super(cmrFieldBridge.getRelatedEntity(), parent);

		if(parent == null) {
			throw new IllegalArgumentException("parent is null");
		}
		if(cmrFieldBridge == null) {
			throw new IllegalArgumentException("cmrFieldBridge is null");
		}

		this.cmrFieldBridge = cmrFieldBridge;
	}
	
	public String getIdentifier(Map identifiersByPathElement) {
		String identifier = (String)identifiersByPathElement.get(this);
		if(identifier == null) {
			return parent.getIdentifier(identifiersByPathElement) + "_" + getName();
		}
		return identifier;
	}
	
	public String getTableDeclarations(Map identifiersByPathElement) {
		StringBuffer buf = new StringBuffer();
		buf.append(entityBridge.getMetaData().getTableName() + " " + getIdentifier(identifiersByPathElement));

		if(cmrFieldBridge.getMetaData().getRelationMetaData().isTableMappingStyle()) {
			buf.append(", ");
			buf.append(cmrFieldBridge.getMetaData().getRelationMetaData().getTableName());
		   buf.append(" ");
		   buf.append(parent.getIdentifier(identifiersByPathElement)).append("_to_").append(getName());
		}
		return buf.toString();
	}

	public String getTableWhereClause(Map identifiersByPathElement) {
		StringBuffer buf = new StringBuffer();
		if(cmrFieldBridge.getMetaData().getRelationMetaData().isForeignKeyMappingStyle()) {
			String parentIdentifier = parent.getIdentifier(identifiersByPathElement);
			String childIdentifier = getIdentifier(identifiersByPathElement);
			
			if(cmrFieldBridge.hasForeignKey()) {				
				JDBCCMPFieldBridge[] parentFkKeyFields = cmrFieldBridge.getForeignKeyFields();
				for(int i=0; i < parentFkKeyFields.length; i++) {
					if(i > 0) {
						buf.append(" AND ");
					}
					JDBCCMPFieldBridge parentFkField = parentFkKeyFields[i];
					JDBCCMPFieldBridge childPkField = getCMPFieldBridge(parentFkField.getFieldName());
					buf.append(SQLUtil.getWhereClause(parentFkField, parentIdentifier, childPkField, childIdentifier));
				}	
			} else {
				JDBCCMPFieldBridge[] childFkKeyFields = cmrFieldBridge.getRelatedCMRField().getForeignKeyFields();
				for(int i=0; i < childFkKeyFields.length; i++) {
					if(i > 0) {
						buf.append(" AND ");
					}
					JDBCCMPFieldBridge childFkKeyField = childFkKeyFields[i];
					JDBCCMPFieldBridge parentPkField = parent.getCMPFieldBridge(childFkKeyField.getFieldName());
					buf.append(SQLUtil.getWhereClause(parentPkField, parentIdentifier, childFkKeyField, childIdentifier));
				}	
			}
		} else {
			String parentIdentifier = parent.getIdentifier(identifiersByPathElement);
			String relationTableIdentifier = parent.getIdentifier(identifiersByPathElement) + "_to_" + getName();
			
			JDBCCMPFieldBridge[] parentTableKeyFields = cmrFieldBridge.getTableKeyFields();
			for(int i=0; i < parentTableKeyFields.length; i++) {
				if(i > 0) {
					buf.append(" AND ");
				}
				JDBCCMPFieldBridge fkField = parentTableKeyFields[i];
				JDBCCMPFieldBridge pkField = parent.getCMPFieldBridge(fkField.getFieldName());
				buf.append(SQLUtil.getWhereClause(pkField, parentIdentifier, fkField, relationTableIdentifier));
			}	

			buf.append(" AND ");

			String childIdentifier = getIdentifier(identifiersByPathElement);
			JDBCCMPFieldBridge[] childTableKeyFields = cmrFieldBridge.getRelatedCMRField().getTableKeyFields();
			for(int i=0; i < childTableKeyFields.length; i++) {
				if(i > 0) {
					buf.append(" AND ");
				}
				JDBCCMPFieldBridge fkField = childTableKeyFields[i];
				JDBCCMPFieldBridge pkField = getCMPFieldBridge(fkField.getFieldName());
				buf.append(SQLUtil.getWhereClause(pkField, childIdentifier, fkField, relationTableIdentifier));
			}	
		}	
		return buf.toString();
	}

	public String getName() {
		return cmrFieldBridge.getFieldName();
	}

	public String getEntityName() {
		return cmrFieldBridge.getEntity().getEntityName();
	}
}