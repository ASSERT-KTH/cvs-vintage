package org.jboss.ejb.plugins.cmp.jdbc;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.StringTokenizer;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;

import org.jboss.ejb.Application;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.plugins.CMPPersistenceManager;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCApplicationMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityMetaData;

import org.jboss.logging.Logger;

public class QueryParameter {
   private int argNum;
   private JDBCCMPFieldBridge field;
   private JDBCTypeComplexProperty property;
   private String parameterString;
   
   private int jdbcType;

   public QueryParameter(
         JDBCStoreManager manager, 
         Method method, 
         String parameterString) {

      this.parameterString = parameterString;

      if(parameterString == null || parameterString.length() == 0) {
         throw new IllegalArgumentException("Parameter string is empty");
      }

      StringTokenizer tok = new StringTokenizer(parameterString, ".");

      // get the argument number
      try {
         argNum = Integer.parseInt(tok.nextToken());
      } catch(NumberFormatException e) {
         throw new IllegalArgumentException("The parameter must begin with " +
               "a number");
      }

      // get the argument type
      if(argNum > method.getParameterTypes().length) {
         throw new IllegalArgumentException("The parameter index is " + argNum +
               " but the query method only has " +
               method.getParameterTypes().length + "parameter(s)");
      }
      Class argType = method.getParameterTypes()[argNum];

      // get the jdbc type object
      JDBCType type;

      // if this is an entity parameter
      if(EJBObject.class.isAssignableFrom(argType) || 
            EJBLocalObject.class.isAssignableFrom(argType)) {

         // get the field name
         // check more tokens
         if(!tok.hasMoreTokens()) {
            throw new IllegalArgumentException("When the parameter is an " +
                  "ejb a field name must be supplied.");
         }
         String fieldName = tok.nextToken();

         // get the field from the entity
         field = getCMPField(manager, argType, fieldName);
         if(!field.isPrimaryKeyMember()) {
            throw new IllegalArgumentException("The specified field must be " +
                  "a primay key field");
         }

         // get the jdbc typ object
         type = field.getJDBCType();
      } else {
         // get jdbc type from type manager
         type = manager.getJDBCTypeFactory().getJDBCType(argType);
      }

      if(type instanceof JDBCTypeSimple) {
         if(tok.hasMoreTokens()){
            throw new IllegalArgumentException("Parameter is NOT a known " +
                  "dependent value class, so a properties cannot supplied.");
         }
         jdbcType = type.getJDBCTypes()[0];
      } else {
         if(!tok.hasMoreTokens()){
            throw new IllegalArgumentException("Parmeter is a known " +
                  "dependent value class, so a property must be supplied");
         }

         // build the propertyName
         StringBuffer propertyName = new StringBuffer(parameterString.length());
         propertyName.append(tok.nextToken());
         while(tok.hasMoreTokens()) {
            propertyName.append(".").append(tok.nextToken());
         }
            
         property = ((JDBCTypeComplex)type).getProperty(
               propertyName.toString());

         jdbcType = property.getJDBCType();
      }
   }

   public QueryParameter(
         int argNum, 
         JDBCCMPFieldBridge field,
         JDBCTypeComplexProperty property,
         int jdbcType) {

      this.argNum = argNum;
      this.field = field;
      this.property = property;
      this.jdbcType = jdbcType;

      StringBuffer parameterBuf = new StringBuffer();
      parameterBuf.append(argNum);
      if(field != null) {
         parameterBuf.append(".").append(field.getFieldName());
      }
      if(property != null) {
         parameterBuf.append(".").append(property.getPropertyName());
      }
      parameterString = parameterBuf.toString();
   }

   /**
    * Gets the dotted parameter string for this parameter.
    */
   public String getParameterString() {
      return parameterString;
   }
   
   public void set(Logger log, PreparedStatement ps, int index, Object[] args) 
         throws Exception {

      Object arg = args[argNum];
      if(field != null) {
         if (arg instanceof EJBObject) {
            arg = ((EJBObject)arg).getPrimaryKey();
         } else if (arg instanceof EJBLocalObject) {
            arg = ((EJBLocalObject)arg).getPrimaryKey();
         } else {
            throw new IllegalArgumentException("Expected an instanc of " +
                  "EJBObject or EJBLocalObject, but got an instance of " + 
                  arg.getClass().getName());
         }
         arg = field.getPrimaryKeyValue(arg);
      }
      if(property != null) {
         arg = property.getColumnValue(arg);
      }
      JDBCUtil.setParameter(log, ps, index, jdbcType, arg);
   }

   private JDBCCMPFieldBridge getCMPField(
         JDBCStoreManager manager, 
         Class intf,
         String fieldName) {
      try {
         // get this application's metadata
         JDBCApplicationMetaData appMD = 
             manager.getMetaData().getJDBCApplication();

         // get the entity metadata associated with supplied interface
         JDBCEntityMetaData entMD = appMD.getBeanByInterface(intf);

         // get this application's object
         Application app = manager.getContainer().getApplication();

         // get the entity container with the name from 
         // the found entity metadata
         EntityContainer con = 
               (EntityContainer)app.getContainer(entMD.getName());
         
         // get the persistence manager from the container
         CMPPersistenceManager pm = 
               (CMPPersistenceManager)con.getPersistenceManager();

         // get the store manager from the persistence manger 
         JDBCStoreManager sm = (JDBCStoreManager)pm.getPersistenceStore();
         
         // get the brige from the persistence store
         JDBCEntityBridge entityBridge = sm.getEntityBridge();

         // finally get the field from the entity
         return entityBridge.getCMPFieldByName(fieldName);
      } catch(EJBException e) {
         throw e;
      } catch(Exception e) {
         // there are way to may ways this could go wrong if the 
         // data structures are not setup correctly, so just throw 
         // a general error.  Feel free to add the error handling 
         // code if you want.
         throw new IllegalArgumentException("Could not find a field for the " +
               "cmp-jdbc data for entity with the interface " + intf.getName());
      }
   } 

   public String toString() {
      return parameterString;
   }
}
