/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.aspect.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import org.jboss.aspect.AspectDefinition;
import org.jboss.aspect.proxy.AspectInitizationException;

/**
 * This class is used to parse the XML doms used in the configurations
 * of the aspects.
 * 
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 *
 */
final public class XMLConfiguration {

	/**
	 * Parses the aspect XML file pointed to by the configSource URL.
	 * return a Map with the aspect names mapped to the corresponding 
	 * AspectDefinition objects.
	 */
	final static public Map loadAspects(URL configSource) throws AspectInitizationException {
		try {
			Map aspects = new HashMap();
			SAXReader xmlReader = new SAXReader();
	    	Document doc = xmlReader.read(configSource);
	    	Iterator i = doc.getRootElement().elements("aspect").iterator();
	    	while( i.hasNext() ) {
	    		Element aspectXML = (Element)i.next();	    	
				AspectDefinition c = loadAspectObjectDefinition(aspectXML);
				Object previous = aspects.put(c.name,c);
				if( previous!=null ) {
					throw new AspectInitizationException("Invalid Aspect configuration file: "+configSource); 
				}
	    	}		
			return aspects;
		} catch ( DocumentException e ) {
			throw new AspectInitizationException("Invalid Aspect configuration file: "+configSource+": "+e); 
		}
	}
	
	/**
	 * 
	 * Creates AspectDefinition given a properly formated Dom4j "aspect" element.
	 * 
	 */
	final static public AspectDefinition loadAspectObjectDefinition(Element aoXML) throws AspectInitizationException {
		
		String name = aoXML.attribute("name").getValue();
		String targetClass = aoXML.attribute("aspectObject-class")==null 
			? null : aoXML.attribute("aspectObject-class").getValue();
		
		ArrayList interceptorList = new ArrayList();
		ArrayList interceptorConfigList = new ArrayList();
		
		// Get the interceptor stack configuration.
    	Iterator j = aoXML.elements("interceptor").iterator();
    	while( j.hasNext() ) {
    		Element interceptorXML = (Element)j.next();
    		String interceptorClass = interceptorXML.attribute("class").getValue();
    		interceptorList.add(interceptorClass);

			// Get this interceptor's attribute configuration.
			Map interceptorConfig = new HashMap();
	    	Iterator k = interceptorXML.elements("attribute").iterator();
	    	while( k.hasNext() ) {
	    		Element attribute = (Element)k.next();
	    		String attname=attribute.attribute("name").getValue();
	    		String attvalue=attribute.attribute("value").getValue();
	    		interceptorConfig.put( attname, attvalue );
	    	}
	    	interceptorConfigList.add( interceptorConfig );
    	}			
    	
    	String interceptors[] = new String[interceptorList.size()];
    	interceptorList.toArray(interceptors);
    	Map interceptorConfigs[] = new Map[interceptorConfigList.size()];
    	interceptorConfigList.toArray(interceptorConfigs);
    	
		return new AspectDefinition(name, interceptors, interceptorConfigs, targetClass);		
	}	

}
