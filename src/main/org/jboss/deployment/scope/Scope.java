/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.deployment.scope;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jboss.logging.Logger;

/**
 * Scope is a manager/mediator that connects several ScopedURLClassLoaders
 * with each other and computes their dependencies. The locks used in the scope
 * implementation are quite coarse-grained, maybe thread-unfriendly, but the
 * rationale is that classloading:
 *  a) happens not too often (hopefully) in the lifecycle of an application 
 *  b) will dispatch only in special cases (where applications depliberately 
 *     share classes) to this scope class 
 *  c) is optimized by caching the locations.
 * @author  cgjung
 * @version $Revision: 1.7 $
 */

public class Scope {
    
    /** keeps a map of class loaders that participate in this scope */
    final protected Map classLoaders=new HashMap();
    
    /** keeps a hashtable of dependencies between the classLoaders */
    final protected Map dependencies=new HashMap();
    
    /** keeps a hashtable of class appearances */
    final protected Map classLocations=new HashMap();
    
    /** keeps a hashtable of resource appearances */
    final protected Map resourceLocations=new HashMap();
    
    /** keeps a reference to a logger which which to interact */
    final protected Logger log;
    
    /**  
     * Creates new Scope
     * @param log - The logger this new scope will interact with. 
     */ 
    public Scope(Logger log) {
        this.log=log;
    }
    
    /** 
     * Registers a classloader in this scope. 
     * 
     * @param loader - The classloader to register. 
     * @return The newly registered classloader. 
     */ 
    public ScopedURLClassLoader registerClassLoader(ScopedURLClassLoader loader) {
        // must synchronize not to collide with deregistrations and
        // dependency logging
        synchronized(classLoaders) {
            return (ScopedURLClassLoader) classLoaders.put(loader.deployment.getLocalUrl(),loader);
        }
    }
    
    /** 
     * Deregisters a classloader in this scope. 
     * Removes all cached data related to this classloader. 
     * 
     * @param loader - The classloader to deregister. 
     * @return The newly deregistered classloader. 
     */ 
    public ScopedURLClassLoader deRegisterClassLoader(ScopedURLClassLoader loader) {
        // synchronized not to collide with registrations
        // and dependency logging
        synchronized(classLoaders) {
            // remove class locations
            clearByValue(classLocations,loader);
            // remove resource locations
            clearByValue(resourceLocations,loader);
            // remove dependency annotations
            dependencies.remove(loader);
            // and remove the loader
            return (ScopedURLClassLoader) classLoaders.remove(loader.deployment.getLocalUrl());
        }
    }
    
    /** 
     * Helper method that will clear all entries from a map 
     * with a dedicated target value. 
     * 
     * @param map - The map we want to clear entries from. 
     * @param value - The object we want to remove from that map. 
     */ 
    protected void clearByValue(Map map, Object value) {
        Iterator values=map.values().iterator();
        while(values.hasNext()) {
            if(values.next().equals(value))
                // uses the very useful remove method of the value iterator!
                values.remove();
        }
    }
    
    /** 
     * Returns the classLoaders that a particular classLoader is 
     * dependent on. Should be called after locking classLoaders. 
     * 
     * @param loader - The classloader we want to find dependencies of. 
     * @return The set of classloaders this classloader is dependent on. 
     */ 
    public Set getDependentClassLoaders(ScopedURLClassLoader loader) {
        Set result=(Set) dependencies.get(loader);
        if(result==null)
            result=new HashSet();
        return result;
    }

    /** 
     * Adds a dependency between two classloaders. This can be called 
     * from within application threads that require resource loading. 
     * Should be called after locking classLoaders. 
     * 
     * @param source - The classloader we are adding dependency from. 
     * @param target - The classloader we are adding dependency to. 
     * @return true if the operation was successful, false otherwise. 
     */ 
    protected boolean addDependency(ScopedURLClassLoader source, ScopedURLClassLoader target) {
        // no rescursions necessary (but not volatile for the code)
        if(source !=null && target!=null && !source.equals(target)) {
            
            Set deps=(Set) dependencies.get(target);
            
            if(deps==null) {
                deps=new HashSet();
                dependencies.put(target,deps);
            }

            if (log.isDebugEnabled())
               log.debug("Adding dependency from deployment "+source+":"+source.deployment.getLocalUrl()+" to deployment "+
                  target+":"+target.deployment.getLocalUrl());

            return deps.add(source);
        } else
            return false;
    }
    
    /** 
     * Loads a class on behalf of a given classloader. 
     *   
     * @param className - The name of the class we are trying to load. 
     * @param resolve - Whether or not the class should be resolved. 
     * @param source - The classloader we want to load the class with. 
     * 
     * @throws ClassNotFoundException - If the class cannot be loaded 
     * properly.  
     * @return The new class that has been loaded.  
     */ 
    public Class loadClass(String className, boolean resolve, ScopedURLClassLoader source)
    throws ClassNotFoundException {
        
        // short look into the class location cache, is synchronized in
        // case that the relevant target is simultaneously teared down
        synchronized(classLoaders) {
            ScopedURLClassLoader target= (ScopedURLClassLoader)
            classLocations.get(className);
            
            // its there, so log and load it
            if(target!=null) {
                addDependency(source,target);
                // we can be sure that the target loader
                // has the class already in its own cache
                // so this call should not cost much
                return target.loadClass(className,resolve);
            }
            
            // otherwise we do a big lookup
            Iterator allLoaders=classLoaders.values().iterator();
        
            while(allLoaders.hasNext()) {
                target=(ScopedURLClassLoader) allLoaders.next();
            
            // no recursion, please
            if(!target.equals(source)) {
                try{
                    Class foundClass=target.loadClassProperly(className,resolve);
                    classLocations.put(className,target);
                    addDependency(source,target);
                    return foundClass;
                } catch(ClassNotFoundException e) {
                    // proceed with the next loaders in scope
                }
            }
        } // while
        
        // no loader in the scope has been able to load the class
        throw new ClassNotFoundException("could not resolve class "+
        className+" in scope.");
        
        } // sync
    }
    
    /** 
     * Gets a URL on behalf of a given classloader which may be null 
     * in case that we do not check dependencies. 
     * 
     * @param name - The name of the target ScopedURLClassLoader. 
     * @param source - The source ScopedURLClassLoader. 
     * @return The name of the target URL where the resource was loaded 
     * or null if no loader in the scope was able to load the 
     * resource. 
     */ 
    public URL getResource(String name, ScopedURLClassLoader source) {
        
        // short look into the resource location cache, is synchronized in
        // case that the relevant target is simultaneously teared down
        synchronized(classLoaders) {
            ScopedURLClassLoader target= (ScopedURLClassLoader)
                resourceLocations.get(name);
            
            // its there, so log and load it
            if(target!=null)
                addDependency(source,target);
        
           // the lock is released here, so that other threads could run too
            if(target!=null)
                return target.getResource(name);
        
            // otherwise we do a big lookup
            Iterator allLoaders=classLoaders.values().iterator();
        
            while(allLoaders.hasNext()) {
                target=(ScopedURLClassLoader) allLoaders.next();
            
            // no recursion, please
            if(!target.equals(source)) {
                    URL foundResource=target.getResourceProperly(name);
                    if(foundResource!=null) {
                        resourceLocations.put(name,target);
                        addDependency(source,target);
                        return foundResource;
                    }
            }
        } // while
        
        // no loader in the scope has been able to load the resource
        return null;
        
        } // sync
    }

}
