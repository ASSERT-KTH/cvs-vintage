/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.jdbc.bridge;

import java.util.HashSet;

import org.jboss.ejb.plugins.cmp.bridge.CMRFieldBridge;
import org.jboss.ejb.EntityEnterpriseContext;

/**
 * JDBCCMRFieldBridge a bean relationship. 
 *
 * Life-cycle:
 *		Haven't decided yet.
 *
 * Multiplicity:	
 *		Haven't decided yet. Will be either one per bean relationship role, or
 * one per relationship (shared between two beans).		
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.1 $
 */                            
public class JDBCCMRFieldBridge implements CMRFieldBridge {
	protected String fieldName;
	protected Class fieldType;
	
	public String getFieldName() {
		return fieldName;
	}
	
	public Class getFieldType() {
		return fieldType;
	}
		
	public Object getValue(EntityEnterpriseContext ctx) {
		return new HashSet(0);
	}
	
   public void setValue(EntityEnterpriseContext ctx, Object value) {
	}
}
