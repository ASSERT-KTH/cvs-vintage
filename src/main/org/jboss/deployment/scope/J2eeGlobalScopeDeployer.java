/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.deployment.scope;

import org.jboss.deployment.Deployment;
import org.jboss.deployment.J2eeDeploymentException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.Set;
import java.util.Map;
import java.io.IOException;
import javax.management.ObjectName;


/**
 * This is a deployer that introduces a J2ee application scoping facility and
 * proper (re-)deployment procedures. It implements a default global scope.
 * @author  <a href="mailto:Christoph.Jung@infor.de">cgjung</a>
 * @version 0.9
 */

public class J2eeGlobalScopeDeployer extends org.jboss.deployment.J2eeDeployer implements J2eeGlobalScopeDeployerMBean {
    
    /** the scopes that are in effect */
    final protected Map scopes=new java.util.HashMap();
    
    /** Creates new J2eeDependencyDeployer */
    public J2eeGlobalScopeDeployer() {
    }
    
    /** 
     * registers a new scope in this deployer
     * @param name unique name of the new scope
     * @param scope the scope to register
     * @return the scope that has been isolated by that action
     */
    protected Scope registerScope(String name, Scope scope) {
	synchronized(scopes) {
    		return (Scope) scopes.put(name,scope);
	}
    }
    
    /** 
     * looks up a scope and creates it of not yet registered
     * @param name the unique name of the scope
     * @return the registered scope
     */
    public Scope getScope(String name) {
	synchronized(scopes) {
		return (Scope) scopes.get(name);
	}
    }
    
    /** 
     * returns a list of the registered scope names
     * @return scope name list
     */
    public String[] listScopes() {
	synchronized(scopes) {
		return (String[]) scopes.keySet().toArray(new String[scopes.size()]);
	}
    }

    /** 
     * deregisters a scope
     * @param name unique name of the scope to deRegister
     * @return the deRegistered scope
     *
     */
    protected Scope deRegisterScope(String name) {
    	synchronized(scopes) {
		return (Scope) scopes.remove(name);
	}
    }
    
    /** 
     * deregisters all scopes
     */
    protected void deRegisterScopes() {
	synchronized(scopes) {
	    Iterator allScopes=scopes.keySet().iterator();
	    while(allScopes.hasNext()) {
		allScopes.remove();
	    }
	}
    }

    /** factory method to create a new scope, May throw a general exception
     * if this very basic enterprise fails.
     * @throws Exception May throw any exception to indicate
     * instantiation problems of the scope (probably
     * because initial meta-data could not be found,
     * is corrupt, etc.)
     * @return a freshly instantiated and preconfigured scope.
     *
     */
    protected Scope createScope() throws Exception {
        return new Scope(log);
    }

    /** creates and registers fresh scope */
    public Scope createScope(String name) throws Exception {
	synchronized(scopes) {
		Scope result=getScope(name);
		if(result==null) {
			result=createScope();
			registerScope(name,result);
		}
		return result;
	}
    }

    /** static name of the global scope */
    final public static String GLOBAL_SCOPE="GLOBAL_SCOPE";
    
