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

import org.jboss.ejb.plugins.jrmp12.interfaces.HomeProxy;
import org.jboss.ejb.plugins.jrmp12.interfaces.StatelessSessionProxy;
import org.jboss.ejb.plugins.jrmp12.interfaces.StatefulSessionProxy;
import org.jboss.ejb.plugins.jrmp12.interfaces.EntityProxy;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.3 $
 */
public final class JRMPContainerInvoker
   extends org.jboss.ejb.plugins.jrmp.server.JRMPContainerInvoker
{
   public EJBHome getEJBHome()
   {
      if (home == null)
      {
         this.home = (EJBHome)Proxy.newProxyInstance(con.getHomeClass().getClassLoader(),
                                              new Class[] { con.getHomeClass() },
                                              new HomeProxy(jndiName, this, optimize));
      }
      return home;
   }
   
   public EJBObject getStatelessSessionEJBObject()
   {
      return (EJBObject)Proxy.newProxyInstance(con.getRemoteClass().getClassLoader(),
                                        new Class[] { con.getRemoteClass() },
                                        new StatelessSessionProxy(jndiName, this, optimize));
   }

   public EJBObject getStatefulSessionEJBObject(Object id)
   {
      return (EJBObject)Proxy.newProxyInstance(con.getRemoteClass().getClassLoader(),
                                           new Class[] { con.getRemoteClass() },
                                           new StatefulSessionProxy(jndiName, this, id, optimize));
   }

   public EJBObject getEntityEJBObject(Object id)
   {
      return (EJBObject)Proxy.newProxyInstance(con.getRemoteClass().getClassLoader(),
                                           new Class[] { con.getRemoteClass() },
                                           new EntityProxy(jndiName, this, id, optimize));
   }

   public Collection getEntityCollection(Collection ids)
   {
      ArrayList list = new ArrayList(ids.size());
      Iterator idEnum = ids.iterator();
      while(idEnum.hasNext())
      {
         list.add(Proxy.newProxyInstance(con.getRemoteClass().getClassLoader(),
                                           new Class[] { con.getRemoteClass() },
                                           new EntityProxy(jndiName, this, idEnum.next(), optimize)));
      }
      return list;
   }
}
