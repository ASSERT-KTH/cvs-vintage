package org.jboss.ejb.plugins.cmp.jdbc.ejbql;

import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;

public class CollectionValuedCMRField extends CMRField {
	public CollectionValuedCMRField(JDBCCMRFieldBridge cmrFieldBridge, PathElement parent) {
		super(cmrFieldBridge, parent);
	}
	
	public String toString() {
		return "[CollectionValuedCMRField: name="+getName()+"]";
	}
}
