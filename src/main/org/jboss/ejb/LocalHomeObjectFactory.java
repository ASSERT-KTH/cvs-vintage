/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.Reference;
import javax.naming.RefAddr;
import javax.naming.spi.ObjectFactory;

/**
 * Based on Scott Stark's NonSerializableObjectFactory
 *
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 * @author <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 * @version $Revision: 1.6 $
 */
public class LocalHomeObjectFactory
   implements ObjectFactory
{
   private static Map applicationMap =
      Collections.synchronizedMap(new HashMap());
   
   private static Map containerMap =
      Collections.synchronizedMap(new HashMap());

   /**
    * Place an object into the NonSerializableFactory namespace for subsequent
    * access by getObject.
    *
    * @param key       the name to bind target under. This should typically 
    *                  be the name that will be used to bind target in the 
    *                  JNDI namespace, but it does not have to be.
    * @param target    the non-Serializable object to bind.
    */
   public static synchronized void rebind(String key,
                                          Application application,
                                          Container container)
   {
      applicationMap.put(key, application);
      containerMap.put(key, container); 
   }
   
   /**
    * Place or replace an object in the NonSerializableFactory namespce
    * for subsequent access by getObject. Any existing binding for key will be
    * replaced by target.
    *
    * <p>Remove a binding from the NonSerializableFactory map.
    *
    * @param key       the key into the NonSerializableFactory map to remove.
    * @param target    the non-Serializable object to bind.
    * 
    * @throws NameNotFoundException    thrown if key does not exist in the
    *                                  NonSerializableFactory map
    */
   public static void unbind(String key) throws NameNotFoundException
   {
      if( applicationMap.remove(key) == null )
         throw new NameNotFoundException
            (key+" was not found in the NonSerializableFactory map");
      containerMap.remove(key);
   }

   /**
    * Lookup a value from the NonSerializableFactory map.
    * 
    * @return   the object bound to key is one exists, null otherwise.
    */
   public static Object lookup(String key)
   {
      Application app = (Application) applicationMap.get(key);
      Container container = (Container) containerMap.get(key);
      return app.getLocalHome( container );
   }

   /**
    * Transform the obj Reference bound into the JNDI namespace into the
    * actual non-Serializable object.
    *
    * @param obj       the object bound in the JNDI namespace. This must be 
    *                  an implementation of javax.naming.Reference with a
    *                  javax.naming.RefAddr of type "nns" whose content is the
    *                  String key used to location the non-Serializable object
    *                  in the NonSerializableFactory map.
    * @param name      ignored.
    * @param nameCtx   ignored.
    * @param env       ignored.
    * @return          the non-Serializable object associated with the obj
    *                  Reference if one exists, null if one does not.
    */
   public Object getObjectInstance(Object obj,
                                   Name name,
                                   Context nameCtx,
                                   Hashtable env)
      throws Exception
   {
      // Get the nns value from the Reference obj and use it as the map key
      Reference ref = (Reference) obj;
      RefAddr addr = ref.get("nns");
      String key = (String) addr.getContent();
      Object target = lookup(key);
      return target;
   }
   // --- End ObjectFactory interface methods
}

