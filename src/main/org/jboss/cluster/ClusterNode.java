/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.cluster;

import java.net.InetAddress;
import java.lang.reflect.*;

import javax.management.*;

/**
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public class ClusterNode
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   InetAddress where;
   ClusterRemote node;
   long lastHeartbeat;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   ClusterNode(InetAddress where, ClusterRemote node)
   {
      this.where = where;
      this.node = node;
      lastHeartbeat = System.currentTimeMillis();
   }
   
   // Public --------------------------------------------------------
   public InetAddress getWhere() { return where; }
   public ClusterRemote getNode() { return node; }
   public long getLastHeartBeat() { return lastHeartbeat; }
   public void setLastHeartBeat(long l) { lastHeartbeat = l; }
   
   public Object getMBean(Class intf, ObjectName name)
   {
      return Proxy.newProxyInstance(intf.getClassLoader(),
                                          new Class[] { intf },
                                          new MBeanProxy(name));
   }
   
   class MBeanProxy
      implements InvocationHandler
   {
      ObjectName name;
      
      MBeanProxy(ObjectName name)
      {
         this.name = name;
      }
      
      public Object invoke(Object proxy,
                        Method method,
                        Object[] args)
                 throws Throwable
      {
         if (args == null) args = new Object[0];
         
         String[] sig = new String[args.length];
         for (int i = 0; i < args.length; i++)
            sig[i] = args[i].getClass().getName();
         
         try
         {
            return node.invoke(name, method.getName(), args, sig);
         } catch (MBeanException e)
         {
            throw e.getTargetException();
         } catch (ReflectionException e)
         {
            throw e.getTargetException();
         }
      }
      
   }
}

