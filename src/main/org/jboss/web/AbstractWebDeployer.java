/*
 * JBoss, the OpenSource J2EE WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.web;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.security.Policy;
import javax.management.MBeanServer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.NamingException;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebUserDataPermission;
import javax.security.jacc.WebRoleRefPermission;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.J2eeApplicationMetaData;
import org.jboss.ejb.Container;
import org.jboss.ejb.EjbUtil;
import org.jboss.logging.Logger;
import org.jboss.metadata.EjbLocalRefMetaData;
import org.jboss.metadata.EjbRefMetaData;
import org.jboss.metadata.EnvEntryMetaData;
import org.jboss.metadata.MessageDestinationMetaData;
import org.jboss.metadata.MessageDestinationRefMetaData;
import org.jboss.metadata.ResourceEnvRefMetaData;
import org.jboss.metadata.ResourceRefMetaData;
import org.jboss.metadata.WebMetaData;
import org.jboss.metadata.WebSecurityMetaData;
import org.jboss.metadata.SecurityRoleRefMetaData;
import org.jboss.metadata.WebSecurityMetaData.WebResourceCollection;
import org.jboss.mx.loading.LoaderRepositoryFactory;
import org.jboss.naming.NonSerializableFactory;
import org.jboss.naming.Util;
import org.jboss.web.AbstractWebContainer.WebDescriptorParser;
import org.jboss.webservice.WebServiceClientHandler;
import org.omg.CORBA.ORB;

/** A template pattern class for web deployer integration into JBoss. This class
should be subclasses by war deployers providers wishing to integrate into
a JBoss server.

It provides support for mapping the following web-app.xml/jboss-web.xml elements
into the JBoss server JNDI namespace:
- env-entry
- resource-ref
- resource-env-ref
- ejb-ref
- ejb-local-ref
- security-domain

Subclasses need to implement the {@link #performDeploy(WebApplication, String,
 WebDescriptorParser) performDeploy()}
and {@link #performUndeploy(String, WebApplication) performUndeploy()} methods to perform the
container specific steps and return the web application info required by the
AbstractWebContainer class.

Integration with the JBossSX security framework is based on the establishment
of a java:comp/env/security context as described in the
{@link #linkSecurityDomain(String,Context) linkSecurityDomain } comments.
The security context provides access to the JBossSX security mgr interface
implementations for use by subclass request interceptors. A outline of the
steps for authenticating a user is:
<code>
   // Get the username & password from the request context...
   String username = f(request);
   String password = f(request);
   // Get the JBoss security manager from the ENC context
   InitialContext iniCtx = new InitialContext();
   SecurityManager securityMgr = (SecurityManager) iniCtx.lookup("java:comp/env/security/securityMgr");
   SimplePrincipal principal = new SimplePrincipal(username);
   if( securityMgr.isValid(principal, password) )
   {
   // Indicate the user is allowed access to the web content...

   // Propagate the user info to JBoss for any calls into made by the servlet
   SecurityAssociation.setPrincipal(principal);
   SecurityAssociation.setCredential(password.toCharArray());
   }
   else
   {
   // Deny access...
   }
</code>

An outline of the steps for authorizing the user is:
<code>
   // Get the username & required roles from the request context...
   String username = f(request);
   String[] roles = f(request);
   // Get the JBoss security manager from the ENC context
   InitialContext iniCtx = new InitialContext();
   RealmMapping securityMgr = (RealmMapping) iniCtx.lookup("java:comp/env/security/realmMapping");
   SimplePrincipal principal = new SimplePrincipal(username);
   Set requiredRoles = new HashSet(Arrays.asList(roles));
   if( securityMgr.doesUserHaveRole(principal, requiredRoles) )
   {
   // Indicate the user has the required roles for the web content...
   }
   else
   {
   // Deny access...
   }
</code>

The one thing to be aware of is the relationship between the thread context
class loader and the JNDI ENC context. Any method that attempts to access
the JNDI ENC context must have the ClassLoader in the WebApplication returned
from the {@link #performDeploy(WebApplication, String, WebDescriptorParser) performDeploy} as its thread
context ClassLoader or else the lookup for java:comp/env will fail with a
name not found exception, or worse, it will receive some other web application
ENC context. If your adapting a web container that is trying be compatible with
both 1.1 and 1.2 Java VMs this is something you need to pay special attention
to. For example, I have seen problems a request interceptor that was handling
the authentication/authorization callouts in tomcat3.2.1 not having the same
thread context ClassLoader as was used to dispatch the http service request.

@see #performDeploy(WebApplication webApp, String warUrl,
        WebDescriptorParser webAppParser)
@see #performUndeploy(String, WebApplication)
@see #parseWebAppDescriptors(DeploymentInfo,ClassLoader, WebMetaData)
@see #linkSecurityDomain(String, Context)
@see org.jboss.security.RealmMapping;
@see org.jboss.security.SimplePrincipal;
@see org.jboss.security.SecurityAssociation;

@jmx.mbean
   name="jboss.web:service=WebServer"
   extends="org.jboss.deployment.SubDeployerMBean"

@author  Scott.Stark@jboss.org
@version $Revision: 1.24 $
*/
public abstract class AbstractWebDeployer
{
   public static final String ERROR = "org.jboss.web.AbstractWebContainer.error";
   protected Logger log;

