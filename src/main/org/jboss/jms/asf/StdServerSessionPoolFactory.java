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

/**
 * An implementation of ServerSessionPoolFactory. <p>
 *
 * Created: Fri Dec 22 09:47:41 2000
 *
 * @author    <a href="mailto:peter.antman@tim.se">Peter Antman</a> .
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a> .
 * @version   $Revision: 1.7 $
 */
public class StdServerSessionPoolFactory
       implements ServerSessionPoolFactory, Serializable
{
   /**
    * The name of this factory.
    */
   private String name;

   /**
    * Construct a <tt>StdServerSessionPoolFactory</tt> .
    */
   public StdServerSessionPoolFactory()
   {
      super();
   }

   /**
    * Set the name of the factory.
    *
    * @param name  The name of the factory.
    */
   public void setName(final String name)
   {
      this.name = name;
   }

   /**
    * Get the name of the factory.
    *
    * @return   The name of the factory.
    */
   public String getName()
   {
      return name;
   }

   /**
    * Create a new <tt>ServerSessionPool</tt> .
    *
    * @param con
    * @param maxSession
    * @param isTransacted
    * @param ack
    * @param listener
    * @param isContainerManaged          Description of Parameter
    * @return                            A new pool.
    * @throws JMSException
    * @exception javax.jms.JMSException  Description of Exception
    */
   public javax.jms.ServerSessionPool getServerSessionPool(javax.jms.Connection con, int maxSession, boolean isTransacted, int ack, boolean useLocalTX, javax.jms.MessageListener listener) throws javax.jms.JMSException
   {
      ServerSessionPool pool = (ServerSessionPool)new StdServerSessionPool(con, isTransacted, ack, useLocalTX, listener, maxSession);
      return pool;
   }
}
