/*
 * @(#) ProtocolInterceptor.java	1.0 02/07/15
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

import org.objectweb.carol.util.multi.ProtocolCurrent;

/**
 * Class <code>ProtocolInterceptor</code> is the CAROL JRMP Client Interceptor For protocol propagation
 * via current thread
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002  
 */
public class ProtocolInterceptor implements  JServerRequestInterceptor {

    /** 
     * interceptor name
     */
   private String interceptorName = null;

    /**
     * constructor 
     * @param String name
     */
    public ProtocolInterceptor() {
	interceptorName = "protocol interceptor xxxx1";
    }

    /**
     * get the name of this interceptor
     * @return name
     */
    public String name() {
	return interceptorName;
    } 

    /**
     * Receive request 
     * @param JServerRequestInfo the jrmp server request information
     * @exception IOException if an exception occur with the ObjectOutput
     */
    public void receive_request(JServerRequestInfo jri) throws IOException {
	ProtocolCurrent.getCurrent().setRMI("jrmp");
    }

    /**
     * send reply with context
     * @param JServerRequestInfo the jrmp server request information
     * @exception IOException if an exception occur with the ObjectOutput
     */
    public void send_reply(JServerRequestInfo jri)throws IOException {
	// do nothing here
    }


    public void send_exception(JServerRequestInfo jri) throws IOException {
	// do nothing here 
    }

    public void send_other(JServerRequestInfo jri) throws IOException {
	// do nothing here 
    }
}
