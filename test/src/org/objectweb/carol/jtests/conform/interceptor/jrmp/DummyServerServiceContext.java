/**
 * Copyright (C) 2002,2005 - INRIA (www.inria.fr)
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
 * $Id: DummyServerServiceContext.java,v 1.5 2005/02/08 10:03:48 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jtests.conform.interceptor.jrmp;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.objectweb.carol.rmi.jrmp.interceptor.JServiceContext;

/**
 * Class <code>DummyClientServiceContext</code> is a JRMP Interface for
 * Interceptor implementation for carol testing
 * @author Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 */
public class DummyServerServiceContext implements JServiceContext, Externalizable {

    /**
     * Adress
     */
    String address = null;

    /**
     * context id
     */
    private int context_id;

    /**
     * the JServiceContext id
     */
    public int getContextId() {
        return context_id;
    }

    /**
     * empty constructor for Externalizable
     */
    public DummyServerServiceContext() {
    }

    /**
     * constructor
     * @param int the context_id
     * @param byte[] the context data
     */
    public DummyServerServiceContext(int context_id, String address) {
        this.context_id = context_id;
        this.address = address;
    }

    public String toString() {
        return "Server Dummy Context From " + address;
    }

    /**
     * readExternal
     * @param in the ObjectInput
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        context_id = in.readInt();
        address = (String) in.readObject();
    }

    /**
     * writeExternal
     * @param out the object output stream
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(context_id);
        out.writeObject(address);
    }
}