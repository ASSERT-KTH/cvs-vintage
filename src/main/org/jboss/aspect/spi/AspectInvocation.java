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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.jboss.aspect.AspectRuntimeException;

/**
 * A method call performed on an aspect will get encapsulated
 * into an AspectInvocation by the AspectInvocationHandler and
 * then passed down the AspectInterceptor list.
 * 
 * This object can be used by the Interceptors to get state data
 * about the aspect object and aspect/interceptor configuration.
 * 
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
final public class AspectInvocation
{

    /** the aspect definition of the aspect */
    final public AspectInterceptorHolder[] interceptors;
    /** attachments that have been made against the aspect object */
    final public Map attachments;
    /** the target object is the original object that the aspect was applyed to, could be null */
    final public Object targetObject;
    /** if the target object was a Proxy, then this is the InvocationHandler to the proxy */
    final public InvocationHandler targetObjectIH;

    /** the aspect object that the invocation was performed on */
    final public Object aspectObject;
    /** the method that was call on the aspect object */
    final public Method method;
    /** the arguments that were passed in the method call */
    final public Object[] args;

	// the index into the interceptor that we are currently executing in.
    private int currentInterceptor = -1;

    /** 
     * Constructor used by the AspectInvocationHandler
     * to create a AspectInvocation.
     */
    public AspectInvocation(AspectObject handler, Object aspectObject, Method method, Object[] args)
    {
        this.interceptors = handler.definition.interceptors;
        this.attachments = handler.attachments;
        this.targetObject = handler.targetObject;
        this.targetObjectIH = handler.targetObjectIH;

        this.aspectObject = aspectObject;
        this.method = method;
        this.args = args;
    }

    public static AspectInvocation getContextAspectInvocation()
    {
        return AspectObject.getContextAspectInvocation();
    }

    /**
     * Passes the method invocation to the next Interceptor
     * in the interceptor list.
     */
    public Object invokeNext() throws Throwable
    {

        int storeInterceptorIndex = currentInterceptor;
        try
        {

            // Iterate until we find an interceptor that wants to process the 
            // method call.  Ths avoids producing needlessly deep call stacks.
            while (true)
            {
                currentInterceptor++;

                // Did we go past the last interceptor??
                if (currentInterceptor == interceptors.length)
                {

                    // Invoke the target object if we can.
			        try {
	                    // Use the InvocationHandler of the target object proxy if we can.
	                    if (targetObjectIH != null)
	                        return targetObjectIH.invoke(targetObject, method, args);
	
	                    // Use reflection to call the method on the target object.
	                    else if (targetObject != null)
	                        return method.invoke(targetObject, args);
			        } catch ( InvocationTargetException e ) {
			        	throw e.getTargetException();
			        }

                    throw new AspectRuntimeException(
                        "Aspect failed to process a method call: "
                            + method.getName()
                            + ", check your aspect definition.");
                }
                if (interceptors[currentInterceptor].isIntrestedInMethodCall(method))
                    return interceptors[currentInterceptor].interceptor.invoke(this);
            }
        }
        finally
        {
            currentInterceptor = storeInterceptorIndex;
        }
    }

	/**
	 * @returns - true if there is another interceptor further down the stack
	 *            that would be interested in the method call.
	 */
    public boolean isNextIntrestedInMethodCall()
    {
        for (int i = currentInterceptor + 1; i < interceptors.length; i++)
        {
            if (interceptors[currentInterceptor].isIntrestedInMethodCall(method))
                return true;
        }
        return false;
    }

}
