/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jaws.metadata;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import java.lang.reflect.Field;

import javax.sql.DataSource;

import javax.naming.InitialContext;

import org.w3c.dom.Element;

import org.jboss.ejb.DeploymentException;

import org.jboss.metadata.EntityMetaData;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.XmlLoadable;


/**
 *	<description> 
 *      
 *	@see <related>
 *	@author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *	@version $Revision: 1.4 $
 */
public class JawsEntityMetaData extends MetaData implements XmlLoadable {
	// Constants -----------------------------------------------------
    
	// Attributes ----------------------------------------------------
    
	// parent metadata structures
	private JawsApplicationMetaData jawsApplication;
	private EntityMetaData entity;
	
	// the name of the bean (same as entity.getEjbName())
	private String ejbName = null;
	
	// the name of the table to use for this bean
	private String tableName = null;
	
	// do we have to try and create the table on deployment?
	private boolean createTable;
	
	// do we have to drop the table on undeployment?
	private boolean removeTable;
	
	// do we use tuned updates?
	private boolean tunedUpdates;

	// is the bean read-only?
	private boolean readOnly;
	private int timeOut;
	
	// is the bean's primary key a composite object
	private boolean compositeKey;
	
	// the class of the primary key
	private Class primaryKeyClass;
	
	// the fields we must persist for this bean
	private Hashtable cmpFields = new Hashtable();
	
	// the fields that belong to the primary key (if composite)
	private ArrayList pkFields = new ArrayList();
	
	// finders for this bean
	private ArrayList finders = new ArrayList();
	
	
	// Static --------------------------------------------------------
   
	// Constructors --------------------------------------------------
    
	public JawsEntityMetaData(JawsApplicationMetaData app, EntityMetaData ent) throws DeploymentException {
		// initialisation of this object goes as follows:
		//  - constructor
		//  - importXml() for standardjaws.xml and jaws.xml
		
		jawsApplication = app;
		entity = ent;
		ejbName = entity.getEjbName();
		compositeKey = entity.getPrimKeyField() == null;
		
		try {
			primaryKeyClass = jawsApplication.getClassLoader().loadClass(entity.getPrimaryKeyClass());
		} catch (ClassNotFoundException e) {
			throw new DeploymentException("could not load primary key class: " + entity.getPrimaryKeyClass());
		}
		
		// we replace the . by _ because some dbs die on it...
		// the table name may be overridden in importXml(jaws.xml)
		tableName = ejbName.replace('.', '_');
		
		// build the metadata for the cmp fields now in case there is no jaws.xml
		Iterator cmpFieldNames = entity.getCMPFields();

		while (cmpFieldNames.hasNext()) {
			String cmpFieldName = (String)cmpFieldNames.next();
		 	CMPFieldMetaData cmpField = new CMPFieldMetaData(cmpFieldName, this);
			
			cmpFields.put(cmpFieldName, cmpField);		    
		}
		
		// build the pkfields metadatas
		if (compositeKey) {
			Field[] pkClassFields = primaryKeyClass.getFields();
        	
			for (int i = 0; i < pkClassFields.length; i++) {
				Field pkField = pkClassFields[i];
				CMPFieldMetaData cmpField = (CMPFieldMetaData)cmpFields.get(pkField.getName());

				if (cmpField == null) throw new DeploymentException("Bean " + ejbName + " has PK of type " + primaryKeyClass.getName() + ", so it should have a cmp-field named " + pkField.getName());

				pkFields.add(new PkFieldMetaData(pkField, cmpField, this));
			}

		} else {
			String pkFieldName = entity.getPrimKeyField();
         	CMPFieldMetaData cmpField = (CMPFieldMetaData)cmpFields.get(pkFieldName);

			pkFields.add(new PkFieldMetaData(cmpField, this));
		}		
	}
	
	// Public --------------------------------------------------------

    public JawsApplicationMetaData getJawsApplication() { return jawsApplication; }

	public EntityMetaData getEntity() { return entity; }
	
