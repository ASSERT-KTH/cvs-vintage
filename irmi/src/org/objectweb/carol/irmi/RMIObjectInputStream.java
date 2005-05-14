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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.rmi.server.RMIClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * RMIObjectInputStream is a specialized subclass of ObjectInputStream
 * used by this RMI implementation. It must be used in concert with
 * {@link RMIObjectOutputStream}. Specialized behavior includes class
 * resolution using {@link RMIClassLoader}, and caching of class
 * descriptors.
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public class RMIObjectInputStream extends ObjectInputStream {

    /**
     * A List object used as a map from int id -> {@link Class}.
     */
    private List cache = new ArrayList();

    /**
     * A cache from {@link ObjectStreamClass} -> resolved {@link Class}.
     */
    private Map resolved = new HashMap();

    /**
     * Constructs a new RMIObjectInputStream that ready from the
     * specified {@link InputStream}.
     *
     * @param in the {@link InputStream} to use for input
     */

    public RMIObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    /**
     * Implementation of the {@link
     * ObjectInputStream#resolveClass(ObjectStreamClass)} callback.
     * This implementation delegates to {@link RMIClassLoader}.
     *
     * @param desc the {@link ObjectStreamClass} serialized
     * representation of the class object
     * @return the resolved {@link Class} object corresponding to the
     * serialized class object
     * @throws IOException if there is an error with the underlying stream
     * @throws ClassNotFoundException if the serialized class cannot
     * be located
     */

    protected Class resolveClass(ObjectStreamClass desc)
        throws IOException, ClassNotFoundException {
        Class result = (Class) resolved.get(desc);
        if (result == null) {
            String annotation = readUTF();
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            result = RMIClassLoader.loadClass(annotation, desc.getName(), cl);
            resolved.put(desc, result);
        }
        return result;
    }

    /**
     * Implementation of the {@link
     * ObjectInputStream#resolveProxyClass(String[])} callback. This
     * implementation delegates to {@link RMIClassLoader}.
     *
     * @param interfaces the interfaces implemented by the serialized
     * proxy class
     * @return the resolved {@link Class} object corresponding to the
     * serialized class object
     * @throws IOException if there is an error with the underlying stream
     * @throws ClassNotFoundException if any of the serialized
     * interfaces cannot be located
     */

    protected Class resolveProxyClass(String[] interfaces)
        throws IOException, ClassNotFoundException {
        Object key = new ArrayList(Arrays.asList(interfaces));
        Class result = (Class) resolved.get(key);
        if (result == null) {
            String annotation = readUTF();
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            result = RMIClassLoader.loadProxyClass(annotation, interfaces, cl);
            resolved.put(key, result);
        }
        return result;
    }

    /**
     * Implementation of the {@link
     * ObjectInputStream#readClassDescriptor()} callback. This
     * implementation reads a class id from the underlying input
     * stream and either delegates to its superclass to read in a full
     * class descriptor or uses the id to return a previosuly read
     * class descriptor.
     *
     * @return the serialized {@link ObjectStreamClass}
     */

    protected ObjectStreamClass readClassDescriptor()
        throws IOException, ClassNotFoundException {
        int num = readShort();
        ObjectStreamClass result;
        if (num == -1) {
            result = super.readClassDescriptor();
            cache.add(result);
        } else {
            result = (ObjectStreamClass) cache.get(num);
        }
        return result;
    }

    /**
     * Implementation of the {@link
     * ObjectInputStream#readStreamHeader()} callback. This
     * implementation is specifically overridden to do nothing. See
     * {@link java.io.ObjectOutputStream#writeStreamHeader()} for
     * details.
     */

    protected void readStreamHeader() {}

}
