/*
 * @(#) JInterceptorStore.java	1.0 02/07/15
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

import java.rmi.server.UID;
import java.net.InetAddress;
import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;

/**
 * Class <code>RemoteKey</code> is the CAROL JRMP Remote Key Value
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 10/03/2003
 */
public class RemoteKey implements Externalizable {

    /**
     * remote uid
     */
    public transient UID uid = null;

    /**
     * remote inet address
     */
    public transient InetAddress inetA = null;

    /**
     * Empty Constructor for externalizabe
     */
    public RemoteKey() {
    }

    /**
     * Constructor
     */
    public RemoteKey(UID uid, InetAddress inetA) {
	this.uid=uid;
	this.inetA=inetA;
    }

    /**
     * equals method
     * return true if the object equals 
     * this Remote Key
     */
    public boolean equals(Object obj) {
	if ( inetA.equals(((RemoteKey)obj).inetA )) {
	    if ( uid.equals(((RemoteKey)obj).uid )) {
		return true;
	    } else {
		return false;
	    }
	} else {
	    return false;
	} 
    }
   
    /**
     * override readExternal to initialise localRef
     * We could actually receive anything from the server on lookup
     * @param in the object input 
     * @param newFormat the new format boolean 
     */
    public void readExternal(ObjectInput in) throws IOException,ClassNotFoundException {
        inetA = (InetAddress)in.readObject();
        uid = UID.read(in);
    }

    /**
     * override writeExternal to send spaceID And the interceptor
     * We could actually send anything to the client on lookup
     * @param out the object output
     * @param newFormat the boolean new format
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(inetA);
        uid.write(out);
    }    
	
}