   protected MBeanServer server;
   /** The parent class loader first model flag */
   protected boolean java2ClassLoadingCompliance = false;
   /** A flag indicating if war archives should be unpacked */
   protected boolean unpackWars = true;
   /** If true, ejb-links that don't resolve don't cause an error (fallback to jndi-name) */
   protected boolean lenientEjbLink = false;
   /** The default security-domain name to use */
   protected String defaultSecurityDomain;

   public AbstractWebDeployer()
   {
      log = Logger.getLogger(getClass());
   }

   public abstract void init(Object containerConfig) throws Exception;

   public MBeanServer getServer()
   {
      return server;
   }
   public void setServer(MBeanServer server)
   {
      this.server = server;
   }

   /** Get the flag indicating if the normal Java2 parent first class loading
    * model should be used over the servlet 2.3 web container first model.
    * @return true for parent first, false for the servlet 2.3 model
    * @jmx.managed-attribute
    */
   public boolean getJava2ClassLoadingCompliance()
   {
      return java2ClassLoadingCompliance;
   }
   /** Set the flag indicating if the normal Java2 parent first class loading
    * model should be used over the servlet 2.3 web container first model.
    * @param flag true for parent first, false for the servlet 2.3 model
    * @jmx.managed-attribute
    */
   public void setJava2ClassLoadingCompliance(boolean flag)
   {
      java2ClassLoadingCompliance = flag;
   }

   /** Set the flag indicating if war archives should be unpacked. This may
    * need to be set to false as long extraction paths under deploy can
    * show up as deployment failures on some platforms.
    * 
    * @jmx.managed-attribute
    * @return true is war archives should be unpacked
    */ 
   public boolean getUnpackWars()
   {
      return unpackWars;
   }
   /** Get the flag indicating if war archives should be unpacked. This may
    * need to be set to false as long extraction paths under deploy can
    * show up as deployment failures on some platforms.
    * 
    * @jmx.managed-attribute
    * @param flag , true is war archives should be unpacked
    */ 
   public void setUnpackWars(boolean flag)
   {
      this.unpackWars = flag;
   }


    /**
     * Get the flag indicating if ejb-link errors should be ignored
     * in favour of trying the jndi-name in jboss-web.xml
     * @return a <code>boolean</code> value
     *    
     * @jmx.managed-attribute
     */
    public boolean getLenientEjbLink ()
    {
        return lenientEjbLink;
    }    
    /**
     * Set the flag indicating if ejb-link errors should be ignored
     * in favour of trying the jndi-name in jboss-web.xml
     * @jmx.managed-attribute
     */    
    public void setLenientEjbLink (boolean flag)
    {
        lenientEjbLink = flag;
    }

   /** Get the default security domain implementation to use if a war
    * does not declare a security-domain.
    *
    * @return jndi name of the security domain binding to use.
    * @jmx.managed-attribute
    */
   public String getDefaultSecurityDomain()
   {
      return defaultSecurityDomain;
   }
   /** Set the default security domain implementation to use if a war
    * does not declare a security-domain.
    *
    * @param defaultSecurityDomain - jndi name of the security domain binding
    * to use.
    * @jmx.managed-attribute
    */
   public void setDefaultSecurityDomain(String defaultSecurityDomain)
   {
      this.defaultSecurityDomain = defaultSecurityDomain;
   }

    public boolean accepts(DeploymentInfo sdi)
    {
        String warFile = sdi.url.getFile();
        return warFile.endsWith("war") || warFile.endsWith("war/");
    }

   /** WARs do not have nested deployments
    * @param di
    */
   protected void processNestedDeployments(DeploymentInfo di)
   {
   }

