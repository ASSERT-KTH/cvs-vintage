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

//java import 
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.server.UID;


/**
 * Class <code>JClientInterceptorHelper</code> is the CAROL JRMP Client Interceptor Helper 
 * this class is used by the other pakage class to manage client interception 
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002
 */
public class JClientInterceptorHelper {
  
    /**
     * hold a unique identifier of this class. This is used to determine if
     * contexts can be passed by reference using the JObjectStore
     * this is for performance optimization
     */
    public static UID getSpaceID() {
        return spaceID;
    }

    /**
     * The spaceID
     */
    private final static UID spaceID = new UID();


    /**
     * send client context with the request. The sendingRequest method of the PortableInterceptors
     * is called prior to marshalling arguments and contexts
     * @param ObjectOutput out
     * @param ClientRequestInterceptor All interceptor for this context
     * @exception IOException if an exception occur with the ObjectOutput
     */
    public static void send_request(ObjectOutput out, JClientRequestInterceptor [] cis) throws IOException {
	if (cis != null) {
	    JClientRequestInfo ri = new  JRMPClientRequestInfoImpl();
	    for (int i = 0; i < cis.length; i++) {
		cis[i].send_request(ri);		
	    }
	    // send all service context
	    out.writeObject(ri.get_all_request_service_context());
	}
	
    }



    /**
     * send client context in pool (see CORBA Specifications)      
     * @param ObjectOutput out
     * @param JClientRequestInterceptor All interceptor for this context
     * @exception IOException if an exception occur with the ObjectOutput 
     */
    public static void send_poll(ObjectOutput out, JClientRequestInterceptor [] cis) throws IOException {
	if (cis != null) {
	    // nothing fo the moment
	}
    }


    /**
     * Receive reply interception
     * @param ObjectInput in
     * @param ClientRequestInterceptor All interceptor for this context
     * @exception IOException if an exception occur with the ObjectOutput
     */
    public static void receive_reply(ObjectInput in, JClientRequestInterceptor [] cis)throws IOException {
	try {
	    if (cis != null) {
		JClientRequestInfo ri = new  JRMPClientRequestInfoImpl((JServiceContext [])in.readObject());
		for (int i = 0; i < cis.length; i++) {
		    cis[i].receive_reply(ri);		
		}
	    }
	} catch (ClassNotFoundException cnfe) {
	    throw new IOException("" + cnfe);
	}
    }

    /**
      
     * Receive exception interception
     * @param ObjectInput in
     * @param ClientRequestInterceptor All interceptor for this context
     * @exception IOException if an exception occur with the ObjectOutput 
     */
    public static void receive_exception(ObjectInput in, JClientRequestInterceptor [] cis) throws IOException {
	try {
	    if (cis != null) {
		JClientRequestInfo ri = new  JRMPClientRequestInfoImpl((JServiceContext [])in.readObject());
		for (int i = 0; i < cis.length; i++) {
		    cis[i].receive_exception(ri);		
		}
	    }
	} catch (ClassNotFoundException cnfe) {
	    throw new IOException("" +cnfe);
	}
    }


    /*
     * Receive other interception
     *
     * @deprecated
     *
     * @param ObjectInput in
     * @param ClientRequestInterceptor All interceptor for this context
     * @exception IOException if an exception occur with the ObjectOutput      
     */
    public static void receive_other(ObjectInput in, JClientRequestInterceptor [] cis) throws IOException {
	try {
	    if (cis != null) {
		JClientRequestInfo ri = new  JRMPClientRequestInfoImpl((JServiceContext [])in.readObject());
		for (int i = 0; i < cis.length; i++) {
		    cis[i].receive_other(ri);		
		}
	    }
	} catch (ClassNotFoundException cnfe) {
	    throw new IOException("" +cnfe);
	}
    }

}
