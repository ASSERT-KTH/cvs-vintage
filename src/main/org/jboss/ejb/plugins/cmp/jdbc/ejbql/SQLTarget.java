package org.jboss.ejb.plugins.cmp.jdbc.ejbql;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.EntityBean;
import org.jboss.ejb.Application;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.plugins.CMPPersistenceManager;
import org.jboss.ejb.plugins.cmp.ejbql.DeepCloneable;
import org.jboss.ejb.plugins.cmp.ejbql.InputParameterToken;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCStoreManager;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCType;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCTypeComplex;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCTypeComplexProperty;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCTypeFactory;
import org.jboss.ejb.plugins.cmp.jdbc.SQLUtil;
import org.jboss.ejb.plugins.cmp.jdbc.QueryParameter;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCFunctionMappingMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCTypeMappingMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCReadAheadMetaData;

public class SQLTarget implements DeepCloneable {
   private final Method method;
   private final JDBCTypeFactory typeFactory;
   private final IdentifierManager idManager;
   private final Application application;
   private final Map managerByAbstractSchemaName = new Hashtable();
   private final List inputParameters = new ArrayList();
   private final JDBCReadAheadMetaData readAhead;
   
   private boolean isSelectDistinct;
   private String selectPath;
   
   private String whereClause = "";
   
   // cached generated sql
   private String sql;
   
   /**
    * Constructs an a sql target for an EJB-QL query over the specified application.
    * @param application the application over which this query is defined
    */
   public SQLTarget(Method method, 
         JDBCTypeFactory typeFactory,
         Application application,
         JDBCReadAheadMetaData readAhead) {

      this.method = method;
      this.typeFactory = typeFactory;
      this.application = application;
      this.readAhead = readAhead;

      if(method.getReturnType().equals(Set.class)) {
         isSelectDistinct = true;
      }
      
      for(Iterator i = application.getContainers().iterator(); i.hasNext(); ) {
         Object o = i.next();
         if(o instanceof EntityContainer) {
            EntityContainer container = (EntityContainer)o;
            if(container.getPersistenceManager() instanceof CMPPersistenceManager) {
               CMPPersistenceManager persistence = (CMPPersistenceManager)container.getPersistenceManager();
               if(persistence.getPersistenceStore() instanceof JDBCStoreManager) {
                  JDBCStoreManager manager = (JDBCStoreManager) persistence.getPersistenceStore();
                  if(manager != null) {
                     managerByAbstractSchemaName.put(manager.getMetaData().getAbstractSchemaName(), manager);
                  }
               }
            }
         }
      }
      
      idManager = new IdentifierManager(getTypeMappingMetaData());
   }

   /**
    * Constructs a copy of the supplied sql target.
    * @param target the SQLTarget to be coppied
    */
   public SQLTarget(SQLTarget target) {
      method = target.method;
      typeFactory = target.typeFactory;
      application = target.application;
      readAhead = target.readAhead;
      idManager = new IdentifierManager(target.idManager);
      managerByAbstractSchemaName.putAll(target.managerByAbstractSchemaName);
      inputParameters.addAll(target.inputParameters);

      isSelectDistinct = target.isSelectDistinct;
      selectPath = target.selectPath;   
      
      whereClause = target.whereClause;
      
      sql = target.sql;
   }

   /**
    * Set this target to generate a sql statement that returns distinct
    * result set. This means that the sql will begin with SELECT DISTINCT.
    */
   public void setSelectDistinct() {
      this.isSelectDistinct = true;
   }

