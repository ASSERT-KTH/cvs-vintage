package org.jboss.aspect.jmx;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.io.DOMReader;
import org.jboss.aspect.AspectFactory;
import org.jboss.aspect.spi.AspectDefinition;
import org.jboss.system.ServiceMBeanSupport;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
//import MBeanSupport
/**
 * This class exposes a AspectFactory via JMX for better managment.
 * 
 * @jmx:mbean 
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
public class ManagedAspectFactory extends JMXRegistered implements ManagedAspectFactoryMBean
{
	Map registeredDefinitions = new HashMap();  

   /** 
    * Extension of the AspectFactory to allow us more control of it's AspectDefintions.
    * 
    * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
    */
   private class ExtendedAspectFactory extends AspectFactory
   {   	
      /**
       * @see org.jboss.aspect.AspectFactory#getDefinitions()
       */
      public Iterator getDefinitions()
      {
         return super.getDefinitions();
      }

      /**
       * @see org.jboss.aspect.AspectFactory#removeDefinition(java.lang.String)
       */
      protected AspectDefinition removeDefinition(String name)
      {
      	try {
	      	ObjectName on = getDefinitionObjectName(name);
   			server.unregisterMBean(on);
      	} catch ( Throwable e ) {
      	}   	
         return super.removeDefinition(name);
      }

      /**
       * @see org.jboss.aspect.AspectFactory#setDefinition(java.lang.String, org.jboss.aspect.spi.AspectDefinition)
       */
      protected AspectDefinition setDefinition(String name, AspectDefinition ad)
      {
      	try {
	      	ObjectName on = getDefinitionObjectName(name);
	      	if( server.isRegistered(on) )
   				server.unregisterMBean(on);
   			server.registerMBean(new ManagedAspectDefinition(ad), on );
      	} catch ( Throwable e ) {
      	}
         return super.setDefinition(name, ad);
      }

	}
   
   private class ExtendedDOMReader extends DOMReader {
   	
      public Document read(Node xml)
      {
      	Document d = DocumentFactory.getInstance().createDocument();
         readTree(xml, d);
         return d;
      }

	}

	
   ExtendedAspectFactory aspectFactory = new ExtendedAspectFactory();

   /**
    * @jmx:managed-attribute
    */
   public AspectFactory getAspectFactory()
   {
      return aspectFactory;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setInitialAspectConfig(Element domXML) throws Exception
   {
		 	ExtendedDOMReader xmlReader = new ExtendedDOMReader();
		   Document d =  xmlReader.read(domXML);
   	
   }

   /**
    * @jmx:managed-operation
    */
   public void removeDefinition(String name) throws Exception
   {
      aspectFactory.removeDefinition(name);
   }
   
   /**
    * @jmx:managed-operation
    */
   public void setDefinition(String name, AspectDefinition def) throws Exception
   {
      aspectFactory.setDefinition(name, def);
   }
   
      
   /**
    * @jmx:managed-operation
    */
   public void configure(URL source) throws Exception
   {
      aspectFactory.configure(source);
   }
   
   /**
    * @jmx:managed-operation
    */
   public void configure(org.dom4j.Document source) throws Exception
   {
      aspectFactory.configure(source);
   }

   
   ObjectName getDefinitionObjectName(String name) throws MalformedObjectNameException {
   	Hashtable map = serviceName.getKeyPropertyList();
   	map.put("aspect", jmxEncode(name));
   	return new ObjectName( serviceName.getDomain(),map);
   }
   
   /**
    * todo: implement this method correctly 
    * 
    * @param value
    * @return String
    */
   public String jmxEncode( String value ) {
   	return value;
   }

   /**
    * 
    * @jmx:managed-attribute
    * @param value
    * @return String
    */
   public ObjectName[] getDefinitions() {
   	
   	ArrayList v = new ArrayList();
   	
 		Iterator i = aspectFactory.getDefinitions();
		while (i.hasNext())
      {
      	try {
	         AspectDefinition ad = (AspectDefinition) i.next();
	         v.add(getDefinitionObjectName(ad.name));
      	} catch ( MalformedObjectNameException e) {
      	}
      }
      
      ObjectName rc[] = new ObjectName[ v.size() ];
      return rc;
   }

}
