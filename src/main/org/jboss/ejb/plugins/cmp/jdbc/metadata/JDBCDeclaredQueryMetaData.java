/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.lang.reflect.Method;

import org.w3c.dom.Element;

import org.jboss.deployment.DeploymentException;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.QueryMetaData;

/**
 * This class contains information about a declated query.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 *   @version $Revision: 1.13 $
 */
public final class JDBCDeclaredQueryMetaData implements JDBCQueryMetaData
{
   /**
    * The method to which this query is bound.
    */
   private final Method method;

   /**
    * The user specified from clause.
    */
   private String from;

   /**
    * The user specified where clause.
    */
   private String where;

   /**
    * The user specified order clause.
    */
   private String order;

   /**
    * The other clause is appended to the end of the sql.  This is useful for
    * hints to the query engine.
    */
   private String other;

   /**
    * Should the select be DISTINCT?
    */
   private boolean distinct;
   
   /**
    * The name of the ejb from which the field will be selected.
    */
   private String ejbName;

   /**
    * The name of the cmp-field to be selected.
    */
   private String fieldName;

   /**
    * The aliase that is used for the main select table.
    */
   private String alias;
   /**
    * Read ahead meta data.
    */
   private JDBCReadAheadMetaData readAhead;

   /**
    * Should the query return Local or Remote beans.
    */
   private boolean resultTypeMappingLocal;

   /**
    * Constructs a JDBCDeclaredQueryMetaData which is invoked by the specified
    * method.
    * @param method the method which invokes this query
    */
   public JDBCDeclaredQueryMetaData(Method method)
   {
      this.method = method;
      readAhead = JDBCReadAheadMetaData.DEFAULT;
      resultTypeMappingLocal = true;
   }   

   public void loadXML(Element queryElement) throws DeploymentException
   {
      from = MetaData.getOptionalChildContent(queryElement, "from");
      where = MetaData.getOptionalChildContent(queryElement, "where");
      order = MetaData.getOptionalChildContent(queryElement, "order");
      other = MetaData.getOptionalChildContent(queryElement, "other");

      // load ejbSelect info
      Element selectElement = 
            MetaData.getOptionalChild(queryElement, "select");
         
      if(selectElement != null)
      {
         // should select use distinct?
         distinct = 
            (MetaData.getOptionalChild(selectElement, "distinct") != null);
         
         if(method.getName().startsWith("ejbSelect"))
         {
            ejbName = MetaData.getUniqueChildContent(selectElement, "ejb-name");
            fieldName = 
                  MetaData.getOptionalChildContent(selectElement, "field-name");
         }
         else
         {
            // the ejb-name and field-name elements are not allowed for finders
            if(MetaData.getOptionalChild(selectElement, "ejb-name") != null)
            {
               throw new DeploymentException(
                     "The ejb-name element of declared-sql select is only " +
                     "allowed for ejbSelect queries.");
            }
            if(MetaData.getOptionalChild(selectElement, "field-name") != null)
            {
               throw new DeploymentException(
                     "The field-name element of declared-sql select is only " +
                     "allowed for ejbSelect queries.");
            }
            ejbName = null;
            fieldName = null;
         }
         alias = MetaData.getOptionalChildContent(selectElement, "alias");
      }
      else
      {
         if(method.getName().startsWith("ejbSelect"))
         {
            throw new DeploymentException("The select element of " +
                  "declared-sql is required for ejbSelect queries.");
         } 
         distinct = false;
         ejbName = null;
         fieldName = null;
         alias = null;
      }
   }

   // javadoc in parent class
   public Method getMethod()
   {
      return method;
   }

   // javadoc in interface
   public boolean isResultTypeMappingLocal()
   {
      return resultTypeMappingLocal;
   }

   // javadoc in interface
   public void setResultTypeMappingLocal(boolean resultTypeMappingLocal)
   {
      this.resultTypeMappingLocal = resultTypeMappingLocal;
   }
      
   // javadoc in interface
   public JDBCReadAheadMetaData getReadAhead()
   {
      return readAhead;
   }

   // javadoc in interface
   public void setReadAhead(JDBCReadAheadMetaData readAhead)
   {
      this.readAhead = readAhead;
   }

   /**
    * Gets the sql FROM clause of this query.
    * @return a String which contains the sql FROM clause
    */
   public String getFrom()
   {
      return from;
   }

