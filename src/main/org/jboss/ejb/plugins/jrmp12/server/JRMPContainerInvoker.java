/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp12.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.EJBMetaData;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;

import org.jboss.proxy.InvocationHandler;
import org.jboss.proxy.Proxy;

import org.jboss.ejb.ContainerInvokerContainer;
import org.jboss.util.FastKey;
import org.jboss.ejb.plugins.jrmp12.interfaces.HomeProxy;
import org.jboss.ejb.plugins.jrmp12.interfaces.StatelessSessionProxy;
import org.jboss.ejb.plugins.jrmp12.interfaces.StatefulSessionProxy;
import org.jboss.ejb.plugins.jrmp12.interfaces.EntityProxy;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *	@version $Revision: 1.7 $
 */
public final class JRMPContainerInvoker
   extends org.jboss.ejb.plugins.jrmp.server.JRMPContainerInvoker
{
    public EJBHome getEJBHome()
    {
        if (home == null)
        {
            // We add the Handle methods to the Home
            Class handleClass;
            try { handleClass = Class.forName("javax.ejb.Handle");} 
                catch (Exception e) {e.printStackTrace();handleClass = null;}
            
            this.home = (EJBHome)Proxy.newProxyInstance(((ContainerInvokerContainer)container).getHomeClass().getClassLoader(),
                new Class[] { ((ContainerInvokerContainer)container).getHomeClass(), handleClass },
                new HomeProxy(jndiName, ejbMetaData, this, optimize));
        }
        return home;
    }
    
    public EJBObject getStatelessSessionEJBObject()
    {
        if (statelessObject == null) {
            
            this.statelessObject = (EJBObject)Proxy.newProxyInstance(((ContainerInvokerContainer)container).getRemoteClass().getClassLoader(),
                new Class[] { ((ContainerInvokerContainer)container).getRemoteClass() },
                new StatelessSessionProxy(jndiName, this, optimize));
        }
        
        return statelessObject;
    }
    
   public EJBObject getStatefulSessionEJBObject(Object id)
   {
      return (EJBObject)Proxy.newProxyInstance(((ContainerInvokerContainer)container).getRemoteClass().getClassLoader(),
                                           new Class[] { ((ContainerInvokerContainer)container).getRemoteClass() },
                                           new StatefulSessionProxy(jndiName, this, id, optimize));
   }

   public EJBObject getEntityEJBObject(FastKey id)
   {
      return (EJBObject)Proxy.newProxyInstance(((ContainerInvokerContainer)container).getRemoteClass().getClassLoader(),
                                           new Class[] { ((ContainerInvokerContainer)container).getRemoteClass() },
                                           new EntityProxy(jndiName, this, id, optimize));
   }

   public Collection getEntityCollection(Collection ids)
   {                                                          
      ArrayList list = new ArrayList(ids.size());
      Iterator idEnum = ids.iterator();
      while(idEnum.hasNext())
      {
         list.add(Proxy.newProxyInstance(((ContainerInvokerContainer)container).getRemoteClass().getClassLoader(),
                                           new Class[] { ((ContainerInvokerContainer)container).getRemoteClass() },
                                           new EntityProxy(jndiName, this, new FastKey(idEnum.next()), optimize)));
      }
      return list;
   }
}
