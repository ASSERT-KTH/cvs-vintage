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
import org.jboss.util.ServiceMBeanSupport;

/** A template pattern class for web container integration into JBoss. This class
should be subclasses by web container providers wishing to integrate their
container into a JBoss server.

It provides support for mapping the following web-app.xml/jboss-web.xml elements
into the JBoss server JNDI namespace:
- env-entry
- resource-ref
- ejb-ref

Subclasses need to implement the {@link #performDeploy(String, String) performDeploy()}
and {@link #performUndeploy(String) performUndeploy()} methods to perform the
container specific steps and return the web application info required by the
AbstractWebContainer c.ass.
 
@see #performDeploy(String, String)
@see #performUndeploy(String)

@author  Scott_Stark@displayscape.com
@version $Revision: 1.2 $
*/
public abstract class AbstractWebContainer extends ServiceMBeanSupport implements AbstractWebContainerMBean
{
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

        WebApplication warInfo = performDeploy(ctxPath, warUrl);
        ClassLoader loader = warInfo.getClassLoader();
        Element webApp = warInfo.getWebApp();
        Element jbossWeb = warInfo.getJbossWeb();
        parseWebAppDescriptors(loader, webApp, jbossWeb);
        deploymentMap.put(warUrl, warInfo);

     @param ctxPath, The context-root element value from the J2EE
        application/module/web application.xml descriptor. This may be null
        if war was is not being deployed as part of an enterprise application.
     @param warUrl, The string for the URL of the web application war.
    */
    public synchronized void deploy(String ctxPath, String warUrl) throws DeploymentException
    {
        try
        {
            WebApplication warInfo = performDeploy(ctxPath, warUrl);
            ClassLoader loader = warInfo.getClassLoader();
            Element webApp = warInfo.getWebApp();
            Element jbossWeb = warInfo.getJbossWeb();
            parseWebAppDescriptors(loader, webApp, jbossWeb);
            deploymentMap.put(warUrl, warInfo);
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

    /** The method called by the deploy() method that must be overriden by
        subclasses to perform the web container specific deployment steps.
     @param ctxPath, The context-root element value from the J2EE
        application/module/web application.xml descriptor. This may be null
        if war was is not being deployed as part of an enterprise application.
     @param warUrl, The string for the URL of the web application war.
     @return WebApplication, the web application information required by the
        AbstractWebContainer class to setup the JNDI ENC and track the war
        deployment status.
    */
    protected abstract WebApplication performDeploy(String ctxPath, String warUrl) throws Exception;

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

    /** Parse the web-app.xml and jboss-web.xml deployment descriptors from a
        war deployment. The method creates the ENC(java:comp/env) env-entry,
        resource-ref, & ejb-ref element values. The creation of the env-entry
        values does not require a jboss-web.xml descriptor. The creation of the
        resource-ref and ejb-ref elements does require a jboss-web.xml descriptor
        for the JNDI name of the deployed resources/EJBs.

        Because the ENC context is private to the web application, the web
        application class loader is used to identify the ENC. The class loader
        is used because each war typically requires a unique class loader to
        isolate the web application classes/resources.

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
            String jndiName = ejb.getLink();
            category.debug("Binding ejb-ref: "+name+" to JNDI name:"+jndiName);
            if( jndiName == null )
                 throw new NamingException("ejb-ref: "+name+", expected jndi-name in jboss-web.xml");
            Util.bind(envCtx, name, new LinkRef(jndiName));
        }
    }
}
