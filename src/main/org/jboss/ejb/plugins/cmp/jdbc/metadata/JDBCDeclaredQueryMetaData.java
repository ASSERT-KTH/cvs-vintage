/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.lang.reflect.Method;

import org.jboss.ejb.DeploymentException;
import org.jboss.metadata.MetaData;

import org.w3c.dom.Element;

/**
 * Imutable class contains information about a declated query.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 *   @version $Revision: 1.5 $
 */
public final class JDBCDeclaredQueryMetaData implements JDBCQueryMetaData {
   private final Method method;
   private final String from;
   private final String where;
   private final String order;
   private final String other;

   /**
    * Read ahead meta data.
    */
   private final JDBCReadAheadMetaData readAhead;


   /**
    * Constructs a JDBCDeclaredQueryMetaData which is defined by the declared-sql xml element
    * and is invoked by the specified method.
    * @param queryElement the xml Element which contains the metadata about this query
    * @param method the method which invokes this query
    */
   public JDBCDeclaredQueryMetaData(Element queryElement, Method method, JDBCReadAheadMetaData readAhead) throws DeploymentException {
      this.method = method;
      this.readAhead = readAhead;

      from = MetaData.getOptionalChildContent(queryElement, "from");
      where = MetaData.getOptionalChildContent(queryElement, "where");
      order = MetaData.getOptionalChildContent(queryElement, "order");
      other = MetaData.getOptionalChildContent(queryElement, "other");
   }

   public Method getMethod() {
      return method;
   }

   public boolean isResultTypeMappingLocal() {
      return false;
   }

   /**
    * Gets the read ahead metadata for the query.
    * @return the read ahead metadata for the query.
    */
   public JDBCReadAheadMetaData getReadAhead() {
      return readAhead;
   }

   /**
    * Gets the sql FROM clause of this query.
    * @returns a String which contains the sql FROM clause
    */
   public String getFrom() {
      return from;
   }

   /**
    * Gets the sql WHERE clause of this query.
    * @returns a String which contains the sql WHERE clause
    */
   public String getWhere() {
      return where;
   }

   /**
    * Gets the sql ORDER BY clause of this query.
    * @returns a String which contains the sql ORDER BY clause
    */
   public String getOrder() {
      return order;
   }
   
   /**
    * Gets other sql code which is appended to the end of the query.
    * This is userful for supplying hints to the query engine.
    * @returns a String which contains additional sql code which is 
    *         appended to the end of the query
    */
   public String getOther() {
      return other;
   }

   /**
    * Compares this JDBCDeclaredQueryMetaData against the specified object. Returns
    * true if the objects are the same. Two JDBCDeclaredQueryMetaData are the same 
    * if they are both invoked by the same method.
    * @param o the reference object with which to compare
    * @return true if this object is the same as the object argument; false otherwise
    */
   public boolean equals(Object o) {
      if(o instanceof JDBCDeclaredQueryMetaData) {
         return ((JDBCDeclaredQueryMetaData)o).method.equals(method);
      }
      return false;
   }
   
   /**
    * Returns a hashcode for this JDBCDeclaredQueryMetaData. The hashcode is computed
    * by the method which invokes this query.
    * @return a hash code value for this object
    */
   public int hashCode() {
      return method.hashCode();
   }
   /**
    * Returns a string describing this JDBCDeclaredQueryMetaData. The exact details
    * of the representation are unspecified and subject to change, but the following
    * may be regarded as typical:
    * 
    * "[JDBCDeclaredQueryMetaData: method=public org.foo.User findByName(java.lang.String)]"
    *
    * @return a string representation of the object
    */
   public String toString() {
      return "[JDBCDeclaredQueryMetaData : method=" + method + "]";
   }
}
