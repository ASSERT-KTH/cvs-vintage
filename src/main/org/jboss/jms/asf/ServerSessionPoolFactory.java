/*
 * Copyright (c) 2000 Peter Antman Tim <peter.antman@tim.se>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jboss.jms.asf;

import javax.jms.Connection;
import javax.jms.MessageListener;
import javax.jms.ServerSessionPool;
import javax.jms.JMSException;

/**
 * Defines the model for creating <tt>ServerSessionPoolFactory</tt> objects.
 *
 * <p>Created: Wed Nov 29 15:55:21 2000
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @version $Revision: 1.2 $
 */
public interface ServerSessionPoolFactory
{
   /**
    * Set the name of the factory.
    *
    * @param name    The name of the factory.
    */
   void setName(String name);

   /**
    * Get the name of the factory.
    *
    * @return    The name of the factory.
    */
   String getName();

   /**
    * Create a new <tt>ServerSessionPool</tt>.
    *
    * @param con
    * @param maxSession
    * @param isTransacted
    * @param ack
    * @param listener
    * @return                A new pool.
    *
    * @throws JMSException
    */
   ServerSessionPool getServerSessionPool(Connection con,
                                          int maxSession,
                                          boolean isTransacted,
                                          int ack,
                                          MessageListener listener)
      throws JMSException;
}
