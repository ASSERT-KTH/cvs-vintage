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
import org.jboss.aspect.model.Aspect;
import org.jboss.aspect.model.Attribute;
import org.jboss.aspect.model.Interceptor;
import org.jboss.aspect.model.InterfaceFilter;
import org.jboss.aspect.model.MethodFilter;
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
					throw new AspectInitizationException("Invalid Aspect configuration file, aspect defined multipled times ("+c.name+") : "+configSource); 
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
		Aspect aspect = new Aspect();
		aspect.setName(aoXML.attribute("name").getValue());
		if( aoXML.attribute("target-class")!=null )
			aspect.setTargetClass(aoXML.attribute("target-class").getValue());
		
		// Get the interceptor stack configuration.
    	Iterator j = aoXML.elements("interceptor").iterator();
    	while( j.hasNext() ) {
         Interceptor interceptor = new Interceptor();
    		Element interceptorXML = (Element)j.next();
         
    		interceptor.setClassname(interceptorXML.attribute("class").getValue());

	    	Iterator k = interceptorXML.elements("attribute").iterator();
	    	while( k.hasNext() ) {
            Attribute attribute = new Attribute();
	    		Element attributeXML = (Element)k.next();
	    		attribute.setName(attributeXML.attribute("name").getValue());
	    		attribute.setValue(attributeXML.attribute("value").getValue());
            interceptor.add( attribute );
	    	}
         

         k = interceptorXML.elements("filter-interface").iterator();
         while( k.hasNext() ) {
            InterfaceFilter interfaceFilter = new InterfaceFilter();
            Element interfaceFilterXML = (Element)k.next();
            interfaceFilter.setClassname(interfaceFilterXML.attribute("name").getValue());
            interceptor.add( interfaceFilter );
         }
         
         k = interceptorXML.elements("filter-method").iterator();
         while( k.hasNext() ) {
            MethodFilter methodFilter = new MethodFilter();
            Element methodFilterXML = (Element)k.next();
            methodFilter.setSignature(methodFilterXML.attribute("name").getValue());
            interceptor.add( methodFilter );
         }
         
         aspect.add(interceptor);
    	}
      
		return new AspectDefinition(aspect);
	}	

}
