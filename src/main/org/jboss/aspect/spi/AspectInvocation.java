/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.aspect.spi;

import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;
import java.util.Stack;

import org.jboss.aspect.AspectRuntimeException;
import org.jboss.aspect.internal.*;


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
final public class AspectInvocation {
	
	public AspectInvocationHandler handler;
	public int currentInterceptor=-1;
	
	/** the aspect object that the invocation was performed on */
	public Object aspectObject;
	/** the method that was call on the aspect object */
	public Method method;
	/** the arguments that were passed in the method call */
	public Object[] args;

	/** 
	 * Constructor used by the AspectInvocationHandler
	 * to create a AspectInvocation.
	 */
	public AspectInvocation(Object aspectObject, Method method, Object[] args, AspectInvocationHandler handler) {
		this.aspectObject=aspectObject;
		this.method=method;
		this.args=args;
		this.handler=handler;
	}

    public static AspectInvocation getContextAspectInvocation() {
    	return AspectInvocationHandler.getContextAspectInvocation();
    }

	/**
	 * Passes the method invocation to the next Interceptor
	 * in the interceptor list.
	 */
	public Object invokeNext() throws Throwable {
      
      AspectInterceptorHolder holders[] = handler.definition.interceptors;
      int storeInterceptorIndex = currentInterceptor;
      try {
         
         // Iterate until we find an interceptor that wants to process the 
         // method call.  Ths avoids producing needlessly deep call stacks.
         while( true ) {
            currentInterceptor++;
                        
            // Did we go past the last interceptor??
   			if(currentInterceptor==holders.length) {
   				
   				// Invoke the target object is we can.
   				Object o = handler.targetObject;
   				if( o != null )
	   				return method.invoke(o, args);
	   				
				throw new AspectRuntimeException("Aspect '"+handler.definition.name+"' failed to process a method call: "+method.getName()+", check your aspect definition.");
   			}
            if( holders[currentInterceptor].isIntrestedInMethodCall(method) )
            	return holders[currentInterceptor].interceptor.invoke(this);
         }
		} finally {
			currentInterceptor=storeInterceptorIndex;
		}
	}

   public boolean isNextIntrestedInMethodCall() {
      AspectInterceptorHolder holders[] = handler.definition.interceptors;
      for( int i=currentInterceptor+1; i < holders.length; i++ ) {
         if( holders[currentInterceptor].isIntrestedInMethodCall(method) )
            return true;
      }
      return false;
   }

	/**
	 * Returns the Map that holds all objects that are attached
	 * to the Aspect obejct.
	 */
	public Map getAttachments() {
		return handler.attachments;
	}
	
}
