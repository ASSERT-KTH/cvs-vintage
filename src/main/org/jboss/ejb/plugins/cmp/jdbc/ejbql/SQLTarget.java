package org.jboss.ejb.plugins.cmp.jdbc.ejbql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EntityBean;

import org.jboss.ejb.Application;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.plugins.CMPPersistenceManager;
import org.jboss.ejb.plugins.cmp.ejbql.DeepCloneable;
import org.jboss.ejb.plugins.cmp.ejbql.InputParameterToken;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCStoreManager;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCType;
import org.jboss.ejb.plugins.cmp.jdbc.SQLUtil;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCFunctionMappingMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCTypeMappingMetaData;

public class SQLTarget implements DeepCloneable {
	private final Application application;
	private final Map pathElements = new Hashtable();
	private final Map managerByAbstractSchemaName = new Hashtable();
	private final List inputParameters = new ArrayList();
	
	private boolean isSelectDistinct;
	private String selectPath;
	
	private String whereClause = "";
	
	// cached generated sql
	private String sql;
	
	/**
	 * Constructs an a sql target for an EJB-QL query over the specified application.
	 * @param application the application over which this query is defined
	 */
	public SQLTarget(Application application) {
		this.application = application;
		
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
	}

	/**
	 * Constructs a copy of the supplied sql target.
	 * @param target the SQLTarget to be coppied
	 */
	public SQLTarget(SQLTarget target) {
		application = target.application;
		pathElements.putAll(target.pathElements);
		managerByAbstractSchemaName.putAll(target.managerByAbstractSchemaName);
		inputParameters.addAll(target.inputParameters);

		isSelectDistinct = target.isSelectDistinct;
		selectPath = target.selectPath;	
		
		whereClause = target.whereClause;
		
		sql = target.sql;
	}

