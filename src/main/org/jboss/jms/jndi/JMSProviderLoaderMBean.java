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

import org.jboss.util.ServiceMBean;
/**
 * JMSProviderLoaderMBean.java
 *
 *
 * Created: Wed Nov 29 14:08:39 2000
 *
 * @author 
 * @version
 */

public interface JMSProviderLoaderMBean  
   extends ServiceMBean
{
    public static final String OBJECT_NAME = ":service=JMSProviderLoader";

    public void setProviderName(String name);
    
    public String getProviderName();
    
    public void setProviderAdapterClass(String clazz);
    
    public String getProviderAdapterClass();
    
}
