package org.jboss.ejb.plugins.cmp.jdbc.ejbql;

import java.util.Map;

import org.jboss.ejb.plugins.cmp.jdbc.SQLUtil;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;

public final class CMPField {
	private final JDBCCMPFieldBridge cmpFieldBridge;
	private final PathElement parent;

	public CMPField(JDBCCMPFieldBridge cmpFieldBridge, PathElement parent) {
		if(cmpFieldBridge == null) {
			throw new IllegalArgumentException("cmpFieldBridge is null");
		}
		if(parent == null) {
			throw new IllegalArgumentException("parent is null");
		}
		this.cmpFieldBridge = cmpFieldBridge;
		this.parent = parent;
	}
	
	public String getColumnNamesClause(Map identifiersByPathElement) {
		String identifier = parent.getIdentifier(identifiersByPathElement);
		return SQLUtil.getColumnNamesClause(cmpFieldBridge, identifier);
	}

	public JDBCCMPFieldBridge getCMPFieldBridge() {
		return cmpFieldBridge;
	}
	
	public PathElement getParent() {
		return parent;
	}
	
	public Class getFieldType() {
		return cmpFieldBridge.getFieldType();
	}
	
	public String getName() {
		return cmpFieldBridge.getFieldName();
	}
	
	public String toString() {
		return "[CMPField: name="+getName()+"]";
	}
}
