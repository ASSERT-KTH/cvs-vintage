/*
 * @(#) IIOPDummyServerInterceptor.java	1.0 02/07/15
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
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.IOP.ServiceContext;
import org.omg.CORBA.LocalObject;


/**
 * Class <code>IIOPDummyServerInterceptor</code> is a  IIOP Dummy server interceptor
 * for carol testing
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002  
 */
public class IIOPDummyServerInterceptor extends LocalObject implements  ServerRequestInterceptor {

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
    public IIOPDummyServerInterceptor(String name) {
	interceptorName = name;
    }

    /**
     * Receive request context 
     * @param JServerRequestInfo the jrmp server request information
     * @exception ForwardRequest if an exception occur with the ObjectOutput
     */
    public void receive_request_service_contexts(ServerRequestInfo jri) throws ForwardRequest {
	try {
	    //System.out.println("IIOPDummyServerInterceptor Get/Receive Dummy Client Service Context:");
	    //System.out.println(new String(((ServiceContext)jri.get_request_service_context(CLIENT_CTX_ID)).context_data));
	} catch (Exception e) {
	    // no service context : do nothing
	}
    }

    /**
     * Receive request 
     * @param JServerRequestInfo the jrmp server request information
     * @exception ForwardRequest if an exception occur with the ObjectOutput
     */
    public void receive_request(ServerRequestInfo jri) throws ForwardRequest {	
    }

    /**
     * send reply with context
     * @param JServerRequestInfo the jrmp server request information
     */
    public void send_reply(ServerRequestInfo jri)  {
	try {
	    byte [] data =  java.net.InetAddress.getLocalHost().getHostName().getBytes();
	    //System.out.println("IIOPDummyServerInterceptor Add/Send Dummy Server Service Context");
	    jri.add_reply_service_context(new ServiceContext(SERVER_CTX_ID, data), true);
	} catch (Exception e) {
	}
    }

    
     /**
     * get the name of this interceptor
     * @return name
     */
    public String name() {
	return interceptorName;
    } 


    public void send_exception(ServerRequestInfo jri) throws ForwardRequest {
    }

    public void send_other(ServerRequestInfo jri) throws ForwardRequest {
    }
    
    public void destroy() {
    }
}
