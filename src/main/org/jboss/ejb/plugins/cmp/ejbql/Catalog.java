/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.ejbql;

import java.util.HashMap;
import java.util.Map;

import org.jboss.ejb.plugins.cmp.bridge.EntityBridge;

/**
 * This class maintains a map of all entitie bridges in an application by name.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.2 $
 */                            
public class Catalog {
   private final Map entityByAbstractSchemaName = new HashMap();
   private final Map entityByEJBName = new HashMap();

   public void addEntity(EntityBridge entityBridge) {
      entityByAbstractSchemaName.put(
            entityBridge.getAbstractSchemaName(), 
            entityBridge);
      entityByEJBName.put(
            entityBridge.getEntityName(), 
            entityBridge);
   }

   public EntityBridge getEntityByAbstractSchemaName(
         String abstractSchemaName) {
      return (EntityBridge) entityByAbstractSchemaName.get(abstractSchemaName);
   }

   public EntityBridge getEntityByEJBName(String ejbName) {
      return (EntityBridge) entityByEJBName.get(ejbName);
   }
}
