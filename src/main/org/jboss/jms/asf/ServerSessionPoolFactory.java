/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jms.asf;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ServerSessionPool;

/**
 * Defines the model for creating <tt>ServerSessionPoolFactory</tt> objects. <p>
 *
 * Created: Wed Nov 29 15:55:21 2000
 *
 * @author    <a href="mailto:peter.antman@tim.se">Peter Antman</a> .
 * @version   $Revision: 1.3 $
 */
public interface ServerSessionPoolFactory
{
   /**
    * Set the name of the factory.
    *
    * @param name  The name of the factory.
    */
   void setName(String name);

   /**
    * Get the name of the factory.
    *
    * @return   The name of the factory.
    */
   String getName();

   /**
    * Create a new <tt>ServerSessionPool</tt> .
    *
    * @param con
    * @param maxSession
    * @param isTransacted
    * @param ack
    * @param listener
    * @param isContainerManaged  Description of Parameter
    * @return                    A new pool.
    * @throws JMSException
    */
   ServerSessionPool getServerSessionPool(Connection con,
         int maxSession,
         boolean isTransacted,
         int ack,
         boolean isContainerManaged,
         MessageListener listener)
          throws JMSException;
}
