/*
 * @(#)  JServerRequestIntercepto.java	1.0 02/07/15
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

/**
 * Interface <code>JServerRequestIntercepto</code> is the CAROL JRMP Server Interceptor interface
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002
 */
public interface JServerRequestInterceptor {

    /**
     * Receive request 
     * @param JServerRequestInfo the jrmp server request information
     * @exception IOException if an exception occur with the ObjectOutput
     */
    public void receive_request(JServerRequestInfo jri) throws IOException;

    /**
     * send reply with context
     * @param JServerRequestInfo the jrmp server request information
     * @exception IOException if an exception occur with the ObjectOutput
     */
    public void send_reply(JServerRequestInfo jri)throws IOException;

    /**
     * send exception with context
     * @param JServerRequestInfo the jrmp server request information
     * @exception IOException if an exception occur with the ObjectOutput 
     */
    public void send_exception(JServerRequestInfo jri) throws IOException;


    /*
     * @deprecated
     * send other with context 
     * @param JServerRequestInfo the jrmp server request information
     * @exception IOException if an exception occur with the ObjectOutput      
     */
    public void send_other(JServerRequestInfo jri) throws IOException;
    
     /**
     * get the name of this interceptor
     * @return name
     */
    public String name();   
}
