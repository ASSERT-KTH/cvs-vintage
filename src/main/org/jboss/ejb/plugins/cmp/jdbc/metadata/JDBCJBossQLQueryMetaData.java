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
 * This class which contains information about an JBossQL query.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.3 $
 */
public final class JDBCJBossQLQueryMetaData implements JDBCQueryMetaData
{
   /**
    * The method to which this query is bound.
    */
   private final Method method;

   /**
    * Should the query return Local or Remote beans.
    */
   private boolean resultTypeMappingLocal;

   /**
    * Read ahead meta data.
    */
   private JDBCReadAheadMetaData readAhead;

   /**
    * The ejb-ql fro the query.
    */
   private String jbossQL;

   /**
    * Constructs a JDBCJBossQLQueryMetaData which is invoked by the specified
    * method.
    * @param method the method which invokes this query
    */
   public JDBCJBossQLQueryMetaData(Method method) throws DeploymentException
   {
      this.method = method;
      readAhead = JDBCReadAheadMetaData.DEFAULT;
      resultTypeMappingLocal = true;
   }

   public void loadXML(Element element) throws DeploymentException
   {
      String jbossQLString = MetaData.getElementContent(element);
      if(jbossQLString == null || jbossQLString.trim().length() == 0)
      {
         throw new DeploymentException("jboss-ql element is empty");
      }
      jbossQL = jbossQLString;
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
    * Gets the JBossQL query which will be invoked.
    * @return the ejb ql String for this query
    */
   public String getJBossQL()
   {
      return jbossQL;
   }

   /**
    * Sets the JBossQL query which will be invoked.
    * @param jbossQL the new ejb ql String for this query
    */
   public void setJBossQL(String jbossQL)
   {
      this.jbossQL = jbossQL;
   }

   /**
    * Compares this JDBCJBossQLQueryMetaData against the specified object.
    * Returns true if the objects are the same. Two JDBCJBossQLQueryMetaData
    * are the same if they are both invoked by the same method.
    * @param o the reference object with which to compare
    * @return true if this object is the same as the object argument; 
    *    false otherwise
    */
   public boolean equals(Object o)
   {
      if(o instanceof JDBCJBossQLQueryMetaData)
      {
         return ((JDBCJBossQLQueryMetaData)o).method == method;
      }
      return false;
   }

   /**
    * Returns a hashcode for this JDBCJBossQLQueryMetaData. The hashcode is
    * computed by the method which invokes this query.
    * @return a hash code value for this object
    */
   public int hashCode()
   {
      return method.hashCode();
   }
   /**
    * Returns a string describing this JDBCJBossQLQueryMetaData. The exact
    * details of the representation are unspecified and subject to change, but
    * the following may be regarded as typical:
    * 
    * "[JDBCJBossQLQueryMetaData: method=public org.foo.User
    *       findByName(java.lang.String)]"
    *
    * @return a string representation of the object
    */
   public String toString()
   {
      return "[JDBCJBossQLQueryMetaData : method=" + method + "]";
   }
}