   /**
    * Set the path to the element to select. The path is a list of the string names.
    * @param selectPath list of strings that make up the path to select
    */
   public void setSelectPath(List selectPathList) {
      //this.selectPath = selectPath;
      if(selectPathList.isEmpty()) {
         throw new IllegalArgumentException("SelectPathList is empty");
      }

      // is this a select object(o) style query?
      if(selectPathList.size() == 1) {
         String identifier = ((String)selectPathList.get(0)).toLowerCase();
         
         // verify that the abstract schema already exists
         // this method will throw an exception if the identifier
         // is unknown or if the identifer does not map to a schema
         idManager.getExistingEntityPathElement(identifier);
         selectPath = identifier;
      } else {
         // select a.b.c.d style query            
         String path = ((String)selectPathList.get(0)).toLowerCase();
         for(int i=1; i < selectPathList.size(); i++) {
            // are we done yet?
            if(i<selectPathList.size()-1) {
               // nope, assure that the next cmr field exists and update path
               String temp  = getSingleValuedCMRField(
                     path, (String)selectPathList.get(i));
               if(temp == null) {
                  throw new IllegalArgumentException("Select path element " +
                        "is not a single valued cmr field: " + 
                        path + "." + selectPathList.get(i));
               }
               path = temp;
            } else {
               // get the final cmp field, if possible, otherwise it must be a 
               // single valued cmr field
               String cmpFieldPath = getCMPField(
                     path, (String)selectPathList.get(i)); 
               if(cmpFieldPath != null) {
                  path = cmpFieldPath;
               } else {
                  // create the single valued cmr field object
                  String cmrFieldPath = getSingleValuedCMRField(
                        path, (String)selectPathList.get(i));
                  if(cmrFieldPath == null) {
                     throw new IllegalArgumentException("Select path is not " +
                           "a single valued cmr field or a cmp field: " + 
                           path + "." + selectPathList.get(i));
                  }
                  path = cmrFieldPath;
               }
            }
         }
         selectPath = path;
      }
   }
   
   public String getSelectPath() {
      return selectPath;
   }
   
   public Object getSelectObject() {
      PathElement selectPathElement = 
            idManager.getExistingPathElement(selectPath);
      if(selectPathElement instanceof AbstractSchema) {
         AbstractSchema schema = (AbstractSchema)selectPathElement;
         return schema.getEntityBridge();
      } else if(selectPathElement instanceof CMPField) {
         CMPField cmpField = (CMPField)selectPathElement;
         return cmpField.getCMPFieldBridge();
      } else if(selectPathElement instanceof CMRField) {
         CMRField cmrField = (CMRField)selectPathElement;
         return cmrField.getEntityBridge();
      }
      // should never happen
      throw new IllegalStateException("Select path element is instance of " +
            "unknown type: selectPath=" + selectPath + 
            " selectPathElement=" + selectPathElement);
   }
   

   public void setWhereClause(String whereClause) {
      this.whereClause = whereClause;
   }
   
   public boolean isIdentifierRegistered(String identifier) {
      return idManager.isIdentifierRegistered(identifier);
   }
   
   public void registerIdentifier(String path, String identifier) {
      CMRField cmrField = idManager.getExistingCMRField(path);
      idManager.registerIdentifier(cmrField, identifier);
   }

   public void registerIdentifier(AbstractSchema abstractSchema, String identifier) {
      idManager.registerIdentifier(abstractSchema, identifier);
   }

   public void registerParameter(InputParameterToken parameter) {
      Class type = getParameterType(parameter.getNumber());
      QueryParameter param = new QueryParameter(
               parameter.getNumber() - 1,
               false, // isPrimaryKeyParameter
               null, // field
               null, // parameter
               typeFactory.getJDBCTypeForJavaType(type));
      inputParameters.add(param);
   }

   public AbstractSchema createAbstractSchema(String abstractSchemaName) {
      JDBCStoreManager manager = (JDBCStoreManager)managerByAbstractSchemaName.get(abstractSchemaName);
      if(manager == null) {
         return null;
      }
      return new AbstractSchema(manager.getEntityBridge());
   }

   public String getCollectionValuedCMRField(String path, String fieldName) {
      String fullPath = path + "." + fieldName;
      if(idManager.isKnownPath(fullPath)) {
         CMRField cmrField = idManager.getCMRField(fullPath);
         if(cmrField != null && cmrField.isCollectionValued()) {
            return fullPath;
         } else {
            return null;
         }
      }

      EntityPathElement pathElement = idManager.getEntityPathElement(path);
      if(pathElement == null) {
         return null;
      }
      
      JDBCCMRFieldBridge cmrFieldBridge = pathElement.getCMRFieldBridge(fieldName);
      if(cmrFieldBridge == null || !cmrFieldBridge.isCollectionValued()) {
         return null;
      }

      CMRField cmrField = new CMRField(cmrFieldBridge, pathElement);
      idManager.registerPath(cmrField, fullPath);
      return fullPath;
   }
   
