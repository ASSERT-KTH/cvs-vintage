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

import java.util.Set;
import java.util.Iterator;

import javax.security.auth.Subject;

import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.SecurityException;
import javax.resource.spi.ConnectionRequestInfo;

import javax.resource.spi.security.PasswordCredential;
/**
 * JmsCred.java
 *
 *
 * Created: Sat Mar 31 03:23:30 2001
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @version $Revision: 1.2 $
 */

public class JmsCred  {
    public String name;
    public String pwd;
    
    public JmsCred() {
	
    }

    
    /**
     * Get our own simple cred
     */
    public static JmsCred getJmsCred(ManagedConnectionFactory mcf,
					     Subject subject, 
					     ConnectionRequestInfo info) 
	throws SecurityException {

	JmsCred jc = new JmsCred();
	if (subject == null && info !=null ) {
	    // Credentials specifyed on connection request
	    jc.name = ((JmsConnectionRequestInfo)info).getUserName();
	    jc.pwd = ((JmsConnectionRequestInfo)info).getPassword();
	} else if (subject != null) {
	    // Credentials from appserver
	    Set creds = 
		subject.getPrivateCredentials(PasswordCredential.class);
	    PasswordCredential pwdc = null;
	    Iterator credentials = creds.iterator();
	    while(credentials.hasNext()) {
		PasswordCredential curCred = 
		    (PasswordCredential) credentials.next();
		if (curCred.getManagedConnectionFactory().equals(mcf)) {
		    pwdc = curCred;
		    break;
		}
	    }
	    if(pwdc == null) {
		// No hit - we do need creds
		throw new SecurityException("No Passwdord credentials found");
	    }
	    jc.name = pwdc.getUserName();
	    jc.pwd = new String(pwdc.getPassword());
	} else {
	    throw new SecurityException("No Subject or ConnectionRequestInfo set, could not get credentials");
	}
	return jc;
    }
} // JmsCred
