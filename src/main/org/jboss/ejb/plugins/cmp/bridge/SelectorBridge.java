/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.bridge;

/**
 * SelectorBridge represents one ejbSelect method. 
 *
 * Life-cycle:
 *		Tied to the EntityBridge.
 *
 * Multiplicity:	
 *		One for each entity bean ejbSelect method. 		
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.1 $
 */                            
public interface SelectorBridge {
	public String getSelectorName();
	public Class getReturnType();
		
	public Object execute(Object[] args);
}