   public String getSingleValuedCMRField(String path, String fieldName) {
      String fullPath = path + "." + fieldName;
      if(idManager.isKnownPath(fullPath)) {
         CMRField cmrField = idManager.getCMRField(fullPath);
         if(cmrField != null && cmrField.isSingleValued()) {
            return fullPath;
         } else {
            return null;
         }
      }

      EntityPathElement pathElement = idManager.getEntityPathElement(path);
      if(pathElement == null) {
         return null;
      }
      
      JDBCCMRFieldBridge cmrFieldBridge = pathElement.getCMRFieldBridge(fieldName);
      if(cmrFieldBridge == null || !cmrFieldBridge.isSingleValued()) {
         return null;
      }

      CMRField cmrField = new CMRField(cmrFieldBridge, pathElement);
      idManager.registerPath(cmrField, fullPath);
      return fullPath;
   }
   
   public String getCMPField(String path, String fieldName) {
      String fullPath = path + "." + fieldName;
      if(idManager.isKnownPath(fullPath)) {
         CMPField cmpField = idManager.getCMPField(fullPath);
         if(cmpField != null) {
            return fullPath;
         } else {
            return null;
         }
      }

      EntityPathElement pathElement = idManager.getEntityPathElement(path);
      if(pathElement == null) {
         return null;
      }
      
      JDBCCMPFieldBridge cmpFieldBridge = pathElement.getCMPFieldBridge(fieldName);
      if(cmpFieldBridge == null) {
         return null;
      }

      CMPField cmpField = new CMPField(cmpFieldBridge, pathElement);
      idManager.registerPath(cmpField, fullPath);
      return fullPath;
   }   
      
   public List getInputParameters() {
      return Collections.unmodifiableList(inputParameters);
   }
   
   public boolean isStringTypePath(String path) {
      return isStringType(getPathType(path));
   }
   
   public boolean isStringTypeParameter(int index) {
      return isStringType(getParameterType(index));
   }
   
   public boolean isStringType(Class type) {
      if(type == null) {
         return false;
      }
      return (type.equals(String.class));
   }

   public boolean isBooleanTypePath(String path) {
      return isBooleanType(getPathType(path));
   }
   
   public boolean isBooleanTypeParameter(int index) {
      return isBooleanType(getParameterType(index));
   }
   
   public boolean isBooleanType(Class type) {
      if(type == null) {
         return false;
      }
      return (type.equals(Boolean.class)) || (type.equals(Boolean.TYPE));
   }
   
   public boolean isArithmeticTypePath(String path) {
      return isArithmeticType(getPathType(path));
   }

   public boolean isArithmeticTypeParameter(int index) {
      return isArithmeticType(getParameterType(index));
   }
   
   public boolean isArithmeticType(Class type) {
      if(type == null) {
         return false;
      }
      return (type.equals(Character.class)) || (type.equals(Character.TYPE)) ||
            (type.equals(Byte.class)) || (type.equals(Byte.TYPE)) ||
            (type.equals(Short.class)) || (type.equals(Short.TYPE)) ||
            (type.equals(Integer.class)) || (type.equals(Integer.TYPE)) ||
            (type.equals(Long.class)) || (type.equals(Long.TYPE)) ||
            (type.equals(Float.class)) || (type.equals(Float.TYPE)) ||
            (type.equals(Double.class)) || (type.equals(Double.TYPE));
   }
   
   public boolean isDatetimeTypePath(String path) {
      return isDatetimeType(getPathType(path));
   }
   
   public boolean isDatetimeTypeParameter(int index) {
      return isDatetimeType(getParameterType(index));
   }
   
   public boolean isDatetimeType(Class type) {
      if(type == null) {
         return false;
      }
      return Date.class.isAssignableFrom(type);
   }
   
   public boolean isEntityBeanTypePath(String path) {
      Class pathType = getPathType(path);
      if(pathType == null) {
         return false;
      }
      return EntityBean.class.isAssignableFrom(pathType);
   }

   public boolean isEntityBeanTypeParameter(int index) {
      Class type = getParameterType(index);
      if(type == null) {
         return false;
      }
      return EJBObject.class.isAssignableFrom(type) ||
           EJBLocalObject.class.isAssignableFrom(type);
   }
   
   public boolean isValueObjectTypePath(String path) {
      Class type = getPathType(path);
      if(type == null) {
         return false;
      }
      
      return !isStringType(type) &&
         !isBooleanType(type) &&
         !isArithmeticType(type) &&
         !isDatetimeType(type) &&
         !isEntityBeanTypePath(path);
   }
   
