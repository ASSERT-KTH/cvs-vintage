/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

import javax.sql.DataSource;

import org.w3c.dom.Element;

import org.jboss.ejb.DeploymentException;

import org.jboss.metadata.EntityMetaData;
import org.jboss.metadata.QueryMetaData;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.XmlLoadable;

/**
 *      
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 *	@author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:dirk@jboss.de">Dirk Zimmermann</a>
 *	@version $Revision: 1.1 $
 */
public class JDBCEntityMetaData extends MetaData implements XmlLoadable {
	// Constants -----------------------------------------------------
	 
	// Attributes ----------------------------------------------------
	 
	// parent metadata structures
	private JDBCApplicationMetaData jdbcApplication;
	private EntityMetaData entity;
	
	// the name of the bean (same as entity.getEntityName())
	private String entityName;
	
	// Class loader for this entity
	private ClassLoader classLoader;
	
	// the implementation class of the bean
	private Class entityClass;
	
	// the implementation class of the bean
	private Class homeClass;
	
	// the implementation class of the bean
	private Class localHomeClass;
	
	// the name of the table to use for this bean
	private String tableName;
	
	// do we have to try and create the table on deployment?
	private boolean createTable;
	
	// do we have to drop the table on undeployment?
	private boolean removeTable;
	
	// do we use 'SELECT ... FOR UPDATE' syntax?
	private boolean selectForUpdate;
	
	// is the bean read-only?
	private boolean readOnly;
	
	// how long is read valid
	private int readTimeOut = -1;
	
	// should the table have a primary key constraint?
	private boolean primaryKeyConstraint;
	
	// the class of the primary key
	private Class primaryKeyClass;
	
	// the fields we must persist for this bean
	private HashMap cmpFields;
	
	// the fields we must persist for this bean
	private ArrayList eagerLoadFields;
	
	// the fields we must persist for this bean
	private ArrayList lazyLoadGroups;
	
	// finders for this bean
	private HashMap queries;
	
	// used to create query meta data
	private JDBCQueryMetaDataFactory queryFactory;

	// Static --------------------------------------------------------
   
	// Constructors --------------------------------------------------
    
	public JDBCEntityMetaData(JDBCApplicationMetaData jdbcApplication, EntityMetaData entity) throws DeploymentException {
		// initialisation of this object goes as follows:
		//  - constructor
		//  - importXml() for standardjbosscmp-jdbc.xml and jbosscmp-jdbc.xml
		
		this.jdbcApplication = jdbcApplication;
		this.entity = entity;
		entityName = entity.getEjbName();
		
		classLoader = jdbcApplication.getClassLoader();
		try {
			entityClass = classLoader.loadClass(entity.getEjbClass());
		} catch (ClassNotFoundException e) {
			throw new DeploymentException("entity class not found: " + entityName);
		}

		try {
			primaryKeyClass = classLoader.loadClass(entity.getPrimaryKeyClass());
		} catch (ClassNotFoundException e) {
			throw new DeploymentException("could not load primary key class: " + entity.getPrimaryKeyClass());
		}
		
		String home = entity.getHome();
		try {
			if(home != null) {
				homeClass = classLoader.loadClass(home);
			}
		} catch (ClassNotFoundException e) {
			throw new DeploymentException("home class not found: " + home);
		}

		String localHome = entity.getHome();
		try {
			if(localHome != null) {
				localHomeClass = classLoader.loadClass(localHome);
			} else if(home == null) {
				throw new DeploymentException("Entity must have atleast a home or local home: " + entityName);
			}
		} catch (ClassNotFoundException e) {
			throw new DeploymentException("local home class not found: " + localHome);
		}

		// we replace the . by _ because some dbs die on it...
		// the table name may be overridden in importXml(jbosscmp-jdbc.xml)
		tableName = entityName.replace('.', '_');
		
		// build the metadata for the cmp fields now in case there is no jbosscmp-jdbc.xml
		cmpFields = new HashMap();
		
		Iterator cmpFieldNames = entity.getCMPFields();
		while (cmpFieldNames.hasNext()) {
			String cmpFieldName = (String)cmpFieldNames.next();
		 	JDBCCMPFieldMetaData cmpField = new JDBCCMPFieldMetaData(cmpFieldName, this);
			
			cmpFields.put(cmpFieldName, cmpField);		    
		}

		// set eager load fields to all cmp fields in case there is no jbosscmp-jdbc.xml
	   eagerLoadFields = new ArrayList(cmpFields.values()); 
		
		// Create no lazy load groups. By default every thing is eager loaded.
	   lazyLoadGroups = new ArrayList(); 
		
		// build the metadata for the queries now in case there is no jbosscmp-jdbc.xml
		queries = new HashMap();
		queryFactory = new JDBCQueryMetaDataFactory(this);
		
		Iterator queriesIterator = entity.getQueries();
		while(queriesIterator.hasNext()) {
			QueryMetaData queryData = (QueryMetaData)queriesIterator.next();
			Method[] methods = queryFactory.getQueryMethods(queryData);
			for(int i=0; i<methods.length; i++) {
				queries.put(methods[i], 
						queryFactory.createJDBCQueryMetaData(queryData, methods[i]));
			}
		}
	}
	