    /** starts the service by first creating
     * a new scope
     * @throws Exception to indicate
     * that either superclass or scope creation
     * went wrong.
     */
    public void startService() throws Exception {
        super.startService();
	createScope(GLOBAL_SCOPE);
    }
    
    
    /** stops the service by freeing
     *  scopes
     */
    public void stopService() {
        super.stopService();
    	deRegisterScopes();
    }
    
    
    /**
     * creates an application class loader for this deployment
     * this class loader will be shared between jboss and
     * tomcat via the contextclassloader
     * we include all ejb and web-modules at this level
     * to also be able to connect flat ejb-jars to each other
     * @param scope the scope in which to create the classloader
     * @param deployment the deployment that is about to be made,
     * but is already installed
     * @throws J2eeDeploymentException to indicate
     * problems with instantiating the classloader
     * (maybe if reading some meta-data did not work properly)
     */
    protected void createContextClassLoader(Deployment deployment, Scope scope) throws J2eeDeploymentException {
        
        try{
            // get urls we want all classloaders of this application to share
            Set allUrls=new java.util.HashSet();
            
            // first we add the common urls (as does our parent)
            Iterator allCommonUrls=deployment.getCommonUrls().iterator();
            
            while(allCommonUrls.hasNext())
                allUrls.add(allCommonUrls.next());
            
            // then the ejbmodules urls
            Iterator allEjbModules=deployment.getEjbModules().iterator();
            
            while(allEjbModules.hasNext()) {
                Iterator allLocalUrls=((Deployment.Module) allEjbModules.next()).getLocalUrls().iterator();
                while(allLocalUrls.hasNext())
                    allUrls.add(allLocalUrls.next());
            }
            
            // then the web modules urls
            Iterator allWebModules=deployment.getWebModules().iterator();
            
            while(allWebModules.hasNext()) {
                Iterator allLocalUrls=((Deployment.Module) allWebModules.next()).getLocalUrls().iterator();
                while(allLocalUrls.hasNext())
                    allUrls.add(allLocalUrls.next());
            }
            
            // create classloader with parent from context
            ClassLoader parent = Thread.currentThread().getContextClassLoader();
            // using the factory method
            ScopedURLClassLoader appCl = createScopedContextClassLoader((URL[]) allUrls.toArray(new URL[allUrls.size()]),parent,deployment,scope);
            
            // set the result loader as the context class
            // loader for the deployment thread
            Thread.currentThread().setContextClassLoader(appCl);
        } catch(Exception e) {
            throw new J2eeDeploymentException("could not construct context classloader",e);
        }
    }
    
    /** factory method for scoped url classloaders factored out. May throw a general
     * exception if this enterprise fails.
     * @return a freshly instantiated and configured class loader
     * @param scope the scope in which the classloader should be created
     * @param urls the urls for which the scoped
     * classloader should be generated
     * @param parent the parent loader
     * @param deployment the deployment to which this classloader is
     * associated.
     * @throws Exception to indicate
     * problems in constructing the
     * classloader (maybe because
     * meta-data was corrupt).
     */
    protected ScopedURLClassLoader createScopedContextClassLoader(URL[] urls,ClassLoader parent,Deployment deployment, Scope scope) throws Exception {
        return new ScopedURLClassLoader(urls,parent,deployment,scope);
    }
    
    /** Overrides the normal (re-)deploy method in order to
     * take dependent applications into account.
     *
     * @param _url the url (file or http) to the archiv to deploy
     * @throws MalformedURLException in case of a malformed url
     * @throws J2eeDeploymentException if something went wrong...
     * @throws IOException if trouble while file download occurs
     */
    public void deploy(String _url) throws MalformedURLException, IOException, J2eeDeploymentException {
        deploy(_url,GLOBAL_SCOPE);
    }


    /** scoped (re-)deploy method. Using a particular scope name
     *
     * @param scope the scope in which the file should be deployed
     * @param _url the url (file or http) to the archiv to deploy
     * @throws MalformedURLException in case of a malformed url
     * @throws J2eeDeploymentException if something went wrong...
     * @throws IOException if trouble while file download occurs
     */
    public void deploy(String _url, String scopeName) throws MalformedURLException, IOException, J2eeDeploymentException {
	deploy(_url,getScope(scopeName));
    }

    /** scoped (re-)deploy method.
     *
     * @param scope the scope in which the file should be deployed
     * @param _url the url (file or http) to the archiv to deploy
     * @throws MalformedURLException in case of a malformed url
     * @throws J2eeDeploymentException if something went wrong...
     * @throws IOException if trouble while file download occurs
     */
    public void deploy(String _url, Scope scope) throws MalformedURLException, IOException, J2eeDeploymentException {
        // build url from string spec
        URL url = new URL(_url);
        
        // initialise teared down deployments just in case that nothing
        // is teared down
        List allTearedDown=new java.util.ArrayList();
        
        // undeploy first if it is a redeploy
        try {
            // use modified undeploy in order to tear down
            // dependent apps as well, reporting will be
            // done here!
            undeployWithDependencies(_url,allTearedDown,url);
        } catch (Exception _e) {
            // not a real exception; fresh deployment case
            allTearedDown.add(url);
        }
        
        // now we (re-)deploy the whole bunch that was teared down
        // with us
        Iterator allDeployments=allTearedDown.iterator();
        
        while(allDeployments.hasNext()) {
            URL nextUrl=(URL) allDeployments.next();
            // maybe this deployment has already been made as
            // a side effect of dependency analysis
            Deployment d = installer.findDeployment(nextUrl.toString());
            // then it would be non-null
            if(d==null) {
                // else we install it
                log.info("(Re-)Deploy J2EE application: " + nextUrl);
                try{
                    d=installApplication(nextUrl);
                    // and start it (and the depending stuff, before)
                    // reporting is done here
                    startApplication(d, scope);
                } catch(Exception e) {
                    uninstallApplication(nextUrl.toString());
                    throw new J2eeDeploymentException("could not start application "+nextUrl,e);
                }
            }
        }
    }
    