   public boolean isValueObjectTypeParameter(int index) {
      Class type = getParameterType(index);
      if(type == null) {
         return false;
      }
      
      return !isStringType(type) &&
         !isBooleanType(type) &&
         !isArithmeticType(type) &&
         !isDatetimeType(type) &&
         !isEntityBeanTypeParameter(index);
   }
   
   private Class getPathType(String fullPath) {
      if(!idManager.isKnownPath(fullPath)) {
         return null;
      }
      
      PathElement pathElement = idManager.getExistingPathElement(fullPath);
      return pathElement.getFieldType();
   }
   
   private Class getParameterType(int index) {
      int zeroBasedIndex = index - 1;
      Class[] params = method.getParameterTypes();
      if(zeroBasedIndex < params.length) {
         return params[zeroBasedIndex];
      }
      return null;
   }

   public String getCMPFieldColumnNamesClause(String path) {
      CMPField cmpField = idManager.getExistingCMPField(path);
      String identifier = idManager.getTableAlias(cmpField.getParent());
      return SQLUtil.getColumnNamesClause(cmpField.getCMPFieldBridge(), identifier);
   }   

   public String getEntityWherePathToParameter(
         String compareFromPath, 
         String compareSymbol,
         InputParameterToken compareToParameter) {

      EntityPathElement entityPathElement = 
            idManager.getExistingEntityPathElement(compareFromPath);
      String identifier = idManager.getTableAlias(entityPathElement);
      JDBCEntityBridge entity = entityPathElement.getEntityBridge();
      
      // verify that parameter is the same type as the entity
      JDBCEntityMetaData metadata = entity.getMetaData();
      Class parameterType = getParameterType(compareToParameter.getNumber());
      if(parameterType == null) {
         return null;
      }
      if(!parameterType.equals(metadata.getRemoteClass()) &&
            !parameterType.equals(metadata.getLocalClass())) {
         return null;
      }
      
      inputParameters.addAll(QueryParameter.createParameters(
            compareToParameter.getNumber() - 1,
            entity));
/*
      // create the parameter objects
      List pkFields = entity.getPrimaryKeyFields();
      for(Iterator iter = pkFields.iterator(); iter.hasNext();) {
         JDBCFieldBridge pkField = (JDBCFieldBridge)iter.next();

         JDBCType type = pkField.getJDBCType();
         if(type instanceof JDBCTypeComplex) {
            JDBCTypeComplexProperty[] props = 
                  ((JDBCTypeComplex)type).getProperties();
            for(int j=0; j<props.length; j++) {
               QueryParameter param = new QueryParameter(
                        compareToParameter.getNumber() - 1,
                        false, // isPrimaryKeyParameter
                        pkField,
                        props[j],
                        props[j].getJDBCType());
               inputParameters.add(param);
            }
         } else {
            QueryParameter param = new QueryParameter(
                     compareToParameter.getNumber() - 1,
                     false, // isPrimaryKeyParameter
                     pkField,
                     null,
                     type.getJDBCTypes()[0]);
            inputParameters.add(param);
         }
      }
*/    
      // generate the sql
      StringBuffer buf = new StringBuffer();
      buf.append("(");
      if(compareSymbol.equals("<>")) {
         buf.append("NOT(");
      }
      
      buf.append(SQLUtil.getWhereClause(
               entity.getPrimaryKeyFields(), identifier));   

      if(compareSymbol.equals("<>")) {
         buf.append(")");
      }
      buf.append(")");
      return buf.toString();
   }
   
   public String getEntityWherePathToPath(String compareFromPath, String compareSymbol, String compareToPath) {
      EntityPathElement fromEntityPathElement = idManager.getExistingEntityPathElement(compareFromPath);
      String fromIdentifier = idManager.getTableAlias(fromEntityPathElement);
      JDBCEntityBridge fromEntity = fromEntityPathElement.getEntityBridge();

      EntityPathElement toEntityPathElement = idManager.getExistingEntityPathElement(compareToPath);
      String toIdentifier = idManager.getTableAlias(toEntityPathElement);
      JDBCEntityBridge toEntity = toEntityPathElement.getEntityBridge();

      // can only compare like kind entities
      if(!fromEntity.equals(toEntity)) {
         return null;
      }
      
      StringBuffer buf = new StringBuffer();
      buf.append("(");
      if(compareSymbol.equals("<>")) {
         buf.append("NOT(");
      }   
      
      buf.append(SQLUtil.getSelfCompareWhereClause(
            fromEntity.getPrimaryKeyFields(), 
            fromIdentifier, 
            toIdentifier));   

      if(compareSymbol.equals("<>")) {
         buf.append(")");
      }   
      buf.append(")");
      return buf.toString();
   }
   
