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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;

/**
 * A provider adapter that uses properties.
 *
 * @version <pre>$Revision: 1.2 $</pre>
 * @author  <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public class JNDIProviderAdapter
   extends AbstractJMSProviderAdapter
{
   public Context getInitialContext() throws NamingException
   {
      if (properties == null)
         return new InitialContext();
      else
         return new InitialContext(properties);
   }
}
