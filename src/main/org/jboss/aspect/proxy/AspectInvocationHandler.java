/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.aspect.proxy;

import java.lang.reflect.Method;

import org.jboss.proxy.compiler.InvocationHandler;
import org.jboss.proxy.compiler.ProxyImplementationFactory;

import org.jboss.aspect.AspectDefinition;
import org.jboss.aspect.IAspectInterceptor;
import org.jboss.aspect.util.IAspectEditor;

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
public class AspectInvocationHandler implements InvocationHandler, IAspectEditor {
	
	AspectDefinition composition;
	private Object attachments[];
	Object targetObject;
   Class targetClass;

	public AspectInvocationHandler(AspectDefinition composition, Object targetObject) {
		this.composition = composition;
      if( targetObject instanceof Class )
         this.targetClass = (Class)targetObject;
      else
   		this.targetObject = targetObject;
	}
	
	/**
	 * @see java.lang.reflect.InvocationHandler#invoke(Object, Method, Object[])
	 */
	public Object invoke(Object target, Method method, Object[] args)
		throws Throwable {
		AspectInvocation i =
			new AspectInvocation(target, method, args, this);
		return i.invokeNext();
	}
	
	Object[] getAttachments() {
		if( attachments==null )
			attachments = new Object[composition.interceptors.length];
		return attachments;
	}
	
   /////////////////////////////////////////////////////////////////
   //
   // Methods that implement the IAspectEditor interface
   //
   /////////////////////////////////////////////////////////////////   
   public int getInterceptorListSize()
   {
      return composition.interceptors.length;
   }

   public void insertInterceptor(int position, IAspectInterceptor interceptor, Object config, Object attachment)
      throws AspectInitizationException
   {
      if( interceptor.getInterfaces(config).length > 0 )
         throw new IllegalArgumentException("Only detyped interceptors can be added.");
      
      IAspectInterceptor interceptors[] = new IAspectInterceptor[getInterceptorListSize()+1];
      arrayinsert( composition.interceptors, interceptors, position );
      interceptors[position] = interceptor;
      
      Object interceptorConfigs[] = new Object[getInterceptorListSize()+1];
      arrayinsert( composition.interceptorConfigs, interceptorConfigs, position );
      interceptorConfigs[position] = config;

      Object attachments[] = new Object[getInterceptorListSize()+1];
      arrayinsert( this.attachments, attachments, position );
      attachments[position] = attachment;
      this.attachments = attachments;
      
      composition = new AspectDefinition(
         composition.name,
         interceptors,
         interceptorConfigs,
         composition.interfaces,
         composition.targetClass);
         
   }
   
   public void removeInterceptor(int position)
   {
      
      if( composition.interceptors[position].getInterfaces(
            composition.interceptorConfigs[position]).length > 0 )
         throw new IllegalArgumentException("Only detyped interceptors can be removed.");
      
      IAspectInterceptor interceptors[] = new IAspectInterceptor[getInterceptorListSize()-1];
      arrayremove( composition.interceptors, interceptors, position );
      
      Object interceptorConfigs[] = new Object[getInterceptorListSize()-1];
      arrayremove( composition.interceptorConfigs, interceptorConfigs, position );

      Object attachments[] = new Object[getInterceptorListSize()-1];
      arrayremove( this.attachments, attachments, position );
      this.attachments = attachments;
      
      composition = new AspectDefinition(
         composition.name,
         interceptors,
         interceptorConfigs,
         composition.interfaces,
         composition.targetClass);
   }
   
   private static void arrayinsert( Object src[], Object dest[], int position) {
      System.arraycopy(src,0,dest,0,position);
      System.arraycopy(src,position,dest,position+1,src.length-position);
   }
   
   private static void arrayremove( Object src[], Object dest[], int position) {
      System.arraycopy(src,0,dest,0,position);
      System.arraycopy(src,position+1,dest,position,(src.length-position)-1);
   }


   public void setTargetObject(Object targetObject)
   {
        this.targetObject=targetObject;
   }

}
