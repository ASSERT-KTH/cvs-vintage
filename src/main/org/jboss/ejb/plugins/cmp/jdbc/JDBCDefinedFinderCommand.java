/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.util.ArrayList;
import java.util.StringTokenizer;

import java.sql.PreparedStatement;

import org.jboss.ejb.DeploymentException;

import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCDeclaredQueryMetaData;

/**
 * JDBCDefinedFinderCommand finds entities based on an xml sql specification.
 * This class needs more work and I will clean it up in CMP 2.x phase 3.
 * The only thing to to note is the seperation of query into a from and where
 * clause. This code has been cleaned up to improve readability.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:michel.anke@wolmail.nl">Michel de Groot</a>
 * @author <a href="danch@nvisia.com">danch (Dan Christopherson</a>
 * @version $Revision: 1.3 $
 */
public class JDBCDefinedFinderCommand extends JDBCFinderCommand
{
   // Attributes ----------------------------------------------------
   
   private JDBCDeclaredQueryMetaData metadata;
   private int[] parameterArray;

   // Constructors --------------------------------------------------

   public JDBCDefinedFinderCommand(JDBCStoreManager manager, JDBCQueryMetaData q) throws DeploymentException {
      super(manager, q);

      metadata = (JDBCDeclaredQueryMetaData) q;
      
      StringBuffer sql = new StringBuffer();
      
      String from = metadata.getFrom();
      if(from != null && from.length()>0) {
         sql.append("SELECT ").append(SQLUtil.getColumnNamesClause(entity.getJDBCPrimaryKeyFields(), entityMetaData.getTableName()));
         sql.append(" FROM ").append(entityMetaData.getTableName());
      } else {
         sql.append("SELECT ").append(SQLUtil.getColumnNamesClause(entity.getJDBCPrimaryKeyFields()));
         sql.append(" FROM ").append(entityMetaData.getTableName());
      }
      
      String where = parseWhere(metadata.getWhere());
      if(where.length() > 0) {
         sql.append(" WHERE ").append(where);
      }
      
      String order = metadata.getOrder();
      if(order != null && order.trim().length() > 0) {
         sql.append(" ORDER BY ").append(order);
      }

      String other = metadata.getOther();
      if(other != null && other.trim().length() > 0) {
         sql.append(other);
      }

      setSQL(sql.toString());
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
   
   protected String parseWhere(String where) throws DeploymentException {
      StringBuffer whereBuf = new StringBuffer();
      ArrayList parameters = new ArrayList();      
      
      // Replace placeholders {0} with ?
      if(where != null) {
         where = where.trim();

         StringTokenizer queryWhere = new StringTokenizer(where,"{}", true);
         while(queryWhere.hasMoreTokens()) {
            String token = queryWhere.nextToken();
            if(token.equals("{")) {
               
               token = queryWhere.nextToken();
               try {
                  Integer parameterIndex = new Integer(token);
                  
                  // of if we are here we can assume that we have a parameter and not a function
                  whereBuf.append("?");
                  parameters.add(parameterIndex);
                  
                  if(!queryWhere.nextToken().equals("}")) {
                     throw new DeploymentException("Invalid parameter - missing closing '}' : " + where);
                  }
               } catch(NumberFormatException e) {
                  // ok we don't have a parameter, we have a function
                  // push the tokens on the buffer and continue
                  whereBuf.append("{").append(token);                  
               }   
            } else {
               // not parameter... just append it
               whereBuf.append(token);
            }
         }
      }

      // save out the parameter order
      parameterArray = new int[parameters.size()];
      for(int i=0; i<parameterArray.length; i++) {
         parameterArray[i] = ((Integer)parameters.get(i)).intValue();
      }
      
      return whereBuf.toString().trim();
   }
}
