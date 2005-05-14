/**
 * Copyright (c) 2004 Red Hat, Inc. All rights reserved.
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
 * Component of: Red Hat Application Server
 *
 * Initial Developers: Rafael H. Schloming
 */
package org.objectweb.carol.irmi;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * <p>The Interceptor API provides callbacks for sending and receiving
 * specialized data along with remote calls made by this RMI
 * implementation. This is a powerful feature that must be carefully
 * used. The {@link #send(byte, ObjectOutput)} and {@link
 * #receive(byte, ObjectInput)} implementations must be symmetric
 * between client and server interceptor implementations so as not to
 * corrupt the protocol used by this RMI implementation.</p>
 *
 * <p>Additionally for performance reasons implementors of this
 * interface should be careful to not send/receive too many bytes from
 * since these callbacks are invoked for each remote method call made
 * by this RMI implementation.</p>
 *
 * <p>This interface is used on both the server and client
 * communication endpoints although client interceptors must implement
 * the {@link ClientInterceptor} specialization in order to be {@link
 * java.io.Serializable}.</p>
 *
 * @see Server#Server(ClientInterceptor, Interceptor)
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public interface Interceptor extends Constants {

    /**
     * This method is invoked when a message is received from the
     * server or client during a remote call. The type of message is
     * indicated by the code parameter. Legal values may be found in
     * the {@link Constants} interface. An ObjectInput instance is
     * provided for reading additional information included by a
     * corresponding {@link Interceptor#send(byte, ObjectOutput)}
     * implementation on the sending end of the message.
     *
     * @param code indicates the type of message being received
     * @param in available for reading interceptor specific protocol
     * data
     * @throws IOException when there is an underlying communication
     * error with the {@link ObjectInput} instance
     * @throws ClassNotFoundException when there is an error
     * deserializing objects from the {@link ObjectInput} instances
     */

    void receive(byte code, ObjectInput in) throws IOException,
                                                   ClassNotFoundException;

    /**
     * This method is invoked when a message is sent to the server or
     * client during a remote call. The type of message is indicated
     * by the code parameter. Legal values may be found in the {@link
     * Constants} interface. An {@link ObjectOutput} instance is
     * provided for sending additional information to the {@link
     * Interceptor#receive(byte, ObjectInput)} implementation on the
     * receiving end of the message.
     *
     * @param code indicates the type of message being sent
     * @param out available for sending output to the receiving
     * interceptor implementation
     * @throws IOException when there is an underlyinc communication
     * error with the {@link ObjectOutput} instance
     */

    void send(byte code, ObjectOutput out) throws IOException;

}
