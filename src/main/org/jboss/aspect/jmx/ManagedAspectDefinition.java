package org.jboss.aspect.jmx;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistration;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.io.DOMReader;
import org.jboss.aspect.AspectFactory;
import org.jboss.aspect.spi.AspectDefinition;
import org.jboss.aspect.spi.AspectInterceptorHolder;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class exposes a AspectFactory via JMX for better managment.
 * 
 * @jmx:mbean 
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
public class ManagedAspectDefinition extends JMXRegistered implements ManagedAspectDefinitionMBean
{
	
	ArrayList interceptorHolderObjectNames = new ArrayList();
	AspectDefinition aspectDefinition;

	public ManagedAspectDefinition(AspectDefinition aspectDefinition) {
		this.aspectDefinition = aspectDefinition;
	}
	
   /**
    * @jmx:managed-operation
    */
	public String getName() {
		return aspectDefinition.name;
	}

   /**
    * @jmx:managed-attribute
    */
	public Class[] getInterfaces() {
		return aspectDefinition.interfaces;
	}

   /**
    * @jmx:managed-attribute
    */
	public AspectInterceptorHolder[] getInterceptors() {
		return aspectDefinition.interceptors;
	}
	
}
