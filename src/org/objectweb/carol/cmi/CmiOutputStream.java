/**
 * Copyright (C) 2002-2005 - Bull S.A.
 *
 * CMI : Cluster Method Invocation
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
 * $Id: CmiOutputStream.java,v 1.2 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RMIClassLoader;

/**
 * To deserialize objects exchanged between CMI registries.
 *
 * @author Simon Nieuviarts
 */
public class CmiOutputStream extends ObjectOutputStream {

    /**
     * Create a new object for the output stream
     * @param out ouput stream
     * @throws IOException if IO exception is encountered
     */
     public CmiOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    /**
     * @see ObjectOutputStream#annotateClass(Class)
     */
    protected void annotateClass(Class cl) throws IOException {
        writeLocation(RMIClassLoader.getClassAnnotation(cl));
    }

    /**
     * @see ObjectOutputStream#annotateProxyClass(Class)
     */
   protected void annotateProxyClass(Class cl) throws IOException {
        annotateClass(cl);
    }

    /**
     * @see ObjectOutputStream#writeLocation(Class)
     */
    protected void writeLocation(String location) throws IOException {
        writeObject(location);
    }

    /**
     * @see ObjectOutputStream#serialize(Class)
     */
    public static byte[] serialize(Remote obj) throws RemoteException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        CmiOutputStream out;
        try {
            out = new CmiOutputStream(outStream);
            out.writeObject(obj);
            out.flush();
            return outStream.toByteArray();
        } catch (IOException e) {
            throw new RemoteException("Can't serialize object", e);
        }
    }
}
