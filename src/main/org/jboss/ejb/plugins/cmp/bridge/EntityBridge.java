/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.bridge;

import org.jboss.ejb.EntityEnterpriseContext;

/**
 * EntityBridge follows the Bridge pattern [Gamma et. al, 1995].
 * In this implementation of the pattern the Abstract is the entity bean class,
 * and the RefinedAbstraction is the entity bean dynamic proxy. This interface
 * can be considered the implementor. Each imlementation of the CMPStoreManager
 * should create a store specifiec implementaion of the bridge. 
 *
 * Life-cycle:
 *		Undefined. Should be tied to CMPStoreManager.
 *
 * Multiplicity:	
 *		One per cmp entity bean type. 		
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.1 $
 */                            
public interface EntityBridge {
	public String getEntityName();
	
	public Class getPrimaryKeyClass();
	public CMPFieldBridge[] getPrimaryKeyFields();
	
	public CMPFieldBridge[] getCMPFields();
	public CMRFieldBridge[] getCMRFields();
	public SelectorBridge[] getSelectors();
	
	/**
	* Mark each field every as clean.
	*/
	public void setClean(EntityEnterpriseContext ctx);
	
	/**
	* Get every field that isDirty
	*/
	public CMPFieldBridge[] getDirtyFields(EntityEnterpriseContext ctx);

}
