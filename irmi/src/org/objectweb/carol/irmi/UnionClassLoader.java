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

import java.util.HashMap;
import java.util.Map;

/**
 * UnionClassLoader is a utility class used by the RMI {@link Server}
 * as the context class loader during deserialization in order to find
 * classes accessible from both the remote object's class loader and
 * the context class loader at the time the remote object was
 * exported.
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public class UnionClassLoader extends ClassLoader {

    /**
     * Canonicalizing {@link Map} from {@link Pair}(<var>one</var>,
     * <var>two</var>) -> UnionClassLoader.
     */
    private static Map LOADERS = new HashMap();

    /**
     * Instantiates a new UnionClassLoader if necessory or returns a
     * previously instantiated one.
     *
     * @param one the first {@link ClassLoader} to load from
     * @param two the second {@link ClassLoader} to load from
     */

    public static synchronized UnionClassLoader get(ClassLoader one, ClassLoader two) {
        Object key = new Pair(one, two);
        UnionClassLoader result = (UnionClassLoader) LOADERS.get(key);
        if (result == null) {
            result = new UnionClassLoader(one, two);
            LOADERS.put(key, result);
        }
        return result;
    }

    private ClassLoader one;
    private ClassLoader two;

    /**
     * Constructs a {@link ClassLoader} that tries to load classes
     * from both of the given {@link ClassLoader} arguments.
     *
     * @param one the first {@link ClassLoader} to check
     * @param two the second {@link ClassLoader} to check
     */

    public UnionClassLoader(ClassLoader one, ClassLoader two) {
        this.one = one;
        this.two = two;
    }

    protected Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException {
        try {
            return one.loadClass(name);
        } catch (ClassNotFoundException e) {
            return two.loadClass(name);
        }
    }

}
