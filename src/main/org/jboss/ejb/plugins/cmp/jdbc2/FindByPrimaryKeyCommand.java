/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc2;

import org.jboss.ejb.plugins.cmp.jdbc2.bridge.JDBCEntityBridge2;
import org.jboss.ejb.plugins.cmp.jdbc2.bridge.JDBCCMPFieldBridge2;
import org.jboss.ejb.plugins.cmp.jdbc2.schema.Schema;
import org.jboss.ejb.plugins.cmp.jdbc.QueryParameter;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCEntityPersistenceStore;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCTypeFactory;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCTypeMappingMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCFunctionMappingMetaData;
import org.jboss.ejb.GenericEntityObjectFactory;
import org.jboss.logging.Logger;
import org.jboss.deployment.DeploymentException;

import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;


/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.5 $</tt>
 */
public class FindByPrimaryKeyCommand
   extends AbstractQueryCommand
{
   public FindByPrimaryKeyCommand(JDBCEntityBridge2 entity) throws DeploymentException
   {
      this.entity = entity;

      JDBCCMPFieldBridge2[] fields = (JDBCCMPFieldBridge2[]) entity.getTableFields();
      String selectColumns = fields[0].getColumnName();
      for(int i = 1; i < fields.length; ++i)
      {
         selectColumns += ", " + fields[i].getColumnName();
      }

      JDBCCMPFieldBridge2[] pkFields = (JDBCCMPFieldBridge2[]) entity.getPrimaryKeyFields();
      String whereColumns = pkFields[0].getColumnName() + "=?";
      for(int i = 1; i < pkFields.length; ++i)
      {
         whereColumns += " and " + pkFields[i].getColumnName() + "=?";
      }

      if(entity.getMetaData().hasRowLocking())
      {
         JDBCEntityPersistenceStore manager = entity.getManager();
         JDBCTypeFactory typeFactory = manager.getJDBCTypeFactory();
         JDBCTypeMappingMetaData typeMapping = typeFactory.getTypeMapping();
         JDBCFunctionMappingMetaData rowLockingTemplate = typeMapping.getRowLockingTemplate();

         if(rowLockingTemplate == null)
         {
            throw new DeploymentException("Row locking template is not defined for mapping: " + typeMapping.getName());
         }

         sql = rowLockingTemplate.getFunctionSql(
            new Object[]{selectColumns, entity.getQualifiedTableName(), whereColumns, null}, new StringBuffer()
         ).toString();
      }
      else
      {
         sql = "select ";
         sql += selectColumns;
         sql += " from " + entity.getQualifiedTableName() + " where ";
         sql += whereColumns;
      }

      log = Logger.getLogger(getClass().getName() + "." + entity.getEntityName() + "#findByPrimaryKey");

      log.debug("sql: " + sql);

      setParameters(QueryParameter.createPrimaryKeyParameters(0, entity));
      setEntityReader(entity);
   }

   public Object fetchOne(Schema schema, GenericEntityObjectFactory factory, Object[] args) throws FinderException
   {
      Object pk = args[0];
      if(pk == null)
      {
         throw new IllegalArgumentException("Null argument for findByPrimaryKey");
      }

      Object instance;
      boolean cached = entity.getTable().hasRow(pk);
      if(!cached)
      {
         instance = super.executeFetchOne(args, factory);
         if(instance == null)
         {
            throw new ObjectNotFoundException("Instance not find: entity=" + entity.getEntityName() + ", pk=" + pk);
         }
      }
      else
      {
         instance = factory.getEntityEJBObject(pk);
      }

      return instance;
   }
}
