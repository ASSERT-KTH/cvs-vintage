package org.jboss.aspect.util;

import java.util.Map;
import java.util.Set;

import org.jboss.aspect.IAspectInterceptor;
import org.jboss.aspect.proxy.AspectInitizationException;

/**
 * 
 */
public interface IAspectEditor 
{
   
   public void insertInterceptor(int position, IAspectInterceptor interceptor, Set methodFilter, Object attachment) throws AspectInitizationException;
   public void removeInterceptor(int position);
   public int  getInterceptorListSize();
   public void setTargetObject(Object targetObject);
     
   
}
