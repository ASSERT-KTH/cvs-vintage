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
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.6 $
 */
public class Deployer
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
    public static void main(String[] args)
    throws Exception {
        for(int count=0;count<args.length;count++) {            
            if(!args[count].equalsIgnoreCase("-undeploy")) { 
                System.out.println("Deploying " + args[count]);
                new Deployer().deploy(args[count]);
                System.out.println(args[count] + " has been deployed successfully");
            } else {
                System.out.println("Undeploying " + args[++count]);
                new Deployer().undeploy(args[count]);
                System.out.println(args[count] + " has been successfully undeployed");
            }
        }
    }

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void deploy(String url)
		throws Exception
   {
	   ObjectName containerFactory = createFactoryName();

	   URL deploymentUrl =createUrl(url);
          
	   JMXAdaptor server = lookupAdaptor();
           
	   server.invoke(containerFactory, 
	   		 "deploy", 
                         new Object[] { deploymentUrl.toString() }, 
	   		 new String[] { "java.lang.String" });
   }
	
  /** undeploys the app under the given url spec
   *  @author cgjung 
   */
  public void undeploy(String url)
		throws Exception
   {
	   ObjectName containerFactory = createFactoryName();
           
	   URL deploymentUrl=createUrl(url);
          
	   JMXAdaptor server = lookupAdaptor();
           server.invoke(containerFactory, 
                         "undeploy", 
	   		 new Object[] { deploymentUrl.toString() }, 
	   		 new String[] { "java.lang.String" });
   }

   /** creation of objectname for the deployer
    *  factored out
    *  @author cgjung
    */
   protected ObjectName createFactoryName() throws javax.management.MalformedObjectNameException {
       return new ObjectName("J2EE:service=J2eeDeployer");
   }
   
   /** creation of url factored out
    *  @author cgjung
    */
   protected URL createUrl(String url) throws java.net.MalformedURLException {
       if (new File(url).exists())
           return new File(url).toURL();
       else
           return new URL(url);
   }
   
   /** lookup of JMXadaptor factored out
    *  @author cgjung
    */
   protected JMXAdaptor lookupAdaptor() throws javax.naming.NamingException {
        return (JMXAdaptor)new InitialContext().lookup("jmx");
   }
   

   // Protected -----------------------------------------------------
}

