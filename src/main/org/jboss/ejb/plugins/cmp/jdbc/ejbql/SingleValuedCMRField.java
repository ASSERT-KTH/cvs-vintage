package org.jboss.ejb.plugins.cmp.jdbc.ejbql;

import java.util.Map;

import org.jboss.ejb.plugins.cmp.jdbc.SQLUtil;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;

public class SingleValuedCMRField extends CMRField {
	public SingleValuedCMRField(JDBCCMRFieldBridge cmrFieldBridge, PathElement parent) {
		super(cmrFieldBridge, parent);
	}
	
	public String getSelectClause(Map identifiersByPathElement) {
		String identifier = getIdentifier(identifiersByPathElement);
		return SQLUtil.getColumnNamesClause(entityBridge.getJDBCPrimaryKeyFields(), identifier);
	}

	public String toString() {
		return "[SingleValuedCMRField: name="+getName()+"]";
	}
}
