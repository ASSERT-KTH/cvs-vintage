package org.jboss.ejb.plugins.cmp.jdbc.ejbql;

import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;

public class CMRField extends EntityPathElement {
   private final JDBCCMRFieldBridge cmrFieldBridge;

   public CMRField(JDBCCMRFieldBridge cmrFieldBridge, EntityPathElement parent) {
      super(cmrFieldBridge.getRelatedEntity(), parent);

      if(parent == null) {
         throw new IllegalArgumentException("parent is null");
      }
      if(cmrFieldBridge == null) {
         throw new IllegalArgumentException("cmrFieldBridge is null");
      }

      this.cmrFieldBridge = cmrFieldBridge;
   }
   
   public JDBCCMRFieldBridge getCMRFieldBridge() {
      return cmrFieldBridge;
   }
   
   public String getName() {
      return cmrFieldBridge.getFieldName();
   }
   
   public boolean isSingleValued() {
      return cmrFieldBridge.isSingleValued();
   }
   
   public boolean isCollectionValued() {
      return cmrFieldBridge.isCollectionValued();
   }

   public String toString() {
      return "[CMRField: name="+getName()+"]";
   }   
}
