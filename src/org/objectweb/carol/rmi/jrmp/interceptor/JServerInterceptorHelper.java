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

import org.objectweb.carol.util.configuration.TraceCarol;
/** 
 * Class <code>JServerInterceptorHelper</code> is the CAROL JRMP Server Interceptor Helper 
 * this class is used by the other pakage class to manage server interception 
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/200
 */
public class JServerInterceptorHelper extends JInterceptorHelper {

    /**
     * Thread Local for protocol context propagation
     */
    private static InheritableThreadLocal threadCtx = new InheritableThreadLocal();

    /**
     * Receive request 
     * @param ObjectInput in
     * @param JServerRequestInterceptor All interceptor for this context
     * @exception IOException if an exception occur with the ObjectOutput
     */
    public static void receive_request(ObjectInput in, JServerRequestInterceptor [] sis) throws IOException {
	try {	
	    int ctxValue= in.readInt();
	    if ((sis==null)||(sis.length==0)) {
		// no interceptions
		if (TraceCarol.isDebugRmiCarol()) {
		    TraceCarol.debugRmiCarol("JServerInterceptorHelper receive exception with no contexts");
		}
		getServerContextFromInput(in, ctxValue, false);
	    } else {
		// context and interception
		if (TraceCarol.isDebugRmiCarol()) {
		    TraceCarol.debugRmiCarol("JServerInterceptorHelper receive exception contexts");
		}
		JServerRequestInfo ri = new JRMPServerRequestInfoImpl(getServerContextFromInput(in, ctxValue, true));
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
    public static void send_reply(ObjectOutput out, JServerRequestInterceptor [] sis) throws IOException {
	if ((sis==null)||(sis.length==0)) {
	    if (TraceCarol.isDebugRmiCarol()) {
		TraceCarol.debugRmiCarol("JServerInterceptorHelper send reply with no contexts");
	    }
	    // send no service context
	    out.writeInt(NO_CTX);
	} else {
	    JServerRequestInfo ri = new  JRMPServerRequestInfoImpl();
	    for (int i = 0; i < sis.length; i++) {
		sis[i].send_reply(ri);		
	    }	    
	    setServerContextInOutput(out, ri, isLocal());
	    threadCtx.set(null);	    
	}
	
    }

    /**
     * send exception with context
     * @param ObjectOutput out
     * @param JServerRequestInterceptor All interceptor for this context
     * @exception IOException if an exception occur with the ObjectOutput 
     */
    public static void send_exception(ObjectOutput out, JServerRequestInterceptor [] sis) throws IOException {
	if ((sis == null)||(sis.length==0)) {	
	    if (TraceCarol.isDebugRmiCarol()) {
		TraceCarol.debugRmiCarol("JServerInterceptorHelper send exception with no contexts");
	    }
	    // send no service context
	    out.writeInt(NO_CTX);
	} else {	    
	    JServerRequestInfo ri = new  JRMPServerRequestInfoImpl();
	    for (int i = 0; i < sis.length; i++) {
		sis[i].send_exception(ri);		
	    }	    	    
	    setServerContextInOutput(out, ri, isLocal());
	    threadCtx.set(null);	    
	}
    }


    /*
     * send other with context 
     * @param ObjectOutput out
     * @param JServerRequestInterceptor All interceptor for this context
     * @exception IOException if an exception occur with the ObjectOutput      
     */
    public static void send_other(ObjectOutput out, JServerRequestInterceptor [] sis) throws IOException {
	if ((sis == null)||(sis.length==0)) {
	    if (TraceCarol.isDebugRmiCarol()) {
		TraceCarol.debugRmiCarol("JServerInterceptorHelper send other with no contexts");
	    }
	    // send no service context
	    out.writeInt(NO_CTX);	
	} else {	    
	    JServerRequestInfo ri = new  JRMPServerRequestInfoImpl();
	    for (int i = 0; i < sis.length; i++) {
		sis[i].send_other(ri);		
	    }	    	    
	    setServerContextInOutput(out, ri, isLocal());
	    threadCtx.set(null);	    
	} 
    }

    /**
     * Get Context from Object Input
     * @param ObjectInput in the object input stream
     * @param int the context value
     * @param boolean do not build Request Info
     */
    public static JServiceContext [] getServerContextFromInput(ObjectInput in, 
							       int ctxValue, 
							       boolean request) throws ClassNotFoundException, 
										       IOException {
	if (ctxValue==NO_CTX) {
	    if (TraceCarol.isDebugRmiCarol()) {
		TraceCarol.debugRmiCarol("JServerInterceptorHelper getObjectFromInput no context, request="+request);
	    }
	    return null;
	} else if (ctxValue==REMOTE_CTX) {
	    if (TraceCarol.isDebugRmiCarol()) {
		TraceCarol.debugRmiCarol("JServerInterceptorHelper getObjectFromInput remote, request="+request);
	    }
	    if (request) {
		return (JServiceContext [])in.readObject();
	    } else {
		in.readObject();
		return null;
	    }
	} else if (ctxValue==LOCAL_CTX) {
	    setLocal();
	    // local context case 
	    int id = in.readInt();
	    // local context
	    if (TraceCarol.isDebugRmiCarol()) {
		TraceCarol.debugRmiCarol("JServerInterceptorHelper getObjectFromInput local id("+id+"), request="+request);
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
    public static void setServerContextInOutput(ObjectOutput out, 
						JServerRequestInfo ri, 
						boolean locRef) throws IOException {
	if (!ri.hasContexts()) {
	    if (TraceCarol.isDebugRmiCarol()) {
		TraceCarol.debugRmiCarol("JServerInterceptorHelper send request without contexts");
	    }
	    // send no service context
	    out.writeInt(NO_CTX);
	} else if (locRef) {
	    Object ctx = ri.get_all_reply_service_context();
	    int k = JContextStore.storeObject(ctx);
	    out.writeInt(LOCAL_CTX);
	    out.writeInt(k);
	    if (TraceCarol.isDebugRmiCarol()) {
		TraceCarol.debugRmiCarol("JServerInterceptorHelper send request with local contexts id("+k+")");
	    }
	    // send local service context		    
	} else {
	    if (TraceCarol.isDebugRmiCarol()) {
		TraceCarol.debugRmiCarol("JServerInterceptorHelper send request with remote contexts");
	    }
	    // send remotes service context
	    out.writeInt(REMOTE_CTX);
	    out.writeObject(ri.get_all_reply_service_context());
	}
    }

    /**
     * Set Local Reference
     */
    public static void setLocal() {
	threadCtx.set("");
    }

    /**
     * is Local Reference
     * @return true if local reference
     */
    public static  boolean isLocal() {
	return (threadCtx.get()!=null);
    }  

}
