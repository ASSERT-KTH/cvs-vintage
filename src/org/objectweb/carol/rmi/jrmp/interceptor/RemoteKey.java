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
import java.util.Arrays;

/**
 * Class <code>RemoteKey</code> is the CAROL JRMP Remote Key Value
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 10/03/2003
 */
public class RemoteKey {

    /**
     * remote uid
     */
    private UID uid = null;

    /**
     * remote inet address
     */
    private byte[] inetA = null;

    /**
     * Empty Constructor for externalizabe
     */
    public RemoteKey() {
    }

    /**
     * Constructor
     */
    public RemoteKey(UID uid, byte[] inetA) {
	this.uid=uid;
	this.inetA=inetA;
    }

    /**
     * equals method
     * return true if the object equals 
     * this Remote Key
     */
    public boolean equals(Object obj) {
	if (Arrays.equals(inetA, ((RemoteKey)obj).inetA )) {
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
     * @return byte arrays
     */
    public byte[] getInetA() {
        return inetA;
    }

    /**
     * @return UID server uid 
     */
    public UID getUid() {
        return uid;
    }

}
