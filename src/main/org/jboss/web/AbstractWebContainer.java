package org.jboss.web;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.NamingException;
import javax.naming.Name;
import javax.naming.NameNotFoundException;

import org.w3c.dom.Element;

import org.apache.log4j.Category;

import org.jboss.ejb.DeploymentException;
import org.jboss.metadata.EjbRefMetaData;
import org.jboss.metadata.EnvEntryMetaData;
import org.jboss.metadata.ResourceRefMetaData;
import org.jboss.metadata.WebMetaData;
import org.jboss.naming.Util;
import org.jboss.security.plugins.NullSecurityManager;
import org.jboss.util.ServiceMBeanSupport;

/** A template pattern class for web container integration into JBoss. This class
should be subclasses by web container providers wishing to integrate their
container into a JBoss server.

It provides support for mapping the following web-app.xml/jboss-web.xml elements
into the JBoss server JNDI namespace:
- env-entry
- resource-ref
- ejb-ref
- security-domain

Subclasses need to implement the {@link #performDeploy(String, String, WebDescriptorParser) performDeploy()}
and {@link #performUndeploy(String) performUndeploy()} methods to perform the
container specific steps and return the web application info required by the
AbstractWebContainer class.

Integration with the JBossSX security framework is based on the establishment
of a java:comp/env/security context as described in the
{@link linkSecurityDomain(String, Context) linkSecurityDomain } comments.
The security context provides access to the JBossSX security mgr interface
implementations for use by subclass request interceptors. A outline of the
steps for authenticating a user is:
<code>
    // Get the username & password from the request context...
    String username = f(request);
    String password = f(request);
    // Get the JBoss security manager from the ENC context
    InitialContext iniCtx = new InitialContext();
    EJBSecurityManager securityMgr = (EJBSecurityManager) iniCtx.lookup("java:comp/env/security/securityMgr");
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
from the {@link #performDeploy(String, String) performDeploy} as its thread
context ClassLoader or else the lookup for java:comp/env will fail with a
name not found exception, or worse, it will receive some other web application
ENC context. If your adapting a web container that is trying be compatible with
both 1.1 and 1.2 Java VMs this is something you need to pay special attention
to. For example, I have seen problems a request interceptor that was handling
the authentication/authorization callouts in tomcat3.2.1 not having the same
thread context ClassLoader as was used to dispatch the http service request.

For a complete example see the {@link org.jboss.tomcat.security.JBossSecurityMgrRealm JBossSecurityMgrRealm}
in the contrib/tomcat module.

@see #performDeploy(String, String)
@see #performUndeploy(String)
@see #parseWebAppDescriptors(ClassLoader, Element, Element)
@see #linkSecurityDomain(String, Context)
@see org.jboss.security.EJBSecurityManager;
@see org.jboss.security.RealmMapping;
@see org.jboss.security.SimplePrincipal;
@see org.jboss.security.SecurityAssociation;

@author  <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
@version $Revision: 1.5 $
*/
public abstract class AbstractWebContainer extends ServiceMBeanSupport implements AbstractWebContainerMBean
{
    public static interface WebDescriptorParser
    {
        /** This method is called as part of subclass performDeploy() method implementations
            to parse the web-app.xml and jboss-web.xml deployment descriptors from a
            war deployment. The method creates the ENC(java:comp/env) env-entry,
            resource-ref, & ejb-ref element values. The creation of the env-entry
            values does not require a jboss-web.xml descriptor. The creation of the
            resource-ref and ejb-ref elements does require a jboss-web.xml descriptor
            for the JNDI name of the deployed resources/EJBs.

            Because the ENC context is private to the web application, the web
            application class loader is used to identify the ENC. The class loader
            is used because each war typically requires a unique class loader to
            isolate the web application classes/resources. This means that the
            ClassLoader passed to this method must be the thread context ClassLoader
            seen by the server/jsp pages during init/destroy/service/etc. method
            invocations if these methods interace with the JNDI ENC context.

        @param loader, the ClassLoader for the web application. May not be null.
        @param webApp, the root element of thw web-app.xml descriptor. May not be null.
        @param jbossWeb, the root element of thw jboss-web.xml descriptor. May be null
            to indicate that no jboss-web.xml descriptor exists.
        */
        public void parseWebAppDescriptors(ClassLoader loader, Element webApp, Element jbossWeb) throws Exception;
    }

    /** The "WebContainer" log4j category instance available for logging related
        to WebContainer events.
     */
    public static final Category category = Category.getInstance("WebContainer");
    /** A mapping of deployed warUrl strings to the WebApplication object */
    protected HashMap deploymentMap = new HashMap();


