package org.jboss.aspect.util;

/**
 * This interface allows you to query an object for 
 * for a given interface.
 * 
 */
public interface IAdaptor
{
   public Object getAdapter(Class type);
}
