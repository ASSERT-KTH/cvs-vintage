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
import org.jboss.tm.XidFactoryMBean;

/**
 * Defines the model for creating <tt>ServerSessionPoolFactory</tt> objects. <p>
 *
 * Created: Wed Nov 29 15:55:21 2000
 *
 * @author    <a href="mailto:peter.antman@tim.se">Peter Antman</a> .
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a> .
 * @version   $Revision: 1.9 $
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
    * The <code>setXidFactory</code> method supplies the XidFactory that 
    * server sessions will use to get Xids to control local transactions.
    *
    * @param xidFactory a <code>XidFactoryMBean</code> value
    */
   void setXidFactory(XidFactoryMBean xidFactory);

   /**
    * The <code>getXidFactory</code> method returns the XidFactory that 
    * server sessions will use to get xids..
    *
    * @return a <code>XidFactoryMBean</code> value
    */
   XidFactoryMBean getXidFactory();

   /**
    * Create a new <tt>ServerSessionPool</tt>.
    * @param con the jms connection
    * @param minSession the minimum number of sessions
    * @param maxSession the maximum number of sessions
    * @param keepAlive the time to keep sessions alive
    * @param isTransacted whether the pool is transacted
    * @param ack the acknowledegement method
    * @param listener the listener
    * @param useLocalTX whether to use local transactions
    * @return A new pool.
    * @throws JMSException for any error
    */
   ServerSessionPool getServerSessionPool(Connection con, int minSession, int maxSession, long keepAlive, boolean isTransacted, int ack,
         boolean useLocalTX, MessageListener listener) throws JMSException;
}