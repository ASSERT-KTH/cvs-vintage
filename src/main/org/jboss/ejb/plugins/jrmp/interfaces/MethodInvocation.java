/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 *	MethodInvocation
 *
 *  This Serializable object carries the method to invoke and an identifier for the target ojbect
 *
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *  @author <a href="mailto:Richard.Monson-Haefel@jGuru.com">Richard Monson-Haefel</a>.
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>.
 *	@version $Revision: 1.8 $
 */
public class MethodInvocation
   implements java.io.Serializable
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   Object id;
   String className;
   int hash = -9999;
   Object[] args;

   // Static --------------------------------------------------------
   // MF FIXME: this is bad.
   // It will grow quite large
   // We need to work from the container context... not the server context it is silly
   static HashMap clazzMap = new HashMap();

   static HashMap invokers = new HashMap(); // Prevent DGC
   public static ContainerRemote getLocal(String jndiName) { return (ContainerRemote)invokers.get(jndiName); }
   public static void addLocal(String jndiName, ContainerRemote invoker) { invokers.put(jndiName, invoker); }
   public static void removeLocal(String jndiName) { invokers.remove(jndiName); }

   // Constructors --------------------------------------------------
   public MethodInvocation(Method m, Object[] args)
   {
      this(null, m, args);
   }

   public MethodInvocation(Object id, Method m, Object[] args)
   {
      this.id = id;
      this.className = m.getDeclaringClass().getName();
	  // m.hashCode only hashes on the name / class.
	  // Overriding is not seen and must include parameters
      this.hash = calculateHash(m);
      this.args = args;
   }
   // Public --------------------------------------------------------


   public Object getId() { return id; }

   /*
   * MF FIXME: I am not sure this is a very good idea.
   *
   * The use of the synchronized map is going to slow things down.
   * Also the penalty on the first invocation can be quite high.
   * It is probably better to have the container pre-map the method.
   * We can then directly look up in the container and no need to
   * extract the "method" from here, just the hashCode() (calculated one)
   * In clear I am saying that this is overkill and slow. Will investigate.
   *
   * People will see this as they run their stuff for the first time (setup)
   */
   public Method getMethod()
      throws NoSuchMethodException, ClassNotFoundException
   {
      HashMap methodMap;
      Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
      synchronized(clazzMap)
      {
         methodMap = (HashMap)clazzMap.get(clazz);

         if (methodMap == null)
         {
            // Create method mapping
            Method[] methods = clazz.getMethods();
            methodMap = new HashMap();
            for (int i = 0; i < methods.length; i++)
            {
			    methodMap.put(new Integer(calculateHash(methods[i])), methods[i]);
            }
            clazzMap.put(clazz, methodMap);
         }
      }

      synchronized(methodMap)
      {
         // Get method based on its hash value
         Method m = (Method)methodMap.get(new Integer(hash));

         if (m == null)
            throw new NoSuchMethodException(clazz+":"+hash);
         return m;
      }
   }

   public Object[] getArguments()
      throws IOException, ClassNotFoundException
   {
      return args;
   }

   /*
   * The use of hashCode is not enough to differenciate methods
   * we override the hashCode
   *
   * This is taken from the RMH code in EJBoss 0.9
   *
   */
   public static int calculateHash(Method method) {

		int hash =
	    	// We use the declaring class
			method.getDeclaringClass().getName().hashCode() ^ //name of class
            // We use the name of the method
			method.getName().hashCode(); //name of method

		Class[] clazz = method.getParameterTypes();

		for (int i = 0; i < clazz.length; i++) {

			 // XOR
			 // We use the constant because
			 // a^b^b = a (thank you norbert)
			 // so that methodA() hashes to methodA(String, String)

			 hash = (hash +20000) ^ clazz[i].getName().hashCode();
		}

		return hash;
   }



    /*
    * equals()
    *
    * For MethodInvocations to be equal, the method must be equal but also the
    * the arguments
    *
    * @since EJBoss 0.9
    */
    public boolean equals(Object obj) {

	    if (obj != null && obj instanceof MethodInvocation) {

    	    MethodInvocation other = (MethodInvocation)obj;

    	    if(other.hash == this.hash) {

				return true;
			}
		}
	    return false;
    }

    /*
    * hashCode
    *
    * Base the hashcode on the name of the class, the name of the method and the
    * first parameter.
    *
    */
    public int hashCode() {

		// See the calculate hashcode it takes everything into account
	    return hash;
    }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------


   // Inner classes -------------------------------------------------
}

