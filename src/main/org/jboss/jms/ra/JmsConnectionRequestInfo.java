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

import javax.resource.spi.ConnectionRequestInfo;

/**
 * JmsConnectionRequestInfo.java
 *
 *
 * Created: Thu Mar 29 16:29:55 2001
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @version $Revision: 1.2 $
 */

public class JmsConnectionRequestInfo implements ConnectionRequestInfo {
    private String userName;
    private String password;

    private boolean transacted = true;
    private int acknowledgeMode;
    private boolean isTopic = true;
    public JmsConnectionRequestInfo(boolean transacted, 
				    int acknowledgeMode,
				    boolean isTopic
				    ) {
	this.transacted = transacted;
	this.acknowledgeMode = acknowledgeMode;
	this.isTopic = isTopic;
    }
    
    // 
    public String getUserName() 
    {
	return userName;
    }
    
    public void setUserName(String name) 
    {
	userName = name;
    }

    public String getPassword() 
    {
	return password;
    }

    public void setPassword(String password) 
    {
	this.password = password;
    }
    // End not used

    public boolean isTransacted()
    {
	return transacted;
    }
    
    public int getAcknowledgeMode()
    {
	return acknowledgeMode;
    }

    public boolean isTopic() {
	return isTopic;
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof JmsConnectionRequestInfo) {
            JmsConnectionRequestInfo you = (JmsConnectionRequestInfo) obj;
            return (this.transacted == you.isTransacted() &&
                    this.acknowledgeMode == you.getAcknowledgeMode() &&
		    this.isTopic == you.isTopic()
		    );
        } else {
            return false;
        }
    }
 
    // FIXME !!
    public int hashCode() {
        String result = "" + transacted + acknowledgeMode + isTopic;
        return result.hashCode();
    }
    
    // May be used if we fill in username and password later 
    private boolean isEqual(Object o1, Object o2) {
        if (o1 == null) {
            return (o2 == null);
        } else {
            return o1.equals(o2);
        }
    }

} // JmsConnectionRequestInfo
