/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.util.HashMap;
import java.util.Iterator;

import java.sql.Connection;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.w3c.dom.Element;

import org.jboss.logging.Logger;
import org.jboss.ejb.DeploymentException;

import org.jboss.metadata.XmlLoadable;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.EntityMetaData;
import org.jboss.metadata.ApplicationMetaData;


/** 
 *      
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 *	@author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *	@version $Revision: 1.3 $
 */
public class JDBCApplicationMetaData extends MetaData implements XmlLoadable {
	// Constants -----------------------------------------------------
	public static final String JDBC_PM = "org.jboss.ejb.plugins.cmp.jdbc.JDBCStoreManager";
	
	// Attributes ----------------------------------------------------
	
	// the classloader comes from the container. It is used to load the classes of the beans
	// and their primary keys
	private ClassLoader classLoader;
	
	// the "parent" application metadata
	private ApplicationMetaData applicationMetaData;
	
	// this only contains the jbosscmp-jdbc-managed cmp entities
	private HashMap entities = new HashMap();
	
	// the datasource to use for this application
	private String dbURL;
	private DataSource dataSource;
	private Integer transactionIsolation;
	
	// should we print tons of info?
	private boolean debug = false;
	
	// all the available type mappings
	private HashMap typeMappings = new HashMap();
	
	// the type mapping to use with the specified database
	private JDBCTypeMappingMetaData typeMapping;
	
	// all the available dependent value classes (by javaType)
	private HashMap valueClasses = new HashMap();
	
	
	// Static --------------------------------------------------------
	
	// Constructors --------------------------------------------------
	public JDBCApplicationMetaData(ApplicationMetaData amd, ClassLoader cl) throws DeploymentException {
		// initialisation of this object goes as follows:
		//  - constructor
		//  - importXml() for standardjbosscmp-jdbc.xml and jbosscmp-jdbc.xml
		//  - init()
		
		// the classloader is the same for all the beans in the application
		classLoader = cl;
		applicationMetaData = amd;
		
		// create metadata for all jbosscmp-jdbc-managed cmp entities
		// we do that here in case there is no jbosscmp-jdbc.xml
		Iterator beans = applicationMetaData.getEnterpriseBeans();
		while (beans.hasNext()) {
			BeanMetaData bean = (BeanMetaData)beans.next();
			
			// only take entities
			if(bean.isEntity()) {
				EntityMetaData entity = (EntityMetaData)bean;
			
				// only take jbosscmp-jdbc-managed CMP entities
				if(entity.isCMP() && entity.getContainerConfiguration().getPersistenceManager().equals(JDBC_PM)) {
					JDBCEntityMetaData jdbcEntity = new JDBCEntityMetaData(this, entity);
					entities.put(entity.getEjbName(), jdbcEntity);
				}
			}
		}
	}
	
	
	// Public --------------------------------------------------------
	public DataSource getDataSource() {
		return dataSource;
	}
	
	public String getDbURL() {
		return dbURL;
	}

	public Integer getTransactionIsolation() {
		return transactionIsolation;
	}
	
	public JDBCTypeMappingMetaData getTypeMapping() {
		return typeMapping;
	}
	
	public Iterator getValueClasses() {
		return valueClasses.values().iterator();
	}
	
	public boolean getDebug() {
		return debug;
	}
	
	protected ClassLoader getClassLoader() {
		return classLoader;
	}

	public JDBCEntityMetaData getBeanByEjbName(String name) { 
		return (JDBCEntityMetaData)entities.get(name);
	}

	
	public void init() throws DeploymentException {		
		// find the datasource
		if(!dbURL.startsWith("jdbc:")) {
			try {
				dataSource = (DataSource)new InitialContext().lookup(dbURL);
			} catch(NamingException e) {
				throw new DeploymentException("Error: can't find data source: " + dbURL);
			}
		}	
	}
	
	
	
