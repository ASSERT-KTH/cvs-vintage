/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.lang.reflect.Method;

/**
 * This class contains information about an automatically generated
 * query. This class is a place holder used to make an automaticlly generated
 * query look more like a user specified query.  This class only contains a
 * referance to the method used to invoke this query.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 *   @author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @version $Revision: 1.6 $
 */
public final class JDBCAutomaticQueryMetaData implements JDBCQueryMetaData
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
    * Constructs a JDBCAutomaticQueryMetaData which is invoked by the specified
    * method.
    * @param method the method which invokes this query
    */
   public JDBCAutomaticQueryMetaData(Method method)
   {
      this.method = method;
      readAhead = JDBCReadAheadMetaData.DEFAULT;
      resultTypeMappingLocal = true;
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
    * Compares this JDBCQlQueryMetaData against the specified object. Returns
    * true if the objects are the same. Two JDBCQlQueryMetaData are the same
    * if they are both invoked by the same method.
    * @param o the reference object with which to compare
    * @return true if this object is the same as the object argument; 
    *    false otherwise
    */
   public boolean equals(Object o)
   {
      if(o instanceof JDBCAutomaticQueryMetaData)
      {
         return ((JDBCAutomaticQueryMetaData)o).method == method;
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
    * Returns a string describing this JDBCAutomaticQueryMetaData. The exact
    * details of the representation are unspecified and subject to change, but
    * the following may be regarded as typical:
    *
    * "[JDBCAutomaticQueryMetaData: 
    *       method=public org.foo.User findByName(java.lang.String)]"
    *
    * @return a string representation of the object
    */
   public String toString() {
      return "[JDBCAutomaticQueryMetaData : method=" + method + "]";
   }

}
