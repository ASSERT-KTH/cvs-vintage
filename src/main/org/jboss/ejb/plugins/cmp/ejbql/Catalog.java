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
 * @version $Revision: 1.1 $
 */                            
public class Catalog {
   private final Map entityBridges = new HashMap();

   public void addEntity(EntityBridge entityBridge) {
      entityBridges.put(entityBridge.getAbstractSchemaName(), entityBridge);
   }
   public EntityBridge getEntity(String abstractSchemaName) {
      return (EntityBridge) entityBridges.get(abstractSchemaName);
   }
}