    /** Starts the successful downloaded deployment. <br>
     * Means the modules are deployed by the responsible container deployer
     * This version of the method does indeed start necessary
     * other applications as well.
     * @param scope the scope in which the deployment should be started
     * @param dep the deployment to start
     * @throws J2eeDeploymentException if an error occures for one of these
     *         modules
     */
    protected void startApplication(Deployment dep, Scope scope)  throws J2eeDeploymentException {
        // here we collect all the started deployments (not only dep)
        // in the order they should be deployed
        List deployments=new java.util.ArrayList();

        // recursively start all sub-deployments
        startApplication(dep, deployments,scope);
        
        Iterator allDeployments=deployments.iterator();
        
        while(allDeployments.hasNext()) {
            
            Deployment _d=(Deployment) allDeployments.next();
            
            // save the old classloader
            ClassLoader oldCl = Thread.currentThread().
            getContextClassLoader();
            
            // find out the corresponding classloader
            ScopedURLClassLoader source=(ScopedURLClassLoader)
            scope.classLoaders.get(_d.getLocalUrl());
            
            Thread.currentThread().setContextClassLoader(source);
            
            try{
                // enable the scoped classloader to setup some
                // metadata or such before entering application modules
                source.onDeploy();
                // redirect all modules to the responsible deployers
                startModules(_d,source,oldCl);
		// enabled the scoped classloader to run some
                // initialization logic after the application modules
		// have been setup
		source.afterStartup();
            } catch(Exception e) {
                stopApplication(_d,new java.util.ArrayList(),null,scope);
                throw new J2eeDeploymentException("could not deploy "+_d.getName());
            }
             
        }
        
    }
    
    /** Starts the successful downloaded deployment. <br>
     * Means the modules are deployed by the responsible container deployer
     * <comment author="cgjung">better be protected for subclassing </comment>
     * @param alreadyMarked the deployments (in order) that have already been installed and
     * that  must be properly deployed afterwards.
     * @param _d the deployment to start
     * @throws J2eeDeploymentException if an error occures for one of these
     *          modules
     */
    protected void startApplication(Deployment _d, List alreadyMarked, Scope scope) throws J2eeDeploymentException {
        
        ClassLoader parent=Thread.currentThread().getContextClassLoader();
        
        // set the context classloader for this application
        createContextClassLoader(_d,scope);
        
        // save the application classloader for later
        ScopedURLClassLoader appCl = (ScopedURLClassLoader)
        Thread.currentThread().getContextClassLoader();
        
        alreadyMarked.add(0,_d);
        
        String[] dependentStuff=appCl.getDependingApplications();
        
        for(int count=0;count<dependentStuff.length;count++) {
            
            // reinstall parent
            Thread.currentThread().setContextClassLoader(parent);
            
            try{
                URL absoluteUrl=new URL(_d.getSourceUrl(),dependentStuff[count]);
                
                Deployment newD=installer.
                    findDeployment(absoluteUrl.toString());
                
                if(newD==null) {
                    log.info("Deploying dependent application "+absoluteUrl);
                    try{
                        newD = installApplication(absoluteUrl);
                    startApplication(newD,alreadyMarked,scope);
            } catch(Exception e) {
                try{
                    uninstallApplication(absoluteUrl.toString());
                }catch(IOException _e) {
                    log.error("Could not properly uninstall application "+absoluteUrl);
                }
                throw new J2eeDeploymentException("could not install dependent application "+dependentStuff[count],e);
            }
                }
            } catch(java.net.MalformedURLException e) {
                throw new J2eeDeploymentException("could not construct url for dependent application "+dependentStuff[count]);
            }
        } // for
        
        // reinstall parent
        Thread.currentThread().setContextClassLoader(parent);
    }
    
