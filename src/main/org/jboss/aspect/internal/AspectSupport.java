/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.aspect.internal;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jboss.aspect.AspectInitizationException;
import org.jboss.aspect.spi.AspectDefinition;
import org.jboss.aspect.spi.AspectDefinitionConstants;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.aspect.spi.AspectInterceptor;
import org.jboss.aspect.spi.AspectInvocation;
import org.jboss.aspect.spi.AspectInvocationHandler;

/**
 *
 * Holds functions that were usefull during the implemenation of
 * the Aspect related classes.
 * 
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
final public class AspectSupport implements AspectDefinitionConstants {
	
    /**
	 * Parses the aspect XML file pointed to by the configSource URL.
	 * return a Map with the aspect names mapped to the corresponding 
	 * AspectDefinition objects.
	 */
	final static public Map loadAspects(URL configSource)
		throws AspectInitizationException {
		try {
			Map namedInterceptors = new HashMap();
			Map namedStacks = new HashMap();
			Map aspects = new HashMap();
			SAXReader xmlReader = new SAXReader();
			Document doc = xmlReader.read(configSource);

			// load any named interceptors..
			Iterator i = doc.getRootElement().elements(ELEM_INTERCEPTOR).iterator();
			while (i.hasNext()) {
				Element iXML = (Element) i.next();				
            	loadInterceptorElement(iXML, namedInterceptors);
			}


			// load any named stacks
			i = doc.getRootElement().elements(ELEM_STACK).iterator();
			while (i.hasNext()) {
				Element iXML = (Element) i.next();
				loadStackElement(iXML, namedInterceptors, namedStacks);
			}
						
			// load up the aspects.
			i = doc.getRootElement().elements(ELEM_ASPECT).iterator();
			while (i.hasNext()) {
				Element aspectXML = (Element) i.next();
				
				AspectDefinition c = new AspectDefinition(aspectXML, namedInterceptors, namedStacks);
				Object previous = aspects.put(c.name, c);
				if (previous != null) {
					throw new AspectInitizationException(
						"An aspect with name '" + c.name + "' has allready been defined");
				}
			}
			
			return aspects;
		} catch (DocumentException e) {
			throw new AspectInitizationException(
				"Invalid Aspect configuration file: "
					+ configSource,e);
		}
	}
	
    final static public ArrayList loadAspectInterceptorHolderList(Element xml, Map namedInterceptors, Map namedStacks) throws AspectInitizationException
    {

		ArrayList v = new ArrayList();
		
        // Get the interceptor stack configuration.
        Iterator iter = xml.elementIterator();
        while (iter.hasNext())
        {
            Element iXML = (Element)iter.next();
            
            if( iXML.getQName().equals(ELEM_INTERCEPTOR) ) {
            	AspectInterceptorHolder aih = loadInterceptorElement(iXML, namedInterceptors);
	            v.add(aih);
            }
            
            if( iXML.getQName().equals(ELEM_INTERCEPTOR_REF) ) {
            	
            	String name = iXML.attribute(ATTR_NAME).getValue();
	            Object ref = namedInterceptors.get(name);
	            if (ref == null) 
	                throw new AspectInitizationException("No interceptor with name '" + name + "' has been defined.");
	            v.add(ref);
            }
            
            if( iXML.getQName().equals(ELEM_STACK) ) {

			    ArrayList stack = loadStackElement(iXML, namedInterceptors, namedStacks);            	
            	Iterator j = stack.iterator();
            	while( j.hasNext() )
            		v.add(j.next());
            		           		
            }
            
            if( iXML.getQName().equals(ELEM_STACK_REF) ) {
            	
            	String name = iXML.attribute(ATTR_NAME).getValue();
	            Object stack = namedStacks.get(name);
	            if (stack == null) 
	                throw new AspectInitizationException("No stack with name '" + name + "' has been defined.");
	                
	            Iterator j = ((Collection)stack).iterator();
            	while( j.hasNext() )
            		v.add(j.next());            		
            }
        }
        
		return v;
    }
	
    final static AspectInterceptorHolder loadInterceptorElement(Element xml, Map namedInterceptors) throws AspectInitizationException
    {
    	String name = null;
    	if( xml.attribute(ATTR_NAME)!=null )
    		name = xml.attribute(ATTR_NAME).getValue();
    	
        AspectInterceptorHolder aih = new AspectInterceptorHolder(xml);
        if( name != null ) {
            Object old = namedInterceptors.put(name, aih);
            if (old != null)
                throw new AspectInitizationException("An interceptor with name '" + name + "' has allready been defined.");
        }
        return aih;
    }

    final static ArrayList loadStackElement(Element xml, Map namedInterceptors, Map namedStacks) throws AspectInitizationException
    {
    	String name = null;
    	if( xml.attribute(ATTR_NAME)!=null )
    		name = xml.attribute(ATTR_NAME).getValue();
    	            		
    	ArrayList stack = loadAspectInterceptorHolderList( xml, namedInterceptors, namedStacks);
    	
        if( name != null ) {
            Object old = namedStacks.put(name, stack);
            if (old != null)
                throw new AspectInitizationException("An stack with name '" + name + "' has allready been defined.");
        }        
        return stack;
    }
    
    
	/**
	 * Returns a Set containing all the methods that were defined
	 * the a list of interfaces.
	 */
	final static public Set getExposedMethods(Class[] interfaces) {
		
		Set set = new HashSet();
		for( int i=0; i < interfaces.length; i ++ ) {
			Method t[] = interfaces[i].getMethods();
			for( int j=0; j < t.length; j++ ) {
				Method method = null;
				
				// If it is a Object method use that method instead.
				try {
					method = Object.class.getMethod(t[j].getName(), t[j].getParameterTypes());
				} catch ( NoSuchMethodException e ) {
				}
				
				if( method == null )
					method = t[j];
					
				set.add(method);
			}
		}
		return set;
	}
   
   /**
    * Adds all the interfaces of the aspectObject class to the interfaces array and returns
    * the new array.  Duplicates will not be added.
    */
   final static public Class[] appendInterfaces( Class interfaces[], Class targetClass ) {
         
      ArrayList interfaceList = new ArrayList();

      for( int i=0; i < interfaces.length; i++ ) 
         interfaceList.add( interfaces[i] );
      
      interfaces = targetClass.getInterfaces();
      for( int i=0; i < interfaces.length; i++ ) {
         if( interfaceList.contains(interfaces[i]) )
            continue;
         interfaceList.add( interfaces[i] );
      }
   
      interfaces = new Class[interfaceList.size()];
      interfaces = (Class[])interfaceList.toArray(interfaces);
      
      return interfaces;
   }

}
