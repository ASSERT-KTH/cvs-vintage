/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.plugins.cmp.ejbql.Assembly;
import org.jboss.ejb.plugins.cmp.ejbql.Parser;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.ejbql.EJBQLParser;
import org.jboss.ejb.plugins.cmp.jdbc.ejbql.SQLTarget;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQlQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;

/**
 * This class generates a query from EJB-QL.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.4 $
 */
public class JDBCEJBQLQuery extends JDBCAbstractQueryCommand {

   public JDBCEJBQLQuery(
         JDBCStoreManager manager, 
         JDBCQueryMetaData q) throws DeploymentException {

      super(manager, q);

      JDBCQlQueryMetaData metadata = (JDBCQlQueryMetaData)q;
      getLog().debug("EQL-QL: "+metadata.getEjbQl());
      
      // get a parser
      Parser ejbql = new EJBQLParser().ejbqlQuery();
      
      // initialize the assembly
      Assembly a = new Assembly(metadata.getEjbQl());
      a.setTarget(new SQLTarget(
            q.getMethod(),
            manager.getJDBCTypeFactory(),
            manager.getContainer().getApplication(),
            q.getReadAhead()));
      
      // match the query
      a = ejbql.soleMatch(a);
      if(a == null) {
         throw new DeploymentException("Unable to parse EJB-QL: " +
               metadata.getEjbQl());
      }
      
      // get the final target
      SQLTarget target = (SQLTarget)a.getTarget();
      
      // set the sql
      setSQL(target.toSQL());
      
      // select bridge object
      Object selectBridgeObject = target.getSelectObject();
      if(selectBridgeObject instanceof JDBCEntityBridge) {
         setSelectEntity((JDBCEntityBridge)selectBridgeObject);
      } else if(selectBridgeObject instanceof JDBCCMPFieldBridge) {
         setSelectField((JDBCCMPFieldBridge)selectBridgeObject);
      } else {
         throw new IllegalStateException("Select bridge object is instance " +
               "of unknown type: selectBridgeObject=" + selectBridgeObject);
      }
      
      // get the parameter order
      setParameterList(target.getInputParameters());
   }
}
