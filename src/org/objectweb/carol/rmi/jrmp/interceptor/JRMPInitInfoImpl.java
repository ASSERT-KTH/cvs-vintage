/*
 * @(#) JRMPInitInfoImpl.java	1.0 02/07/15
 *
 * Copyright (C) 2002 - INRIA (www.inria.fr)
 *
 * CAROL: Common Architecture for RMI ObjectWeb Layer
 *
 * This library is developed inside the ObjectWeb Consortium,
 * http://www.objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 * 
 *
 */
package org.objectweb.carol.rmi.jrmp.interceptor;

//java import
import java.util.Enumeration;
import java.util.Hashtable;

/** 
 * Class <code>JRMPInitInfoImpl</code> is the CAROL JRMP Initializer Implementation
 *
 * @see org.objectweb.carol.rmi.jrmp.interceptor.JInitInfo
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002
 */
public class JRMPInitInfoImpl implements JInitInfo {

    /**
     * Request Server Interceptor Hashtable
     */
    protected Hashtable serverInterceptors = new Hashtable();

    /**
     * Request Client Interceptor Hashtable
     */
    protected Hashtable clientInterceptors = new Hashtable();

    /**
     * add client interceptor
     * @param JClientRequestInterceptor the client interceptor to add
     */
    public void add_client_request_interceptor(JClientRequestInterceptor interceptor) throws JDuplicateName {
	    if (clientInterceptors.containsKey(interceptor.name())) {
		throw new JDuplicateName(interceptor.name()); 
	    } else {
		clientInterceptors.put(interceptor.name(), interceptor);
	    }
    }

    /**
     * add server interceptor
     * @param JServerRequestInterceptor the server interceptor to add
     */
    public void add_server_request_interceptor(JServerRequestInterceptor interceptor) throws JDuplicateName {
	if (serverInterceptors.containsKey(interceptor.name())) {
	    throw new JDuplicateName(interceptor.name()); 
	} else {
	    serverInterceptors.put(interceptor.name(), interceptor);
	}
    }

    /**
     * get all the client interceptor 
     * @return array of ClientRequestInterceptor
     */
    public JClientRequestInterceptor [] getClientRequestInterceptors() {
	JClientRequestInterceptor [] result = new JClientRequestInterceptor [clientInterceptors.size()];
	int i = 0;
	for (Enumeration e = clientInterceptors.elements() ; e.hasMoreElements() ;) {
	    result[i] = (JClientRequestInterceptor) e.nextElement(); 
	    i++;
	}
	return result;
    }

    /**
     * get all the server interceptor 
     * @return array of ServerRequestInterceptor
     */
    public JServerRequestInterceptor [] getServerRequestInterceptors() {
	JServerRequestInterceptor [] result = new JServerRequestInterceptor [serverInterceptors.size()];
	int i = 0;
	for (Enumeration e = serverInterceptors.elements() ; e.hasMoreElements() ;) {
	    result[i] = (JServerRequestInterceptor) e.nextElement(); 
	    i++;
	}	
	return result;
    }
}
