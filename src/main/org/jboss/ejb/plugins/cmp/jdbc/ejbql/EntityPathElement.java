package org.jboss.ejb.plugins.cmp.jdbc.ejbql;

import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;

public abstract class EntityPathElement extends PathElement {
	protected final JDBCEntityBridge entityBridge;	

	public EntityPathElement(JDBCEntityBridge entityBridge, EntityPathElement parent) {
		super(parent);
		this.entityBridge = entityBridge;
	}

	public JDBCEntityBridge getEntityBridge() {
		return entityBridge;
	}
	
	public Class getFieldType() {
		return entityBridge.getMetaData().getEntityClass();
	}
		
	public JDBCCMRFieldBridge getCMRFieldBridge(String fieldName) {
		return entityBridge.getCMRFieldByName(fieldName);
	}

	public JDBCCMPFieldBridge getCMPFieldBridge(String fieldName) {
		return entityBridge.getCMPFieldByName(fieldName);
	}
}