   public String getValueObjectWherePathToParameter(
         String compareFromPath,
         String compareSymbol,
         InputParameterToken compareToParameter) {

      CMPField cmpField = idManager.getExistingCMPField(compareFromPath);
      String parentIdentifier = idManager.getTableAlias(cmpField.getParent());
      JDBCCMPFieldBridge cmpFieldBridge = cmpField.getCMPFieldBridge();

      // verify that parameter is the same type as the entity
      Class parameterType = getParameterType(compareToParameter.getNumber());
      if(parameterType == null) {
         return null;
      }
      if(!parameterType.equals(cmpFieldBridge.getFieldType())) {
         return null;
      }
      
      inputParameters.addAll(QueryParameter.createParameters(
            compareToParameter.getNumber() - 1,
            cmpFieldBridge));
/*
      // create the parameter objects
      JDBCType type = cmpFieldBridge.getJDBCType();
      if(type instanceof JDBCTypeComplex) {
         JDBCTypeComplexProperty[] props = 
               ((JDBCTypeComplex)type).getProperties();
         for(int i=0; i<props.length; i++) {
            QueryParameter param = new QueryParameter(
                     compareToParameter.getNumber() - 1,
                     false, // isPrimaryKeyParameter
                     null,
                     props[i],
                     props[i].getJDBCType());
            inputParameters.add(param);
         }
      } else {
         QueryParameter param = new QueryParameter(
                  compareToParameter.getNumber() - 1,
                  false, // isPrimaryKeyParameter
                  null,
                  null,
                  type.getJDBCTypes()[0]);
         inputParameters.add(param);
      }
*/    
      // generate the sql
      StringBuffer buf = new StringBuffer();
      buf.append("(");
      if(compareSymbol.equals("<>")) {
         buf.append("NOT(");
      }   
      
      buf.append(SQLUtil.getWhereClause(cmpFieldBridge, parentIdentifier));   

      if(compareSymbol.equals("<>")) {
         buf.append(")");
      }   
      buf.append(")");
      return buf.toString();
   }
   
   public String getValueObjectWherePathToPath(String compareFromPath, String compareSymbol, String compareToPath) {
      CMPField fromCMPField = idManager.getExistingCMPField(compareFromPath);
      String fromParentIdentifier = idManager.getTableAlias(fromCMPField.getParent());
      JDBCCMPFieldBridge fromCMPFieldBridge = fromCMPField.getCMPFieldBridge();

      CMPField toCMPField = idManager.getExistingCMPField(compareToPath);
      String toParentIdentifier = idManager.getTableAlias(toCMPField.getParent());
      JDBCCMPFieldBridge toCMPFieldBridge = toCMPField.getCMPFieldBridge();

      // can only compare like types
      if(!fromCMPFieldBridge.getFieldType().equals(toCMPFieldBridge.getFieldType())) {
         return null;
      }
      
      StringBuffer buf = new StringBuffer();
      buf.append("(");
      if(compareSymbol.equals("<>")) {
         buf.append("NOT(");
      }   
      
      buf.append(SQLUtil.getSelfCompareWhereClause(fromCMPFieldBridge.getJDBCType(), 
            fromParentIdentifier, 
            toParentIdentifier));   

      if(compareSymbol.equals("<>")) {
         buf.append(")");
      }   
      buf.append(")");
      return buf.toString();
   }
   