	/**
	 * Set this target to generate a sql statement that returns distinct result set.
	 * This means that the sql will begin with SELECT DISTINCT.
	 * @param isSelectDisctinct should this target generate a SELECT DISTINCT query
	 */
	public void setSelectDistinct(boolean isSelectDistinct) {
		this.isSelectDistinct = isSelectDistinct;
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
			String path = (String)selectPathList.get(0);
			AbstractSchema schema = (AbstractSchema)pathElements.get(path);
			if(schema == null) {
				throw new IllegalStateException("Unknown identifier: " + path);
			}
			selectPath = path;
		} else {
			// select a.b.c.d style query				
			String path = (String)selectPathList.get(0);
			for(int i=1; i < selectPathList.size(); i++) {
				// are we done yet?
				if(i<selectPathList.size()-1) {
					// nope, assure that the next cmr field exists and update path
					path = getSingleValuedCMRField(path, (String)selectPathList.get(i));
				} else {
					// get the final cmp field, if possible, otherwise it is a single valued cmr field
					String cmpFieldPath = getCMPField(path, (String)selectPathList.get(i)); 
					if(cmpFieldPath != null) {
						path = cmpFieldPath;
					} else {
						// create the single valued cmr field object
						String cmrFieldPath = this.getSingleValuedCMRField(path, (String)selectPathList.get(i));
						if(cmrFieldPath == null) {
							throw new IllegalStateException("Unknown path: " + path + "." + selectPathList.get(i));
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
	
	public Object getSelectBridgeObject() {
		Object selectPathElement = pathElements.get(selectPath);
		if(selectPathElement instanceof PathElement) {
			PathElement pathElement = (PathElement)selectPathElement;
			return pathElement.getEntityBridge();
		} else if(selectPathElement instanceof CMPField) {
			CMPField cmpField = (CMPField)selectPathElement;
			return cmpField.getCMPFieldBridge();
		}
		throw new IllegalStateException("Select path element is instance of unknown type: " +
				"selectPath=" + selectPath + " selectPathElement=" + selectPathElement);
	}
	
	public void setWhereClause(String whereClause) {
		this.whereClause = whereClause;
	}
	
	public boolean isIdentifierRegistered(String identifier) {
		if(identifier.indexOf(".")>=0) {
			throw new IllegalArgumentException("identifier is a path not an identifier: " + identifier);
		}
		return pathElements.containsKey(identifier);
	}
	
	public void registerIdentifier(String path, String identifier) {
		if(identifier.indexOf(".")>=0) {
			throw new IllegalArgumentException("identifier is a path not an identifier: " + identifier);
		}		
		Object o = pathElements.get(path);
		if(o == null) {
			throw new IllegalArgumentException("Unknown path: "+path);
		}
		if(!(o instanceof CollectionValuedCMRField)) {
			throw new IllegalArgumentException("path must map to an instance CollectionValuedCMRField: path="+path+", mappedPath="+o);
		}
		CollectionValuedCMRField cmrField = (CollectionValuedCMRField)o;
		
		pathElements.put(identifier, cmrField);
	}

	public void registerIdentifier(AbstractSchema abstractSchema, String identifier) {
		if(identifier.indexOf(".")>=0) {
			throw new IllegalArgumentException("identifier is apath not an identifier: " + identifier);
		}
		if(abstractSchema == null) {
			throw new IllegalArgumentException("abstractSchema is null");
		}
		pathElements.put(identifier, abstractSchema);
	}

	public void registerParameter(InputParameterToken parameter) {
		inputParameters.add(new Integer(parameter.getNumber()));
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
		if(pathElements.containsKey(fullPath)) {
			Object o = pathElements.get(fullPath);
			if(o instanceof CollectionValuedCMRField) {
				return fullPath;
			} else {
				return null;
			}
		}
		
		Object o = pathElements.get(path);
		if(o == null || !(o instanceof PathElement)) {
			return null;
		}
		PathElement pathElement = (PathElement)o;
		
		JDBCCMRFieldBridge cmrFieldBridge = pathElement.getCMRFieldBridge(fieldName);
		if(cmrFieldBridge == null || !cmrFieldBridge.isCollectionValued()) {
			return null;
		}

		CollectionValuedCMRField collectionValuedCMRField = new CollectionValuedCMRField(cmrFieldBridge, pathElement);
		pathElements.put(fullPath, collectionValuedCMRField);
		return fullPath;
	}
	
	public String getSingleValuedCMRField(String path, String fieldName) {
		String fullPath = path + "." + fieldName;
		
		if(pathElements.containsKey(fullPath)) {
			Object o = pathElements.get(fullPath);
			if(o instanceof SingleValuedCMRField) {
				return fullPath;
			} else {
				return null;
			}
		}
		
		Object o = pathElements.get(path);
		if(o == null || !(o instanceof PathElement)) {
			return null;
		}
		PathElement pathElement = (PathElement)o;
		
		JDBCCMRFieldBridge cmrFieldBridge = pathElement.getCMRFieldBridge(fieldName);
		if(cmrFieldBridge == null || !cmrFieldBridge.isSingleValued()) {
			return null;
		}

		SingleValuedCMRField singleValuedCMRField = new SingleValuedCMRField(cmrFieldBridge, pathElement);
		pathElements.put(fullPath, singleValuedCMRField);
		return fullPath;
	}
	
	public String getCMPField(String path, String fieldName) {
		String fullPath = path + "." + fieldName;
		if(pathElements.containsKey(fullPath)) {
			Object o = pathElements.get(fullPath);
			if(o instanceof CMPField) {
				return fullPath;
			} else {
				return null;
			}
		}
		
		Object o = pathElements.get(path);
		if(o == null || !(o instanceof PathElement)) {
			return null;
		}
		PathElement pathElement = (PathElement)o;
		
		JDBCCMPFieldBridge cmpFieldBridge = pathElement.getCMPFieldBridge(fieldName);
		if(cmpFieldBridge == null) {
			return null;
		}

		CMPField cmpField = new CMPField(cmpFieldBridge, pathElement);
		pathElements.put(fullPath, cmpField);
		return fullPath;
	}	
	
	public String getCMPFieldColumnNamesClause(String path) {
		Map identifiersByPathElement = getIdentifiersByPathElement();	
		CMPField cmpField = (CMPField)pathElements.get(path);
		return cmpField.getColumnNamesClause(identifiersByPathElement);
	}
	
	public String toSQL() {
		if(sql == null) {
			Map identifiersByPathElement = getIdentifiersByPathElement();
			StringBuffer buf = new StringBuffer();
			
			// SELECT
			buf.append("SELECT ");
			if(isSelectDistinct) {
				buf.append("DISTINCT ");
			}
			
			Object selectPathElement = pathElements.get(selectPath);
			if(selectPathElement instanceof AbstractSchema) {
				AbstractSchema schema = (AbstractSchema)selectPathElement;
				buf.append(schema.getSelectClause(identifiersByPathElement));
			} else if(selectPathElement instanceof SingleValuedCMRField) {
				SingleValuedCMRField cmrField = (SingleValuedCMRField)selectPathElement;
				buf.append(cmrField.getSelectClause(identifiersByPathElement));
			} else if(selectPathElement instanceof CMPField) {
				CMPField cmpField = (CMPField)selectPathElement;
				buf.append(cmpField.getColumnNamesClause(identifiersByPathElement));
			} else {
				throw new IllegalStateException("Path element is instance of unknown type: " +
						"selectPath=" + selectPath + " selectPathElement=" + selectPathElement);
			}			
			
			// FROM
			buf.append(" FROM ");
	
			for(Iterator i = getUniquePathElements().iterator(); i.hasNext(); ) {
				PathElement pathElement = (PathElement)i.next();
				buf.append(pathElement.getTableDeclarations(identifiersByPathElement));
				if(i.hasNext()) {
					buf.append(", ");
				}
			}
	
			// [WHERE]
			Set cmrFields = getUniqueCMRFields();
			if(whereClause.length() > 0 || cmrFields.size() > 0) {
				buf.append(" WHERE ");
				
				if(whereClause.length() > 0) {
					if(cmrFields.size() > 0) {
						buf.append("(");
					}
					buf.append(whereClause);
					if(cmrFields.size() > 0) {
						buf.append(") AND ");
					}
				}
	
				for(Iterator i = getUniqueCMRFields().iterator(); i.hasNext(); ) {
					CMRField pathElement = (CMRField)i.next();
					buf.append(pathElement.getTableWhereClause(identifiersByPathElement));
					if(i.hasNext()) {
						buf.append(" AND ");
					}
				}
			}
			sql = buf.toString();
		}
		return sql;
	}
	
	public List getInputParameters() {
		return Collections.unmodifiableList(inputParameters);
	}
	
	public boolean isStringTypePath(String path) {
		Class pathType = getPathType(path);
		
		return (pathType.equals(String.class));
	}
	
	public boolean isBooleanTypePath(String path) {
		Class pathType = getPathType(path);
		
		return (pathType.equals(Boolean.class)) || (pathType.equals(Boolean.TYPE));
	}
	
	public boolean isArithmeticTypePath(String path) {
		Class pathType = getPathType(path);

		return (pathType.equals(Character.class)) || (pathType.equals(Character.TYPE)) ||
				(pathType.equals(Byte.class)) || (pathType.equals(Byte.TYPE)) ||
				(pathType.equals(Short.class)) || (pathType.equals(Short.TYPE)) ||
				(pathType.equals(Integer.class)) || (pathType.equals(Integer.TYPE)) ||
				(pathType.equals(Long.class)) || (pathType.equals(Long.TYPE)) ||
				(pathType.equals(Float.class)) || (pathType.equals(Float.TYPE)) ||
				(pathType.equals(Double.class)) || (pathType.equals(Double.TYPE));
	}
	
	public boolean isDatetimeTypePath(String path) {
		Class pathType = getPathType(path);

		return Date.class.isAssignableFrom(pathType);
	}
	
	public boolean isEntityBeanTypePath(String path) {
		Class pathType = getPathType(path);

		return EntityBean.class.isAssignableFrom(pathType);
	}

	public boolean isValueObjectTypePath(String path) {
		Class pathType = getPathType(path);
		if(pathType == null) {
			return false;
		}
		
		return !isStringTypePath(path) &&
			!isBooleanTypePath(path) &&
			!isArithmeticTypePath(path) &&
			!isDatetimeTypePath(path) &&
			!isEntityBeanTypePath(path);
	}
	
	public String getEntityWherePathToParameter(String compareFromPath, String compareSymbol) {
		Map identifiersByPathElement = getIdentifiersByPathElement();

		PathElement e = (PathElement)pathElements.get(compareFromPath);
		JDBCEntityBridge entity = e.getEntityBridge();
		
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		if(compareSymbol.equals("<>")) {
			buf.append("NOT(");
		}	
		
		buf.append(SQLUtil.getWhereClause(entity.getJDBCPrimaryKeyFields(), e.getIdentifier(identifiersByPathElement)));	

		if(compareSymbol.equals("<>")) {
			buf.append(")");
		}	
		buf.append(")");
		return buf.toString();
	}
	
	public String getEntityWherePathToPath(String compareFromPath, String compareSymbol, String compareToPath) {
		Map identifiersByPathElement = getIdentifiersByPathElement();

		PathElement fromPathElement = (PathElement)pathElements.get(compareFromPath);
		PathElement toPathElement = (PathElement)pathElements.get(compareToPath);
		JDBCEntityBridge fromEntity = fromPathElement.getEntityBridge();
		JDBCEntityBridge toEntity = toPathElement.getEntityBridge();
		if(!fromEntity.equals(toEntity)) {
			return null;
		}
		
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		if(compareSymbol.equals("<>")) {
			buf.append("NOT(");
		}	
		
		buf.append(SQLUtil.getSelfCompareWhereClause(fromEntity.getJDBCPrimaryKeyFields(), 
				fromPathElement.getIdentifier(identifiersByPathElement), 
				toPathElement.getIdentifier(identifiersByPathElement)));	

		if(compareSymbol.equals("<>")) {
			buf.append(")");
		}	
		buf.append(")");
		return buf.toString();
	}
	
	public String getValueObjectWherePathToParameter(String compareFromPath, String compareSymbol) {
		Map identifiersByPathElement = getIdentifiersByPathElement();

		CMPField cmpField = (CMPField)pathElements.get(compareFromPath);
		JDBCCMPFieldBridge cmpFieldBridge = cmpField.getCMPFieldBridge();

		StringBuffer buf = new StringBuffer();
		buf.append("(");
		if(compareSymbol.equals("<>")) {
			buf.append("NOT(");
		}	
		
		buf.append(SQLUtil.getWhereClause(cmpFieldBridge.getJDBCType(), cmpField.getParent().getIdentifier(identifiersByPathElement)));	

		if(compareSymbol.equals("<>")) {
			buf.append(")");
		}	
		buf.append(")");
		return buf.toString();
	}
	
	public String getValueObjectWherePathToPath(String compareFromPath, String compareSymbol, String compareToPath) {
		Map identifiersByPathElement = getIdentifiersByPathElement();

		CMPField fromCMPField = (CMPField)pathElements.get(compareFromPath);
		CMPField toCMPField = (CMPField)pathElements.get(compareToPath);
		JDBCCMPFieldBridge fromCCMPFieldBridge = fromCMPField.getCMPFieldBridge();
		JDBCCMPFieldBridge toCCMPFieldBridge = toCMPField.getCMPFieldBridge();
		if(!fromCCMPFieldBridge.getFieldType().equals(toCCMPFieldBridge.getFieldType())) {
			return null;
		}
		
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		if(compareSymbol.equals("<>")) {
			buf.append("NOT(");
		}	
		
		buf.append(SQLUtil.getSelfCompareWhereClause(fromCCMPFieldBridge.getJDBCType(), 
				fromCMPField.getParent().getIdentifier(identifiersByPathElement), 
				toCMPField.getParent().getIdentifier(identifiersByPathElement)));	

		if(compareSymbol.equals("<>")) {
			buf.append(")");
		}	
		buf.append(")");
		return buf.toString();
	}
	
	public String getNotExistsClause(String path, String field) {
		Map identifiersByPathElement = getIdentifiersByPathElement();

		PathElement parent = (PathElement)pathElements.get(path);
		JDBCCMRFieldBridge cmrField = parent.getCMRFieldBridge(field);
		if(cmrField == null) {
			return null;
		}
		
		String childIdentifier = field+"_blah";
		JDBCEntityBridge entity = cmrField.getRelatedEntity();
		
		StringBuffer buf = new StringBuffer();
		buf.append("NOT EXISTS (");
			buf.append("SELECT ");
				buf.append(SQLUtil.getColumnNamesClause(
						entity.getJDBCPrimaryKeyFields(), childIdentifier));
			buf.append(" FROM ");
				buf.append(entity.getMetaData().getTableName());
				buf.append(" ");
				buf.append(childIdentifier);
			buf.append(" WHERE ").append(getTableWhereClause(cmrField, parent, childIdentifier, identifiersByPathElement));
		buf.append(")");
	   return buf.toString();
	}
	
	public String getNullComparison(String path, boolean not) {
		Map identifiersByPathElement = getIdentifiersByPathElement();

		Object o = pathElements.get(path);
		JDBCCMPFieldBridge[] fields;
		String identifier;
		if(o instanceof CMPField) {
			fields = new JDBCCMPFieldBridge[1];
			fields[0] = ((CMPField)o).getCMPFieldBridge();
			identifier = ((CMPField)o).getParent().getIdentifier(identifiersByPathElement);
		} else {
			fields = ((PathElement)o).getEntityBridge().getJDBCPrimaryKeyFields();
			identifier = ((PathElement)o).getIdentifier(identifiersByPathElement);
		}
			
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		for(int i=0; i<fields.length; i++) {
			if(i > 0) {
				buf.append(" AND ");
			}
			JDBCType type = fields[i].getJDBCType();
			String[] columnNames = type.getColumnNames();
			for(int j=0; j<columnNames.length; j++) {
				if(j > 0) {
					buf.append(" AND ");
				}
				buf.append(identifier).append(".").append(columnNames[i]);
				buf.append(" IS ");
				if(not) {
					buf.append("NOT ");
				}
				buf.append("NULL");
			}
		}
		buf.append(")");
	   return buf.toString();
	}

	public Object deepClone() {
		return new SQLTarget(this);
	}
	
	private Set getUniquePathElements() {
		Set set = new HashSet();
		for(Iterator i=pathElements.values().iterator(); i.hasNext(); ) {
			Object o = i.next();
			if(o instanceof PathElement) {
				set.add(o);
			}
		}
		return set;
	}
	
	private Set getUniqueCMRFields() {
		Set set = new HashSet();
		for(Iterator i=pathElements.values().iterator(); i.hasNext(); ) {
			Object o = i.next();
			if(o instanceof CMRField) {
				set.add(o);
			}
		}
		return set;
	}
	
	private Map getIdentifiersByPathElement() {
		Map map = new Hashtable();
		for(Iterator i=pathElements.keySet().iterator(); i.hasNext(); ) {
			String identifier = (String)i.next();
			if(identifier.indexOf(".")<0) {
				map.put(pathElements.get(identifier), identifier);
			}
		}
		return map;
	}
	
	private Class getPathType(String fullPath) {
		if(!pathElements.containsKey(fullPath)) {
			return null;
		}
		
		Object o = pathElements.get(fullPath);
		if(o instanceof CMPField) {
			return ((CMPField)o).getFieldType();
		} else if(!(o instanceof CollectionValuedCMRField)) {
			return ((PathElement)o).getFieldType();
		} else {
			return null;
		}
	}
	
	public String getTableWhereClause(JDBCCMRFieldBridge cmrFieldBridge, PathElement parent, String childIdentifier, Map identifiersByPathElement) {
		StringBuffer buf = new StringBuffer();
		if(cmrFieldBridge.getMetaData().getRelationMetaData().isForeignKeyMappingStyle()) {
			String parentIdentifier = parent.getIdentifier(identifiersByPathElement);
			
			if(cmrFieldBridge.hasForeignKey()) {				
				JDBCCMPFieldBridge[] parentFkKeyFields = cmrFieldBridge.getForeignKeyFields();
				for(int i=0; i < parentFkKeyFields.length; i++) {
					if(i > 0) {
						buf.append(" AND ");
					}
					JDBCCMPFieldBridge parentFkField = parentFkKeyFields[i];
					JDBCCMPFieldBridge childPkField = cmrFieldBridge.getRelatedEntity().getCMPFieldByName(parentFkField.getFieldName());
					buf.append(SQLUtil.getWhereClause(parentFkField, parentIdentifier, childPkField, childIdentifier));
				}	
			} else {
				JDBCCMPFieldBridge[] childFkKeyFields = cmrFieldBridge.getRelatedCMRField().getForeignKeyFields();
				for(int i=0; i < childFkKeyFields.length; i++) {
					if(i > 0) {
						buf.append(" AND ");
					}
					JDBCCMPFieldBridge childFkKeyField = childFkKeyFields[i];
					JDBCCMPFieldBridge parentPkField = parent.getCMPFieldBridge(childFkKeyField.getFieldName());
					buf.append(SQLUtil.getWhereClause(parentPkField, parentIdentifier, childFkKeyField, childIdentifier));
				}	
			}
		} else {
			String parentIdentifier = parent.getIdentifier(identifiersByPathElement);
			String relationTableIdentifier = parent.getIdentifier(identifiersByPathElement) + "_to_" + childIdentifier;
			
			JDBCCMPFieldBridge[] parentTableKeyFields = cmrFieldBridge.getTableKeyFields();
			for(int i=0; i < parentTableKeyFields.length; i++) {
				if(i > 0) {
					buf.append(" AND ");
				}
				JDBCCMPFieldBridge fkField = parentTableKeyFields[i];
				JDBCCMPFieldBridge pkField = parent.getCMPFieldBridge(fkField.getFieldName());
				buf.append(SQLUtil.getWhereClause(pkField, parentIdentifier, fkField, relationTableIdentifier));
			}	

			buf.append(" AND ");

			JDBCCMPFieldBridge[] childTableKeyFields = cmrFieldBridge.getRelatedCMRField().getTableKeyFields();
			for(int i=0; i < childTableKeyFields.length; i++) {
				if(i > 0) {
					buf.append(" AND ");
				}
				JDBCCMPFieldBridge fkField = childTableKeyFields[i];
				JDBCCMPFieldBridge pkField = cmrFieldBridge.getRelatedEntity().getCMPFieldByName(fkField.getFieldName());
				buf.append(SQLUtil.getWhereClause(pkField, childIdentifier, fkField, relationTableIdentifier));
			}	
		}	
		return buf.toString();
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
}