	// XmlLoadable implementation ------------------------------------    
	public void importXml(Element element) throws DeploymentException {
		// importXml will be called at least once: with standardjbosscmp-jdbc.xml
		// it may be called a second time with user-provided jbosscmp-jdbc.xml
		// we must ensure to set all defaults values in the first call
		Iterator iterator;
		
		// first get the type mappings. (optional, but always set in standardjbosscmp-jdbc.xml)
		Element typeMaps = getOptionalChild(element, "type-mappings");

		if(typeMaps != null) {
			iterator = getChildrenByTagName(typeMaps, "type-mapping");
			
			while (iterator.hasNext()) {
				Element typeMappingElement = (Element)iterator.next();
				JDBCTypeMappingMetaData typeMapping = new JDBCTypeMappingMetaData();
				typeMapping.importXml(typeMappingElement);
				typeMappings.put(typeMapping.getName(), typeMapping);
			}
		}

		// get the datasource (optional, but always set in standardjbosscmp-jdbc.xml)
		Element db = getOptionalChild(element, "datasource");
		if (db != null) dbURL = getElementContent(db);
		
		// Make sure it is prefixed with java:
		if (!dbURL.startsWith("java:/")) {
			dbURL = "java:/"+dbURL;
		}

		// get the datasource (optional, but always set in standardjbosscmp-jdbc.xml)
		String txIsolation = getElementContent(getOptionalChild(element, "transaction-isolation"));
		if(txIsolation != null) {
			if(txIsolation.equals("transaction-none")) {
				transactionIsolation = new Integer(Connection.TRANSACTION_NONE);
			} else if(txIsolation.equals("transaction-read-committed")) {
				transactionIsolation = new Integer(Connection.TRANSACTION_READ_COMMITTED);
			} else if(txIsolation.equals("transaction-read-uncommitted")) {
				transactionIsolation = new Integer(Connection.TRANSACTION_READ_UNCOMMITTED);
			} else if(txIsolation.equals("transaction-repeatable-read")) {
				transactionIsolation = new Integer(Connection.TRANSACTION_REPEATABLE_READ);
			} else if(txIsolation.equals("transaction-serializable")) {
				transactionIsolation = new Integer(Connection.TRANSACTION_SERIALIZABLE);
			} else {
				throw new DeploymentException("Unknown transaction isolation level " + txIsolation);
			}
		}

		// get the type mapping for this datasource (optional, but always set in standardjbosscmp-jdbc.xml)
		String typeMappingString = getElementContent(getOptionalChild(element, "type-mapping"));		
		if (typeMappingString != null) {
			typeMapping = (JDBCTypeMappingMetaData)typeMappings.get(typeMappingString);
		
			if (typeMapping == null) {
				throw new DeploymentException("Error in jbosscmp-jdbc.xml : type-mapping " + typeMappingString + " not found");
			}
		}
      
		// enable extra debugging?
		Element debugElement = getOptionalChild(element, "debug");
		if (debugElement != null) {
			String stringDebug = getElementContent( debugElement );
			debug = Boolean.valueOf(stringDebug).booleanValue();
		}
      

		// get default settings for the beans (optional, but always set in standardjbosscmp-jdbc.xml)
		Element defaultEntity = getOptionalChild(element, "default-entity");
		if (defaultEntity != null) {
			iterator = entities.values().iterator();		
			while(iterator.hasNext()) {
				((JDBCEntityMetaData)iterator.next()).importXml(defaultEntity);
			}
		}		
		
		// get the beans data (only in jbosscmp-jdbc.xml)
		Element enterpriseBeans = getOptionalChild(element, "enterprise-beans");
		if(enterpriseBeans != null) {
			String ejbName = null;			
			iterator = getChildrenByTagName(enterpriseBeans, "entity");				
			while(iterator.hasNext()) {
				Element bean = (Element)iterator.next();

				// get the bean's data, gaurenteed to work because we create
				// a metadata object for each bean in the constructor.
				ejbName = getElementContent(getUniqueChild(bean, "ejb-name"));
				JDBCEntityMetaData entity = (JDBCEntityMetaData)entities.get(ejbName);					
				if (entity != null) {
					entity.importXml(bean);
				} else {
					Logger.warning("Warning: data found in jbosscmp-jdbc.xml for entity " + ejbName + " but bean is not a jbosscmp-jdbc-managed cmp entity in ejb-jar.xml"); 
				}
			}
		}
		
		// dependent-value-objects
		Element valueClassesElement = getOptionalChild(element, "dependent-value-classes");
		if(valueClassesElement != null) {
			iterator = getChildrenByTagName(valueClassesElement, "dependent-value-class");
			while(iterator.hasNext()) {
				Element valueClassElement = (Element)iterator.next();
				JDBCValueClassMetaData valueClass = new JDBCValueClassMetaData(valueClassElement, this);
				valueClasses.put(valueClass.getJavaType(), valueClass);
			}
		}
	}
	
	// Package protected ---------------------------------------------
	
	// Protected -----------------------------------------------------
	
	// Private -------------------------------------------------------
	
	// Inner classes -------------------------------------------------
}
