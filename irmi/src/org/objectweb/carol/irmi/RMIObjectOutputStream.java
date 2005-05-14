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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.rmi.Remote;
import java.rmi.server.RMIClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * RMIObjectOutputStream is a specialized subclass of
 * ObjectOutputStream used by this RMI implementation. It must be used
 * in concert with {@link RMIObjectInputStream}. Specialized behavior
 * includes class annotation using {@link RMIClassLoader}, automatic
 * conversion of Remote objects to stubs, caching of class descriptors
 * accross calls to {@link #reset()}, and output buffering.
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public class RMIObjectOutputStream extends ObjectOutputStream {

    /**
     * Cache from {@link Class} -> {@link Integer} id.
     */
    private Map cache = new HashMap();

    /**
     * Set of previously annoated {@link Class} objects.
     */
    private Set annotated = new HashSet();

    /**
     * Constructs a new RMIObjectOutputStream that writes to the given
     * {@link OutputStream}.
     *
     * @param out the {@link OutputStream} to use for output
     */

    public RMIObjectOutputStream(OutputStream out) throws IOException {
        super(new BufferedOutputStream(out, 4*1024));
        enableReplaceObject(true);
    }

    /**
     * Implementation of the {@link
     * ObjectOutputStream#annotateClass(Class)} callback. This
     * implementation delegates to {@link RMIClassLoader}.
     *
     * @param cl the {@link Class} to annotate
     * @throws IOException when there is an error with the underlying
     * stream
     */

    protected void annotateClass(Class cl) throws IOException {
        if (annotated.contains(cl)) { return; }
        String annotation = RMIClassLoader.getClassAnnotation(cl);
        if (annotation == null) { annotation = ""; }
        writeUTF(annotation);
        annotated.add(cl);
    }

    /**
     * Implementation of the {@link
     * ObjectOutputStream#annotateProxyClass(Class cl)} callback. This
     * implementation defers to {@link #annotateClass(Class)}.
     *
     * @param cl the {@link Class} to annotate
     * @throws IOException when there is an error with the underlying
     * stream
     */

    protected void annotateProxyClass(Class cl) throws IOException {
        annotateClass(cl);
    }

    /**
     * Implementation of the {@link
     * ObjectOutputStream#replaceObject(Object)} callback. This
     * implementation converts Remote objects to stubs if necessary.
     *
     * @param obj the object being considered for serialization
     * @return the object to be serialized
     */

    protected Object replaceObject(Object obj) {
        if (obj instanceof Remote) {
            Collection servers = Server.getServers();
            for (Iterator it = servers.iterator(); it.hasNext(); ) {
                Server server = (Server) it.next();
                Object stub = server.getStub((Remote) obj);
                if (stub != null) {
                    return stub;
                }
            }
            throw new RuntimeException("can't get stub for: " + obj);
        } else {
            return obj;
        }
    }

    /**
     * Implementation of the {@link
     * ObjectOutputStream#writeClassDescriptor(ObjectStreamClass)}
     * callback. This implementation delegates to the superclass if
     * this descripter has not already been written to the output
     * stream, otherwise it sends an integer id referencing the
     * previously written class.
     *
     * @param desc the {@link ObjectStreamClass} to write
     * @throws IOException if there is an error with the underlying
     * stream
     */

    protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
        Class klass = desc.forClass();
        Integer num = (Integer) cache.get(klass);
        if (num == null) {
            num = new Integer(cache.size());
            cache.put(klass, num);
            writeShort(-1);
            super.writeClassDescriptor(desc);
        } else {
            writeShort(num.intValue());
        }
    }

    /**
     * Implementation of the {@link
     * ObjectOutputStream#writeStreamHeader()} callback. This
     * implementation is specifically overriden to do nothing because
     * both the {@link ObjectOutputStream#writeStreamHeader()} and
     * {@link java.io.ObjectInputStream#readStreamHeader()} callbacks
     * are invoked from each class' respective constructor. This
     * introduces a potentially non obvious side effect when
     * constructing new stream objects in a client server context
     * since the {@link java.io.ObjectInputStream} constructor will
     * block waiting for input and this can cause deadlock if the
     * {@link java.io.ObjectInputStream} is constructed first on both
     * the client and server ends.
     */

    protected void writeStreamHeader() {}

}