	public Iterator getCMPFields() { return cmpFields.values().iterator(); }
	
	public CMPFieldMetaData getCMPFieldByName(String name) {
		return (CMPFieldMetaData)cmpFields.get(name);
	}
	
	public Iterator getPkFields() { return pkFields.iterator(); }
	
	public String getTableName() { return tableName; }
	
	public boolean getCreateTable() { return createTable; }
	
	public boolean getRemoveTable() { return removeTable; }
	
	public boolean hasTunedUpdates() { return tunedUpdates; }
	
	public int getReadOnlyTimeOut() { return timeOut; }
	
	public boolean hasCompositeKey() { return compositeKey; }
    
	public DataSource getDataSource() { return jawsApplication.getDataSource(); }
	
	public String getDbURL() { return jawsApplication.getDbURL(); }
	
	public Iterator getFinders() { return finders.iterator(); }
	
	public String getName() { return ejbName; }
	
	public int getNumberOfCMPFields() { return cmpFields.size(); }
	
	public Class getPrimaryKeyClass() { return primaryKeyClass; }
	
	public boolean isReadOnly() { return readOnly; }
	
	public Iterator getEjbReferences() { return entity.getEjbReferences(); }
	
	public String getPrimKeyField() { return entity.getPrimKeyField(); }
	
	
		
	// XmlLoadable implementation ------------------------------------
	
	public void importXml(Element element) throws DeploymentException {		
		// This method will be called:
		//  - with element = <default-entity> from standardjaws.xml (always)
		//  - with element = <default-entity> from jaws.xml (if provided)
		//  - with element = <entity> from jaws.xml (if provided)
		
		// All defaults are set during the first call. The following calls override them. 
		
		
		// get table name
		String tableStr = getElementContent(getOptionalChild(element, "table-name"));
		if (tableStr != null) tableName = tableStr;
			
		// create table?  If not provided, keep default.
		String createStr = getElementContent(getOptionalChild(element, "create-table"));
		if (createStr != null) createTable = Boolean.valueOf(createStr).booleanValue();
			
    	// remove table?  If not provided, keep default.
		String removeStr = getElementContent(getOptionalChild(element, "remove-table"));
		if (removeStr != null) removeTable = Boolean.valueOf(removeStr).booleanValue();
    	
		// tuned updates?  If not provided, keep default.
		String tunedStr = getElementContent(getOptionalChild(element, "tuned-updates"));
		if (tunedStr != null) tunedUpdates = Boolean.valueOf(tunedStr).booleanValue();
		
		// read only?  If not provided, keep default.
		String roStr = getElementContent(getOptionalChild(element, "read-only"));
	    if (roStr != null) readOnly = Boolean.valueOf(roStr).booleanValue();
		
		// read only timeout?  
		String toStr = getElementContent(getOptionalChild(element, "time-out"));
		if (toStr != null) timeOut = Integer.valueOf(toStr).intValue();
			
		// cmp fields
		Iterator iterator = getChildrenByTagName(element, "cmp-field");

		while (iterator.hasNext()) {
			Element cmpField = (Element)iterator.next();
			String fieldName = getElementContent(getUniqueChild(cmpField, "field-name"));
			
			CMPFieldMetaData cmpFieldMetaData = getCMPFieldByName(fieldName);
            if (cmpFieldMetaData == null) 
				throw new DeploymentException("cmp-field '"+fieldName+"' found in jaws.xml but not in ejb-jar.xml");
				
			cmpFieldMetaData.importXml(cmpField);
		}
		
		// finders
		iterator = getChildrenByTagName(element, "finder");

		while (iterator.hasNext()) {
			Element finder = (Element)iterator.next();
			FinderMetaData finderMetaData = new FinderMetaData();
			finderMetaData.importXml(finder);

			finders.add(finderMetaData);
		}
		
	}
		
		
	// Package protected ---------------------------------------------
 
	// Protected -----------------------------------------------------
    
	// Private -------------------------------------------------------

	// Inner classes -------------------------------------------------
}
