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
 * The AspectFactory's configuration XML file will look something like:
 * 
<pre>&lt;a:aspects 
<em><strong>	XML namespaces are used to seperate aspect configuration data and
	interceptor configuration data.
</strong></em>	xmlns:a=&quot;org.jboss.aspect&quot;
	xmlns:di=&quot;org.jboss.aspect.interceptors.DelegatingInterceptor&quot;
	xmlns:jii=&quot;org.jboss.aspect.interceptors.JMXInvokerInterceptor&quot;&gt; 
<em><strong>
	define an aspect named &quot;AandB&quot;</strong></em>
	&lt;a:aspect a:name=&quot;AandB&quot;&gt;<em><strong>
		</strong></em><strong><em>You list the interceptors that will make up the aspect here</em></strong>

<strong><em>		The followiing interceptor delegates method calls to a lazy load ObjectA object,
		the aspect object will have all the interfaces that ObjectA exposes.
		</em></strong>&lt;a:interceptor a:class=&quot;org.jboss.aspect.interceptors.DelegatingInterceptor&quot;
			di:delegate=&quot;org.foo.ObjectA&quot;/&gt;

<em><strong>		The following interceptor sends all method invocations it receives 
		to a JMX MBean.  The aspect object will provide the InterfaceB interface that is listed.
</strong></em>		&lt;a:interceptor a:class=&quot;org.jboss.aspect.interceptors.JMXInvokerInterceptor&quot;
			jii:mbean=&quot;jboss-test:service=MockMMBean&quot;&gt;
			&lt;jii:expose-interface jii:class=&quot;org.foo.InterfaceB&quot;/&gt;
		&lt;/a:interceptor&gt;
	&lt;/a:aspect&gt;
   
&lt;/a:aspects&gt;
 </pre>
 * 
 * @see org.jboss.aspect.spi.AspectInterceptor
 *
 * @author <a href = "mailto:hchirino@jboss.org" > Hiram Chirino </a>
 */
public class AspectFactory
{
	private static ThreadLocal contextAspectFactory = new ThreadLocal();
	
    private AspectFactory parent;
    private Map aspects = new HashMap();

    public AspectFactory()
    {
    }
    
    public AspectFactory(AspectFactory parent)
    {
        this.parent = parent;
    }
    
    /**
     * Allows you to associate an AspectFactory with the current thread.
     * The associated AspectFactory can be obtained later through a call to 
     * getContextAspectFactory().
     */
    static public void setContextAspectFactory(AspectFactory af) {
    	contextAspectFactory.set( af );
    }
    
    /**
     * Allows you to get the AspectFactory that was associated with the 
     * current thread.
     */
    static public AspectFactory getContextAspectFactory() {
    	return (AspectFactory)contextAspectFactory.get();
    }
    
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

    /**
     * Gives you back the AspectDefinition for a given aspect name.
     * If not defined in the AspectFactory, the parent is searched for
     * the definition.
     */
    public AspectDefinition getDefinition(String aspect)
    {
        AspectDefinition rc = (AspectDefinition) aspects.get(aspect);
        if (rc == null && parent != null)
            rc = parent.getDefinition(aspect);
        return rc;
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
