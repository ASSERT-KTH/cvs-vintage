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
 * This class which contains information about an EJB QL query.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.9 $
 */
public final class JDBCQlQueryMetaData implements JDBCQueryMetaData
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
   private String ejbQl;

   /**
    * Constructs a JDBCQlQueryMetaData which is invoked by the specified method.
    * @param method the method which invokes this query
    */
   public JDBCQlQueryMetaData(Method method)
   {
      this.method = method;
      readAhead = JDBCReadAheadMetaData.DEFAULT;
      resultTypeMappingLocal = true;
   }

   public void loadXML(Element element) throws DeploymentException
   {
      ejbQl = MetaData.getElementContent(element);
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
    * Gets the EJB QL query which will be invoked.
    * @return the ejb ql String for this query
    */
   public String getEjbQl()
   {
      return ejbQl;
   }

   /**
    * Sets the EJB QL query which will be invoked.
    * @param ejbQl the new ejb ql String for this query
    */
   public void setEjbQl(String ejbQl)
   {
      this.ejbQl = ejbQl;
   }
   
   /**
    * Compares this JDBCQlQueryMetaData against the specified object. Returns
    * true if the objects are the same. Two JDBCQlQueryMetaData are the same
    * if they are both invoked by the same method.
    * @param o the reference object with which to compare
    * @return true if this object is the same as the object argument; 
    *    false otherwise
    */
   public boolean equals(Object o)
   {
      if(o instanceof JDBCQlQueryMetaData)
      {
         return ((JDBCQlQueryMetaData)o).method == method;
      }
      return false;
   }

   /**
    * Returns a hashcode for this JDBCQlQueryMetaData. The hashcode is computed
    * by the method which invokes this query.
    * @return a hash code value for this object
    */
   public int hashCode()
   {
      return method.hashCode();
   }
   /**
    * Returns a string describing this JDBCQlQueryMetaData. The exact details
    * of the representation are unspecified and subject to change, but the 
    * following may be regarded as typical:
    * 
    * "[JDBCQlQueryMetaData: method=public org.foo.User
    *       findByName(java.lang.String)]"
    *
    * @return a string representation of the object
    */
   public String toString()
   {
      return "[JDBCQlQueryMetaData : method=" + method + "]";
   }
}
