/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.jboss.cmp.ejb.CMPEntity;
import org.jboss.cmp.ejb.CMPField;
import org.jboss.cmp.ejb.EJB20Schema;
import org.jboss.cmp.ejb.JavaType;
import org.jboss.cmp.ejb.ejbql.EJBQL20Compiler;
import org.jboss.deployment.DeploymentException;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.EntityMetaData;
import org.jboss.persistence.schema.DuplicateNameException;
import org.jboss.persistence.schema.AbstractType;
import org.jboss.persistence.sql.jdbc.JDBCSchema;
import org.jboss.persistence.sql.Table;
import org.jboss.persistence.sql.Column;
import org.jboss.persistence.sql.SQLDataType;
import org.jboss.persistence.sql.SQL92Generator;
import org.jboss.persistence.transform.SchemaMapper;
import org.jboss.persistence.transform.PathUnnester;
import org.jboss.logging.Logger;

/**
 * Utilities to aid in migration from JB3 to JB4.
 *
 * @author <a href="mailto:jeremy@boynes.com">Jeremy Boynes</a>
 */
public class JDBCMetaDataMigrationUtil
{
   private static Logger log = Logger.getLogger(JDBCMetaDataMigrationUtil.class);

   public static Config createSchemata(ApplicationMetaData amd, JDBCApplicationMetaData jamd) throws DeploymentException
   {
      EJB20Schema ejbSchema = new EJB20Schema();
      JDBCSchema sqlSchema = new JDBCSchema();
      EJBQL20Compiler compiler = new EJBQL20Compiler(ejbSchema);
      Map schemaMap = new HashMap();
      schemaMap.put(ejbSchema.getBuiltinType(AbstractType.Family.UNKNOWN), sqlSchema.getBuiltinType(AbstractType.Family.UNKNOWN));
      schemaMap.put(ejbSchema.getBuiltinType(AbstractType.Family.OBJECT), sqlSchema.getBuiltinType(AbstractType.Family.OBJECT));
      schemaMap.put(ejbSchema.getBuiltinType(AbstractType.Family.BOOLEAN), sqlSchema.getBuiltinType(AbstractType.Family.BOOLEAN));
      schemaMap.put(ejbSchema.getBuiltinType(AbstractType.Family.STRING), sqlSchema.getBuiltinType(AbstractType.Family.STRING));
      schemaMap.put(ejbSchema.getBuiltinType(AbstractType.Family.INTEGER), sqlSchema.getBuiltinType(AbstractType.Family.INTEGER));
      schemaMap.put(ejbSchema.getBuiltinType(AbstractType.Family.DOUBLE), sqlSchema.getBuiltinType(AbstractType.Family.DOUBLE));
      schemaMap.put(ejbSchema.getBuiltinType(AbstractType.Family.DECIMAL), sqlSchema.getBuiltinType(AbstractType.Family.DECIMAL));
      schemaMap.put(ejbSchema.getBuiltinType(AbstractType.Family.DATETIME), sqlSchema.getBuiltinType(AbstractType.Family.DATETIME));
      schemaMap.put(ejbSchema.getBuiltinType(AbstractType.Family.BINARY), sqlSchema.getBuiltinType(AbstractType.Family.BINARY));
      SchemaMapper mapper = new SchemaMapper(schemaMap);
      SQL92Generator generator = new SQL92Generator();

      for (Iterator i = amd.getEnterpriseBeans(); i.hasNext();) {
         // Iterate over CMP2x Entities only
         BeanMetaData bmd = (BeanMetaData) i.next();
         if (bmd instanceof EntityMetaData == false) {
            continue;
         }
         EntityMetaData entityMetaData = (EntityMetaData) bmd;
         if (entityMetaData.isCMP2x() == false) {
            continue;
         }
         JDBCEntityMetaData jdbcMetaData = jamd.getBeanByEjbName(entityMetaData.getEjbName());

         CMPEntity entity;
         JavaType pkType;
         Table table;
         String pkClassName = entityMetaData.getPrimaryKeyClass();
         try {
            Class pkClass = Thread.currentThread().getContextClassLoader().loadClass(pkClassName);
            pkType = (JavaType) ejbSchema.getClassByJavaClass(pkClass);
            if (pkType == null) {
               log.warn("Unable to find logical mapping for primary key class: " + pkClassName);
//            throw new DeploymentException("Unable to find logical mapping for primary key class: " + pkClassName);
            }
         } catch (ClassNotFoundException e) {
            throw new DeploymentException("Unable to load primary key class for name:"+pkClassName);
         }
         try {
            entity = ejbSchema.addCMPEntity(entityMetaData.getEjbName(), jdbcMetaData.getAbstractSchemaName(), pkType);
            table = sqlSchema.addTable(jdbcMetaData.getDefaultTableName());
            schemaMap.put(entity, table);
         } catch (DuplicateNameException e) {
            throw new DeploymentException(e);
         }

         List pkFields = new ArrayList();
         for (Iterator fields = entityMetaData.getCMPFields(); fields.hasNext();) {
            String cmpFieldName = (String) fields.next();
            JDBCCMPFieldMetaData cmpFieldMetaData = jdbcMetaData.getCMPFieldByName(cmpFieldName);
            Class cmpFieldClass = cmpFieldMetaData.getFieldType();
            AbstractType cmpFieldType = ejbSchema.getClassByJavaClass(cmpFieldClass);
            if (cmpFieldType == null) {
               log.warn("Unable to find logical mapping for cmp-field class: " + cmpFieldClass.getName());
               continue; // for now
//               throw new DeploymentException("Unable to find logical mapping for cmp-field class: " + cmpFieldClass.getName());
            }
            CMPField cmpField = new CMPField(cmpFieldName, cmpFieldType);
            String columnName = cmpFieldMetaData.getColumnName();
            if (cmpFieldMetaData.isPrimaryKeyMember()) {
               pkFields.add(columnName);
            }
            SQLDataType columnType = (SQLDataType)sqlSchema.getBuiltinType(cmpFieldType.getFamily());
            Column column = new Column(columnName, columnType);

            entity.addCMPField(cmpField);
            table.addColumn(column);
            schemaMap.put(cmpField, column);
         }
         table.setPkFields((String[]) pkFields.toArray(new String[pkFields.size()]));
      }
      Config config = new Config();
      config.ejbSchema = ejbSchema;
      config.sqlSchema = sqlSchema;
      config.compiler = compiler;
      config.generator = generator;
      config.mapper = mapper;
      config.unnester = new PathUnnester();
      return config;
   }

   public static class Config {
      public EJB20Schema ejbSchema;
      public JDBCSchema sqlSchema;
      public EJBQL20Compiler compiler;
      public SQL92Generator generator;
      public SchemaMapper mapper;
      public PathUnnester unnester;
   }
}