    public AbstractWebContainer()
    {
    }

    /** A template pattern implementation of the deploy() method. This method
     calls the {@link #performDeploy(String, String) performDeploy()} method to
     perform the container specific deployment steps and registers the
     returned WebApplication in the deployment map. The steps performed are:

        WebDescriptorParser webAppParser = ...;
        WebApplication warInfo = performDeploy(ctxPath, warUrl, webAppParser);
        ClassLoader loader = warInfo.getClassLoader();
        Element webApp = warInfo.getWebApp();
        Element jbossWeb = warInfo.getJbossWeb();
        deploymentMap.put(warUrl, warInfo);

     The subclass performDeploy() implementation needs to invoke
     webAppParser.parseWebAppDescriptors(loader, webApp, jbossWeb) to have the
     JNDI java:comp/env namespace setup before any web app component can access
     this namespace.

     @param ctxPath, The context-root element value from the J2EE
        application/module/web application.xml descriptor. This may be null
        if war was is not being deployed as part of an enterprise application.
     @param warUrl, The string for the URL of the web application war.
    */
    public synchronized void deploy(String ctxPath, String warUrl) throws DeploymentException
    {
        try
        {
            WebDescriptorParser webAppParser = new DescriptorParser();
            WebApplication warInfo = performDeploy(ctxPath, warUrl, webAppParser);
            ClassLoader loader = warInfo.getClassLoader();
            Element webApp = warInfo.getWebApp();
            Element jbossWeb = warInfo.getJbossWeb();
            //parseWebAppDescriptors(loader, webApp, jbossWeb);
            deploymentMap.put(warUrl, warInfo);
        }
        catch(DeploymentException e)
        {
            throw (DeploymentException) e.fillInStackTrace();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new DeploymentException("Error during deploy", e);
        }
    }

    /** This method is called by the deploy() method template and must be overriden by
        subclasses to perform the web container specific deployment steps. 
     @param ctxPath, The context-root element value from the J2EE
        application/module/web application.xml descriptor. This may be null
        if war was is not being deployed as part of an enterprise application.
     @param warUrl, The string for the URL of the web application war.
     @param webAppParser, The callback interface the web container should use to
     setup the web app JNDI environment for use by the web app components. This
     needs to be invoked after the web app class loader is known, but before
     and web app components attempt to access the java:comp/env JNDI namespace.
     @return WebApplication, the web application information required by the
        AbstractWebContainer class to track the war deployment status.
    */
    protected abstract WebApplication performDeploy(String ctxPath, String warUrl,
        WebDescriptorParser webAppParser) throws Exception;

    /** A template pattern implementation of the undeploy() method. This method
     calls the {@link #performUndeploy(String) performUndeploy()} method to
     perform the container specific undeployment steps and unregisters the
     the warUrl from the deployment map.
    */
    public synchronized void undeploy(String warUrl) throws DeploymentException
    {
        try
        {
            performUndeploy(warUrl);
            // Remove the web application ENC...
            deploymentMap.remove(warUrl);
        }
        catch(DeploymentException e)
        {
            throw (DeploymentException) e.fillInStackTrace();
        }
        catch(Exception e)
        {
            throw new DeploymentException("Error during deploy", e);
        }
    }
    protected abstract void performUndeploy(String warUrl) throws Exception;
    /** See if a war is deployed.
    */
    public boolean isDeployed(String warUrl)
    {
        return deploymentMap.containsKey(warUrl);
    }

    /** Get the WebApplication object for a deployed war.
    @param warUrl, the war url string as originally passed to deploy().
    @return The WebApplication created during the deploy step if the
        warUrl is valid, null if no such deployment exists.
     */
    public WebApplication getDeployedApp(String warUrl)
    {
        WebApplication appInfo = (WebApplication) deploymentMap.get(warUrl);
        return appInfo;
    }

    /** Returns the applications deployed by the web container subclasses.
    @return An Iterator of WebApplication objects for the deployed wars.
    */
    public Iterator getDeployedApplications()
    {
        return deploymentMap.values().iterator();
    }

