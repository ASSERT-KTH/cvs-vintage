/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/
package org.jboss.aspect;

import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jboss.aspect.internal.AspectSupport;
import org.jboss.aspect.spi.AspectDefinition;
import org.jboss.aspect.spi.AspectObject;
import org.jboss.util.Classes;

/**
 * The AspectFactory creates new instances of aspects objects.
 * <p>
 * 
 * An aspect is povides you a simple and dynamic way to compose
 * new objects or to dynamicaly alter the behavior of existing 
 * objects.  An aspect works on the principle that the interfaces
 * that an object provides can be extended/modified at runtime by 
 * using a Dynamic Proxy to filter the method calls destined
 * to the object through a list of AspectInterceptor objects.
 * <p>
 * 
 * The AspectInterceptor interface allows you to implement
 * custom aspect behaviors.  The behaviors that a AspectInterceptor
 * can implement are only limited by your imagination.  Keep
 * in mind that this "interceptor" pattern is what implements the 
 * EJB containers in jboss (interceptors provide security, transaction
 * managment, lazy loading, etc.) 
 * <p>
 * 
 * Before you can create an aspect with an AspectFactory,
 * you must configure the AspectFactory.  The simplest way to do 
 * this is shown in the following example:
 * <code><pre>
 *AspectFactory af = new AspectFactory().configure();
 * </pre></code>
 * 
 * This will configure the AspectFactory by searching the CLASSPATH 
 * for the "aspect-config.xml" file. 
 *
 * You can add multiple aspect configurations to the AspectFactory by 
 * using the <code>configure(URL configSource)</code> method.  Example:
 * <code><pre>
 *AspectFactory af = 
 *	new AspectFactory().configure(new URL("file:aspect-set1.xml"))
 *		.configure(new URL("file:aspect-set2.xml"));
 * </pre></code>
 * 
 * After the AspectFactory has been configured, you can create a new aspect
 * object by using one of the <code>createAspect(...)</code> methods.  Example:
 * 
 * <code><pre>s
 *AspectFactory af = new AspectFactory().configure();
 *...
 *Accout account = new Account( ... );
 *Runnable aspect = (Runnable)af.createAspect("foo.Transacted", account);
 *aspect.run();
 * </pre></code>
 * 
 * 
 * @see org.jboss.aspect.spi.AspectInterceptor
 *
 * @author < a href = "mailto:hchirino@jboss.org" > Hiram Chirino < / a >
 */
public class AspectFactory
{

    private Map aspects = new HashMap();

    /////////////////////////////////////////////////////////
    //
    // The following methods allow you to configure the Factory.
    //
    /////////////////////////////////////////////////////////

    /**
     * Searches for the <code>aspect-config.xml</code> file in the classpath
     * and adds the aspects contained in the file to the AspectFactory configuration.
     * 
     * @throws AspectInitizationException if the aspect-config.xml file is not in the 
     *          classpath or if it is invalid or some aspects could not be initialized.
     */
    public AspectFactory configure() throws AspectInitizationException
    {
        URL source = Thread.currentThread().getContextClassLoader().getResource("aspect-config.xml");
        if (source == null)
            throw new AspectInitizationException("aspect-config.xml could not be found in the classpath");
        return configure(source);
    }

    /**
     * Adds the the set of aspect definitions contained in the source URL
     * to the AspectFactory configuration.
     * <p>
     * If the source contains a definition for a previously configured aspect,
     * a AspectInitizationException will be thrown.
     * 
     * @throws AspectInitizationException - if the source file is invalid or some aspects could not be initialized.
     */
    public AspectFactory configure(URL source) throws AspectInitizationException
    {
        if (source == null)
            throw new NullPointerException("configuration source was null.");

        Map map = AspectSupport.loadAspects(source);
        Iterator i = map.keySet().iterator();
        while (i.hasNext())
        {
            Object aspectName = i.next();
            Object composition = map.get(aspectName);
            Object previous = aspects.put(aspectName, composition);
            if (previous != null)
                throw new AspectInitizationException("Invalid Aspect configuration file: " + source);
        }
        return this;
    }

    public AspectDefinition getDefinition(String aspect)
    {
        return (AspectDefinition) aspects.get(aspect);
    }

    /////////////////////////////////////////////////////////
    //
    // The following methods allow you to create 
    // new Aspect objects (the dynamic proxies)
    //
    /////////////////////////////////////////////////////////

    /**
     * @throws AspectNotFoundException if the aspectName was not configured with the AspectFactory
     */
    public Object createAspect(Object targetObject) throws AspectNotFoundException
    {
        String aspectName = targetObject.getClass().getName();
        AspectDefinition composition = getDefinition(aspectName);
        if (composition == null)
            throw new AspectNotFoundException(aspectName);
        return createAspect(composition, targetObject);
    }

    /**
     * Creates an aspect object from a given aspect name.
     * 
     * This form of createAspect sets the targetObject of the aspect to 
     * be the provided object.
     * <p>
     * All interfaces of the targetClass will also be exposed by the aspect 
     * object.
     * 
     * @throws AspectNotFoundException if the aspectName was not configured with the AspectFactory
     */
    public Object createAspect(String aspectName, Object targetObject) throws AspectNotFoundException
    {
        AspectDefinition composition = getDefinition(aspectName);
        if (composition == null)
            throw new AspectNotFoundException(aspectName);
        return createAspect(composition, targetObject);
    }

    /**
     * Creates an aspect object from a given AspectDefinition.
     * 
     * Instead of using XML files to define the aspects, you can 
     * create new AspectDefinition objects to create new types
     * of aspects.
     * 
     */
    static public Object createAspect(AspectDefinition composition, Object targetObject)
    {
        AspectObject h = new AspectObject(composition, targetObject);
        Class interfaces[] = composition.interfaces;
        if (targetObject != null)
            interfaces = AspectSupport.appendInterfaces(interfaces, targetObject.getClass());
        return Proxy.newProxyInstance(Classes.getContextClassLoader(), interfaces, h);
    }

    static public boolean isAspectObject(Object aspectObject)
    {
        try
        {
            getAspectObject(aspectObject);
            return true;
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
    }

    static public AspectObject getAspectObject(Object aspectObject) throws IllegalArgumentException
    {
        try
        {
            return (AspectObject) Proxy.getInvocationHandler(aspectObject);
        }
        catch (Throwable e)
        {
            throw new IllegalArgumentException();
        }
    }

}
