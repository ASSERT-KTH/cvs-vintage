/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Set;
import java.util.HashSet;

import java.sql.PreparedStatement;

import org.jboss.ejb.plugins.jaws.metadata.FinderMetaData;
import org.jboss.ejb.plugins.jaws.metadata.TypeMappingMetaData;

/**
 * JAWSPersistenceManager JDBCDefinedFinderCommand
 *
 * @see <related>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:michel.anke@wolmail.nl">Michel de Groot</a>
 * @version $Revision: 1.12 $
 */
public class JDBCDefinedFinderCommand extends JDBCFinderCommand
{
   // Attributes ----------------------------------------------------

   private int[] parameterArray;
   private TypeMappingMetaData typeMapping;

   // Constructors --------------------------------------------------

   public JDBCDefinedFinderCommand(JDBCCommandFactory factory, FinderMetaData f)
   {
      super(factory, f.getName());

      typeMapping = jawsEntity.getJawsApplication().getTypeMapping();

      // Replace placeholders with ?, but only if query is defined
      String query = "";
      ArrayList parameters = new ArrayList();
      if (f.getQuery() != null)  {
	      StringTokenizer finderQuery = new StringTokenizer(f.getQuery(),"{}", true);

	      while (finderQuery.hasMoreTokens())
	      {
	         String t = finderQuery.nextToken();
	         if (t.equals("{"))
	         {
	            query += "?";
	            String idx = finderQuery.nextToken(); // Remove number
	            parameters.add(new Integer(idx));
	            finderQuery.nextToken(); // Remove }
	         } else
	            query += t;
	      }
      }

      // Copy index numbers to parameterArray
      parameterArray = new int[parameters.size()];
      for (int i = 0; i < parameterArray.length; i++)
         parameterArray[i] = ((Integer)parameters.get(i)).intValue();

      // Since the fields in order clause also will form the select clause together with
      // the pk field list, we have to clean the order clause from ASC/DESC's and fields
      // that already are within the pk list
      String strippedOrder = "";
      if(f.getOrder()!=null && f.getOrder()!="")
      {
        //Split it into tokens. These tokens might contain ASC/DESC that we have to get rid of
        StringTokenizer orderTokens = new StringTokenizer(f.getOrder(), ",");
        String orderToken;
        String[] checkedOrderTokens = new String[orderTokens.countTokens()];
        int ix = 0;
        while(orderTokens.hasMoreTokens())
        {
          orderToken = orderTokens.nextToken().trim();
          //Get rid of ASC's
          int i = orderToken.toUpperCase().indexOf(" ASC");
          if(i!=-1)
            checkedOrderTokens[ix] = orderToken.substring(0, i).trim();
          else
          {
            //Get rid of DESC's
            i = orderToken.toUpperCase().indexOf(" DESC");
            if(i!=-1)
              checkedOrderTokens[ix] = orderToken.substring(0, i).trim();
            else
            {
              //No ASC/DESC - just use it as it is
              checkedOrderTokens[ix] = new String(orderToken).trim();
            }
          }
          ix++;
        }

        //Next step is to make up a Set of all pk tokens
        StringTokenizer pkTokens = new StringTokenizer(getPkColumnList(), ",");
        Set setOfPkTokens = new HashSet(pkTokens.countTokens());
        while(pkTokens.hasMoreTokens())
        {
          setOfPkTokens.add(pkTokens.nextToken().trim());
        }

        //Now is the time to check for duplicates between pk and order tokens
        int i = 0;
        while(i < checkedOrderTokens.length)
        {
          //If duplicate token, null it away
          if(setOfPkTokens.contains(checkedOrderTokens[i]))
          {
            checkedOrderTokens[i]=null;
          }
          i++;
        }

        //Ok, build a new order string that we can use later on
        StringBuffer orderTokensToUse = new StringBuffer("");
        i = 0;
        while(i < checkedOrderTokens.length)
        {
          if(checkedOrderTokens[i]!=null)
          {
            orderTokensToUse.append(", ");
            orderTokensToUse.append(checkedOrderTokens[i]);
          }
          i++;
        }
        // Note that orderTokensToUse will always start with ", " if there is any order tokens
        strippedOrder = orderTokensToUse.toString();
      }

      // Construct SQL
      // In case of join query:
      // order must explicitly identify tablename.field to order on
      // query must start with "INNER JOIN <table to join with> WHERE
      // <regular query with fully identified fields>"
      String sql = null;
      if (query.toLowerCase().startsWith(",")) {
          //Modified by Vinay Menon
          StringBuffer sqlBuffer = new StringBuffer();

      	  sqlBuffer.append("SELECT ");

          String primaryKeyList = getPkColumnList();
          String tableName = jawsEntity.getTableName();
          StringTokenizer stok = new StringTokenizer(primaryKeyList,",");

          while(stok.hasMoreTokens()){
            sqlBuffer.append(tableName);
            sqlBuffer.append(".");
            sqlBuffer.append(stok.nextElement().toString());
            sqlBuffer.append(",");
          }

         sqlBuffer.setLength(sqlBuffer.length()-1);
         sqlBuffer.append(strippedOrder);
         sqlBuffer.append(" FROM ");
         sqlBuffer.append(jawsEntity.getTableName());
         sqlBuffer.append(" ");
         sqlBuffer.append(query);

         sql = sqlBuffer.toString();
      } else
      if (query.toLowerCase().startsWith("inner join")) {
          StringBuffer sqlBuffer = new StringBuffer();

      	  sqlBuffer.append("SELECT ");

          String primaryKeyList = getPkColumnList();
          String tableName = jawsEntity.getTableName();
          StringTokenizer stok = new StringTokenizer(primaryKeyList,",");

          while(stok.hasMoreTokens()){
            sqlBuffer.append(tableName);
            sqlBuffer.append(".");
            sqlBuffer.append(stok.nextElement().toString());
            sqlBuffer.append(",");
          }

         sqlBuffer.setLength(sqlBuffer.length()-1);
         sqlBuffer.append(strippedOrder);
         sqlBuffer.append(" FROM ");
         sqlBuffer.append(jawsEntity.getTableName());
         sqlBuffer.append(" ");
         sqlBuffer.append(query);

         sql = sqlBuffer.toString();
      } else {
      	// regular query; check if query is empty,
      	// if so, this is a select all and WHERE should not be used
      	if (f.getQuery() == null)  {
	      	sql = "SELECT " + getPkColumnList() + strippedOrder +
	      	 	" FROM " + jawsEntity.getTableName();
      	} else {
	      	sql = "SELECT " + getPkColumnList() + strippedOrder +
	         	" FROM " + jawsEntity.getTableName() + " WHERE " + query;
      	}
      }
      if (f.getOrder() != null && !f.getOrder().equals(""))
      {
         sql += " ORDER BY "+f.getOrder();
      }

      setSQL(sql);
   }

   // JDBCFinderCommand overrides ------------------------------------

   protected void setParameters(PreparedStatement stmt, Object argOrArgs)
      throws Exception
   {
      Object[] args = (Object[])argOrArgs;

      for (int i = 0; i < parameterArray.length; i++)
      {
          Object arg = args[parameterArray[i]];
          int jdbcType = typeMapping.getJdbcTypeForJavaType(arg.getClass());
          setParameter(stmt,i+1,jdbcType,arg);
      }
   }
}
