/*
 * JBoss, the OpenSource J2EE webOS
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
 *      Haven't decided yet.
 *
 * Multiplicity:   
 *      Haven't decided yet. Will be either one per bean relationship role, or
 * one per relationship (shared between two beans).      
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.5 $
 */                            
public interface CMRFieldBridge {
   /**
    * Gets the name of this cmr.
    * @return the name of this field
    */
   public String getFieldName();
      
   /**
    * Is this field read only.
    * @return true if this field is read only
    */ 
   public boolean isReadOnly();
 
   /**
    * Gets the value of this field for the specified context.
    * @param ctx the context for which this field's value should be fetched
    * @return the value of this field
    */
   public Object getValue(EntityEnterpriseContext ctx);
      
   /**
    * Sets the value of this field for the specified context.
    * @param ctx the context for which this field's value should be set
    * @param value the new value of this field
    */
   public void setValue(EntityEnterpriseContext ctx, Object value);
}
