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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeErrorException;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.jboss.jmx.connector.rmi.RMIConnectorImpl;
import org.jboss.jmx.connector.RemoteMBeanServer;

import org.jboss.deployment.MainDeployerMBean;
import org.jboss.deployment.DeploymentException;

import org.jboss.util.jmx.JMXExceptionDecoder;

import org.jboss.logging.Logger;

/**
 * A JMX client to deploy an application into a running JBoss server.
 *
 * @version <tt>$Revision: 1.7 $</tt>
 * @author  <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author  <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class Deployer
   implements org.jboss.deployment.Deployer, org.jboss.deployment.DeployerMBean
{
   /** Class logger. */
   private static final Logger log = Logger.getLogger(Deployer.class);
   
   /**
    * Name of the server (how it is registered on the JNDI server as second
    * part of the name (name spec is: "jmx:<server name>:rmi")).
    */
   protected String mServerName;
   
   /**
    * Creates a deployer accessing the RMI Connector for the given server.
    *
    * @param pServerName Name of the server (how it is registered on
    *                    the JNDI server as second part of the name
    *                    (name spec is: "jmx:<server name>:rmi").
    */
   public Deployer(final String pServerName)
   {
      mServerName = pServerName;
   }

   /**
    * Make a URL from the given URL spec.  Will handle protocol-less strings
    * as a file:// URL.
    */
   protected static URL makeURL(final String urlspec) throws MalformedURLException
   {
      URL url;
      
      try {
         url = new URL(urlspec);
      }
      catch (Exception e) {
         // make sure we have a absolute file url
         File file = new File(urlspec).getAbsoluteFile();
         url = file.toURL();
      }

      log.debug("Using URL: " + url);
      
      return url;
   }
   
   /**
    * Deploys the given url on the remote server.
    *
    * @param url    The url of the application to deploy.
    *
    * @throws DeploymentException   Failed to deploy application.
    */
   public void deploy(final URL url) throws DeploymentException
   {
      invoke("deploy", url);
   }

   /**
    * Deploys the given url on the remote server.
    *
    * @param url    The url of the application to deploy.
    *
    * @throws DeploymentException      Failed to deploy application.
    * @throws MalformedURLException    Invalid URL.
    */
   public void deploy(final String url) throws MalformedURLException, DeploymentException
   {
      deploy(makeURL(url));
   }
   
   /**
    * Undeploys the application specifed by the given url on the remote server.
    *
    * @param url    The url of the application to undeploy.
    *
    * @throws DeploymentException   Failed to undeploy application.
    */
   public void undeploy(final URL url) throws DeploymentException
   {
      invoke("undeploy", url);
   }

   /**
    * Undeploys the application specifed by the given url on the remote server.
    *
    * @param url    The url of the application to undeploy.
    *
    * @throws DeploymentException      Failed to undeploy application.
    * @throws MalformedURLException    Invalid URL.
    */
   public void undeploy(final String url) throws MalformedURLException, DeploymentException
   {
      undeploy(makeURL(url));
   }
   
   /**
    * Check if the given url is deployed on thr remote server.
    *
    * @param url    The url of the application to check.
    * @return       True if the application is deployed.
    */
   public boolean isDeployed(final URL url)
   {
      try {
         Boolean bool = (Boolean)invoke("isDeployed", url);
         return bool.booleanValue();
      }
      catch (Exception e) {
         return false;
      }
   }

   /**
    * Check if the given url is deployed on thr remote server.
    *
    * @param url    The url of the application to check.
    * @return       True if the application is deployed.
    *
    * @throws DeploymentException      Failed to determine if application is deployed.
    * @throws MalformedURLException    Invalid URL.
    */
   public boolean isDeployed(final String url) throws MalformedURLException
   {
      return isDeployed(makeURL(url));
   }
   
   /**
    * Get the JMX object name of the factory to use.
    *
    * @return   {@link MainDeployerMBean#OBJECT_NAME}
    */
   protected ObjectName getFactoryName()
      throws MalformedObjectNameException
   {
      return MainDeployerMBean.OBJECT_NAME;
   }

   /**
    * Lookup the RemoteMBeanServer which will be used to invoke methods on.
    *
    * @throws Exception   Failed to lookup connector reference or retruned reference
    *                     was not of type {@link RMIAdapter}.
    */
   protected RemoteMBeanServer lookupConnector() throws Exception
   {
      RemoteMBeanServer connector = null;
      InitialContext ctx = new InitialContext();
      
      try {
         Object lObject = ctx.lookup( "jmx:" + mServerName + ":rmi" );
         log.debug("RMI Adapter: " + lObject);
         
         if (!(lObject instanceof RMIAdaptor)) {
            throw new RuntimeException("Object not of type: RMIAdaptorImpl, but: " +
                                       (lObject == null ? "not found" : lObject.getClass().getName()));
         }
         
         connector = new RMIConnectorImpl((RMIAdaptor) lObject);
         log.debug("RMI Connector: " + connector);
      }
      finally {
         ctx.close();
      }
      
      return connector;
   }
   
   /**
    * Invoke the target deployer and decode JMX exceptions that are thrown.
    *
    * @param methodName    The menthod name of the target deployer to invoke.
    * @param url           The url to deploy/undeploy.
    * @return              The return value of the invocation.
    *
    * @throws DeploymentException    Server invoke failed (JMX exceptions are decoded).
    */
   protected Object invoke(final String methodName, final URL url)
      throws DeploymentException
   {
      try {
         // lookup the adapter to use
         RemoteMBeanServer server = lookupConnector();

         // get the deployer name to use
         ObjectName name = getFactoryName();

         // invoke the server and decode jmx exceptions
         return server.invoke(name, methodName,
                              new Object[] { url },
                              new String[] { "java.net.URL" });
      }
      catch (Exception e) {
         Throwable t = JMXExceptionDecoder.decode(e);
         if (t instanceof DeploymentException) {
            throw (DeploymentException)t;
         }
         throw new DeploymentException(t);
      }
   }


   /////////////////////////////////////////////////////////////////////////
   //                         Command Line Support                        //
   /////////////////////////////////////////////////////////////////////////

   public static final String PROGRAM_NAME = System.getProperty("program.name", "deployer");

   //
   // Switches equate to commands for the desired deploy/undeploy operation to execute
   //
   
   protected static abstract class DeployerCommand
   {
      protected String url;
      
      public abstract void execute(Deployer deployer) throws Exception;
   }

   protected static class DeployCommand
      extends DeployerCommand
   {
      public DeployCommand(final String url)
      {
         this.url = url;
      }

      public void execute(Deployer deployer) throws Exception
      {
         deployer.deploy(url);
         System.out.println(url + " has been deployed.");         
      }
   }

   protected static class UndeployCommand
      extends DeployerCommand
   {
      public UndeployCommand(final String url)
      {
         this.url = url;
      }

      public void execute(Deployer deployer) throws Exception
      {
         deployer.undeploy(url);
         System.out.println(url + " has been undeployed.");
      }
   }

   protected static class IsDeployedCommand
      extends DeployerCommand
   {
      public IsDeployedCommand(final String url)
      {
         this.url = url;
      }
      
      public void execute(Deployer deployer) throws Exception
      {
         boolean deployed = deployer.isDeployed(url);
         System.out.println(url + " is " + (deployed ? "deployed." : "not deployed."));
      }
   }

   protected static void displayUsage()
   {
      System.out.println("usage: " + PROGRAM_NAME + " [options] (operation)+");
      System.out.println();
      System.out.println("options:");
      System.out.println("    -h, --help                Show this help message");
      System.out.println("    -D<name>[=<value>]        Set a system property");
      System.out.println("    --                        Stop processing options");
      System.out.println("    -s, --server=<name>       Specify the hostname name of the server to use");
      System.out.println();
      System.out.println("operations:");
      System.out.println("    -d, --deploy=<url>        Deploy a URL into the remote server");
      System.out.println("    -u, --undeploy=<url>      Undeploy a URL from the remote server");
      System.out.println("    -i, --isdeployed=<url>    Check if a URL is deployed on the remote server");
      System.out.println();
   }

   public static void main(final String[] args) throws Exception
   {
      if (args.length == 0) {
         displayUsage();
         System.exit(0);
      }
      
      String sopts = "-:hD:s:d:u:i:";
      LongOpt[] lopts =
      {
         new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
         new LongOpt("server", LongOpt.REQUIRED_ARGUMENT, null, 's'),
         new LongOpt("deploy", LongOpt.REQUIRED_ARGUMENT, null, 'd'),
         new LongOpt("undeploy", LongOpt.REQUIRED_ARGUMENT, null, 'u'),
         new LongOpt("isdeployed", LongOpt.REQUIRED_ARGUMENT, null, 'i'),
      };

      Getopt getopt = new Getopt(PROGRAM_NAME, args, sopts, lopts);
      int code;
      String arg;

      String serverName = InetAddress.getLocalHost().getHostName();
      List commands = new ArrayList();
      
      while ((code = getopt.getopt()) != -1)
      {
         switch (code)
         {
            case ':':
            case '?':
               // for now both of these should exit with error status
               System.exit(1);
               break; // for completeness
               
            case 1:
               // this will catch non-option arguments
               // (which we don't currently care about)
               System.err.println(PROGRAM_NAME + ": unused non-option argument: " +
                                  getopt.getOptarg());
               break; // for completeness
               
            case 'h':
               // show command line help
               displayUsage();
               System.exit(0);
               break; // for completeness
               
            case 'D':
            {
               // set a system property
               arg = getopt.getOptarg();
               String name, value;
               int i = arg.indexOf("=");
               if (i == -1)
               {
                  name = arg;
                  value = "true";
               }
               else
               {
                  name = arg.substring(0, i);
                  value = arg.substring(i + 1, arg.length());
               }
               System.setProperty(name, value);
               break;
            }

            case 's':
            {
               serverName = getopt.getOptarg();
               break;
            }
            
            case 'd':
            {
               commands.add(new DeployCommand(getopt.getOptarg()));
               break;
            }

            case 'u':
            {
               commands.add(new UndeployCommand(getopt.getOptarg()));
               break;
            }

            case 'i':
            {
               commands.add(new IsDeployedCommand(getopt.getOptarg()));
               break;               
            }

            default:
               // this should not happen,
               // if it does throw an error so we know about it
               throw new Error("unhandled option code: " + code);
         }
      }

      // setup the deployer
      Deployer deployer = new Deployer(serverName);
      
      // now execute all of the commands
      Iterator iter = commands.iterator();
      while (iter.hasNext()) {
         DeployerCommand command = (DeployerCommand)iter.next();
         command.execute(deployer);
      }
   }
}
