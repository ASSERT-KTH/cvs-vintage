/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/
package org.jboss.aspect.spi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * An aspect object is in reality a Dynamic Proxy which forwards all
 * method calls to a AspectObject ( an InvocationHandler ).
 * 
 * The AspectObject stores all the state information associated
 * with an aspect object instance.  When the method call occurs, that 
 * state information is passed down in an AspectInvocation to the first
 * interceptor defined in the aspect definition.
 * 
 * An AspectObject can be used to change the interceptor stack configuration 
 * for an instanciated aspect object.  Changing the state of the AspectObject
 * while method calls are being made on the aspect object is garanteed to be 
 * thread-safe. 
 * 
 * 
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
final public class AspectObject implements InvocationHandler
{

    private final static ThreadLocal invocationContexThreadLocal = new ThreadLocal();
    AspectDefinition definition;
    Object targetObject;
    Map attachments = new HashMap();
    InvocationHandler targetObjectIH = null;

    public AspectObject(AspectDefinition composition, Object targetObject)
    {
        this.definition = composition;
        setTargetObject(targetObject);
    }

    /**
     * Returns the current AspectInvocation that is being carried 
     * invoked.  If no AspectInvocation is currently being invoked,
     * null is returned.
     * 
     */
    static AspectInvocation getContextAspectInvocation()
    {
        Stack invocationStack = (Stack) invocationContexThreadLocal.get();
        if (invocationStack == null)
            return null;
        return (AspectInvocation) invocationStack.peek();
    }

    /**
     * Creates an AspectInvocation and 
     * - puts it on a ThreadLocal object so that it can be retrieved later at any time.
     * - passes it down the interceptor stack.
     * 
     */
    public Object invoke(Object target, Method method, Object[] args) throws Throwable
    {
        AspectInvocation invocation = new AspectInvocation(this, target, method, args);

        Stack invocationStack = (Stack) invocationContexThreadLocal.get();
        if (invocationStack == null)
        {
            invocationStack = new Stack();
            invocationContexThreadLocal.set(invocationStack);
        }
        try
        {
            invocationStack.push(invocation);
            return invocation.invokeNext();
        }
        finally
        {
            invocationStack.pop();
        }
    }

    /**
     * Returns the attachments.
     * @return Map
     */
    public Map getAttachments()
    {
        return attachments;
    }

    /**
     * Returns the definition.
     * @return AspectDefinition
     */
    public AspectDefinition getDefinition()
    {
        return definition;
    }

    /**
     * Returns the targetObject.
     * @return Object
     */
    public Object getTargetObject()
    {
        return targetObject;
    }

    /**
     * Sets the attachments.
     * @param attachments The attachments to set
     */
    public void setAttachments(Map attachments)
    {
        this.attachments = attachments;
    }

    /**
     * Sets the definition.
     * @param definition The definition to set
     */
    public void setDefinition(AspectDefinition definition)
    {
        this.definition = definition;
    }

    /**
     * Sets the targetObject.
     * @param targetObject The targetObject to set
     */
    public void setTargetObject(Object targetObject)
    {
        // Find out if the target is a Proxy object, then cause can 
        // bypass Proxy interface and hit the InvocationHandler's invoke directly.
        this.targetObject = targetObject;
        if( targetObject!=null ) 
        {
	        try
	        {
	            this.targetObjectIH = Proxy.getInvocationHandler(targetObject);
	        }
	        catch (IllegalArgumentException e)
	        {
	            this.targetObjectIH = null;
	        }
        }
    }

}
