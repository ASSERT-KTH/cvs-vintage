/*
 * @(#) IIOPDummyClientInterceptor.java	1.0 02/07/15
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
package org.objectweb.carol.jtests.conform.interceptor.iiop;

// java import
import java.io.IOException;

// omg import
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.IOP.ServiceContext;
import org.omg.CORBA.LocalObject;

/**
 * Class <code>IIOPDummyClientInterceptor</code> is a  IIOP Dummy client interceptor
 * for carol testing
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002  
 */
public class IIOPDummyClientInterceptor extends LocalObject implements ClientRequestInterceptor {

    /**
     * Server dummy context id
     */
    private static int SERVER_CTX_ID = 50;

    /**
     * Client dummy context id
     */
    private static int CLIENT_CTX_ID = 51;

    /**
     * interceptor name
     */
    private String interceptorName = null;

    /**
     * constructor 
     * @param String name
     */
    public IIOPDummyClientInterceptor(String name) {
	interceptorName = name;
    }

    /**
     * get the name of this interceptor
     * @return name
     */
    public String name() {
	return interceptorName;
    } 

    public void destroy() 
    {
    }
    /**
     * send client context with the request. The sendingRequest method of the JPortableInterceptors
     * is called prior to marshalling arguments and contexts
     * @param JClientRequestInfo jri the jrmp client info 
     * @exception ForwardRequest if an exception occur with the ObjectOutput
     */
    public void send_request(ClientRequestInfo jri) throws ForwardRequest {
	try {
	    byte [] data =  java.net.InetAddress.getLocalHost().getHostName().getBytes();
	    //System.out.println("IIOPDummyClientInterceptor Add/Send Dummy Client Service Context");
	    jri.add_request_service_context(new ServiceContext(CLIENT_CTX_ID, data), true);
	} catch (Exception e) {
	    // no service context : do nothing
	}
    }

    /**
     * Receive reply interception
     * @param JClientRequestInfo jri the jrmp client info 
     */
    public void receive_reply(ClientRequestInfo jri) {
	try {
	    //System.out.println("IIOPDummyClientInterceptor Get/Receive Dummy Server Service Context:");
	    //System.out.println(new String (((ServiceContext)jri.get_reply_service_context(SERVER_CTX_ID)).context_data));
	} catch (Exception e) {
	    // no service context : do nothing
	}
	
    }

    // empty method
    public void send_poll(ClientRequestInfo jri) {
    }

    public void receive_exception(ClientRequestInfo jri) throws ForwardRequest {
    }

    public void receive_other(ClientRequestInfo jri) throws ForwardRequest {
    }
}
