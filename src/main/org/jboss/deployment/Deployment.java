/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.deployment;

import java.net.URL;

import java.util.Iterator;
import java.util.Vector;
import java.util.Date;

import java.util.jar.Manifest;

/**
 * Represents a J2EE application or module (EJB.jar, Web.war, Connector.rar, Client.jar
 * or App.ear).
 * <br>
 * Extended on 5th October 2001 by CGJ to also include
 * connector and java-client support.
 *
 * @author <a href="mailto:daniel.schulze@telkel.com">Daniel Schulze</a>
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @version $Revision: 1.15 $
 */
public class Deployment
implements java.io.Serializable {
    /** the apploications name */
    protected String name;
    
    /** the date this deployment was made */
    protected Date date;
    
    /** the local position of the apps root directory */
    protected URL localUrl;
    
    /** the position from which this deployment is installed */
    protected URL sourceUrl;
    
    /** the content of the <code>commonLibs</code> directory as
    URL Collection */
    protected Vector commonUrls;
    
    /** the EJB Modules */
    protected Vector ejbModules;
    
    /** the WEB Modules */
    protected Vector webModules;
    
    /** the java modules */
    protected Vector javaModules;
    
    /** the connector modules */
    protected Vector connectorModules;
    
    /** Content of the EAR deployment descriptor **/
    protected String applicationDeploymentDescriptor;
    
    /** the manifest entry of the deployment (if any)
     *  manifest is not serializable ... is only needed
     *  at deployment time, so we mark it transient
     */
    protected transient Manifest manifest;
    
    /** Creates a new Deployment object. */
    Deployment() {
        date = new Date();
        ejbModules = new Vector();
        webModules = new Vector();
        javaModules=new Vector();
        connectorModules=new Vector();
        commonUrls = new Vector();
    }
    
    /** returns a new instance of the Module innerclass */
    public Module newModule() {
        return new Module();
    }
    
    /** returns the name of this deployment
     */
    public String getName() {
        return name;
    }
    
    /** returns the source url that points to from where this deployment has been
     *  downloaded
     *  @author cgjung
     */
    public URL getSourceUrl() {
        return sourceUrl;
    }
    
    /** returns the local url that points to the place where this deployment
     *  has been downloaded
     *  @author cgjung
     */
    public URL getLocalUrl() {
        return localUrl;
    }
    
    /** Add a module manifest Class-Path element
     */
    public void addCommonUrl(URL url) {
        commonUrls.add(url);
    }
    /** returns the common urls
     *  @author cgjung
     */
    public Vector getCommonUrls() {
        return commonUrls;
    }
    
    /** Create a new Module for an EJB and add it to the ejbModules list.
    @param name, the name of the ejb-jar module
    @param localJar, the local ejb-jar
    @param mfUrls, the resolved URLs for the ejb-jar manifest Class-Path:
     */
    public void addEjbModule(String name, URL localJar, URL[] mfUrls) {
        Module m = new Module();
        m.name = name;
        int count = mfUrls != null ? mfUrls.length : 0;
        for(int u = 0; u < mfUrls.length; u ++)
            commonUrls.add(mfUrls[u]);
        m.localUrls.add(localJar);
        ejbModules.add(m);
    }
    
    /** returns the ejbModules
     */
    public Vector getEjbModules() {
        return ejbModules;
    }
    
    /** Create a new Module for an WAR and add it to the webModules list.
    @param name, the name of the war module
    @param webContext, the context under which the web-app should be deployed
    @param localJar, the local war
    @param mfUrls, the resolved URLs for the war manifest Class-Path:
     */
    public void addWebModule(String name, String webContext, URL localJar, URL[] mfUrls) {
        Module m = new Module();
        m.name = name;
        m.webContext = webContext;
        int count = mfUrls != null ? mfUrls.length : 0;
        for(int u = 0; u < mfUrls.length; u ++)
            commonUrls.add(mfUrls[u]);
        m.localUrls.add(localJar);
        webModules.add(m);
    }
    
    /** returns the webModules
     */
    public Vector getWebModules() {
        return webModules;
    }
    
    /** Create a new Module for a java module and add it to the javaModules list.
    @param name, the name of the ejb-jar module
    @param jar, the local jar file
    @param mfUrls, the resolved URLs for the ejb-jar manifest Class-Path:
     */
    public void addJavaModule(String name, URL localJar, URL[] mfUrls) {
        Module m = new Module();
        m.name = name;
        int count = mfUrls != null ? mfUrls.length : 0;
        for(int u = 0; u < mfUrls.length; u ++)
            commonUrls.add(mfUrls[u]);
        m.localUrls.add(localJar);
        javaModules.add(m);
    }
    
    /** returns the javaModules
     */
    public Vector getJavaModules() {
        return javaModules;
    }
    
    /** Create a new Module for a java module and add it to the javaModules list.
    @param name, the name of the ejb-jar module
    @param jar, the local jar file
    @param mfUrls, the resolved URLs for the ejb-jar manifest Class-Path:
     */
    public void addConnectorModule(String name, URL localJar, URL[] mfUrls) {
        Module m = new Module();
        m.name = name;
        int count = mfUrls != null ? mfUrls.length : 0;
        for(int u = 0; u < mfUrls.length; u ++)
            commonUrls.add(mfUrls[u]);
        m.localUrls.add(localJar);
        connectorModules.add(m);
    }
    
    /** returns the javaModules
     */
    public Vector getConnectorModules() {
        return connectorModules;
    }
    
    /** returns the manifest entry of the deployment
     */
    public Manifest getManifest() {
        return manifest;
    }
    
    /**
     * @return EAR deployment descriptor content
     **/
    public String getApplicationDeploymentDescriptor() {
        return applicationDeploymentDescriptor;
    }
    
    /** returns all files (URLs) that are needed to run this deployment properly */
    public Vector getAllFiles() {
        // the common libs
        Vector result = new Vector();
        Iterator it = commonUrls.iterator();
        while (it.hasNext()) {
            String s = ((URL)it.next()).getFile();
            result.add(s.substring(s.lastIndexOf("/")+1));
        }
        
        // the ejb packages
        it = ejbModules.iterator();
        while (it.hasNext()) {
            String s = ((URL)((Module)it.next()).localUrls.firstElement()).getFile();
            result.add(s.substring(s.lastIndexOf("/")+1));
        }
        
        // the web packages
        it = webModules.iterator();
        while (it.hasNext()) {
            String s = ((URL)((Module)it.next()).localUrls.firstElement()).getFile();
            result.add(s.substring(s.lastIndexOf("/")+1));
        }
        
        // the java packages
        it = javaModules.iterator();
        while (it.hasNext()) {
            String s = ((URL)((Module)it.next()).localUrls.firstElement()).getFile();
            result.add(s.substring(s.lastIndexOf("/")+1));
        }
        
        // the connector packages
        it = connectorModules.iterator();
        while (it.hasNext()) {
            String s = ((URL)((Module)it.next()).localUrls.firstElement()).getFile();
            result.add(s.substring(s.lastIndexOf("/")+1));
        }
        
        // and the config file
        result.add(J2eeDeployer.CONFIG);
        
        return result;
    }
    
    
    /** Represents a J2ee module. */
    public class Module
    implements java.io.Serializable {
        /** a short name for the module */
        protected String name;
        
        /** a collection of urls that make this module. <br>
       actually there is only one url for the modules jar file or
       in case of web the modules root directory needed. But to be able
       to allow alternative descriptors, the base directories of this alternative
       descriptors can be put here before the real module url, so that these where
       found first */
        protected Vector localUrls;
        
        /** the web root context in case of war file */
        protected String webContext;
        
        protected Module() {
            localUrls = new Vector();
        }
        
        /**
         * returns the local urls
         */
        public Vector getLocalUrls() {
            return localUrls;
        }
        
        /**
         * returns the name
         */
        public String getName() {
            return name;
        }
        
        /**
         * returns the web context
         */
        public String getWebContext() {
            return webContext;
        }
        
    }
}