   /** A template pattern implementation of the deploy() method. This method
   calls the {@link #performDeploy(WebApplication, String, WebDescriptorParser) performDeploy()} method to
   perform the container specific deployment steps and registers the
   returned WebApplication in the deployment map. The steps performed are:

      ClassLoader appClassLoader = thread.getContextClassLoader();
      URLClassLoader warLoader = URLClassLoader.newInstance(empty, appClassLoader);
      thread.setContextClassLoader(warLoader);
      WebDescriptorParser webAppParser = ...;
      WebMetaData metaData = di.metaData;
      // Create JACC permissions, contextID, etc. ...
      WebApplication warInfo = new WebApplication(metaData);
      performDeploy(warInfo, warUrl, webAppParser);
      deploymentMap.put(warUrl, warInfo);
      thread.setContextClassLoader(appClassLoader);

   The subclass performDeploy() implementation needs to invoke
   webAppParser.parseWebAppDescriptors(loader, warInfo) to have the JNDI
   java:comp/env namespace setup before any web app component can access
   this namespace.

    Also, an MBean for each servlet deployed should be created and its
    JMX ObjectName placed into the DeploymentInfo.mbeans list so that the
    JSR77 layer can create the approriate model view. The servlet MBean
    needs to provide access to the min, max and total time in milliseconds.
    Expose this information via MinServiceTime, MaxServiceTime and TotalServiceTime
    attributes to integrate seemlessly with the JSR77 factory layer.

   @param di The deployment info that contains the context-root element value
    from the J2EE application/module/web application.xml descriptor. This may
    be null if war was is not being deployed as part of an enterprise application.
    It also contains the URL of the web application war.
   */
   public synchronized WebApplication start(DeploymentInfo di) throws DeploymentException
   {
      Thread thread = Thread.currentThread();
      ClassLoader appClassLoader = thread.getContextClassLoader();
      WebApplication warInfo = null;
      try
      {
         // Create a classloader for the war to ensure a unique ENC
         URL[] empty = {};
         URLClassLoader warLoader = URLClassLoader.newInstance(empty, di.ucl);
         thread.setContextClassLoader(warLoader);
         WebDescriptorParser webAppParser = new DescriptorParser(di);
         String webContext = di.webContext;
         if (webContext != null && webContext.startsWith("/") == false)
            webContext = "/" + webContext;

         // Get the war URL
         URL warURL = di.localUrl != null ? di.localUrl : di.url;

         if (log.isDebugEnabled())
         {
            log.debug("webContext: " + webContext);
            log.debug("warURL: " + warURL);
            log.debug("webAppParser: " + webAppParser);
         }

         // Get the web.xml and jboss-web.xml descriptor metadata
         WebMetaData webMetaData = (WebMetaData) di.metaData;

         // inherit the security setup from jboss-app.xml
         if (di.parent != null && di.parent.metaData instanceof J2eeApplicationMetaData)
         {
            J2eeApplicationMetaData appMetaData = (J2eeApplicationMetaData)di.parent.metaData;

            if (webMetaData.getSecurityDomain() == null)
               webMetaData.setSecurityDomain(appMetaData.getSecurityDomain());

            webMetaData.mergeSecurityRoles(appMetaData.getSecurityRoles());
         }

         // Register the permissions with the JACC layer
         String contextID = di.shortName;
         if( contextID == null )
            contextID = di.shortName;
         webMetaData.setJaccContextID(contextID);
         PolicyConfigurationFactory pcFactory = PolicyConfigurationFactory.getPolicyConfigurationFactory();
         PolicyConfiguration pc = pcFactory.getPolicyConfiguration(contextID, true);
         createPermissions(webMetaData, pc);
         // Link this to the parent PC
         DeploymentInfo current = di;
         while( current.parent != null )
            current = current.parent;
         PolicyConfiguration parentPC = (PolicyConfiguration)
            current.context.get("javax.security.jacc.PolicyConfiguration");
         if( parentPC != null && parentPC != pc )
            parentPC.linkConfiguration(pc);

         // Commit the policy configuration
         pc.commit();
         // Allow the policy to incorporate the policy configs
         Policy.getPolicy().refresh();
         
         warInfo = new WebApplication(webMetaData);
         warInfo.setDeploymentInfo(di);
         warInfo.setClassLoader(warLoader);
         performDeploy(warInfo, warURL.toString(), webAppParser);
      }
      catch(DeploymentException e)
      {
         di.context.put(ERROR, e);
         throw e;
      }
      catch(Exception e)
      {
         DeploymentException ex = new DeploymentException("Error during deploy", e);
         di.context.put(ERROR, ex);
         throw ex;
      }
      finally
      {
         thread.setContextClassLoader(appClassLoader);
      }
      return warInfo;
   }

   /** This method is called by the deploy() method template and must be overriden by
   subclasses to perform the web container specific deployment steps.
   @param webApp The web application information context. This contains the
    metadata such as the context-root element value from the J2EE
   application/module/web application.xml descriptor and virtual-host.
   @param warUrl The string for the URL of the web application war.
   @param webAppParser The callback interface the web container should use to
   setup the web app JNDI environment for use by the web app components. This
   needs to be invoked after the web app class loader is known, but before
   and web app components attempt to access the java:comp/env JNDI namespace.
   */
   protected abstract void performDeploy(WebApplication webApp, String warUrl,
      WebDescriptorParser webAppParser) throws Exception;

   /** A template pattern implementation of the undeploy() method. This method
   calls the {@link #performUndeploy(String, WebApplication) performUndeploy()} method to
   perform the container specific undeployment steps and unregisters the
   the warUrl from the deployment map.
   */
   public synchronized void stop(DeploymentInfo di)
      throws DeploymentException
   {
      URL warURL = di.localUrl != null ? di.localUrl : di.url;
      String warUrl = warURL.toString();
      try
      {
         WebApplication webApp = (WebApplication) di.context.get(AbstractWebContainer.WEB_APP);
         performUndeploy(warUrl, webApp);
         // Unegister the permissions with the JACC layer
         WebMetaData webMetaData = (WebMetaData) di.metaData;
         String contextID = webMetaData.getJaccContextID();
         PolicyConfigurationFactory pcFactory = PolicyConfigurationFactory.getPolicyConfigurationFactory();
         PolicyConfiguration pc = pcFactory.getPolicyConfiguration(contextID, true);
         pc.delete();
      }
      catch(DeploymentException e)
      {
         throw e;
      }
      catch(Exception e)
      {
         throw new DeploymentException("Error during deploy", e);
      }
   }

