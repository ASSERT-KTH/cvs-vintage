/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.aspect.spi;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import java.lang.reflect.InvocationHandler;

import org.jboss.aspect.*;

/**
 * An aspect object is in reality a Dynamic Proxy which forwards all
 * method calls to a AspectInvocationHandler.
 * 
 * The AspectInvocationHandler stores all the state information associated
 * with an aspect object instance.  When the method call occurs, that 
 * state information is passed down in a AspectInvocation to the first
 * interceptor defined in the aspect definition.
 * 
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
final public class AspectInvocationHandler implements InvocationHandler
{

    final static ThreadLocal invocationContexThreadLocal = new ThreadLocal();
    
    final AspectDefinition definition;
    final Object targetObject;
    final Map attachments = new HashMap();

    public AspectInvocationHandler(AspectDefinition composition, Object targetObject)
    {
        this.definition = composition;
        this.targetObject = targetObject;
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
        AspectInvocation invocation = new AspectInvocation(target, method, args, this);
        
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


}
