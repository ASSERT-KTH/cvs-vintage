/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.jdbc.bridge;

/**
 * JDBCSelectorBridge represents one ejbSelect method. 
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
import org.jboss.ejb.plugins.cmp.bridge.SelectorBridge;

public class JDBCSelectorBridge implements SelectorBridge {
	protected String selectorName;
	protected Class returnType;
	
	public String getSelectorName() {
		return selectorName;
	}
	
	public Class getReturnType() {
		return returnType;
	}
		
	public Object execute(Object[] args) {
		return null;
	}
}
