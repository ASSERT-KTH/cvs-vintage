/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Element;

import org.jboss.ejb.DeploymentException;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.QueryMetaData;

/**
 * JDBCQueryMetaDataFactory constructs a JDBCQueryMetaData object based
 * on the query specifiection type.
 *    
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.7 $
 */
public class JDBCQueryMetaDataFactory {
   private JDBCEntityMetaData entity;
   
   public JDBCQueryMetaDataFactory(JDBCEntityMetaData entity) {
      this.entity = entity;
   }

   public JDBCQueryMetaData createJDBCQueryMetaData(
         QueryMetaData queryMetaData,
         Method method) throws DeploymentException  {

      return new JDBCQlQueryMetaData(queryMetaData, method);
   }

   public JDBCQueryMetaData createJDBCQueryMetaData(
         JDBCQueryMetaData jdbcQueryMetaData,
         Element queryElement,
         Method method,
         JDBCReadAheadMetaData readAhead) throws DeploymentException {

      // RAW-SQL
      Element rawSql = MetaData.getOptionalChild(queryElement, "raw-sql");
      if(rawSql != null) {
         return new JDBCRawSqlQueryMetaData(method);
      }

      // DECLARED-SQL
      Element delcaredSql = 
            MetaData.getOptionalChild(queryElement, "declared-sql");
      if(delcaredSql != null) {
         return new JDBCDeclaredQueryMetaData(
               jdbcQueryMetaData,
               delcaredSql,
               method,
               readAhead);
      }

      // EJB-QL
      Element ejbQl = MetaData.getOptionalChild(queryElement, "ejb-ql");
      if(ejbQl != null) {
         return new JDBCQlQueryMetaData(
               (JDBCQlQueryMetaData)jdbcQueryMetaData,
               ejbQl,
               method,
               readAhead);
      }

      throw new DeploymentException(
            "Error in query spedification for method " + method.getName());
   }

   public Method[] getQueryMethods(Element queryElement)
         throws DeploymentException {

      // query-method sub-element
      Element queryMethod = 
            MetaData.getUniqueChild(queryElement, "query-method");
      
      // method name
      String methodName =
            MetaData.getUniqueChildContent(queryMethod, "method-name");
      
      // method params
      ArrayList methodParams = new ArrayList();
      Element methodParamsElement =
            MetaData.getUniqueChild(queryMethod, "method-params");
      Iterator iterator =
            MetaData.getChildrenByTagName(methodParamsElement, "method-param");
      while (iterator.hasNext()) {
         methodParams.add(MetaData.getElementContent((Element)iterator.next()));
      }
      Class[] parameters = convertToJavaClasses(methodParams.iterator());
      
      return getQueryMethods(methodName, parameters);
   }

   public Method[] getQueryMethods(QueryMetaData queryData)
         throws DeploymentException {
      String methodName = queryData.getMethodName();
      Class[] parameters = convertToJavaClasses(queryData.getMethodParams());
      return getQueryMethods(methodName, parameters);
   }

   public Method[] getQueryMethods(
         String methodName,
         Class parameters[]) throws DeploymentException {

      // find the query and load the xml
      ArrayList methods = new ArrayList(2);
      if(methodName.startsWith("ejbSelect")) {
         // bean method
         methods.add(getQueryMethod(
                  methodName,
                  parameters,
                  entity.getEntityClass()));
      } else {
         // remote home
         Class homeClass = entity.getHomeClass();
         if(homeClass != null) {
            methods.add(getQueryMethod(methodName, parameters, homeClass));
         }
         // local home
         Class localHomeClass = entity.getLocalHomeClass();
         if(localHomeClass != null) {
            methods.add(getQueryMethod(methodName, parameters, localHomeClass));
         }
      }          

      if(methods.size() == 0) {
         throw new DeploymentException("Query method not found: " + methodName);
      }
      return (Method[])methods.toArray(new Method[methods.size()]);
   }
      
   private Method getQueryMethod(
         String queryName,
         Class[] parameters,
         Class clazz) {

      try {
         Method method  = clazz.getMethod(queryName, parameters);

         // is the method abstract?
         // (remember interface methods are always abstract)
         if(Modifier.isAbstract(method.getModifiers())) {
            return method;
         }
      } catch(NoSuchMethodException e) {
         // that's cool
      }
      return null;
   }
   
   private Class[] convertToJavaClasses(Iterator iter)
         throws DeploymentException {

      ArrayList classes = new ArrayList();
      while(iter.hasNext()) {
         classes.add(convertToJavaClass((String)iter.next()));
      }
      return (Class[]) classes.toArray(new Class[classes.size()]);
   }
   
   private static final String[] PRIMITIVES = {
         "boolean",
         "byte",
         "char",
         "short",
         "int",
         "long",
         "float",
         "double"};
   
   private static final Class[] PRIMITIVE_CLASSES = {
         Boolean.TYPE,
         Byte.TYPE,
         Character.TYPE,
         Short.TYPE,
         Integer.TYPE,
         Long.TYPE,
         Float.TYPE,
         Double.TYPE};

   private Class convertToJavaClass(String name) throws DeploymentException {
      // Check primitive first
      for (int i = 0; i < PRIMITIVES.length; i++) {
         if(name.equals(PRIMITIVES[i])) {
            return PRIMITIVE_CLASSES[i];
         }
      }
      
      try {
         return entity.getClassLoader().loadClass(name);
      } catch(ClassNotFoundException e) {
         throw new DeploymentException("Parameter class not found: " + name);
      }
   }
}
