/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Element;

import org.jboss.deployment.DeploymentException;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.QueryMetaData;

/**
 * JDBCQueryMetaDataFactory constructs a JDBCQueryMetaData object based
 * on the query specifiection type.
 *    
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.12 $
 */
public class JDBCQueryMetaDataFactory {
   private JDBCEntityMetaData entity;
   
   public JDBCQueryMetaDataFactory(JDBCEntityMetaData entity) {
      this.entity = entity;
   }

   public Map createJDBCQueryMetaData(QueryMetaData queryData)
         throws DeploymentException {

      Method[] methods = getQueryMethods(queryData);
      Map queries = new HashMap(methods.length);
      for(int i=0; i<methods.length; i++) {
         queries.put(methods[i],
               new JDBCQlQueryMetaData(queryData, methods[i]));
      }
      return queries;
   }

   public Map createJDBCQueryMetaData(
         Element queryElement,
         Map defaultValues,
         JDBCReadAheadMetaData readAhead) throws DeploymentException {

      // get the query methods
      Method[] methods = getQueryMethods(queryElement);
      
      // read-ahead
      Element readAheadElement =
            MetaData.getOptionalChild(queryElement, "read-ahead");
      if(readAheadElement != null) {
         readAhead = new JDBCReadAheadMetaData(readAheadElement, readAhead);
      }

      Map queries = new HashMap(methods.length);
      for(int i=0; i<methods.length; i++) {
         JDBCQueryMetaData defaultValue = 
               (JDBCQueryMetaData)defaultValues.get(methods[i]);

         if(defaultValue == null) {
            throw new DeploymentException("Unknown query method : "+methods[i]);
         }

         JDBCQueryMetaData jdbcQueryData = createJDBCQueryMetaData(
               defaultValue, 
               queryElement, 
               methods[i], 
               readAhead);

         queries.put(methods[i], jdbcQueryData);
      }
      return queries;
   }
         
   private JDBCQueryMetaData createJDBCQueryMetaData(
         JDBCQueryMetaData jdbcQueryMetaData,
         Element queryElement,
         Method method,
         JDBCReadAheadMetaData readAhead) throws DeploymentException {

      // RAW-SQL
      Element rawSql = MetaData.getOptionalChild(queryElement, "raw-sql");
      if(rawSql != null) {
         return new JDBCRawSqlQueryMetaData(method);
      }

      // JBOSS-QL
      Element jbossQL = 
            MetaData.getOptionalChild(queryElement, "jboss-ql");
      if(jbossQL != null) {
         return new JDBCJBossQLQueryMetaData(
               jdbcQueryMetaData,
               jbossQL,
               method,
               readAhead);
      }

      // DYNAMIC-SQL
      Element dynamicQL = 
            MetaData.getOptionalChild(queryElement, "dynamic-ql");
      if(dynamicQL != null) {
         return new JDBCDynamicQLQueryMetaData(
               jdbcQueryMetaData,
               dynamicQL,
               method);
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

      // EJB-QL: default
      if(jdbcQueryMetaData instanceof JDBCQlQueryMetaData) {
         return new JDBCQlQueryMetaData(
               (JDBCQlQueryMetaData)jdbcQueryMetaData,
               method,
               readAhead);
      }

      throw new DeploymentException(
            "Error in query specification for method " + method.getName());
   }

   private Method[] getQueryMethods(Element queryElement)
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

   private Method[] getQueryMethods(QueryMetaData queryData)
         throws DeploymentException {
      String methodName = queryData.getMethodName();
      Class[] parameters = convertToJavaClasses(queryData.getMethodParams());
      return getQueryMethods(methodName, parameters);
   }

   private Method[] getQueryMethods(
         String methodName,
         Class parameters[]) throws DeploymentException {

      // find the query and load the xml
      ArrayList methods = new ArrayList(2);
      if(methodName.startsWith("ejbSelect")) {
         // bean method
         Method method = getQueryMethod(
                  methodName, parameters, entity.getEntityClass());
         if(method != null) {
            methods.add(method);
         }
      } else {
         // remote home
         Class homeClass = entity.getHomeClass();
         if(homeClass != null) {
            Method method = getQueryMethod(methodName, parameters, homeClass);
            if(method != null) {
               methods.add(method);
            }
         }
         // local home
         Class localHomeClass = entity.getLocalHomeClass();
         if(localHomeClass != null) {
            Method method = getQueryMethod(
                  methodName, parameters, localHomeClass);
            if(method != null) {
               methods.add(method);
            }
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
      
      int arraySize = 0;
      while(name.endsWith("[]")) {
         name = name.substring(0, name.length()-2);
         arraySize++;
      }

      try {
         // get the base class
         Class c = entity.getClassLoader().loadClass(name);

         // if we have an array get the array class
         if(arraySize > 0) {
            int[] dimensions = new int[arraySize];
            for(int i=0; i<arraySize; i++) {
               dimensions[i]=1;
            }
            c = Array.newInstance(c, dimensions).getClass();
         }

         return c;
      } catch(ClassNotFoundException e) {
         throw new DeploymentException("Parameter class not found: " + name);
      }
   }
}
