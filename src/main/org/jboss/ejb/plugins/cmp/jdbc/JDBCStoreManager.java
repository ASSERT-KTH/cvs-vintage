/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.jboss.ejb.DeploymentException;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;

import org.jboss.ejb.plugins.cmp.CMPStoreManager; 
import org.jboss.ejb.plugins.cmp.CommandFactory;
 
import org.jboss.ejb.plugins.cmp.bridge.EntityBridgeInvocationHandler; 
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge; 
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge; 

import org.jboss.logging.Log;

import org.jboss.proxy.Proxy;

import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCXmlFileLoader;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCApplicationMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCTypeMappingMetaData;
import org.jboss.metadata.ApplicationMetaData;

/**
 * JDBCStoreManager manages storage of persistence data into a table.
 * Other then loading the initial jbosscmp-jdbc.xml file this class
 * does very little. The interesting tasks are performed by the command
 * classes.
 *
 * Life-cycle:
 *		Tied to the life-cycle of the entity container.
 *
 * Multiplicity:	
 *		One per cmp entity bean. This could be less if another implementaion of 
 * EntityPersistenceStore is created and thoes beans use the implementation 		
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @see org.jboss.ejb.EntityPersistenceStore
 * @version $Revision: 1.3 $
 */                            
public class JDBCStoreManager extends CMPStoreManager {
	protected DataSource dataSource;
	
	protected JDBCTypeFactory typeFactory;
	protected boolean debug;
	
	protected JDBCEntityMetaData metaData;
	protected JDBCEntityBridge entityBridge;
	
	protected JDBCLoadFieldCommand loadFieldCommand;
	
   public void init() throws Exception {
		metaData = loadJDBCEntityMetaData();
		typeFactory = new JDBCTypeFactory(metaData.getJDBCApplication());
		entityBridge = new JDBCEntityBridge(metaData, log, this);

		super.init();
		
		loadFieldCommand = getCommandFactory().createLoadFieldCommand();
	}
	
	public JDBCEntityBridge getEntityBridge() {
		return entityBridge;
	}
	
	public JDBCTypeFactory getJDBCTypeFactory() {
	   return typeFactory;
	}
	
	public boolean getDebug() {
		return debug;
	}

	public JDBCEntityMetaData getMetaData() {
		return metaData;
	}
	
	protected CommandFactory createCommandFactory() throws Exception {
		return new JDBCCommandFactory(this);
	}
	
	public JDBCCommandFactory getCommandFactory() {
		return (JDBCCommandFactory) commandFactory;
	}

	public void loadField(JDBCCMPFieldBridge field, EntityEnterpriseContext ctx) {
      loadFieldCommand.execute(field, ctx);
   }
   

 	/**
	* Returns a new instance of a class which implemnts the bean class.
	* 
	* @see java.lang.Class#newInstance 
	* @return the new instance
	*/
	public Object createBeanClassInstance() throws Exception {
		Class beanClass = container.getBeanClass();
		
		Class[] classes = new Class[] { beanClass };
		EntityBridgeInvocationHandler handler = new EntityBridgeInvocationHandler(entityBridge, beanClass);           
		ClassLoader classLoader = beanClass.getClassLoader();

		return Proxy.newProxyInstance(classLoader, classes, handler);
	}
	
	/** 
	 * Returns a database connection
	 */
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}
	
	//
	// Remove this after metadata code is updated
	//
	private JDBCEntityMetaData loadJDBCEntityMetaData() throws DeploymentException {
		ApplicationMetaData amd = container.getBeanMetaData().getApplicationMetaData();

		// Get JDBC MetaData
		JDBCApplicationMetaData jamd = (JDBCApplicationMetaData)amd.getPluginData("CMP-JDBC");
		if (jamd == null) {
			// we are the first cmp entity to need jbosscmp-jdbc. Load jbosscmp-jdbc.xml for the whole application
			JDBCXmlFileLoader jfl = new JDBCXmlFileLoader(amd, container.getClassLoader(), container.getLocalClassLoader(), log);
			jamd = jfl.load();
			amd.addPluginData("CMP-JDBC", jamd);
		}
		
		// set debug flag
		debug = jamd.getDebug();
		
		// Get the datasource
		dataSource = jamd.getDataSource();
	   if(dataSource == null) {
			throw new DeploymentException("Unable to locate data source.");
		}
		
		// Get JDBC Bean MetaData
		String ejbName = container.getBeanMetaData().getEjbName();
		JDBCEntityMetaData metadata = jamd.getBeanByEjbName(ejbName);
		if(metadata == null) {
			throw new DeploymentException("No metadata found for bean " + ejbName);
		}
		return metadata;
	}
}
