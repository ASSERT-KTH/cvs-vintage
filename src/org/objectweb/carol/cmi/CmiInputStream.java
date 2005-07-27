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
 * $Id: CmiInputStream.java,v 1.2 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;
import java.rmi.server.RMIClassLoader;

/**
 * To serialize objects exchanged between CMI registries.
 *
 * @author Simon Nieuviarts
 */
public class CmiInputStream extends ObjectInputStream {

    /**
     * Create a new object for the input stream
     * @param in input stream
     * @throws IOException if IO exception is encountered
     * @throws StreamCorruptedException if stream is corrupted
     */
    public CmiInputStream(InputStream in)
        throws IOException, StreamCorruptedException {
        super(in);
    }

    /**
     * @see ObjectInputStream#resolveClass(Class)
     */
    protected Class resolveClass(ObjectStreamClass classDesc)
        throws IOException, ClassNotFoundException {
        Object annotation = readLocation();
        String className = classDesc.getName();

        try {
            return super.resolveClass(classDesc);
        } catch (ClassNotFoundException e) {
        }

        if (annotation != null && (annotation instanceof String)) {
            String location = (String) annotation;
            return RMIClassLoader.loadClass(location, className);
        } else {
            return RMIClassLoader.loadClass((String) null, className);
        }
    }

    /**
     * @see ObjectInputStream#resolveProxyClass(Class)
     */
    protected Class resolveProxyClass(String[] interfaces)
        throws IOException, ClassNotFoundException {
        Object annotation = readLocation();

        ClassLoader loader;
        if (annotation != null && (annotation instanceof String)) {
            String location = (String) annotation;
            loader = RMIClassLoader.getClassLoader(location);
        } else {
            loader = RMIClassLoader.getClassLoader(null);
        }

        Class[] classObjs = new Class[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            classObjs[i] = Class.forName(interfaces[i], false, loader);
        }
        return java.lang.reflect.Proxy.getProxyClass(loader, classObjs);
    }

    /**
     * @see ObjectInputStream#readLocation(Class)
     */
    protected Object readLocation()
        throws IOException, ClassNotFoundException {
        return readObject();
    }
}