   /** Called as part of the undeploy() method template to ask the
   subclass for perform the web container specific undeployment steps.
   */
   protected abstract void performUndeploy(String warUrl, WebApplication webApp)
      throws Exception;

   /** This method is invoked from within subclass performDeploy() method
   implementations when they invoke WebDescriptorParser.parseWebAppDescriptors().

   @param loader the ClassLoader for the web application. May not be null.
   @param metaData the WebMetaData from the WebApplication object passed to
    the performDeploy method.
   */
   protected void parseWebAppDescriptors(DeploymentInfo di, ClassLoader loader,
      WebMetaData metaData)
      throws Exception
   {
      log.debug("AbstractWebContainer.parseWebAppDescriptors, Begin");
      InitialContext iniCtx = new InitialContext();
      Context envCtx = null;
      Thread currentThread = Thread.currentThread();
      ClassLoader currentLoader = currentThread.getContextClassLoader();
      try
      {
         // Create a java:comp/env environment unique for the web application
         log.debug("Creating ENC using ClassLoader: "+loader);
         ClassLoader parent = loader.getParent();
         while( parent != null )
         {
            log.debug(".."+parent);
            parent = parent.getParent();
         }
         // TODO: Where does this ENC get tidied up?
         currentThread.setContextClassLoader(loader);
         metaData.setENCLoader(loader);
         envCtx = (Context) iniCtx.lookup("java:comp");

         ORB orb = null;
         try
         {
            orb = (ORB) server.getAttribute(Container.ORB_NAME, "ORB");
         }
         catch (Throwable t)
         {
            log.debug("Unable to retrieve orb" + t.toString());
         }

         // Bind the orb
         if (orb != null)
         {
            NonSerializableFactory.rebind(envCtx, "ORB", orb);
            log.debug("Bound java:comp/ORB");
         }
         
         // Add a link to the global transaction manager
         envCtx.bind("UserTransaction", new LinkRef("UserTransaction"));
         log.debug("Linked java:comp/UserTransaction to JNDI name: UserTransaction");
         envCtx = envCtx.createSubcontext("env");
      }
      finally
      {
         currentThread.setContextClassLoader(currentLoader);
      }

      Iterator envEntries = metaData.getEnvironmentEntries();
      log.debug("addEnvEntries");
      addEnvEntries(envEntries, envCtx);
      Iterator resourceEnvRefs = metaData.getResourceEnvReferences();
      log.debug("linkResourceEnvRefs");
      linkResourceEnvRefs(resourceEnvRefs, envCtx);
      Iterator resourceRefs = metaData.getResourceReferences();
      log.debug("linkResourceRefs");
      linkResourceRefs(resourceRefs, envCtx);
      log.debug("linkMessageDestinationRefs");
      linkMessageDestinationRefs(metaData, envCtx, di);
      Iterator ejbRefs = metaData.getEjbReferences();
      log.debug("linkEjbRefs");
      linkEjbRefs(ejbRefs, envCtx, di);
      Iterator ejbLocalRefs = metaData.getEjbLocalReferences();
      log.debug("linkEjbLocalRefs");
      linkEjbLocalRefs(ejbLocalRefs, envCtx, di);
      Iterator serviceRefs = metaData.getServiceReferences();
      log.debug("linkServiceRefs");
      WebServiceClientHandler.setupServiceRefEnvironment(envCtx, serviceRefs, di);
      String securityDomain = metaData.getSecurityDomain();
      log.debug("linkSecurityDomain");
      linkSecurityDomain(securityDomain, envCtx);
      log.debug("AbstractWebContainer.parseWebAppDescriptors, End");
   }

   protected void addEnvEntries(Iterator envEntries, Context envCtx)
      throws ClassNotFoundException, NamingException
   {
      while( envEntries.hasNext() )
      {
         EnvEntryMetaData entry = (EnvEntryMetaData) envEntries.next();
            log.debug("Binding env-entry: "+entry.getName()+" of type: " +
                      entry.getType()+" to value:"+entry.getValue());
         EnvEntryMetaData.bindEnvEntry(envCtx, entry);
      }
   }

