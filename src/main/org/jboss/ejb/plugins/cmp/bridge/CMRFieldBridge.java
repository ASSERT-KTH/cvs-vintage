/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.bridge;

import org.jboss.ejb.EntityEnterpriseContext;

/**
 * CMRFieldBridge a bean relationship. 
 *
 * Life-cycle:
 *		Haven't decided yet.
 *
 * Multiplicity:	
 *		Haven't decided yet. Will be either one per bean relationship role, or
 * one per relationship (shared between two beans).		
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.2 $
 */                            
public interface CMRFieldBridge {
	public String getFieldName();
//	public Class getFieldType();
		
	public Object getValue(EntityEnterpriseContext ctx);
   public void setValue(EntityEnterpriseContext ctx, Object value);
}
