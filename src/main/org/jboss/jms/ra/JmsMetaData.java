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
import javax.resource.spi.ManagedConnectionMetaData;

/**
 * JmsMetaData.java
 *
 *
 * Created: Sat Mar 31 03:36:27 2001
 *
 * @author Peter Antman (peter.antman@tim.se)
 * @version $Revision: 1.1 $
 */

public class JmsMetaData  implements ManagedConnectionMetaData {
    
    private JmsManagedConnection mc;
    
    public JmsMetaData(JmsManagedConnection mc) {
	this.mc = mc;
    }
    public String getEISProductName() throws ResourceException {
	return "JMS CA Resource Adapter";
    }

    public String getEISProductVersion() throws ResourceException {
	return "0.1";//Is this possible to get another way
    }

    public int getMaxConnections() throws ResourceException {
	// Dont know how to get this, from Jms, we
	// set it to unlimited
	return 0;
    }
    
    public String getUserName() throws ResourceException {
        return mc.getUserName();
    }
} // JmsMetaData
