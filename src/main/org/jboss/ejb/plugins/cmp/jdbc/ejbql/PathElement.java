package org.jboss.ejb.plugins.cmp.jdbc.ejbql;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;

public abstract class PathElement {
	protected final JDBCEntityBridge entityBridge;	
	protected final PathElement parent;

	public PathElement(JDBCEntityBridge entityBridge, PathElement parent) {
		this.entityBridge = entityBridge;
		this.parent = parent;
	}

	public abstract String getName();
	public abstract String getEntityName();
	public abstract String getIdentifier(Map identifiersByPathElement);
	public abstract String getTableDeclarations(Map identifiersByPathElement);
	
	public PathElement getParent() {
		return parent;
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
