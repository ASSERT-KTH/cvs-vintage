package org.jboss.ejb.plugins.cmp.jdbc.ejbql;

import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;

public final class CMPField extends PathElement {
   private final JDBCCMPFieldBridge cmpFieldBridge;

   public CMPField(JDBCCMPFieldBridge cmpFieldBridge, EntityPathElement parent) {
      super(parent);
      if(cmpFieldBridge == null) {
         throw new IllegalArgumentException("cmpFieldBridge is null");
      }
      if(parent == null) {
         throw new IllegalArgumentException("parent is null");
      }
      this.cmpFieldBridge = cmpFieldBridge;
   }
   
   public String getName() {
      return cmpFieldBridge.getFieldName();
   }
   
   public Class getFieldType() {
      return cmpFieldBridge.getFieldType();
   }
   
   public JDBCCMPFieldBridge getCMPFieldBridge() {
      return cmpFieldBridge;
   }
   
   public String toString() {
      return "[CMPField: name="+getName()+"]";
   }
}

