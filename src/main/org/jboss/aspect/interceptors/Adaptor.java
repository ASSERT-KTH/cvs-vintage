package org.jboss.aspect.interceptors;

/**
 * This interface allows you to query an object for 
 * for a given interface.
 * 
 */
public interface Adaptor
{
   public Object getAdapter(Class type);
}
