/**
 * Copyright (C) 2002,2005 - INRIA (www.inria.fr)
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
 * --------------------------------------------------------------------------
 * $Id: IIOPDummyClientInterceptor.java,v 1.5 2005/02/14 09:41:56 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jtests.conform.interceptor.iiop;

import org.omg.CORBA.LocalObject;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;

import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Class <code>IIOPDummyClientInterceptor</code> is a IIOP Dummy client
 * interceptor for carol testing
 * @author Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 */
public class IIOPDummyClientInterceptor extends LocalObject implements ClientRequestInterceptor {

    /**
     * Server dummy context id
     */
    //private static final int SERVER_CTX_ID = 50;

    /**
     * Client dummy context id
     */
    private static final int CLIENT_CTX_ID = 51;

    /**
     * interceptor name
     */
    private String interceptorName = null;

    /**
     * constructor
     * @param name of the interceptor
     */
    public IIOPDummyClientInterceptor(String name) {
        interceptorName = name;
    }

    /**
     * get the name of this interceptor
     * @return name of this interceptor
     */
    public String name() {
        return interceptorName;
    }

    /**
     * Destroy interceptor
     */
    public void destroy() {
    }

    /**
     * send client context with the request. The sendingRequest method of the
     * JPortableInterceptors is called prior to marshalling arguments and
     * contexts
     * @param jri the jrmp client info
     * @exception ForwardRequest if an exception occur with the ObjectOutput
     */
    public void send_request(ClientRequestInfo jri) throws ForwardRequest {
        try {
            byte[] data = java.net.InetAddress.getLocalHost().getHostName().getBytes();
            TraceCarol.debugCarol("Add/Send Dummy Client Service Context");
            jri.add_request_service_context(new ServiceContext(CLIENT_CTX_ID, data), true);
        } catch (Exception e) {
            TraceCarol.debugCarol("No service context");
        }
    }

    /**
     * Allows an Interceptor to query the information on a reply after it is
     * returned from the server and before control is returned to the client.
     * @param jri the jrmp client info
     */
    public void receive_reply(ClientRequestInfo jri) {
        TraceCarol.debugCarol("Get/Receive Dummy Server Service Context:");
        //System.out.println(new String
        // (((ServiceContext)jri.get_reply_service_context(SERVER_CTX_ID)).context_data));

    }

    /**
     * Allows an Interceptor to query information during a Time-Independent
     * Invocation (TII) polling get reply sequence.
     * @param jri Information about the current request being intercepted.
     */
    public void send_poll(ClientRequestInfo jri) {
    }

    /**
     * Indicates to the interceptor that an exception occurred. Allows an
     * Interceptor to query the exception's information before it is thrown to
     * the client.
     * @param jri Information about the current request being intercepted.
     * @exception ForwardRequest If thrown, indicates to the ORB that a retry of
     *            the request should occur with the new object given in the
     *            exception.
     */
    public void receive_exception(ClientRequestInfo jri) throws ForwardRequest {
    }

    /**
     * Allows an Interceptor to query the information available when a request
     * results in something other than a normal reply or an exception.
     * @param jri Information about the current request being intercepted.
     * @exception ForwardRequest If thrown, indicates to the ORB that a retry of
     *            the request should occur with the new object given in the
     *            exception.
     */
    public void receive_other(ClientRequestInfo jri) throws ForwardRequest {
    }
}