   protected void linkResourceEnvRefs(Iterator resourceEnvRefs, Context envCtx)
      throws NamingException
   {
      while( resourceEnvRefs.hasNext() )
      {
         ResourceEnvRefMetaData ref = (ResourceEnvRefMetaData) resourceEnvRefs.next();
         String resourceName = ref.getJndiName();
         String refName = ref.getRefName();
         if( ref.getType().equals("java.net.URL") )
         {
             try
             {
                 log.debug("Binding '"+refName+"' to URL: "+resourceName);
                 URL url = new URL(resourceName);
                 Util.bind(envCtx, refName, url);
             }
             catch(MalformedURLException e)
             {
                 throw new NamingException("Malformed URL:"+e.getMessage());
             }
         }
         else if( resourceName != null )
         {
            log.debug("Linking '"+refName+"' to JNDI name: "+resourceName);
            Util.bind(envCtx, refName, new LinkRef(resourceName));
         }
         else
         {
            throw new NamingException("resource-env-ref: "+refName
               +" has no valid JNDI binding. Check the jboss-web/resource-env-ref.");
         }
      }
   }

   protected void linkResourceRefs(Iterator resourceRefs, Context envCtx)
      throws NamingException
   {
      while( resourceRefs.hasNext() )
      {
         ResourceRefMetaData ref = (ResourceRefMetaData) resourceRefs.next();
         String jndiName = ref.getJndiName();
         String refName = ref.getRefName();
         if( ref.getType().equals("java.net.URL") )
         {
             try
             {
                String resURL = ref.getResURL();
                 if( ref.getResURL() != null )
                 {
                   log.debug("Binding '"+refName+"' to URL: "+resURL);
                   URL url = new URL(resURL);
                   Util.bind(envCtx, refName, url);
                 }
                 else
                 {
                    log.debug("Linking '"+refName+"' to URL: "+resURL);
                    LinkRef urlLink = new LinkRef(jndiName);
                    Util.bind(envCtx, refName, urlLink);
                 }
             }
             catch(MalformedURLException e)
             {
                 throw new NamingException("Malformed URL:"+e.getMessage());
             }
         }
         else if( jndiName != null )
         {
             log.debug("Linking '"+refName+"' to JNDI name: "+jndiName);
             Util.bind(envCtx, refName, new LinkRef(jndiName));
         }
         else
         {
            throw new NamingException("resource-ref: "+refName
               +" has no valid JNDI binding. Check the jboss-web/resource-ref.");
         }
      }
   }

   protected void linkMessageDestinationRefs(WebMetaData metaData, Context envCtx, DeploymentInfo di)
      throws NamingException, DeploymentException
   {
      Iterator enum = metaData.getMessageDestinationReferences();

      while (enum.hasNext())
      {
         MessageDestinationRefMetaData ref = (MessageDestinationRefMetaData) enum.next();

         String refName = ref.getRefName();
         String jndiName = ref.getJNDIName();
         String link = ref.getLink();
         if (link != null)
         {
            if (jndiName == null)
            {
               MessageDestinationMetaData messageDestination = EjbUtil.findMessageDestination(server, di, link);
               if (messageDestination == null)
                  throw new DeploymentException("message-destination-ref '" + refName + 
                     "' message-destination-link '" + link + "' not found and no jndi-name in jboss-web.xml");
               else
               {
                  String linkJNDIName = messageDestination.getJNDIName();
                  if (linkJNDIName == null)
                     log.warn("message-destination '" + link + "' has no jndi-name in jboss-web.xml");
                  else
                     jndiName = linkJNDIName;
               }
            }
            else
               log.warn("message-destination-ref '" + refName + 
                  "' ignoring message-destination-link '" + link + "' because it has a jndi-name in jboss-web.xml");
         }
         else if (jndiName == null)
            throw new DeploymentException("message-destination-ref '" + refName + 
                  "' has no message-destination-link in web.xml and no jndi-name in jboss-web.xml");
         Util.bind(envCtx, refName, new LinkRef(jndiName));
      }
   }

   protected void linkEjbRefs(Iterator ejbRefs, Context envCtx, DeploymentInfo di)
      throws NamingException
   {
      while( ejbRefs.hasNext() )
      {
         EjbRefMetaData ejb = (EjbRefMetaData) ejbRefs.next();
         String name = ejb.getName();
         String linkName = ejb.getLink();
         String jndiName = null;

         //use ejb-link if it is specified
         if ( linkName != null )
         {
             jndiName = EjbUtil.findEjbLink(server, di, linkName);
             
             //if flag does not allow misconfigured ejb-links, it is an error
             if ( ( jndiName == null ) && !(getLenientEjbLink()) )
                 throw new NamingException("ejb-ref: "+name+", no ejb-link match");
         }

         
         //fall through to the jndiName
         if ( jndiName == null )
         { 
             jndiName = ejb.getJndiName();
             if (jndiName == null )
                 throw new NamingException("ejb-ref: "+name+", no ejb-link in web.xml and no jndi-name in jboss-web.xml");
         }
         
         log.debug("Linking ejb-ref: "+name+" to JNDI name: "+jndiName);
         Util.bind(envCtx, name, new LinkRef(jndiName));
      }
   }

