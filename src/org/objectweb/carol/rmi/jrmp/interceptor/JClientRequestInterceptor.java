/**
 * Copyright (C) 2002,2004 - INRIA (www.inria.fr)
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
 * $Id: JClientRequestInterceptor.java,v 1.2 2004/09/01 11:02:41 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.rmi.jrmp.interceptor;

// java import
import java.io.IOException;
import java.io.Serializable;

/**
 * Interface <code>JClientRequestInterceptor</code> is the CAROL JRMP Client
 * Interceptor Interface this interface is use for client interceptor definition
 * @author Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002
 */
public interface JClientRequestInterceptor extends Serializable {

    /**
     * send client context with the request. The sendingRequest method of the
     * JPortableInterceptors is called prior to marshalling arguments and
     * contexts
     * @param JClientRequestInfo jri the jrmp client info
     * @exception IOException if an exception occur with the ObjectOutput
     */
    public void send_request(JClientRequestInfo jri) throws IOException;

    /**
     * send client context in pool
     * @param JClientRequestInfo jri the jrmp client info
     * @exception IOException if an exception occur with the ObjectOutput
     */
    public void send_poll(JClientRequestInfo jri) throws IOException;

    /**
     * Receive reply interception
     * @param JClientRequestInfo jri the jrmp client info
     * @exception IOException if an exception occur with the ObjectOutput
     */
    public void receive_reply(JClientRequestInfo jri) throws IOException;

    /**
     * Receive exception interception
     * @param JClientRequestInfo jri the jrmp client info
     * @exception IOException if an exception occur with the ObjectOutput
     */
    public void receive_exception(JClientRequestInfo jri) throws IOException;

    /*
     * Receive other interception @param JClientRequestInfo jri the jrmp client
     * info @exception IOException if an exception occur with the ObjectOutput
     */
    public void receive_other(JClientRequestInfo jri) throws IOException;

    /**
     * get the name of this interceptor
     * @return name
     */
    public String name();
}