/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.jmx.service;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.net.MalformedURLException;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeErrorException;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.jmx.connector.RemoteMBeanServer;

/**
 * A JMX client to deploy an application into a running JBoss server.
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @version $Revision: 1.4 $
 */
public class Deployer
{
   /**
    * A command line driven interface to the deployer.
    * <BR>
    * Attention: option -server tells the deployer which
    * server to use if not the local one
    */
   public static void main(final String[] args)
      throws Exception
   {
      // Check for option "-server"
      String lServer = InetAddress.getLocalHost().getHostName();
      for( int i = 0; i < args.length; i++ ) {
         if( args[ i ].equals( "-server" ) && ( i + 1 ) < args.length ) {
            lServer = args[ i + 1 ];
         }
      }
      
      for (int count=0;count<args.length;count++) {            
         if (!args[count].equalsIgnoreCase("-undeploy")) { 
            System.out.println("Deploying " + args[count]);
            new Deployer( lServer ).deploy(args[count]);
            System.out.println(args[count] +
                               " has been deployed successfully");
         }
         else {
            System.out.println("Undeploying " + args[++count]);
            new Deployer( lServer ).undeploy(args[count]);
            System.out.println(args[count] +
                               " has been successfully undeployed");
         }
      }
   }

   private String mServerName;
   
   /**
    * Creates a deployer accessing the RMI Connector for
    * the given server
    *
    * @param pServerName Name of the server (how it is registered on
    *                    the JNDI server as second part of the name
    *                    (name spec is: "jmx:<server name>:rmi").
    */
   public Deployer( String pServerName ) {
      mServerName = pServerName;
   }
   
   /**
    * Deploys the app under the given url spec.
    *
    * @param url    The url spec of the application to deploy.
    *
    * @throws Exception    Failed to deploy application.
    */
   public void deploy(final String url) throws Exception
   {
      invoke(url, "deploy");
   }
	
   /**
    * Undeploys the app under the given url spec.
    *
    * @param url    The url spec of the application to undeploy.
    *
    * @throws Exception    Failed to undeploy application.
    */
   public void undeploy(final String url) throws Exception
   {
      invoke(url, "undeploy");
   }

   /**
    * Get the JMX object name of the factory to use.
    */
   protected ObjectName getFactoryName()
      throws MalformedObjectNameException
   {
      return new ObjectName("jboss.j2ee:service=J2eeDeployer");
   }
   
   /**
    * Create a url from the given application url spec.
    * <p>Will attempt to create a file:// url first.
    */
   protected URL createURL(final String url) throws MalformedURLException {
      if (new File(url).exists()) {
         return new File(url).toURL();
      }
      else {
         return new URL(url);
      }
   }

   /**
    * Lookup of RemoteMBeanServer factored out.
    */
   protected RemoteMBeanServer lookupConnector() throws NamingException {
      RemoteMBeanServer connector = null;
      InitialContext ctx = new InitialContext();
      try {
         connector = (RemoteMBeanServer) ctx.lookup( "jmx:" + mServerName + ":rmi" );
      }
      finally {
         ctx.close();
      }
      return connector;
   }
   
   /**
    * Invoke the target deployer and decode JMX exceptions that
    * are thrown.
    *
    * @param url      The raw url
    * @param method   Either "deploy" or "undeploy"
    *
    * @throws Exception    Server invoke failed.
    */
   protected void invoke(final String url, final String method)
      throws Exception
   {
      URL _url = createURL(url);
      // lookup the adapter to use
      RemoteMBeanServer server = lookupConnector();

      // get the deployer name to use
      ObjectName name = getFactoryName();

      // invoke the server and decode jmx exceptions
      try {
         server.invoke(name, method,
                       new Object[] { _url.toString() },
                       new String[] { "java.lang.String" });
      }
      catch (MBeanException e) {
         throw e.getTargetException();
      }
      catch (ReflectionException e) {
         throw e.getTargetException();
      }
      catch (RuntimeOperationsException e) {
         throw e.getTargetException();
      }
      catch (RuntimeMBeanException e) {
         throw e.getTargetException();
      }
      catch (RuntimeErrorException e) {
         throw e.getTargetError();
      }
   }
}
