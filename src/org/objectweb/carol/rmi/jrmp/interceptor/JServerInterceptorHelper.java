/*
 * @(#) JClientInterceptorHelper.java	1.0 02/07/15
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


// java import 
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.server.UID;

/** 
 * Class <code>JServerInterceptorHelper</code> is the CAROL JRMP Server Interceptor Helper 
 * this class is used by the other pakage class to manage server interception 
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/200
 */
public class JServerInterceptorHelper {
  
    /**
     * hold a unique identifier of this class. This is used to determine if
     * contexts can be passed by reference using the JObjectStore
     */
    public static UID getSpaceID() {
        return spaceID;
    }

    /**
     * The spaceID
     */
    private final static UID spaceID = new UID();


    /**
     * Receive request 
     * @param ObjectInput in
     * @param JServerRequestInterceptor All interceptor for this context
     * @exception IOException if an exception occur with the ObjectOutput
     */
    public static void receive_request(ObjectInput in, JServerRequestInterceptor [] sis) throws IOException {
	try {
	    if (sis != null) {
		JServerRequestInfo ri = new  JRMPServerRequestInfoImpl((JServiceContext []) in.readObject());
		for (int i = 0; i < sis.length; i++) {
		    sis[i].receive_request(ri);		
		}
	    }
	} catch (ClassNotFoundException cnfe) {
	    throw new IOException("" + cnfe);
	}
    }


    /**
     * send reply with context
     * @param ObjectOutput out
     * @param JServerRequestInterceptor All interceptor for this context
     * @exception IOException if an exception occur with the ObjectOutput
     */
    public static void send_reply(ObjectOutput out, JServerRequestInterceptor [] sis)throws IOException {
	if (sis != null) {
	    JServerRequestInfo ri = new  JRMPServerRequestInfoImpl();
	    for (int i = 0; i < sis.length; i++) {
		sis[i].send_reply(ri);		
	    }
	    // send all service context
	    out.writeObject(ri.get_all_reply_service_context());
	}
    }

    /**
     * send exception with context
     * @param ObjectOutput out
     * @param JServerRequestInterceptor All interceptor for this context
     * @exception IOException if an exception occur with the ObjectOutput 
     */
    public static void send_exception(ObjectOutput out, JServerRequestInterceptor [] sis) throws IOException {
	if (sis != null) {
	    JServerRequestInfo ri = new  JRMPServerRequestInfoImpl();
	    for (int i = 0; i < sis.length; i++) {
		sis[i].send_exception(ri);		
	    }
	    // send all service context
	    out.writeObject(ri.get_all_reply_service_context());
	}
    }


    /*
     * send other with context 
     * @param ObjectOutput out
     * @param JServerRequestInterceptor All interceptor for this context
     * @exception IOException if an exception occur with the ObjectOutput      
     */
    public static void send_other(ObjectOutput out, JServerRequestInterceptor [] sis) throws IOException {
	if (sis != null) {
	    JServerRequestInfo ri = new  JRMPServerRequestInfoImpl();
	    for (int i = 0; i < sis.length; i++) {
		sis[i].send_other(ri);		
	    }
	    // send all service context
	    out.writeObject(ri.get_all_reply_service_context());
	}
    }
}
