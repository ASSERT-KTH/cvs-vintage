package org.jboss.ejb.plugins.cmp.jdbc.ejbql;

import org.jboss.ejb.plugins.cmp.jdbc.SQLUtil;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import java.util.Map;

public class AbstractSchema extends PathElement {
	public AbstractSchema(JDBCEntityBridge entityBridge) {
		super(entityBridge, null);
	}
	
	public String getName() {
		return entityBridge.getMetaData().getAbstractSchemaName();
	}

	public String getEntityName() {
		return entityBridge.getEntityName();
	}
	
	public String getSelectClause(Map identifiersByPathElement) {
		String identifier = getIdentifier(identifiersByPathElement);
		return SQLUtil.getColumnNamesClause(entityBridge.getJDBCPrimaryKeyFields(), identifier);
	}

	public String getIdentifier(Map identifiersByPathElement) {
		String identifier = (String)identifiersByPathElement.get(this);
		if(identifier == null) {
			throw new IllegalStateException("No registered identifier for AbstractSchema: "+this);
		}
		return identifier;
	}
	
	public String getTableDeclarations(Map identifiersByPathElement) {
		StringBuffer buf = new StringBuffer();
		buf.append(entityBridge.getMetaData().getTableName() + " " + getIdentifier(identifiersByPathElement));
		return buf.toString();
	}

	public String toString() {
		return "[AbstractSchema: name="+getName()+"]";
	}	
}
