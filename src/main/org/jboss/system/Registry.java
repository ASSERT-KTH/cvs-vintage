/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.system;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * A registry, really, a registry.
 *
 * <p>All methods static to lookup pointers from anyplace in the VM.  
 *    We use it for hooking up JMX managed objects.  Use the JMX MBeanName 
 *    to put objects here.
 *  
 * @author <a href="mailto:marc.fleury@jboss.org>Marc Fleury</a>
 * @version $Revision: 1.2 $
 */
public class Registry
{
   public static Map entries = Collections.synchronizedMap(new HashMap());
   
   public static void bind(Object key, Object value) { entries.put(key,value);}
   
   public static Object unbind(Object key) { return entries.remove(key); }
   
   public static Object lookup(Object key) { return entries.get(key); }
}
