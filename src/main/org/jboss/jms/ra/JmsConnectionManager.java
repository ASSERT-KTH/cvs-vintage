/*
 * Copyright (c) 2001 Peter Antman Tim <peter.antman@tim.se>
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
package org.jboss.jms.ra;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;

/**

  <p>The resource adapters own ConnectionManager, used in non managed
 environments. 
  </p>
   <p>
 Will handle some of the houskeeping an appserver nomaly does.
 </p>

 *
 * Created: Thu Mar 29 16:09:26 2001
 *
 * @author Peter Antman (peter.antman@tim.se)
 * @version $Revision: 1.1 $
 */

public class JmsConnectionManager implements ConnectionManager {
    
    public JmsConnectionManager() {
	
    }

    public Object allocateConnection(ManagedConnectionFactory mcf,
				     ConnectionRequestInfo cxRequestInfo) 
	throws ResourceException{
	ManagedConnection mc = mcf.createManagedConnection(null,cxRequestInfo);
	return mc.getConnection(null,cxRequestInfo);

    }
} // JmsConnectionManager
