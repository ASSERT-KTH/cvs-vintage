/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/
package org.jboss.aspect.spi;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.dom4j.Element;
import org.jboss.aspect.AspectInitizationException;
import org.jboss.aspect.internal.AspectSupport;
import org.jboss.util.Classes;

/**
 * The AspectDefinition holds the definition for a aspect.
 * <p>
 * 
 * Multiple instances of the same aspect will share the AspectDefinition
 * configuration object. 
 * <p>
 * 
 * AspectDefinition objects can be dynamicaly created at runtime and passed
 * to the AspectFactory to create dynamicaly generated aspects.
 * <p>
 * 
 * @see org.jboss.aspect.AspectFactory#createAspect(AspectDefinition, Object)
 *
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
final public class AspectDefinition implements AspectDefinitionConstants, Serializable, Cloneable
{
   /** the name of the aspect definition */
   public String name;
   /** the interceptor stack of the aspect */
   public AspectInterceptorHolder interceptors[];
   /** caches the interceptor routes that a method call takes */
   transient private Map cachedMethodCallRoutes;

   /** the interfaces that the aspect exposes */
   public Class interfaces[];

   /**
    * Constructor.
    */
   public AspectDefinition(String name, AspectInterceptorHolder interceptors[], Class interfaces[])
   {
      this.name = name;
      this.interceptors = interceptors;
      this.interfaces = interfaces;
   }

   /**
    * Build the AspectDefinition from a XML Element Fragment.  If the fragment
    * contains any interceptor-ref elements or stack-ref, they will be looked up 
    * in the namedInterceptors and namedStacks Maps respectivly.
    */
   public AspectDefinition(Element xml, Map namedInterceptors, Map namedStacks)
      throws AspectInitizationException
   {

      if (xml.attribute(ATTR_NAME) == null)
         throw new AspectInitizationException(
            "attribute " + ATTR_NAME.getQualifiedName() + " is required");
      name = xml.attribute(ATTR_NAME).getValue();

      ArrayList v =
         AspectSupport.loadAspectInterceptorHolderList(xml, namedInterceptors, namedStacks);
      interceptors = new AspectInterceptorHolder[v.size()];
      v.toArray(interceptors);

      v.clear();

      //
      // All the interfaces listed in the 'interfaces' attribute of the 
      // aspect xml def need to be be exposed.
      // 
      ClassLoader cl = Classes.getContextClassLoader();
      if (xml.attribute(ATTR_INTERFACES) != null)
      {
         String interfaces = xml.attribute(ATTR_INTERFACES).getValue();
         String ilist[] = splitTrimmed(interfaces, ",");
         for (int j = 0; j < ilist.length; j++)
         {
            Class t;
            try
            {
               t = cl.loadClass(ilist[j]);
               if (!v.contains(t))
                  v.add(t);
            }
            catch (ClassNotFoundException e)
            {
               throw new AspectInitizationException("Could not load interface: " + ilist[j], e);
            }
         }
      }

      //
      // All the interfaces that interceptors expose need to also be
      // added to the list of exposed interfaces.
      // 
      for (int i = 0; i < interceptors.length; i++)
      {
         Class x[] = interceptors[i].getInterfaces();
         if (x == null)
            continue;
         for (int j = 0; j < x.length; j++)
         {
            if (!v.contains(x[j]))
               v.add(x[j]);
         }
      }

      interfaces = new Class[v.size()];
      v.toArray(interfaces);

   }

   /**
    * Method split.
    * @param interfaces
    * @param string
    * @return String
    */
   private String[] splitTrimmed(String s, String delimiter)
   {
      ArrayList v = new ArrayList();
      StringTokenizer st = new StringTokenizer(s, delimiter, false);
      while (st.hasMoreElements())
      {
         String t = st.nextToken().trim();
         if (t.length() == 0)
            continue;
         v.add(t);
      }
      String rc[] = new String[v.size()];
      v.toArray(rc);
      return rc;
   }

   public AspectDefinition cloneAspectDefinition()
   {
      try
      {
         return (AspectDefinition) clone();
      }
      catch (CloneNotSupportedException e)
      {
         return new AspectDefinition(name, interceptors, interfaces);
      }
   }

   /**
    * Creates a duplicate AspectDefinition but with the provided
    * interceptor inserted at the index position in the stack.
    * 
    * @throws IndexOutOfBoundsException - if the index is out of range.
    */
   public void insertInterceptor(int index, AspectInterceptorHolder holder)
      throws IndexOutOfBoundsException
   {
      if (index < 0 || index > interceptors.length)
         throw new IndexOutOfBoundsException();
         
      AspectInterceptorHolder dest[] = new AspectInterceptorHolder[interceptors.length + 1];
      arrayInsetShift(interceptors, dest, index);
      dest[index] = holder;
      interceptors = dest;
      getCachedMethodCallRoutes().clear();
   }

   /**
    * Creates a duplicate AspectDefinition but with the 
    * interceptor at the index position removed from the stack.
    * 
    * @throws IndexOutOfBoundsException - if the index is out of range.
    */
   public void removeInterceptor(int index) throws IndexOutOfBoundsException
   {
      if (index < 0 || index >= interceptors.length)
         throw new IndexOutOfBoundsException();
      AspectInterceptorHolder dest[] = new AspectInterceptorHolder[interceptors.length - 1];
      arrayRemoveShift(interceptors, dest, index);
      interceptors = dest;
      getCachedMethodCallRoutes().clear();
   }

   private static void arrayInsetShift(Object src[], Object dest[], int position)
   {
      System.arraycopy(src, 0, dest, 0, position);
      System.arraycopy(src, position, dest, position + 1, src.length - position);
   }

   private static void arrayRemoveShift(Object src[], Object dest[], int position)
   {
      System.arraycopy(src, 0, dest, 0, position);
      System.arraycopy(src, position + 1, dest, position, (src.length - position) - 1);
   }

	/**
	 * Computes the optimal interceptor stack for the given method call.
	 * 	 * @param method	 * @return AspectInterceptor[]	 */
	public AspectInterceptor[] getMethodCallRoute(Method method) {
		
		AspectInterceptor rc[] = (AspectInterceptor[])getCachedMethodCallRoutes().get(method);
		if( rc!=null )
			return rc;
			
		ArrayList v = new ArrayList(interceptors.length);
		for (int i = 0; i < interceptors.length; i++)
      {
         if( interceptors[i].isIntrestedInMethodCall(method) ) {
         	v.add( interceptors[i].interceptor );
         }
      }
      rc = new AspectInterceptor[v.size()];
      v.toArray(rc);
		getCachedMethodCallRoutes().put(method, rc);
      return rc;
	}
	
	private Map getCachedMethodCallRoutes() {
		if(cachedMethodCallRoutes==null)
			cachedMethodCallRoutes = new HashMap();
		return cachedMethodCallRoutes;
	} 
}