   protected void linkEjbLocalRefs(Iterator ejbRefs, Context envCtx, DeploymentInfo di)
      throws NamingException
   {
      while( ejbRefs.hasNext() )
      {
         EjbLocalRefMetaData ejb = (EjbLocalRefMetaData) ejbRefs.next();
         String name = ejb.getName();
         String linkName = ejb.getLink();
         String jndiName = null;

         //use the ejb-link field if it is specified
         if ( linkName != null )
         {
             jndiName = EjbUtil.findLocalEjbLink(server, di, linkName);
             
             //if flag does not allow misconfigured ejb-links, it is an error    
             if ( ( jndiName == null ) && !(getLenientEjbLink()) )
                 throw new NamingException("ejb-ref: "+name+", no ejb-link match");
         }

         
         if (jndiName == null)
         {
             jndiName = ejb.getJndiName();
             if ( jndiName == null )
             {
                String msg = null;
                if( linkName == null )
                {
                  msg = "ejb-local-ref: '"+name+"', no ejb-link in web.xml and "
                   + "no local-jndi-name in jboss-web.xml";
                }
                else
                {
                   msg = "ejb-local-ref: '"+name+"', with web.xml ejb-link: '"
                   + linkName + "' failed to resolve to an ejb with a LocalHome";
                }
                throw new NamingException(msg);
             }
         }

         log.debug("Linking ejb-local-ref: "+name+" to JNDI name: "+jndiName);
         Util.bind(envCtx, name, new LinkRef(jndiName));
      }
   }

   /** This creates a java:comp/env/security context that contains a
   securityMgr binding pointing to an AuthenticationManager implementation
   and a realmMapping binding pointing to a RealmMapping implementation.
   If the jboss-web.xml descriptor contained a security-domain element
   then the bindings are LinkRefs to the jndi name specified by the
   security-domain element. If there was no security-domain element then
   the bindings are to NullSecurityManager instance which simply allows
   all access.
   */
   protected void linkSecurityDomain(String securityDomain, Context envCtx)
      throws NamingException
   {
      if( securityDomain == null )
      {
         securityDomain = getDefaultSecurityDomain();
         log.debug("No security-domain given, using default: "+securityDomain);
      }
      log.debug("Linking security/securityMgr to JNDI name: "+securityDomain);
      Util.bind(envCtx, "security/securityMgr", new LinkRef(securityDomain));
      Util.bind(envCtx, "security/realmMapping", new LinkRef(securityDomain));
      Util.bind(envCtx, "security/security-domain", new LinkRef(securityDomain));
      Util.bind(envCtx, "security/subject", new LinkRef(securityDomain+"/subject"));
   }

   /** A utility method that searches the given loader for the
    resources: "javax/servlet/resources/web-app_2_3.dtd",
    "org/apache/jasper/resources/jsp12.dtd", and "javax/ejb/EJBHome.class"
    and returns an array of URL strings. Any jar: urls are reduced to the
    underlying <url> portion of the 'jar:<url>!/{entry}' construct.
    */
   public String[] getStandardCompileClasspath(ClassLoader loader)
   {
      String[] jspResources = {
         "javax/servlet/resources/web-app_2_3.dtd",
         "org/apache/jasper/resources/jsp12.dtd",
         "javax/ejb/EJBHome.class"
      };
      ArrayList tmp = new ArrayList();
      for(int j = 0; j < jspResources.length; j ++)
      {
         URL rsrcURL = loader.getResource(jspResources[j]);
         if( rsrcURL != null )
         {
            String url = rsrcURL.toExternalForm();
            if( rsrcURL.getProtocol().equals("jar") )
            {
               // Parse the jar:<url>!/{entry} URL
               url = url.substring(4);
               int seperator = url.indexOf('!');
               url = url.substring(0, seperator);
            }
            tmp.add(url);
         }
         else
         {
            log.warn("Failed to fin jsp rsrc: "+jspResources[j]);
         }
      }
      log.trace("JSP StandardCompileClasspath: " + tmp);
      String[] cp = new String[tmp.size()];
      tmp.toArray(cp);
      return cp;
   }

   /** A utility method that walks up the ClassLoader chain starting at
    the given loader and queries each ClassLoader for a 'URL[] getURLs()'
    method from which a complete classpath of URL strings is built.
    */
   public String[] getCompileClasspath(ClassLoader loader)
   {
      HashSet tmp = new HashSet();
      ClassLoader cl = loader;
      while( cl != null )
      {
         URL[] urls = AbstractWebContainer.getClassLoaderURLs(cl);
         addURLs(tmp, urls);
         cl = cl.getParent();
      }
      try
      {
         URL[] globalUrls = (URL[])server.getAttribute(LoaderRepositoryFactory.DEFAULT_LOADER_REPOSITORY,
                                                         "URLs");
         addURLs(tmp, globalUrls);
      }
      catch (Exception e)
      {
         log.warn("Could not get global URL[] from default loader repository!", e);
      } // end of try-catch
      log.trace("JSP CompileClasspath: " + tmp);
      String[] cp = new String[tmp.size()];
      tmp.toArray(cp);
      return cp;
   }

   private void addURLs(Set urlSet, URL[] urls)
   {
      for(int u = 0; u < urls.length; u ++)
      {
         URL url = urls[u];
         urlSet.add(url.toExternalForm());
      }
   }

