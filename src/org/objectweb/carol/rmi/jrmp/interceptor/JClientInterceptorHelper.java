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


import org.objectweb.carol.util.configuration.TraceCarol;
/**
 * Class <code>JClientInterceptorHelper</code> is the CAROL JRMP Client Interceptor Helper 
 * this class is used by the other pakage class to manage client interception 
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002
 */
public class JClientInterceptorHelper extends JInterceptorHelper {
 
    /**
     * send client context with the request. The sendingRequest method of the PortableInterceptors
     * is called prior to marshalling arguments and contexts
     * @param ObjectOutput out
     * @param ClientRequestInterceptor All interceptor for this context
     * @exception IOException if an exception occur with the ObjectOutput
     */
    public static void send_request(ObjectOutput out, 
				    JClientRequestInterceptor [] cis, 
				    boolean localRef) throws IOException {
	if ((cis==null)||(cis.length==0)) {
	    if (TraceCarol.isDebugRmiCarol()) {
		TraceCarol.debugRmiCarol("JClientInterceptorHelper send request with no contexts");
	    }
	    // send no service context
	    out.writeInt(NO_CTX);
	} else { 
	    JClientRequestInfo ri = new  JRMPClientRequestInfoImpl();
	    for (int i = 0; i < cis.length; i++) {
		cis[i].send_request(ri);		
	    }
	    setClientContextInOutput(out, ri, localRef);
	}
    }



    /**
     * send client context in pool (see CORBA Specifications)      
     * @param ObjectOutput out
     * @param JClientRequestInterceptor All interceptor for this context
     * @exception IOException if an exception occur with the ObjectOutput 
     */
    public static void send_poll(ObjectOutput out, 
				 JClientRequestInterceptor [] cis, 
				 boolean localRef) throws IOException {
    }


    /**
     * Receive reply interception
     * @param ObjectInput in
     * @param ClientRequestInterceptor All interceptor for this context
     * @exception IOException if an exception occur with the ObjectOutput
     */
    public static void receive_reply(ObjectInput in, JClientRequestInterceptor [] cis)throws IOException {
	try {
	    int ctxValue= in.readInt();
	    if ((cis == null)||(cis.length==0)) {
		// no interceptions
		if (TraceCarol.isDebugRmiCarol()) {
		    TraceCarol.debugRmiCarol("JClientInterceptorHelper receive reply with no contexts");
		}
		getClientContextFromInput(in, ctxValue, false);
	    } else {
		// context and interception
		if (TraceCarol.isDebugRmiCarol()) {
		    TraceCarol.debugRmiCarol("JClientInterceptorHelper receive reply contexts");
		}
		JClientRequestInfo ri = new JRMPClientRequestInfoImpl(getClientContextFromInput(in, ctxValue, true));
		for (int i = 0; i < cis.length; i++) {
		    cis[i].receive_reply(ri);		
		}
	    }
	} catch (ClassNotFoundException cnfe) {
	    throw new IOException("" + cnfe);
	}
    }

    /**
     *      
     * Receive exception interception
     * @param ObjectInput in
     * @param ClientRequestInterceptor All interceptor for this context
     * @exception IOException if an exception occur with the ObjectOutput 
     */
    public static void receive_exception(ObjectInput in, JClientRequestInterceptor [] cis) throws IOException {
	try {	  
	    int ctxValue= in.readInt();
	    if ((cis == null)||(cis.length==0)) {
		// no interceptions
		if (TraceCarol.isDebugRmiCarol()) {
		    TraceCarol.debugRmiCarol("JClientInterceptorHelper receive exception with no contexts");
		}
		getClientContextFromInput(in, ctxValue, false);
	    } else {
		// context and interception
		if (TraceCarol.isDebugRmiCarol()) {
		    TraceCarol.debugRmiCarol("JClientInterceptorHelper receive exception contexts");
		}
		JClientRequestInfo ri = new JRMPClientRequestInfoImpl(getClientContextFromInput(in, ctxValue, true));
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
	    int ctxValue= in.readInt();
	    if ((cis == null)||(cis.length==0)) {
		// no interceptions
		if (TraceCarol.isDebugRmiCarol()) {
		    TraceCarol.debugRmiCarol("JClientInterceptorHelper receive other with no contexts");
		}
		getClientContextFromInput(in, ctxValue, false);
	    } else {
		// context and interception
		if (TraceCarol.isDebugRmiCarol()) {
		    TraceCarol.debugRmiCarol("JClientInterceptorHelper receive other contexts");
		}
		JClientRequestInfo ri = new JRMPClientRequestInfoImpl(getClientContextFromInput(in, ctxValue, true));
		for (int i = 0; i < cis.length; i++) {
		    cis[i].receive_other(ri);		
		}
	    }
	} catch (ClassNotFoundException cnfe) {
	    throw new IOException("" +cnfe);
	}
    }    


    /**
     * Get Context from Object Input
     * @param ObjectInput in the object input stream
     * @param int the context value
     * @param boolean do not build Request Info
     */
    public static JServiceContext [] getClientContextFromInput(ObjectInput in, 
							       int ctxValue, 
							       boolean request) throws ClassNotFoundException, 
										       IOException {
	if (ctxValue==NO_CTX) {
	    if (TraceCarol.isDebugRmiCarol()) {
		TraceCarol.debugRmiCarol("JClientInterceptorHelper getObjectFromInput no context, request="+request);
	    }
	    return null;
	} else if (ctxValue==REMOTE_CTX) {
	    if (TraceCarol.isDebugRmiCarol()) {
		TraceCarol.debugRmiCarol("JClientInterceptorHelper getObjectFromInput remote, request="+request);
	    }
	    if (request) {
		return (JServiceContext [])in.readObject();
	    } else {
		in.readObject();
		return null;
	    }
	} else if (ctxValue==LOCAL_CTX) {
	    // local context case 
	    int id = in.readInt();
	    // local context
	    if (TraceCarol.isDebugRmiCarol()) {
		TraceCarol.debugRmiCarol("JClientInterceptorHelper getObjectFromInput local id("+id+"), request="+request);
	    }
	    if (request) {
		return (JServiceContext [])JContextStore.getObject(id);
	    } else {
		JContextStore.getObject(id);
		return null;
	    }
	} else {
	    throw new IOException("Unknow context type:" + ctxValue);
	}
    }

    /**
     * Set Context inObject Outut
     * @param ObjectOutput in the object OutPutStream
     * @param int the context value
     * @param boolean do not build Request Info
     */
    public static void setClientContextInOutput(ObjectOutput out, 
						JClientRequestInfo ri, 
						boolean locRef) throws IOException {
	if (!ri.hasContexts()) {
	    if (TraceCarol.isDebugRmiCarol()) {
		TraceCarol.debugRmiCarol("JClientInterceptorHelper send request without contexts");
	    }
	    // send no service context
	    out.writeInt(NO_CTX);
	} else if (locRef) {
	    Object ctx = ri.get_all_request_service_context();
	    int k = JContextStore.storeObject(ctx);
	    out.writeInt(LOCAL_CTX);
	    out.writeInt(k);
	    if (TraceCarol.isDebugRmiCarol()) {
		TraceCarol.debugRmiCarol("JClientInterceptorHelper send request with local contexts id("+k+")");
	    }
	    // send local service context		    
	} else {
	    if (TraceCarol.isDebugRmiCarol()) {
		TraceCarol.debugRmiCarol("JClientInterceptorHelper send request with remote contexts");
	    }
	    // send remotes service context
	    out.writeInt(REMOTE_CTX);
	    out.writeObject(ri.get_all_request_service_context());
	}
    }
}
