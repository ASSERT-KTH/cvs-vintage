/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.jmx.client;

import java.io.File;
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

import org.jboss.jmx.interfaces.JMXAdaptor;

/**
 * A JMX client to deploy an application into a running JBoss server.
 *      
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @version $Revision: 1.9 $
 */
public class Deployer
{
   /**
    * A command line driven interface to the deployer.
    */
   public static void main(final String[] args)
      throws Exception {
      for (int count=0;count<args.length;count++) {            
         if (!args[count].equalsIgnoreCase("-undeploy")) { 
            System.out.println("Deploying " + args[count]);
            new Deployer().deploy(args[count]);
            System.out.println(args[count] +
                               " has been deployed successfully");
         } else {
            System.out.println("Undeploying " + args[++count]);
            new Deployer().undeploy(args[count]);
            System.out.println(args[count] +
                               " has been successfully undeployed");
         }
      }
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
      return new ObjectName("J2EE:service=J2eeDeployer");
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
    * Lookup of JMXadaptor factored out.
    */
   protected JMXAdaptor lookupAdaptor() throws NamingException {
      JMXAdaptor adapter;
      InitialContext ctx = new InitialContext();
      try {
         adapter = (JMXAdaptor)ctx.lookup("jmx");
      }
      finally {
         ctx.close();
      }
      
      return adapter;
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
      JMXAdaptor server = lookupAdaptor();

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

