/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.jboss.ejb.DeploymentException;
import org.jboss.metadata.MetaData;
import org.w3c.dom.Element;

/**
 * Imutable class which holds a map between Java Classes and JDBCMappingMetaData.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 *	@author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *	@version $Revision: 1.3 $
 */
public final class JDBCTypeMappingMetaData {
	
	private static final String[] PRIMITIVES = {"boolean","byte","char","short","int","long","float","double"};
	
	private static final String[] PRIMITIVE_CLASSES = {"java.lang.Boolean","java.lang.Byte","java.lang.Character","java.lang.Short","java.lang.Integer","java.lang.Long","java.lang.Float","java.lang.Double"};   
	
	private final String name;
	
	private final HashMap mappings = new HashMap();

	/**
	 * Constructs a mapping with the data contained in the type-mapping xml element
	 * from a jbosscmp-jdbc xml file.
	 *
	 * @param element the xml Element which contains the metadata about
	 * 		this type mapping
	 * @throws DeploymentException if the xml element is not semantically correct
	 */
	public JDBCTypeMappingMetaData(Element element) throws DeploymentException {
	
		// get the name of this type-mapping
		name = MetaData.getUniqueChildContent(element, "name");
		
		// get the mappings
		Iterator iterator = MetaData.getChildrenByTagName(element, "mapping");
		
		while (iterator.hasNext()) {
			Element mappingElement = (Element)iterator.next();
			JDBCMappingMetaData mapping = new JDBCMappingMetaData(mappingElement);
			mappings.put(mapping.getJavaType(), mapping);
		}
	}
    	
	/**
	 * Gets the name of this mapping. The mapping name used to differentiate this
	 * mapping from other mappings and the mapping the application used is retrieved
	 * by name.
	 * @return the name of this mapping.
	 */
	public String getName() {
		return name;
	}
   
	/**
	 * Gets the jdbc type which this class has mapped to the specified java class. 
	 * The jdbc type is used to retrieve data from a result set and to set 
	 * parameters in a prepared statement.
	 *
	 * @param type the Class for which the jdbc type will be returned
	 * @return the jdbc type which is mapped to the type
	 */
	public int getJdbcTypeForJavaType(Class type) {
		String javaType = type.getName();
      
		// Check primitive first
		for (int i = 0; i < PRIMITIVES.length; i++) {
			if (javaType.equals(PRIMITIVES[i])) {
				// Translate into class
				javaType = PRIMITIVE_CLASSES[i];
				break;
			}
		}
      
		// Check other types
		JDBCMappingMetaData mapping = (JDBCMappingMetaData)mappings.get(javaType);
		
		// if not found, return mapping for java.lang.object
		if (mapping == null) {
			mapping = (JDBCMappingMetaData)mappings.get("java.lang.Object");
		}	
			
		return mapping.getJdbcType();
	}
   
	/**
	 * Gets the sql type which this class has mapped to the java class. The sql 
	 * type is the sql column data type, and is used in CREATE TABLE statements.
	 *
	 * @param type the Class for which the sql type will be returned
	 * @return the sql type which is mapped to the type
	 */
	public String getSqlTypeForJavaType(Class type) {
		String javaType = type.getName();
		
		// if the type is a primitive convert it to it's wrapper class
		for (int i = 0; i < PRIMITIVES.length; i++) {
			if (javaType.equals(PRIMITIVES[i])) {
				// Translate into class
				javaType = PRIMITIVE_CLASSES[i];
				break;
			}
		}
		
		// Check other types
		JDBCMappingMetaData mapping = (JDBCMappingMetaData)mappings.get(javaType);
		
		// if not found, return mapping for java.lang.object
		if(mapping == null) {
			mapping = (JDBCMappingMetaData)mappings.get("java.lang.Object");
		}
		
		return mapping.getSqlType();
	}
}
