/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.aspect.proxy;

import java.net.URL;

import org.jboss.proxy.compiler.InvocationHandler;

import org.jboss.aspect.*;
import org.jboss.aspect.AspectDefinition;

/**
 * This class is used to generate most of the BECL code that
 * is used in the AspectProxyImplementationFactory.
 * 
 * When run, this class produces the BECL code the generates
 * this class.  AspectProxyImplementationFactory use the BECL
 * code generated for the constructor so that you can use
 * a default constructor to create instances of proxy classes.
 * 
 * The current BECL version in the jboss CVS is too old to 
 * run this program correctly, use a recent CVS snapshot of BECL
 * to get this guy running.
 * 
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
public class ProxyTemplate {

	// This field is used to emulate the invocationHandler
	// field found in the real Proxy Class that is generated
	// by the ProxyImplementationFactory.
	InvocationHandler invocationHandler;
	
	/**
	 * Constructor for Proxy.
	 */
	public ProxyTemplate() {
		
		ClassLoader c = getClass().getClassLoader();
		while( c!=null ) {
			if( c instanceof AspectClassLoader) {
				AspectClassLoader acl = (AspectClassLoader)c;
				AspectDefinition ac = acl.getAspectDefinition(getClass());
				invocationHandler = new AspectInvocationHandler(ac, ac.targetClass);
				return;
			}
			c = c.getParent();
		}
		throw new RuntimeException("ClassLoader structure invalid: Could not find a parent classloader of type AspectClassLoader");
	}
	
	static public void main( String args[] ) throws Exception {
		
		URL base = ProxyTemplate.class.getProtectionDomain().getCodeSource().getLocation();
		URL classFile = new URL(base, ProxyTemplate.class.getName().replace('.','/')+".class");
		System.out.println("Running BCELifier on: "+classFile.getFile());
		
		Class.forName("org.apache.bcel.util.BCELifier").
			getMethod("main", new Class[] {String[].class}).
			invoke(null, new Object[] { new String[]{classFile.getFile()} });
	}
}