    /** This method is invoked from within subclass performDeploy() method
     implementations when they invoke WebDescriptorParser.parseWebAppDescriptors().

    @param loader, the ClassLoader for the web application. May not be null.
    @param webApp, the root element of thw web-app.xml descriptor. May not be null.
    @param jbossWeb, the root element of thw jboss-web.xml descriptor. May be null
        to indicate that no jboss-web.xml descriptor exists.
    */
    protected void parseWebAppDescriptors(ClassLoader loader, Element webApp, Element jbossWeb) throws Exception
    {
        category.debug("AbstractWebContainer.parseWebAppDescriptors, Begin");
        WebMetaData metaData = new WebMetaData();
        metaData.importXml(webApp);
        if( jbossWeb != null )
            metaData.importXml(jbossWeb);

        InitialContext iniCtx = new InitialContext();
        Context envCtx = null;
        ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
        try
        {
            // Create a java:comp/env environment unique for the web application
            Thread.currentThread().setContextClassLoader(loader);
            envCtx = (Context) iniCtx.lookup("java:comp");
            envCtx = envCtx.createSubcontext("env");
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(currentLoader);
        }

        Iterator envEntries = metaData.getEnvironmentEntries();
        category.debug("addEnvEntries");
        addEnvEntries(envEntries, envCtx);
        Iterator resourceRefs = metaData.getResourceReferences();
        category.debug("linkResourceRefs");
        linkResourceRefs(resourceRefs, envCtx);
        Iterator ejbRefs = metaData.getEjbReferences();
        category.debug("linkEjbRefs");
        linkEjbRefs(ejbRefs, envCtx);
        String securityDomain = metaData.getSecurityDomain();
        category.debug("linkSecurityDomain");
        linkSecurityDomain(securityDomain, envCtx);
        category.debug("AbstractWebContainer.parseWebAppDescriptors, End");
    }

    protected void addEnvEntries(Iterator envEntries, Context envCtx)
        throws ClassNotFoundException, NamingException
    {
        while( envEntries.hasNext() )
        {
            EnvEntryMetaData entry = (EnvEntryMetaData) envEntries.next();
            category.debug("Binding env-entry: "+entry.getName()+" of type: "+entry.getType()+" to value:"+entry.getValue());
            EnvEntryMetaData.bindEnvEntry(envCtx, entry);
        }
    }

    protected void linkResourceRefs(Iterator resourceRefs, Context envCtx)
        throws NamingException
    {
        while( resourceRefs.hasNext() )
        {
            ResourceRefMetaData ref = (ResourceRefMetaData) resourceRefs.next();
            String resourceName = ref.getResourceName();
            String refName = ref.getRefName();
            if( ref.getType().equals("java.net.URL") )
            {
                try
                {
                    category.debug("Binding '"+refName+"' to URL: "+resourceName);
                    URL url = new URL(resourceName);
                    Util.bind(envCtx, refName, url);
                }
                catch(MalformedURLException e)
                {
                    throw new NamingException("Malformed URL:"+e.getMessage());
                }
            }
            else
            {
                category.debug("Linking '"+refName+"' to JNDI name: "+resourceName);
                Util.bind(envCtx, refName, new LinkRef(resourceName));
            }
        }
    }

    protected void linkEjbRefs(Iterator ejbRefs, Context envCtx)
        throws NamingException
    {
        while( ejbRefs.hasNext() )
        {
            EjbRefMetaData ejb = (EjbRefMetaData) ejbRefs.next();
            String name = ejb.getName();
            String jndiName = ejb.getJndiName();
            String linkName = ejb.getLink();
            if( jndiName == null )
                jndiName = linkName;
            category.debug("Linking ejb-ref: "+name+" to JNDI name: "+jndiName);
            if( jndiName == null )
                 throw new NamingException("ejb-ref: "+name+", expected jndi-name in jboss-web.xml");
            Util.bind(envCtx, name, new LinkRef(jndiName));
        }
    }

    /** This creates a java:comp/env/security context that contains a
        securityMgr binding pointing to an EJBSecurityMgr implementation
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
            category.debug("Binding security/securityMgr to NullSecurityManager");
            Object securityMgr = new NullSecurityManager(securityDomain);
            Util.bind(envCtx, "security/securityMgr", securityMgr);
            Util.bind(envCtx, "security/realmMapping", securityMgr);
        }
        else
        {
            category.debug("Linking security/securityMgr to JNDI name: "+securityDomain);
            Util.bind(envCtx, "security/securityMgr", new LinkRef(securityDomain));
            Util.bind(envCtx, "security/realmMapping", new LinkRef(securityDomain));
        }
    }

    /** An inner class that maps the WebDescriptorParser.parseWebAppDescriptors()
     onto the protected parseWebAppDescriptors() AbstractWebContainer method.
    */
    private class DescriptorParser implements WebDescriptorParser
    {
        public void parseWebAppDescriptors(ClassLoader loader, Element webApp, Element jbossWeb) throws Exception
        {
            AbstractWebContainer.this.parseWebAppDescriptors(loader, webApp, jbossWeb);
        }
    }

}