   /**
    * Sets the sql FROM clause of this query.
    * @param from the new sql FROM clause for this query
    */
   public void setFrom(String from)
   {
      this.from = from;
   }

   /**
    * Gets the sql WHERE clause of this query.
    * @return a String which contains the sql WHERE clause
    */
   public String getWhere()
   {
      return where;
   }

   /**
    * Sets the sql WHERE clause of this query.
    * @param where the new sql WHERE clause for this query
    */
   public void setWhere(String where)
   {
      this.where = where;
   }

   /**
    * Gets the sql ORDER BY clause of this query.
    * @return a String which contains the sql ORDER BY clause
    */
   public String getOrder()
   {
      return order;
   }
   
   /**
    * Sets the sql ORDER BY clause of this query.
    * @param order the new sql ORDER BY clause for this query
    */
   public void setOrder(String order)
   {
      this.order = order;
   }
   
   /**
    * Gets other sql code which is appended to the end of the query.
    * This is userful for supplying hints to the query engine.
    * @return a String which contains additional sql code which is 
    * appended to the end of the query
    */
   public String getOther()
   {
      return other;
   }

   /**
    * Sets other sql code which is appended to the end of the query.
    * This is userful for supplying hints to the query engine.
    * @param other the new additional sql code that will be appended to 
    * the end of the query
    */
   public void setOther(String other)
   {
      this.other = other;
   }

   /**
    * Should the select be DISTINCT?
    * @return true if the select clause should contain distinct
    */
   public boolean isSelectDistinct()
   {
      return distinct;
   }

   /**
    * Sets query to select DISTINCT or not.
    * @param distinct if true, the select clause should contain distinct
    */
   public void setSelectDistinct(boolean distinct)
   {
      this.distinct = distinct;
   }

   /**
    * Gets the name of the ejb from which the field will be selected.
    * @return the name of the ejb which will be selected or the name of
    * the ejb from which a field will be selected
    */
   public String getEJBName()
   {
      return ejbName;
   }

   /**
    * Sets the name of the ejb from which the field will be selected.
    * @param ejbName the name of the ejb which will be selected or the name of
    * the ejb from which a field will be selected
    */
   public void setEJBName(String ejbName)
   {
      this.ejbName = ejbName;
   }

   /**
    * Gets the name of the cmp-field to be selected.
    * @return the name of the cmp-field to be selected or null if returning a 
    * whole ejb
    */
   public String getFieldName()
   {
      return fieldName;
   }
   
   /**
    * Sets the name of the cmp-field to be selected.
    * @param fieldName the name of the cmp-field to be selected or null if the
    * entire ejb object is to be selected
    */
   public void setFieldName(String fieldName)
   {
      this.fieldName = fieldName;
   }
   
   /**
    * Gets the alias that is used for the select table.
    * @return the alias that is used for the table from which the entity or
    * field is selected.
    */
   public String getAlias()
   {
      return alias;
   }
   
   /**
    * Sets the alias that is used for the select table.
    * @param alias the alias that is used for the table from which the entity or
    * field is selected.
    */
   public void setAlias(String alias)
   {
      this.alias = alias;
   }
   
   /**
    * Compares this JDBCDeclaredQueryMetaData against the specified object.
    * Returns true if the objects are the same. Two JDBCDeclaredQueryMetaData
    * are the same if they are both invoked by the same method.
    * @param o the reference object with which to compare
    * @return true if this object is the same as the object argument; false
    * otherwise
    */
   public boolean equals(Object o)
   {
      if(o instanceof JDBCDeclaredQueryMetaData)
      {
         return ((JDBCDeclaredQueryMetaData)o).method == method;
      }
      return false;
   }
   
   /**
    * Returns a hashcode for this JDBCDeclaredQueryMetaData. The hashcode is
    * computed by the method which invokes this query.
    * @return a hash code value for this object
    */
   public int hashCode()
   {
      return method.hashCode();
   }

   /**
    * Returns a string describing this JDBCDeclaredQueryMetaData. The exact 
    * details of the representation are unspecified and subject to change, 
    * but the following may be regarded as typical:
    * 
    * "[JDBCDeclaredQueryMetaData: method=public org.foo.User findByName(
    *    java.lang.String)]"
    *
    * @return a string representation of the object
    */
   public String toString()
   {
      return "[JDBCDeclaredQueryMetaData : method=" + method + "]";
   }
}
