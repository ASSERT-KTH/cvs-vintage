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
import java.util.Collection;
import java.util.Iterator;

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
		TraceCarol.debugRmiCarol("JClientInterceptorHelper send request without interceptors");
	    }
	    // send no service context
	    out.writeInt(NO_CTX);
	} else { 
		JClientRequestInfo jrc = new JRMPClientRequestInfoImpl();
	    for (int i = 0; i < cis.length; i++) {
		cis[i].send_request(jrc);		
	    }
	    setClientContextInOutput(out, jrc, localRef);
	}
		// flush and reset output stream for garbage collection
		out.flush();
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
		JClientRequestInfo jrc = new JRMPClientRequestInfoImpl();
	    if ((cis == null)||(cis.length==0)) {
		// no interceptions
		if (TraceCarol.isDebugRmiCarol()) {
		    TraceCarol.debugRmiCarol("JClientInterceptorHelper receive reply without interceptors");
		}
		getClientRequestContextFromInput(in, ctxValue, jrc);
	    } else {
		// context and interception
		if (TraceCarol.isDebugRmiCarol()) {
		    TraceCarol.debugRmiCarol("JClientInterceptorHelper receive reply contexts");
		}
		JClientRequestInfo ri = getClientRequestContextFromInput(in, ctxValue, jrc);
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
		JClientRequestInfo jrc = new JRMPClientRequestInfoImpl();
	    if ((cis == null)||(cis.length==0)) {
		// no interceptions
		if (TraceCarol.isDebugRmiCarol()) {
		    TraceCarol.debugRmiCarol("JClientInterceptorHelper receive exception without interceptors");
		}
		getClientRequestContextFromInput(in, ctxValue, jrc);
	    } else {
		// context and interception
		if (TraceCarol.isDebugRmiCarol()) {
		    TraceCarol.debugRmiCarol("JClientInterceptorHelper receive exception contexts");
		}
		JClientRequestInfo ri = getClientRequestContextFromInput(in, ctxValue, jrc);
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
		JClientRequestInfo jrc = new JRMPClientRequestInfoImpl();
	    if ((cis == null)||(cis.length==0)) {
		// no interceptions
		if (TraceCarol.isDebugRmiCarol()) {
		    TraceCarol.debugRmiCarol("JClientInterceptorHelper receive other without interceptors");
		}
		getClientRequestContextFromInput(in, ctxValue, jrc);
	    } else {
		// context and interception
		if (TraceCarol.isDebugRmiCarol()) {
		    TraceCarol.debugRmiCarol("JClientInterceptorHelper receive other contexts");
		}
		JClientRequestInfo ri = getClientRequestContextFromInput(in, ctxValue,jrc);
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
     * @return jrc the client request info
     */
    public static JClientRequestInfo getClientRequestContextFromInput(ObjectInput in, 
							       int ctxValue, 
							       JClientRequestInfo jrc) throws ClassNotFoundException, 
										       IOException {
	if (ctxValue==NO_CTX) {
	    if (TraceCarol.isDebugRmiCarol()) {
		TraceCarol.debugRmiCarol("JClientInterceptorHelper getObjectFromInput no context");
	    }
	    return jrc;
	}  else if (ctxValue==REMOTE_CTX) {
	    if (TraceCarol.isDebugRmiCarol()) {
		TraceCarol.debugRmiCarol("JClientInterceptorHelper getObjectFromInput remote");
	    }
	    int sz = in.readInt();
		for (int i=0; i<sz; i++) {
		    jrc.add_request_service_context((JServiceContext)in.readObject());
		}
		return jrc;
	} else if (ctxValue==LOCAL_CTX) {
	    // local context case 
	    int id = in.readInt();
	    // local context
	    if (TraceCarol.isDebugRmiCarol()) {
		TraceCarol.debugRmiCarol("JClientInterceptorHelper getObjectFromInput local id("+id+")");
	    }
		jrc.add_all_request_service_context((Collection)JContextStore.getObject(id));
		return jrc;
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
			// print the contexts sended
			for (Iterator i = ((Collection)ctx).iterator(); i.hasNext();) {
				TraceCarol.debugRmiCarol("ctx:"  + i.next());
			}		
	    }	    
	} else {
	    // send remotes service context
	    out.writeInt(REMOTE_CTX);
	   	Collection allCtx = ri.get_all_request_service_context();
	    out.writeInt(allCtx.size());
		for (Iterator i = allCtx.iterator(); i.hasNext();) {
		out.writeObject(i.next());
		}
		if (TraceCarol.isDebugRmiCarol()) {
		 TraceCarol.debugRmiCarol("JClientInterceptorHelper send request with remote contexts");	
		 // print the contexts sended
		 for (Iterator i = allCtx.iterator(); i.hasNext();) {
			 TraceCarol.debugRmiCarol("ctx:"  + i.next());
		 }		
		}
	}
    }
}
