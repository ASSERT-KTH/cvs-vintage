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
import java.util.HashMap;
import java.util.Map;

import org.jboss.aspect.AspectRuntimeException;
import org.jboss.util.Coercible;
import org.jboss.util.CoercionException;

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
final public class AspectInvocation implements Coercible
{
	/** 
	 * If an interceptor throws a WrappedRuntimeException,
	 * the original exception will be rethrown by the AspectObject.
	 */
	public static class WrappedRuntimeException extends RuntimeException {
		public Throwable original;
		WrappedRuntimeException(Throwable original) {
			this.original=original;
		}
	}
	
	/** 
	 * If the RedoRuntimeException is thrown, The AspectObject
	 * will redo the method-call.  Usfull if an interceptor
	 * modified the AspectDefinition of an AspectObject and he wants
	 * the invocation to be done again using the new interceptor
	 * stack.
	 */
	public static class RedoRuntimeException extends RuntimeException {
	}

    /** the aspect definition of the aspect */
    final public AspectInterceptorHolder[] interceptors;
    /** attachments that have been made against the aspect object */
    final public Map aspectAttachments;
    /** the target object is the original object that the aspect was applyed to, could be null */
    final public Object targetObject;
    /** if the target object was a Proxy, then this is the InvocationHandler to the proxy */
    final public InvocationHandler targetObjectIH;

    /** the proxy object that the invocation was performed on */
    final public Object proxy;
    /** the method that was call on the aspect object */
    final public Method method;
    /** the arguments that were passed in the method call */
    final public Object[] args;

	// the index into the interceptor that we are currently executing in.
    private int currentInterceptor = -1;
    // Mapes Class types to implementations.  Used to provide coercions 
    // for the AspectInvocation.
    private Map coercibleTypes;

    /** 
     * Constructor used by the AspectInvocationHandler
     * to create a AspectInvocation.
     */
    public AspectInvocation(AspectObject handler, Object aspectObject, Method method, Object[] args)
    {
        this.interceptors = handler.definition.interceptors;
        this.aspectAttachments = handler.attachments;
        this.targetObject = handler.targetObject;
        this.targetObjectIH = handler.targetObjectIH;

        this.proxy = aspectObject;
        this.method = method;
        this.args = args;
    }

	/**
	 * Used to get the current AspectInvocation that is being called.
	 */
    public static AspectInvocation getContextAspectInvocation()
    {
        return AspectObject.getContextAspectInvocation();
    }

	/**
     * Passes the method invocation to the next Interceptor
     * in the interceptor list.  Any exception that is thrown
     * is wrapped in a RuntimeException which will be unwrapped
     * later at the AspectObject.
     * 
     * This method has higher overhead than calling invokeNext() directly
     * but it is handy if an interceptor method calling the next interceptor
     * does not delcare it throws Throwable.
	 * 
	 */
    public Object invokeNextAndWrapException() {
    	try {
    		return invokeNext();
    	} catch (Throwable e ) {
    		throw new WrappedRuntimeException(e);
    	}
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
	 * @return - true if there is another interceptor further down the stack
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


	/**
	 * Allows you to register a coercion so that at a later time 
	 * you can coerce this invocation into a specified type. 
	 * 
	 * @return The previously registered coercion for the given type.
	 */
	public Object registerCoercion( Class type, Object coercion) throws IllegalArgumentException {
		if( coercion==null || type == null )
			throw new NullPointerException("arguments cannot be null");
		if( type.isAssignableFrom(coercion.getClass()) )
			throw new IllegalArgumentException("type is not assignable from impl");
		if( coercibleTypes == null )
			coercibleTypes = new HashMap(10);
		return coercibleTypes.put( type, coercion);
	}

	/**
	 * Coerce this object into a specified type
	 * 
	 * @see org.jboss.util.Coercible#coerce(java.lang.Class)
	 */
	public Object coerce(Class type) throws CoercionException {
		if( coercibleTypes==null )
			return null;
		return coercibleTypes.get( type );
	}

}
