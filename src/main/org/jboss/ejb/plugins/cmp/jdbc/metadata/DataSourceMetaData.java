/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import org.jboss.system.ServiceMBeanSupport;

import javax.management.ObjectName;

/**
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.2 $</tt>
 */
public class DataSourceMetaData
   extends ServiceMBeanSupport
   implements DataSourceMetaDataMBean
{
   private ObjectName metadataLibrary;
   private String typeMapping;

   /**
    * @jmx.managed-attribute
    */
   public String getTypeMapping()
   {
      return typeMapping;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setTypeMapping(String typeMapping)
   {
      this.typeMapping = typeMapping;
   }

   /**
    * @jmx.managed-attribute
    */
   public ObjectName getMetadataLibrary()
   {
      return metadataLibrary;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setMetadataLibrary(ObjectName metadataLibrary)
   {
      this.metadataLibrary = metadataLibrary;
   }

   /**
    * @jmx.managed-attribute
    */
   public JDBCTypeMappingMetaData getTypeMappingMetaData() throws Exception
   {
      return (JDBCTypeMappingMetaData)server.invoke(
         metadataLibrary,
         "findTypeMappingMetaData",
         new Object[]{typeMapping},
         new String[]{String.class.getName()}
      );
   }
}
