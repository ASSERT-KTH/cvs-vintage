/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jms.asf;

import java.io.Serializable;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageListener;

import javax.jms.ServerSessionPool;
import org.jboss.tm.XidFactoryMBean;

/**
 * An implementation of ServerSessionPoolFactory. <p>
 *
 * Created: Fri Dec 22 09:47:41 2000
 *
 * @author    <a href="mailto:peter.antman@tim.se">Peter Antman</a> .
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a> .
 * @version   $Revision: 1.12 $
 */
public class StdServerSessionPoolFactory
       implements ServerSessionPoolFactory, Serializable
{
   /** The name of this factory. */
   private String name;

   private XidFactoryMBean xidFactory;

   public StdServerSessionPoolFactory()
   {
      super();
   }

   public void setName(final String name)
   {
      this.name = name;
   }

   public String getName()
   {
      return name;
   }

   public void setXidFactory(final XidFactoryMBean xidFactory)
   {
      this.xidFactory = xidFactory;
   }

   public XidFactoryMBean getXidFactory()
   {
      return xidFactory;
   }

   public javax.jms.ServerSessionPool getServerSessionPool(Connection con, int minSession, int maxSession, long keepAlive, boolean isTransacted, int ack, boolean useLocalTX, javax.jms.MessageListener listener) throws javax.jms.JMSException
   {
      ServerSessionPool pool = (ServerSessionPool)new StdServerSessionPool(con, isTransacted, ack, useLocalTX, listener, minSession, maxSession, keepAlive, xidFactory);
      return pool;
   }
}
