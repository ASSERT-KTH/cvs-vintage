/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Set;
import org.jboss.ejb.DeploymentException;
import org.jboss.ejb.plugins.cmp.ejbql.Assembly;
import org.jboss.ejb.plugins.cmp.ejbql.Parser;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.ejbql.EJBQLParser;
import org.jboss.ejb.plugins.cmp.jdbc.ejbql.SQLTarget;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQlQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;

/**
 * JDBCDefinedFinderCommand finds entities based on an xml sql specification.
 * This class needs more work and I will clean it up in CMP 2.x phase 3.
 * The only thing to to note is the seperation of query into a from and where
 * clause. This code has been cleaned up to improve readability.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.5 $
 */
public class JDBCEJBQLFinderCommand extends JDBCFinderCommand
{
   // Attributes ----------------------------------------------------
   
   private int[] parameterArray;

   // Constructors --------------------------------------------------

   public JDBCEJBQLFinderCommand(JDBCStoreManager manager, JDBCQueryMetaData q) throws DeploymentException {
      super(manager, q);

      JDBCQlQueryMetaData metadata = (JDBCQlQueryMetaData)q;
      log.debug("EQL-QL: "+metadata.getEjbQl());
      
      // get a parser
      Parser ejbql = new EJBQLParser().ejbqlQuery();
      
      // initialize the assembly
      Assembly a = new Assembly(metadata.getEjbQl());
      a.setTarget(new SQLTarget(manager.getContainer().getApplication()));
      
      // match the query
      a = ejbql.soleMatch(a);
      log.debug("Assembly: "+a);
      
      // get the final target
      SQLTarget target = (SQLTarget)a.getTarget();
      
      // set the target to use select distinct if the return type is set
      if(metadata.getMethod().getReturnType().equals(Set.class)) {
         target.setSelectDistinct(true);
      }
      
      // set the sql
      setSQL(target.toSQL());
      
      // select bridge object
      Object selectBridgeObject = target.getSelectObject();
      if(selectBridgeObject instanceof JDBCEntityBridge) {
         selectEntity = (JDBCEntityBridge)selectBridgeObject;
         selectCMPField = null;
      } else if(selectBridgeObject instanceof JDBCCMPFieldBridge) {
         selectCMPField = (JDBCCMPFieldBridge)selectBridgeObject;
         selectEntity = null;
      } else {
         throw new IllegalStateException("Select bridge object is instance of unknown type: " +
               "selectBridgeObject=" + selectBridgeObject);
      }
      
      // get the parameter order
      List l  = target.getInputParameters();
      parameterArray = new int[l.size()];
      for(int i=0; i<l.size(); i++) {
         // convert to 0 based parameter index
         parameterArray[i] = ((Integer)l.get(i)).intValue()-1;
      }
   }
 
   // JDBCFinderCommand overrides ------------------------------------

   protected void setParameters(PreparedStatement ps, Object argOrArgs) throws Exception {
      Object[] args = (Object[])argOrArgs;
   
      for(int i = 0; i < parameterArray.length; i++) {
         Object arg = args[parameterArray[i]];
         int jdbcType = manager.getJDBCTypeFactory().getJDBCTypeForJavaType(arg.getClass());
         JDBCUtil.setParameter(log, ps, i+1, jdbcType, arg);
      }
   }
}
