/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.jmx.client;

import java.util.Iterator;

import javax.management.ObjectInstance;
import javax.naming.InitialContext;

import org.jboss.jmx.interfaces.JMXAdaptor;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.3 $
 */
public class Stop
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
	public static void main(String[] args)
		throws Exception
	{
		System.out.println("Stopping server");		
		new Stop().stop();
		System.out.println("Server has been successfully stopped");		
	}

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void stop()
		throws Exception
   {
	   JMXAdaptor server = (JMXAdaptor)new InitialContext().lookup("jmx");
		Iterator enum = server.queryMBeans(null, null).iterator();
		while (enum.hasNext())
		{
			ObjectInstance oi = (ObjectInstance)enum.next();
			try
			{
				server.invoke(oi.getObjectName(), "stop", new Object[0], new String[0]);
			} catch (Exception e)
			{
				// Ignore
			}
		}
   }
	
   // Protected -----------------------------------------------------
}

