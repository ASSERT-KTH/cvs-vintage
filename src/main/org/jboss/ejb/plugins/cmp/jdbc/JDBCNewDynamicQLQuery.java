/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.sql.PreparedStatement;
import javax.ejb.FinderException;

import org.jboss.cmp.ejb.CMPEntity;
import org.jboss.cmp.ejb.CMPField;
import org.jboss.cmp.ejb.ejbql.CompileException;
import org.jboss.cmp.ejb.ejbql.EJBQL20Compiler;
import org.jboss.cmp.ejb.ejbql.ParseException;
import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.ejbql.Catalog;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCDynamicQLQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCMetaDataMigrationUtil;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
import org.jboss.persistence.query.CommandNode;
import org.jboss.persistence.query.Expression;
import org.jboss.persistence.query.Path;
import org.jboss.persistence.query.Projection;
import org.jboss.persistence.query.Query;
import org.jboss.persistence.schema.AbstractAssociationEnd;
import org.jboss.persistence.schema.AbstractClass;
import org.jboss.persistence.schema.AbstractType;
import org.jboss.persistence.transform.TransformException;
import org.jboss.persistence.sql.SQLDataType;

/**
 * This class generates a query from JBoss-QL.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.1 $
 */
public class JDBCNewDynamicQLQuery extends JDBCAbstractQueryCommand {
   private final JDBCDynamicQLQueryMetaData metadata;
   private final Catalog catalog;
   private final JDBCMetaDataMigrationUtil.Config config;
   private ThreadLocal local = new ThreadLocal();

   public JDBCNewDynamicQLQuery(
         JDBCStoreManager manager, 
         JDBCQueryMetaData q) throws DeploymentException {

      super(manager, q);
      catalog = (Catalog)manager.getApplicationData("CATALOG");
      metadata = (JDBCDynamicQLQueryMetaData)q;
      config = manager.getConfig();
   }

   public Collection execute(
         Method finderMethod,
         Object[] args,
         EntityEnterpriseContext ctx) throws FinderException {

      String dynamicQL = (String)args[0];
      if (dynamicQL == null) {
         throw new FinderException("DynamicQL is null");
      }
      Object[] parameters = (Object[])args[1];
      if(parameters == null) {
         throw new FinderException("Parameters is null");
      }

      if(getLog().isDebugEnabled()) {
         getLog().debug("DYNAMIC-QL: " + dynamicQL);
      }

      AbstractType[] paramTypes = new AbstractType[parameters.length];
      for (int i = 0; i < parameters.length; i++) {
         Object parameter = parameters[i];
         paramTypes[i] = config.ejbSchema.getClassByJavaClass(parameter.getClass());
      }

      EJBQL20Compiler compiler = config.compiler;
      try {
         Query ejbQuery = compiler.compile(dynamicQL, paramTypes);
         CommandNode unnestedQuery = config.unnester.transform(ejbQuery);
         CommandNode sqlQuery = config.mapper.transform(unnestedQuery);
         local.set(sqlQuery.getParameters());
         setSQL(config.generator.generate(sqlQuery));

         Projection selectList = ejbQuery.getProjection();
         Expression expr = (Expression) selectList.getChildren().get(0);
         if (expr.getType() instanceof AbstractClass) {
            // we are selecting an entity
            CMPEntity selectEntity = (CMPEntity) expr.getType();
            JDBCEntityBridge bridge = (JDBCEntityBridge) catalog.getEntityByEJBName(selectEntity.getName());
            setSelectEntity(bridge);
         } else {
            // we are selecting a field
            Path path = (Path)expr.getType();
            List steps = path.getSteps();
            CMPEntity selectEntity;
            if (steps.size() == 1) {
               selectEntity = (CMPEntity) path.getRoot().getType();
            } else {
               selectEntity = (CMPEntity) ((AbstractAssociationEnd)steps.get(steps.size()-2)).getType();
            }
            CMPField selectField = (CMPField) expr.getType();
            JDBCEntityBridge entityBridge = (JDBCEntityBridge) catalog.getEntityByEJBName(selectEntity.getName());
            JDBCCMPFieldBridge bridge = (JDBCCMPFieldBridge) entityBridge.getFieldByName(selectField.getName());
            setSelectField(bridge);
         }
      } catch (ParseException e) {
         throw new FinderException(e.getMessage());
      } catch (CompileException e) {
         throw new FinderException(e.getMessage());
      } catch (TransformException e) {
         throw new FinderException(e.getMessage());
      }
      setParameterList(Collections.EMPTY_LIST);
      setPreloadFields(Collections.EMPTY_LIST);
      return super.execute(finderMethod, parameters, ctx);
   }

   protected void bindParameters(PreparedStatement ps, Object[] args) throws Exception
   {
      AbstractType[] paramTypes = (AbstractType[]) local.get();
      for (int i=0; i < args.length; i++) {
         int jdbcType = ((SQLDataType)paramTypes[i]).getJdbcType();
         JDBCUtil.setParameter(getLog(), ps, i+1, jdbcType, args[i]);
      }
   }
}
