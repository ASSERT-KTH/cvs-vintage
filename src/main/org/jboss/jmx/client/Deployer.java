/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.jmx.client;

import java.io.File;
import java.net.URL;

import javax.management.ObjectName;
import javax.naming.InitialContext;

import org.jboss.jmx.interfaces.JMXAdaptor;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard �berg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.5 $
 */
public class Deployer
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
	public static void main(String[] args)
		throws Exception
	{
		System.out.println("Deploying " + args[0]);		
		new Deployer().deploy(args[0]);
		System.out.println(args[0] + " has been deployed successfully");		
	}

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void deploy(String url)
		throws Exception
   {
	   ObjectName containerFactory = new ObjectName("J2EE:service=J2eeDeployer");

	   URL deploymentUrl;
	   if (new File(url).exists())
	   	deploymentUrl = new File(url).toURL();
	   else
	   	deploymentUrl = new URL(url);

	   JMXAdaptor server = (JMXAdaptor)new InitialContext().lookup("jmx");
	   server.invoke(containerFactory, 
	   					"deploy", 
	   					new Object[] { deploymentUrl.toString() }, 
	   					new String[] { "java.lang.String" });
   }
	
   // Protected -----------------------------------------------------
}