	// Public --------------------------------------------------------
      
	public void importXml(Element element) throws DeploymentException {		
		// This method will be called:
		//  - with element = <default-entity> from standardjbosscmp-jdbc.xml (always)
		//  - with element = <default-entity> from jbosscmp-jdbc.xml (if provided)
		//  - with element = <entity> from jbosscmp-jdbc.xml (if provided)
		
		// All defaults are set during the first call. The following calls override them. 
		
		
		// get table name
		String tableStr = getElementContent(getOptionalChild(element, "table-name"));
		if(tableStr != null) {
			tableName = tableStr;
		}
			
		// create table?  If not provided, keep default.
		String createStr = getElementContent(getOptionalChild(element, "create-table"));
		if(createStr != null) {
	   	createTable = Boolean.valueOf(createStr).booleanValue();
		}
			
    	// remove table?  If not provided, keep default.
		String removeStr = getElementContent(getOptionalChild(element, "remove-table"));
		if(removeStr != null) {
			removeTable = Boolean.valueOf(removeStr).booleanValue();
		}
    	
		// read-only
		String readOnlyStr = getElementContent(getOptionalChild(element, "read-only"));
		if(readOnlyStr != null) {
			readOnly = Boolean.valueOf(readOnlyStr).booleanValue();
		}

		// read-time-out
		if(isReadOnly()) {
			// read-time-out
			String readTimeOutStr = getElementContent(getOptionalChild(element, "read-time-out"));
		   if(readTimeOutStr != null) {
				readTimeOut = Integer.parseInt(readTimeOutStr);
			}
		}		

		String sForUpStr = getElementContent(getOptionalChild(element, "select-for-update"));
		if(sForUpStr != null) {
	   	selectForUpdate = (Boolean.valueOf(sForUpStr).booleanValue());
			selectForUpdate = selectForUpdate && !isReadOnly();
		}

		// primary key constraint?  If not provided, keep default.
		String pkStr = getElementContent(getOptionalChild(element, "pk-constraint"));
		if(pkStr != null) {
			primaryKeyConstraint = Boolean.valueOf(pkStr).booleanValue();
		}

		// cmp fields
		Iterator iterator = getChildrenByTagName(element, "cmp-field");
		while (iterator.hasNext()) {
			Element cmpField = (Element)iterator.next();
			String fieldName = getElementContent(getUniqueChild(cmpField, "field-name"));
			
			JDBCCMPFieldMetaData cmpFieldMetaData = getExistingFieldByName(fieldName);
			cmpFieldMetaData.importXml(cmpField);
		}

		// eager-load
		loadEagerLoadXml(element);

		// lazy-loads
		loadLazyLoadGroupsXml(element);

		// build the metadata for the queries now in case there is no jbosscmp-jdbc.xml
		iterator = getChildrenByTagName(element, "query");
		while(iterator.hasNext()) {
			Element queryElement = (Element)iterator.next();
			Method[] methods = queryFactory.getQueryMethods(queryElement);
			for(int i=0; i<methods.length; i++) {
				JDBCQueryMetaData jdbcQueryData = (JDBCQueryMetaData)queries.get(methods[i]);
				jdbcQueryData = queryFactory.createJDBCQueryMetaData(jdbcQueryData, queryElement, methods[i]); 
				queries.put(methods[i], jdbcQueryData);
			}
		}
	}
	
