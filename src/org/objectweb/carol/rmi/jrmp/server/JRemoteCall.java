/*
 * @(#) JRemoteCall.java	1.0 02/07/15
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

//java import
import java.io.ObjectInput;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.rmi.server.ObjID;

import org.objectweb.carol.rmi.jrmp.interceptor.JClientInterceptorHelper;
import org.objectweb.carol.rmi.jrmp.interceptor.JClientRequestInfo;
import org.objectweb.carol.rmi.jrmp.interceptor.JClientRequestInterceptor;

import sun.rmi.transport.Connection;
import sun.rmi.transport.StreamRemoteCall;

/**
 * Class <code>JRemoteCall</code> is the CAROL JRMP Remote call with context propagation
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002 
*/
public class JRemoteCall extends StreamRemoteCall {

    /**
     * Client Interceptor for context propagation
     */
    protected JClientRequestInterceptor [] cis = null;  

    /**
     * Constructor for client side call
     * @param c The connection
     * @param id the object id
     * @param hash the hash code
     * @param cis the client interceptors array
     */
    public JRemoteCall(Connection c, ObjID id, int op, long hash, JClientRequestInterceptor [] cis)
            throws RemoteException {
        super(c, id, op, hash);
	this.cis = cis;
    }


    /**
     * override executeCall to receive and reassociate contexts that were
     * sent back from the server in the case of exeptional return
     * 
     * @deprecated
     */
    public void executeCall() throws Exception {
          try {
            super.executeCall();
        } catch (Exception e) {
            // if it is our dummy exception read the real one
            if (e.getMessage().equals("dummy")) {
                ObjectInput in = getInputStream();
                JClientInterceptorHelper.receive_exception(in, cis);
		Object ex;
		try {
		    ex = in.readObject();
		} catch (Exception exc) {
		    throw new UnmarshalException("Error unmarshaling return", exc);
                    }
		if (ex instanceof Exception) {
		    // this throw an exception
		    exceptionReceivedFromServer((Exception) ex);
                    } else {
                        throw new UnmarshalException("Return type not Exception");
                    }
            } else {
		// There is no other receive context propagation
		// The other case is generaly a network problem, so there is 
		// no context propagation inside from the server 
                throw e;
            }
        }
        ObjectInput in = getInputStream();
        JClientInterceptorHelper.receive_reply(in, cis);
    }
}
