/*
 * @(#) ProtocolInitializer.java	1.0 02/07/15
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
package org.objectweb.carol.rmi.iiop.interceptor;

//java import 
import java.io.IOException;

//omg import 
import org.omg.CORBA.LocalObject;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.ServerRequestInfo;

//carol import 
import org.objectweb.carol.util.multi.ProtocolCurrent;

/**
 * Class <code>ProtocolInterceptor</code> is the CAROL JNDI IIOP Interceptor for iiop protocol
 * this interceptor mark the current thread for each call with the iiop mark
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @see org.omg.PortableInterceptor.ServerRequestInterceptor
 * @version 1.0, 15/07/2002
 */
public class ProtocolInterceptor extends LocalObject implements  ServerRequestInterceptor {

    /**
     * interceptor name
     */
   private String interceptorName = null;

    /**
     * constructor 
     */
    public ProtocolInterceptor() {
	interceptorName = "protocol interceptor xxxx2";
    }

    /**
     * Receive request context 
     * @param JServerRequestInfo the jrmp server request information
     * @exception ForwardRequest if an exception occur with the ObjectOutput
     */
    public void receive_request_service_contexts(ServerRequestInfo jri) throws ForwardRequest {	
    }

    /**
     * Receive request 
     * @param JServerRequestInfo the jrmp server request information
     * @exception ForwardRequest if an exception occur with the ObjectOutput
     */
    public void receive_request(ServerRequestInfo jri) throws ForwardRequest {
	ProtocolCurrent.getCurrent().setRMI("iiop");
    }

    /**
     * send reply with context
     * @param JServerRequestInfo the jrmp server request information
     */
    public void send_reply(ServerRequestInfo jri)  {

    }

    
     /**
     * get the name of this interceptor
     * @return name
     */
    public String name() {
	return interceptorName;
    } 

    // methods not used 
    public void send_exception(ServerRequestInfo jri) throws ForwardRequest {
    }

    public void send_other(ServerRequestInfo jri) throws ForwardRequest {
    }
    
    public void destroy() {
    }
}
