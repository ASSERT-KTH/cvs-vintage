/*
 * @(#) JUnicastRefSf.java	1.0 02/07/15
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

import org.objectweb.carol.rmi.jrmp.interceptor.JClientRequestInterceptor;

import sun.rmi.transport.LiveRef;

/** 
 * Class <code>JUnicastRefSf</code> is the CAROL JRMP UnicastRef with context propagation
 * Unicast Reference ensuring context propagation with custom sockets
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002    
 */
public class JUnicastRefSf extends JUnicastRef {

    /**
     * Create a new (empty) Unicast remote reference.
     */
    public JUnicastRefSf() {
    }

    /**
     * Create a new Unicast RemoteRef.
     * @param liveRef the live reference
     * @param cis the client interceptor array
     */
    public JUnicastRefSf(LiveRef liveRef, JClientRequestInterceptor [] cis) {
        super(liveRef, cis);
    }

    /**
     * override readExternal to initialise localRef
     * We could actually receive anything from the server on lookup
     * @param in the object input 
     */
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        readExternal(in, true);
    }

    /**
     * override writeExternal to send spaceID
     * We could actually send anything to the client on lookup
     * @param out the object output stream
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        writeExternal(out, true);
    }

}
