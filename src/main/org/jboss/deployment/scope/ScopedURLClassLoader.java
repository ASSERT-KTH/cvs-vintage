/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/

package org.jboss.deployment.scope;

import org.jboss.deployment.Deployment;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.Collection;

/**
 * A URLClassLoader that is tight to some J2EE deployment and that is 
 * able to share classes/resources under the associated scope. Hey, JDK-S**ckers
 * why did you annotate getResources final? 
 * @author  cgjung
 * @version 0.9
 */
public class ScopedURLClassLoader extends URLClassLoader {

    /** reference to the scope to which resource loading calls
     *  can be delegated to. 
     */
    final protected Scope scope;
    
    /**
     * reference to the deployment that is associated
     * with this classloader
     */
    final protected Deployment deployment;
    

    /** Creates new ScopedURLClassLoader given a set of urls and a parent,
     *  representing a particular deployment */
    public ScopedURLClassLoader(URL[] urls, ClassLoader parent, Deployment deployment, Scope scope) {
        super(urls,parent);
        this.scope=scope;
        this.deployment=deployment;
        scope.registerClassLoader(this);
    }
    
    /** exposes the proper loadClass call */
    protected Class loadClassProperly(String name, boolean resolve) throws ClassNotFoundException {
        return super.loadClass(name,resolve);
    }
    
    /** redirects loadClass in case that it could not be found locally */
    protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try{
            return super.loadClass(name,resolve);
        } catch(ClassNotFoundException e) {
            // redirect
            return scope.loadClass(name,this,resolve);
        }
    }
            
    /** exposes the proper getResource call */
    protected URL getResourceProperly(String name) {
        return super.getResource(name);
    }
    
    /** redirects getResource in case that it could not be found locally */
    public URL getResource(String name) {
        URL result=super.getResource(name);
        if(result==null)
            result=scope.getResource(name,this);
        return result;
    }
    
    /** what happens on undeploy, could be overridden to tear down meta-data and such */
    protected void onUndeploy() {
        // nothing
    }

    /** returns a set of relative urls in string spec that
     *  point to applications to which this application
     *  is (most likely) dependent on. Works via analysing the
     *  Class-Path: in the Manifest.mf. 
     *  Marc: Please note that sharing scope works also without 
     *  having these explicit annotations. However,
     *  there may be circular dependencies that are already relevant
     *  at deployment time (e.g., bean verification) which you otherwise
     *  could never setup manually (i.e., by calling the deploy method). 
     */
    public String[] getDependingApplications() {
        try{
            java.util.jar.Manifest manifest= (java.util.jar.Manifest) 
                new java.io.ObjectInputStream(getResourceProperly("META-INF/Manifest.mf").
                    openStream()).readObject();
            
            java.util.jar.Attributes attributes=
                manifest.getMainAttributes();
            
            java.util.StringTokenizer tok=
                new java.util.StringTokenizer(attributes.getValue(java.util.jar.Attributes.Name.CLASS_PATH)," ");
            
            Collection allDeps=new java.util.ArrayList();
            
            while(tok.hasMoreTokens())
                allDeps.add(tok.nextToken());
            
            return (String[]) allDeps.toArray(new String[allDeps.size()]);
        } catch(Exception e) {
            return new String[0];
        }
    }
    
    

}
