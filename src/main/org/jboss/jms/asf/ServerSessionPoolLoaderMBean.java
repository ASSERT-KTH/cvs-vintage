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

import org.jboss.system.ServiceMBean;

/**
 * The management interface for the <tt>ServerSessionPoolLoader</tt>.
 *
 * <p>Created: Wed Nov 29 16:20:17 2000
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @version $Revision: 1.5 $
 */
public interface ServerSessionPoolLoaderMBean 
   extends ServiceMBean 
{
   /** The default MBean object name. */
   String OBJECT_NAME = "jboss:service=ServerSessionPoolMBean";

   /**
    * Set the pool name.
    *
    * @param name    The pool name.
    */
   void setPoolName(String name);

   /**
    * Get the pool name.
    *
    * @return    The pool name.
    */
   String getPoolName();

   /**
    * Set the classname of pool factory to use.
    *
    * @param classname    The name of the pool factory class.
    */
   void setPoolFactoryClass(String classname);

   /**
    * Get the classname of pool factory to use.
    *
    * @return    The name of the pool factory class.
    */
   String getPoolFactoryClass();
}
