/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.bridge;

import org.jboss.ejb.EntityEnterpriseContext;

/**
 * CMPFieldBridge represents one cmp field for one entity. 
 *
 * Life-cycle:
 *		Tied to the EntityBridge.
 *
 * Multiplicity:	
 *		One for each entity bean cmp field. 		
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.3 $
 */                            
public interface CMPFieldBridge {
	public String getFieldName();
	public Class getFieldType();
	public boolean isPrimaryKeyMember();
	public boolean isReadOnly();
		
	public Object getInstanceValue(EntityEnterpriseContext ctx);
   public void setInstanceValue(EntityEnterpriseContext ctx, Object value);

	public Object getPrimaryKeyValue(Object primaryKey) throws IllegalArgumentException;
   public Object setPrimaryKeyValue(Object primaryKey, Object value) throws IllegalArgumentException;

	/**
	* Set CMPFieldValue to Java default value (i.e., 0 or null).
	*/
	public void initInstance(EntityEnterpriseContext ctx);

	/**
	* Has the value of this field changes since the last time clean was called.
	*/
	public boolean isDirty(EntityEnterpriseContext ctx);	

	/**
	* Mark this field as clean.
	*/
	public void setClean(EntityEnterpriseContext ctx);
}