    /** A new stop method that stops a running deployment
     * and its dependent applications and that logs their
     * urls (where the current deployment will be redeployed under newUrl)
     * in a set for redeployment.
     *
     * @param scope the scope in which the running deployment has been found and in which the
     * dependent ones must be found, too.
     *
     * @param _d deployment to stop
     * @param redeployUrls collects the sourceUrls of the
     * undeployed apps
     * @param newUrl the url under which the current deployment should be redeployed, if at all
     * @throws J2eeDeploymentException to
     * indicate problems in undeployment.
     */
    protected void stopApplication(Deployment _d, List redeployUrls, URL newUrl, Scope scope) throws J2eeDeploymentException {
        
        // synchronize on the scope
        synchronized(scope.classLoaders) {
            
            // find out the corresponding classloader
            ScopedURLClassLoader source=(ScopedURLClassLoader)
            scope.classLoaders.get(_d.getLocalUrl());
            
            // its still here, so the thing is not already stopped
            if(source!=null) {
                
                try{
                    log.info("About to stop application "+_d.getName());
                    
                    // add it to the stopped list
                    redeployUrls.add(newUrl);
                    
                    // get dependency information
                    Iterator allDependencies=scope.getDependentClassLoaders(source).
                    iterator();
                    
                    // deregister classloader
                    scope.deRegisterClassLoader(source);
                    
                    // first stop the dependent stuff
                    while(allDependencies.hasNext()) {
                        ScopedURLClassLoader dependentLoader=(ScopedURLClassLoader)
                        allDependencies.next();
                        
                        stopApplication(dependentLoader.deployment,
                        redeployUrls,dependentLoader.deployment.getSourceUrl(),scope);
                    }
                    
                } finally {
                    try{
                        // now we do the real stopping
                        super.stopApplication(_d);
                        // and leave a last message to the classloader to
                        // tear down meta-data or such
                        source.onUndeploy();
                    } finally {
                        try{
                            uninstallApplication(_d);
                        } catch(IOException e) {
                            log.error("could not properly uninstall "+_d.getName());
                        }
                    }
                }
                
            } // if
        } // sync
        
    }
    
    /** Overloads the proper stop in order to
     *  be redirected to the dependency stopper
     * @param scope the scope in which the deployment should be stopped.
     *
     * @param _d the deployment to stop
     * @throws J2eeDeploymentException if an error occures for one of these
     *          modules
     */
    protected void stopApplication(Deployment _d, Scope scope) throws J2eeDeploymentException {
        stopApplication(_d,new java.util.ArrayList(),_d.getSourceUrl(),scope);
    }
    
    /** Undeploys the given URL (if it is deployed) and returns an array
     * of deployments that have been teared down
     * Actually only the file name is of interest, so it dont has to be
     * an URL to be undeployed, the file name is ok as well.
     * @param _app the stirng spec of the app to tear down
     *
     * @param allTearedDown collection of deployments that have been teared down as a result.
     *
     * @param newUrl url under which the application is to be redeployed, if at all
     *
     * @throws J2eeDeploymentException if something went wrong (but should have removed all files)
     * @throws IOException if file removement fails
     */
    public void undeployWithDependencies(String _app, List allTearedDown, URL newUrl ) throws IOException, J2eeDeploymentException {
        // find currect deployment
        Deployment d = installer.findDeployment(_app);
        
        if (d == null)
            throw new J2eeDeploymentException("The application \""+name+"\" has not been deployed.");
        
        Iterator allScopes=scopes.values().iterator();
        
        while(allScopes.hasNext()) {
            Scope nextScope=(Scope) allScopes.next();
            
            if(nextScope.classLoaders.get(d.getLocalUrl())!=null) {
                // use dependency stopper that uninstalls already
                stopApplication(d, allTearedDown, newUrl,nextScope);
                return;
            }
        }
        
        throw new J2eeDeploymentException("could not find scope for deployment "+d.getName());
        
        
    }
    
    /** Overrides parent undeploy in order to dispatch
     * to the dependency undeployer
     * @param _app name of the application to tear down
     *
     * @throws J2eeDeploymentException if something went wrong (but should have removed all files)
     * @throws IOException if file removement fails
     */
    public void undeploy(String _app) throws IOException, J2eeDeploymentException {
        undeployWithDependencies(_app,new java.util.ArrayList(),null);
    }
    
}
