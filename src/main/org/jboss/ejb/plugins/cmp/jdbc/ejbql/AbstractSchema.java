package org.jboss.ejb.plugins.cmp.jdbc.ejbql;

import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;

public class AbstractSchema extends EntityPathElement {
	public AbstractSchema(JDBCEntityBridge entityBridge) {
		super(entityBridge, null);
	}
	
	public String getName() {
		return entityBridge.getMetaData().getAbstractSchemaName();
	}

	public String toString() {
		return "[AbstractSchema: name="+getName()+"]";
	}	
}