   public String getNotExistsClause(String path, String fieldName) {
      EntityPathElement parent = idManager.getExistingEntityPathElement(path);      
      JDBCCMRFieldBridge cmrFieldBridge = parent.getCMRFieldBridge(fieldName);
      if(cmrFieldBridge == null || !cmrFieldBridge.isCollectionValued()) {
         return null;
      }

      CMRField cmrField = new CMRField(cmrFieldBridge, parent);
      JDBCEntityBridge entity = cmrFieldBridge.getRelatedEntity();
      String tableAlias = idManager.getTableAlias(cmrField);
      
      StringBuffer buf = new StringBuffer();
      buf.append("NOT EXISTS (");
         buf.append("SELECT ");
            buf.append(SQLUtil.getColumnNamesClause(
                  entity.getPrimaryKeyFields(), tableAlias));
         buf.append(" FROM ");
            buf.append(entity.getMetaData().getTableName());
            buf.append(" ");
            buf.append(tableAlias);
         buf.append(" WHERE ");
            SQLGenerator sqlGen = new SQLGenerator(idManager);
            buf.append(sqlGen.getTableWhereClause(cmrField));
      buf.append(")");
      return buf.toString();
   }
   
   public String getNullComparison(String path, boolean not) {
      PathElement pathElement = idManager.getExistingPathElement(path);

      List fields;
      String identifier;
      if(pathElement instanceof CMPField) {
         CMPField cmpField = (CMPField)pathElement;
         fields = Collections.singletonList(cmpField.getCMPFieldBridge());
         identifier = idManager.getTableAlias(cmpField.getParent());
      } else {
         EntityPathElement entityPathElement = (EntityPathElement)pathElement;
         fields = entityPathElement.getEntityBridge().getPrimaryKeyFields();
         identifier = idManager.getTableAlias(entityPathElement);
      }
         
      StringBuffer buf = new StringBuffer();
      buf.append("(");
      if(not) {
         buf.append(" NOT(");
      }
      
      for(Iterator iter = fields.iterator(); iter.hasNext();) {
         JDBCFieldBridge field = (JDBCFieldBridge)iter.next();

         JDBCType type = field.getJDBCType();
         String[] columnNames = type.getColumnNames();
         for(int j=0; j<columnNames.length; j++) {
            if(j > 0) {
               buf.append(" AND ");
            }
            buf.append(identifier).append(".").append(columnNames[j]);
            buf.append(" IS NULL");
         }
         if(iter.hasNext()) {
            buf.append(" AND ");
         }
      }

      if(not) {
         buf.append(")");
      }
      buf.append(")");
      return buf.toString();
   }

   public Object deepClone() {
      return new SQLTarget(this);
   }
   
   public String getConcatFunction(String param1, String param2) {
      String[] args = new String[] {param1, param2};
      JDBCFunctionMappingMetaData function = getTypeMappingMetaData().getFunctionMapping("concat");
      return function.getFunctionSql(args);
   }
   
   public String getSubstringFunction(String param1, String param2, String param3) {
      String[] args = new String[] {param1, param2, param3}; 
      JDBCFunctionMappingMetaData function = getTypeMappingMetaData().getFunctionMapping("substring");
      return function.getFunctionSql(args);
   }
   
   public String getLengthFunction(String param) {
      String[] args = new String[] {param}; 
      JDBCFunctionMappingMetaData function = getTypeMappingMetaData().getFunctionMapping("length");
      return function.getFunctionSql(args);
   }
   
   public String getLocateFunction(String param1, String param2, String param3) {
      String[] args = new String[] {param1, param2, param3}; 
      JDBCFunctionMappingMetaData function = getTypeMappingMetaData().getFunctionMapping("locate");
      return function.getFunctionSql(args);
   }
   
   public String getAbsFunction(String param) {
      String[] args = new String[] {param}; 
      JDBCFunctionMappingMetaData function = getTypeMappingMetaData().getFunctionMapping("abs");
      return function.getFunctionSql(args);
   }
   
   public String getSqrtFunction(String param) {
      String[] args = new String[] {param}; 
      JDBCFunctionMappingMetaData function = getTypeMappingMetaData().getFunctionMapping("sqrt");
      return function.getFunctionSql(args);
   }
   
   private JDBCTypeMappingMetaData getTypeMappingMetaData() {
      JDBCStoreManager manager = (JDBCStoreManager)managerByAbstractSchemaName.values().iterator().next();
      return manager.getMetaData().getTypeMapping();
   }

   public String toSQL() {
      if(sql == null) {
         SQLGenerator sqlGen = new SQLGenerator(idManager);
         sql = sqlGen.getSQL(
               isSelectDistinct,
               selectPath,
               whereClause,
               readAhead);
      }
      return sql;
   }
}