   /** Create the JACC permission based on the security constraints obtained
    * from the web.xml metadata.
    * 
    * @param metaData
    * @param pc
    * @throws PolicyContextException
    */ 
   protected void createPermissions(WebMetaData metaData, PolicyConfiguration pc)
      throws PolicyContextException
   {
      Iterator constraints = metaData.getSecurityContraints();
      // The url pattern to http methods for patterns for WebResourcePermiss
      HashMap patternsWithHttpMethodSubsetsWRP = new HashMap();
      // The url pattern to http methods for patterns for WebUserDataPermiss
      HashMap patternsWithHttpMethodSubsetsWUDP = new HashMap();
      while( constraints.hasNext() )
      {
         WebSecurityMetaData wsmd = (WebSecurityMetaData) constraints.next();
         String transport = wsmd.getTransportGuarantee();
         if( transport != null && transport.equalsIgnoreCase("NONE") )
            transport = null;
         if( wsmd.isExcluded() || wsmd.isUnchecked() )
         {
            // Build the permissions for the excluded/unchecked resources
            Iterator resources = wsmd.getWebResources().values().iterator();
            while( resources.hasNext() )
            {
               WebResourceCollection wrc = (WebResourceCollection) resources.next();
               String[] httpMethods = wrc.getHttpMethods();
               String[] urlPatterns = wrc.getUrlPatterns();
               for(int n = 0; n < urlPatterns.length; n ++)
               {
                  String url = urlPatterns[n];
                  WebResourcePermission p = new WebResourcePermission(url, httpMethods);
                  WebUserDataPermission p2 = new WebUserDataPermission(url,
                     httpMethods, wsmd.getTransportGuarantee());
                  if( wsmd.isExcluded() )
                  {
                     pc.addToExcludedPolicy(p);
                     pc.addToExcludedPolicy(p2);                     
                  }
                  else
                  {
                     pc.addToUncheckedPolicy(p);
                     pc.addToUncheckedPolicy(p2);
                  }
                  // Track the incomplete coverage of http methods
                  if( urlPatterns.length > 0 )
                  {
                     HashSet methodsWRP = (HashSet) patternsWithHttpMethodSubsetsWRP.get(url);
                     if( methodsWRP == null )
                     {
                        methodsWRP = new HashSet();
                        patternsWithHttpMethodSubsetsWRP.put(url, methodsWRP);
                     }
                     /* Only add methods to excluded perms because incomplete
                     coverage of unchecked perms http method should not exclude
                     the explicitly stated http methods. For example, a url
                     pattern /unchecked with explicit https method POST, GET that
                     as no auth constraint should result in a unchecked permission
                     that includes all http methods since the logic for incomplete
                     coverage would add an unchecked permission with the missing
                     methods: DELETE, PUT, HEAD, OPTIONS, TRACE. However, two
                     unchecked permissions whose union of http methods covers
                     all methods does not result in a permission collection
                     that implies WebResourcePermission("/unchecked, null). So
                     we leave off the http methods for the explicit unchecked
                     permission so that an unchecked permission with a null
                     http methods spec results if appropriate.
                     */
                     if( wsmd.isExcluded() )
                        methodsWRP.addAll(Arrays.asList(httpMethods));

                     HashSet methodsWUDP = (HashSet) patternsWithHttpMethodSubsetsWUDP.get(url);
                     if( methodsWUDP == null )
                     {
                        methodsWUDP = new HashSet();
                        patternsWithHttpMethodSubsetsWUDP.put(url, methodsWUDP);
                     }
                     if( wsmd.isExcluded() || transport != null )
                        methodsWUDP.addAll(Arrays.asList(httpMethods));
                  }
               }
            }
         }
         else
         {
            // Build the permission for the resources x roles
            Iterator resources = wsmd.getWebResources().values().iterator();
            while( resources.hasNext() )
            {
               WebResourceCollection wrc = (WebResourceCollection) resources.next();
               String[] httpMethods = wrc.getHttpMethods();
               String[] urlPatterns = wrc.getUrlPatterns();
               for(int n = 0; n < urlPatterns.length; n ++)
               {
                  String url = urlPatterns[n];
                  WebResourcePermission p = new WebResourcePermission(url, httpMethods);
                  WebUserDataPermission p2 = new WebUserDataPermission(url,
                     httpMethods, wsmd.getTransportGuarantee());
                  Iterator roles = wsmd.getRoles().iterator();
                  while( roles.hasNext() )
                  {
                     String role = (String) roles.next();
                     pc.addToRole(role, p);
                  }
                  pc.addToUncheckedPolicy(p2);
                  // Track the incomplete coverage of http methods
                  if( urlPatterns.length > 0 )
                  {
                     HashSet methodsWRP = (HashSet) patternsWithHttpMethodSubsetsWRP.get(url);
                     if( methodsWRP == null )
                     {
                        methodsWRP = new HashSet();
                        patternsWithHttpMethodSubsetsWRP.put(url, methodsWRP);
                     }
                     methodsWRP.addAll(Arrays.asList(httpMethods));

                     HashSet methodsWUDP = (HashSet) patternsWithHttpMethodSubsetsWUDP.get(url);
                     if( methodsWUDP == null )
                     {
                        methodsWUDP = new HashSet();
                        patternsWithHttpMethodSubsetsWUDP.put(url, methodsWUDP);
                     }
                     if( transport != null )
                        methodsWUDP.addAll(Arrays.asList(httpMethods));
                  }
               }
            }
         }
      }

      /* Create unchecked permissions. We are required to create an unchecked
      permission for every url pattern that is not covered by a every possible
      http method.
      */
      Iterator iter = patternsWithHttpMethodSubsetsWRP.entrySet().iterator();
      while( iter.hasNext() )
      {
         Map.Entry e = (Map.Entry) iter.next();
         String url = (String) e.getKey();
         HashSet methods = (HashSet) e.getValue();
         String[] missingMethods = WebSecurityMetaData.getMissingHttpMethods(methods);
         WebResourcePermission p = new WebResourcePermission(url, missingMethods);
         pc.addToUncheckedPolicy(p);
      }
      
      iter = patternsWithHttpMethodSubsetsWUDP.entrySet().iterator();
      while( iter.hasNext() )
      {
         Map.Entry e = (Map.Entry) iter.next();
         String url = (String) e.getKey();
         HashSet methods = (HashSet) e.getValue();
         String[] missingMethods = WebSecurityMetaData.getMissingHttpMethods(methods);
         WebUserDataPermission p2 = new WebUserDataPermission(url, missingMethods, null);
         pc.addToUncheckedPolicy(p2);
      }

      // 
      if( patternsWithHttpMethodSubsetsWRP.containsKey("/") == false )
         patternsWithHttpMethodSubsetsWRP.put("/", null);
      iter = patternsWithHttpMethodSubsetsWRP.keySet().iterator();
      StringBuffer allURLs = new StringBuffer();
      while( iter.hasNext() )
      {
         String url = (String) iter.next();
         allURLs.append(url);
         allURLs.append(':');
      }
      allURLs.setLength(allURLs.length()-1);
      String all = allURLs.toString();
      WebResourcePermission p = new WebResourcePermission(all, (String) null);
      pc.addToUncheckedPolicy(p);
      WebUserDataPermission p2 = new WebUserDataPermission(all, null);
      pc.addToUncheckedPolicy(p2);

      patternsWithHttpMethodSubsetsWRP.clear();
      patternsWithHttpMethodSubsetsWUDP.clear();

      /* Create WebRoleRefPermissions for all servlet/security-role-refs along
      with all the cross product of servlets and security-role elements that
      are not referenced via a security-role-ref as described in JACC section
      3.1.3.2
      */
      Set unreferencedRoles = metaData.getSecurityRoleNames();
      Map servletRoleRefs = metaData.getSecurityRoleRefs();
      Iterator roleRefsIter = servletRoleRefs.keySet().iterator();
      while( roleRefsIter.hasNext() )
      {
         String servletName = (String) roleRefsIter.next();
         ArrayList roleRefs = (ArrayList) servletRoleRefs.get(servletName);
         for(int n = 0; n < roleRefs.size(); n ++)
         {
            SecurityRoleRefMetaData roleRef = (SecurityRoleRefMetaData) roleRefs.get(n);
            String roleName = roleRef.getLink();
            WebRoleRefPermission wrrp = new WebRoleRefPermission(servletName, roleRef.getName());
            pc.addToRole(roleName, wrrp);
            /* A bit of a hack due to how tomcat calls out to its Realm.hasRole()
            with a role name that has been mapped to the role-link value. We
            may need to handle this with a custom request wrapper.
            */
            wrrp = new WebRoleRefPermission(servletName, roleName);
            pc.addToRole(roleRef.getName(), wrrp);
            // Remove the role from the unreferencedRoles
            unreferencedRoles.remove(roleName);
         }
      }
      
      // Now build the cross product of the unreferencedRoles and servlets
      Set servletNames = metaData.getServletNames();
      Iterator names = servletNames.iterator();
      while( names.hasNext() )
      {
         String servletName = (String) names.next();
         Iterator roles = unreferencedRoles.iterator();
         while( roles.hasNext() )
         {
            String role = (String) roles.next();
            WebRoleRefPermission wrrp = new WebRoleRefPermission(servletName, role);
            pc.addToRole(role, wrrp);            
         }
      }
   }

   /** An inner class that maps the WebDescriptorParser.parseWebAppDescriptors()
   onto the protected parseWebAppDescriptors() AbstractWebContainer method.
   */
   private class DescriptorParser implements WebDescriptorParser
   {
      DeploymentInfo di;
      DescriptorParser(DeploymentInfo di)
      {
         this.di = di;
      }
      public void parseWebAppDescriptors(ClassLoader loader, WebMetaData metaData)
         throws Exception
      {
         AbstractWebDeployer.this.parseWebAppDescriptors(di, loader, metaData);
      }
      public DeploymentInfo getDeploymentInfo()
      {
         return di;
      }
   }
}
