package org.jboss.aspect.util;

import java.util.Map;

import org.jboss.aspect.IAspectInterceptor;
import org.jboss.aspect.proxy.AspectInitizationException;

/**
 * 
 */
public interface IAspectEditor 
{
   
   public void insertInterceptor(int position, IAspectInterceptor interceptor, Object config, Object attachment) throws AspectInitizationException;
   public void removeInterceptor(int position);
   public int  getInterceptorListSize();
   public void setTargetObject(Object targetObject);
     
   
}
