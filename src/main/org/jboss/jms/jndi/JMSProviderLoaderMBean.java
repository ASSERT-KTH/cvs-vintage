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

package org.jboss.jms.jndi;

import javax.management.ObjectName;

import org.jboss.util.ObjectNameFactory;

import org.jboss.system.ServiceMBean;

/**
 * The JMX managment interface for the {@link JMSProviderLoader} MBean.
 *
 * Created: Wed Nov 29 14:08:39 2000
 * 6/22/01 - hchirino - The queue/topic jndi references are now configed via JMX
 *
 * @author  <a href="mailto:cojonudo14@hotmail.com">Hiram Chirino</a>
 * @version $Revision: 1.6 $
 */
public interface JMSProviderLoaderMBean  
   extends ServiceMBean
{
   ObjectName OBJECT_NAME = ObjectNameFactory.create(":service=JMSProviderLoader");
   
   void setProviderName(String name);
   
   String getProviderName();
   
   void setProviderAdapterClass(String clazz);
   
   String getProviderAdapterClass();
   
   void setProviderUrl(String url);
   
   String getProviderUrl();
   
   String getQueueFactoryRef();
   
   String getTopicFactoryRef();
   
   void setQueueFactoryRef(String newQueueFactoryRef);
   
   void setTopicFactoryRef(String newTopicFactoryRef);
}
