/*
 * @(#) JUnicastServerRef.java	1.0 02/07/15
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
package org.objectweb.carol.rmi.jrmp.server;


// sun import
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.Remote;
import java.rmi.server.RemoteCall;
import java.rmi.server.RemoteRef;

import org.objectweb.carol.rmi.jrmp.interceptor.JClientRequestInterceptor;
import org.objectweb.carol.rmi.jrmp.interceptor.JRMPServerRequestInfoImpl;
import org.objectweb.carol.rmi.jrmp.interceptor.JServerInterceptorHelper;
import org.objectweb.carol.rmi.jrmp.interceptor.JServerRequestInfo;
import org.objectweb.carol.rmi.jrmp.interceptor.JServerRequestInterceptor;

import sun.rmi.server.UnicastServerRef;
import sun.rmi.transport.LiveRef;

/**
 * Class <code>JUnicastServerRef</code> implements the remote reference layer server-side
 * behavior for remote objects exported with the JUnicastRef reference
 * type.
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002  
 */
public class JUnicastServerRef extends UnicastServerRef {

    /**
     * ServerRequestInterceptor array
     */
    protected JServerRequestInterceptor [] sis = null;

    /**
     * ClientRequestInterceptor array
     */
    protected JClientRequestInterceptor [] cis = null;
    
    /**
     * constructor 
     */
    public JUnicastServerRef() {
    }

    /**
     * Constructor  with interceptor
     * @param ref the live reference
     * @param sis the server interceptor array
     * @param cis the client interceptor array
     */
    public JUnicastServerRef(LiveRef ref, JServerRequestInterceptor[] sis, JClientRequestInterceptor[] cis) {
        super(ref);
	this.sis = sis;	
	this.cis = cis;
    }

    /**
     * Constructor  with interceptor
     * @param port the port reference
     * @param sis the server interceptor array
     * @param cis the client interceptor array
     */
    public JUnicastServerRef(int port, JServerRequestInterceptor[] sis, JClientRequestInterceptor[] cis) {
        super(new LiveRef(port));
	this.sis = sis;
	this.cis = cis;
    }

    /**
     * get the ref class name
     * @return String the class name
     */ 
    public String getRefClass(ObjectOutput out) {
        super.getRefClass(out);
        return "org.objectweb.carol.rmi.jrmp.server.JUnicastServerRef";
    }

    /**
     * use a different kind of RemoteRef instance
     * @return remoet Ref the remote reference
     */
    protected RemoteRef getClientRef() {
        return new JUnicastRef(ref, cis);
    }

    /**
     * override unmarshalCustomCallData to receive and establish contexts
     * sent by the client
     * @param in the object input 
     */
    protected void unmarshalCustomCallData(ObjectInput in)
            throws IOException, ClassNotFoundException {
        JServerInterceptorHelper.receive_request(in, sis);
        super.unmarshalCustomCallData(in);
    }

    /**
     * override dispatch to use a specific thread factory
     * @param obj the remote object
     * @param call the remote call on this object
     */
    public void dispatch(Remote obj, RemoteCall call) throws IOException {
        JUnicastThreadFactory factory = JUnicastRemoteObject.getDefaultThreadFactory();
        if (factory==null) {
            runDispatch(obj,call);
        } else {
            DispatchRunnable dr = new DispatchRunnable(obj,call);
            factory.getThread(dr).run(); // run the target
            if (dr.getIOException()!=null)
                throw dr.getIOException();
        }
    }

    /**
     *  method used to invoke <code>super.dispatch</code> and wrap the
     *  call to ensure invocation of context propagators.
     * @param obj the remote object
     * @param call the remote call on this object
     */
    private void runDispatch(Remote obj, RemoteCall call) throws IOException {
        super.dispatch(obj, new JRemoteServerCall(call, sis));
    }

    /**
     * Class used to run dispatch in a separated thread
     */
    private class DispatchRunnable implements Runnable {
	
	/**
	 * the remote object
	 */
        Remote obj;
	
	/**
	 * the remote call
	 */	
        RemoteCall call;

	/**
	 * the exception (IOException)
	 */
        IOException e = null;

	/**
	 * method used to invoke <code>super.dispatch</code> and wrap the
	 * call to ensure invocation of context propagators.
	 * @param obj the remote object
	 * @param call the remote call on this object 
	 */
        public DispatchRunnable(Remote obj, RemoteCall call) {
            this.obj = obj;
            this.call = call;
        }

	/**
	 * thread run method
	 */
        public void run() {
            try {
                runDispatch(obj, call);
            } catch(IOException e) {
                this.e = e;
            }
        }

	/**
	 * Exception builder
	 */
        public IOException getIOException() {
            return e;
        }
    }
}
