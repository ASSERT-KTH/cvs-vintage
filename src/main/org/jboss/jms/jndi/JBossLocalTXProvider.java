/*
 * Copyright (c) 2000 Peter Antman DN <peter.antman@dn.se>
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
package org.jboss.jms.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * A JMS provider adapter for <em>JBossMQ</em> with non transacted factories.
 *
 * May be used to get LocalTransaction in JMS ra.
 *
 * Created: Fri Dec 22 09:34:04 2000
 *
 * @author Peter Antman  (peter.antman@tim.se)
 * @version $Revision: 1.1 $
 */
public class JBossLocalTXProvider 
   extends JBossMQProvider
{
    public static final String TOPIC_CONNECTION_FACTORY = "TopicConnectionFactory";
    public static final String QUEUE_CONNECTION_FACTORY = "QueueConnectionFactory";

    public JBossLocalTXProvider() {
        super();
	// Hackish
	queueFactoryName =QUEUE_CONNECTION_FACTORY;
	topicFactoryName = TOPIC_CONNECTION_FACTORY;
    }


} // JBossMQProvider