	protected void loadEagerLoadXml(Element element) throws DeploymentException {
		Element eagerLoadElement = getOptionalChild(element, "eager-load");
		
		// If no info, we're done. Default work was already done in constructor.
		if(eagerLoadElement == null) {
			return;
		}
		
		// only allowed for cmp 2.x
		if(entity.isCMP1x()) {
			throw new DeploymentException("eager-load is only allowed for CMP 2.x");
		}
		
		// get the fields
		Iterator iterator = getChildrenByTagName(eagerLoadElement, "field-name");
		
		// If no eager fields, clear current list and return
		if(!iterator.hasNext()) {
			eagerLoadFields.clear();
			return;
		}
			
		// check for * option
		String fieldName = getElementContent((Element)iterator.next());
		if("*".equals(fieldName)) {
			// all case, which is the default
			// default work already done in constructor, so do nothing
			
			// check that there are no other fields listed
			if(iterator.hasNext()) {
				throw new DeploymentException("When * is specified in eager-load, it is the only field-name element allowed.");
			}
			return;
		} 

		// We have a field list, clear current set and add the fields
		eagerLoadFields.clear();
		
		// add the field we just loaded
		eagerLoadFields.add(getExistingFieldByName(fieldName));
		
		// add the rest
		while (iterator.hasNext()) {
			fieldName = getElementContent((Element)iterator.next());
			eagerLoadFields.add(getExistingFieldByName(fieldName));
		}

		// remove any primary key fields from the set
		// primary key fields do not need to be loaded
		iterator = getEagerLoadFields();
		while(iterator.hasNext()) {
			JDBCCMPFieldMetaData field = (JDBCCMPFieldMetaData)iterator.next();
			if(field.isPrimaryKeyMember()) {
				iterator.remove();
			}
		}
	}

	protected void loadLazyLoadGroupsXml(Element element) throws DeploymentException {
		Element lazyLoadGroupsElement = getOptionalChild(element, "lazy-load-groups");
		if(lazyLoadGroupsElement == null) {
			// no info, default work already done in constructor
			return;
		}
		
		// only allowed for cmp 2.x
		if(entity.isCMP1x()) {
			throw new DeploymentException("lazy-load-groups are only allowed for CMP 2.x");
		}
		
		Iterator groups = getChildrenByTagName(lazyLoadGroupsElement, "lazy-load-group");
		while(groups.hasNext()) {
			Element groupsElement = (Element)groups.next();
			ArrayList group = new ArrayList();

			// add each field
			Iterator fields = getChildrenByTagName(groupsElement, "field-name");
			while(fields.hasNext()) {
				String fieldName = getElementContent((Element)fields.next());
				group.add(getExistingFieldByName(fieldName));
			}
			
			lazyLoadGroups.add(group);
		}
	}

	public String getName() {
		return entityName;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}
	
	public Class getEntityClass() {
		return entityClass;
	}
	
	public Class getHomeClass() {
		return homeClass;
	}
	
	public Class getLocalHomeClass() {
		return localHomeClass;
	}
	
	public JDBCApplicationMetaData getJDBCApplication() {
		return jdbcApplication;
	}
	
	public boolean isCMP1x() {
		return entity.isCMP1x();
	}
	
	public boolean isCMP2x() {
		return entity.isCMP2x();
	}
	
	public EntityMetaData getEntity() {
		return entity;
	}
	
	public int getCMPFieldCount() {
		return cmpFields.size();
	}
	
	public Iterator getCMPFields() {
		return cmpFields.values().iterator();
	}
	
	public Iterator getEagerLoadFields() {
		return eagerLoadFields.iterator();
	}
	
	public Iterator getLazyLoadGroups() {
		return lazyLoadGroups.iterator();
	}

	public JDBCCMPFieldMetaData getCMPFieldByName(String name) {
		return (JDBCCMPFieldMetaData)cmpFields.get(name);
	}
	
	protected JDBCCMPFieldMetaData getExistingFieldByName(String name) throws DeploymentException {
		JDBCCMPFieldMetaData field = getCMPFieldByName(name);
		if(field == null) {
			throw new DeploymentException("field-name '"+name+"' found in jbosscmp-jdbc.xml but not in ejb-jar.xml");
		}
		return field;
	}

	public String getTableName() {
		return tableName;
	}
	
	public boolean getCreateTable() {
		return createTable;
	}
	
	public boolean getRemoveTable() {
		return removeTable;
	}
	
	public boolean hasPrimaryKeyConstraint() {
		return primaryKeyConstraint;
	}
	
	public DataSource getDataSource() {
		return jdbcApplication.getDataSource();
	}
	
	public String getDbURL() {
		return jdbcApplication.getDbURL();
	}
	
	public Iterator getQueries() {
		return queries.values().iterator();
	}
	
	public Class getPrimaryKeyClass() {
		return primaryKeyClass;
	}
	
	public boolean isReadOnly() {
		return readOnly;
	}
	
	public int getReadTimeOut() {
		return readTimeOut;
	}
	
	public String getPrimKeyField() {
		return entity.getPrimKeyField();
	}
	
	public boolean hasSelectForUpdate() {
		return selectForUpdate;
	}
				
	// Package protected ---------------------------------------------
 
	// Protected -----------------------------------------------------

	// Private -------------------------------------------------------

	// Inner classes -------------------------------------------------
}
