/*
 * @(#) JRemoteServerCall.java	1.0 02/07/15
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
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StreamCorruptedException;
import java.rmi.server.RemoteCall;

//carol import 
import org.objectweb.carol.rmi.jrmp.interceptor.JServerInterceptorHelper;
import org.objectweb.carol.rmi.jrmp.interceptor.JServerRequestInterceptor;

/**
 * Class <code>JRemoteServerCall</code> is the CAROL JRMP Remote Server call with context propagation
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002
 */
public class JRemoteServerCall implements RemoteCall {
    
    /**
     * The Remote Call Impl
     */
    RemoteCall impl;

    /**
     * Array of Interceptor for this Server Ref
     */
    protected JServerRequestInterceptor [] sis = null;

    /**
     * Constructor for server side call
     * @param impl the Remote call 
     * @param sis the server interceptor
     */
    public JRemoteServerCall(RemoteCall impl, JServerRequestInterceptor [] sis) {
        // we use delegation but need to extend StreamRemoteCall for 1.1 Stub
        this.impl = impl;
	this.sis = sis;
    }



    /**
     * override getResultStream to dissociate and pass contexts
     * back to the client. This method might be called several times.
     * @param success if success
     * 
     * @deprecated
     */
    public ObjectOutput getResultStream(boolean success) throws IOException,
            StreamCorruptedException {
        ObjectOutput out = impl.getResultStream(success);
        // we must send a dummy exception due to StreamRemoteCall.executeCall flow of control
        if (!success) {
            out.writeObject(new Exception("dummy"));
	    JServerInterceptorHelper.send_exception(out, sis);
	    return out;
        } else { // succes !
	    JServerInterceptorHelper.send_reply(out, sis);
	    return out;
	}
    }

 
    // standard server remote call methods
 	/**
 	 * @deprecated
 	 */   
     public ObjectOutput getOutputStream() throws IOException {
        return impl.getOutputStream();
    }

	/**
	 * @see java.rmi.server.RemoteCall#releaseOutputStream()
	 * @deprecated 
	 */
    public void releaseOutputStream() throws IOException {
        impl.releaseOutputStream();
    }

	/**
	 * @see java.rmi.server.RemoteCall#getInputStream()
	 * @deprecated 
	 */
    public ObjectInput getInputStream() throws IOException {
        return impl.getInputStream();
    }

	/**
	 * 
	 * @see java.rmi.server.RemoteCall#releaseInputStream()
	 * @deprecated 
	 */
    public void releaseInputStream() throws IOException {
        impl.releaseInputStream();
    }

	/**
	 * @see java.rmi.server.RemoteCall#executeCall()
	 * @deprecated 
	 */
    public void executeCall() throws Exception {
        throw new Error("should never be called by server");
    }

	/**	 
	 * @see java.rmi.server.RemoteCall#done()
	 * @deprecated 
	 */
    public void done() throws IOException {
        impl.done();
    }

}
