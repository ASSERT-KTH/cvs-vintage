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
 *                     Vadim Nasardinov
 */
package org.objectweb.carol.irmi;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * The Hashes class contains static utility methods for converting
 * between hashes and methods. The hash algorithm used is the one
 * described in the RMI specification. Any converstions from {@link
 * Method}s to hashes and from ({@link Class}, hash) to {@link Method}
 * are statically cached.
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public class Hashes {

    /**
     * Private constructor to prevent instantiation of this class.
     */

    private Hashes() {}

    private static Map HASHES = new HashMap();
    private static Map METHODS = new HashMap();

    /**
     * Converts (and caches) the hash for the given {@link Method}.
     * The hash algorithm used is the one described in the RMI
     * specification.
     *
     * @param meth the {@link Method} to hash
     * @return the hash code for <var>meth</var>
     */

    public static synchronized long getHash(Method meth) {
        Long result = (Long) HASHES.get(meth);
        if (result == null) {
            result = new Long(opnum(meth));
            HASHES.put(meth, result);
        }
        return result.longValue();
    }

    /**
     * Searches a given {@link Class} for a {@link Method} having a
     * given hash. The result of this search is cached.
     *
     * @param klass the {@link Class} to search
     * @param hash the hash of the desired method
     * @return the {@link Method} matching the given hash, or null if
     * none exists
     */

    public static synchronized Method getMethod(Class klass, long hash) {
        Object key = new Pair(klass, new Long(hash));
        Method meth = (Method) METHODS.get(key);
        if (meth == null) {
            Method[] methods = klass.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (getHash(methods[i]) == hash) {
                    meth = methods[i];
                    break;
                }
            }
            METHODS.put(key, meth);
        }
        return meth;
    }

    /**
     * Computes the RMI specified hash for the given method.
     *
     * @param method the method to hash
     * @return the RMI specified hash for <var>method</var>
     */

    public static long opnum(Method method) {
        String descriptor = methodSignature(method);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(bos);

        final byte[] descStream;
        try {
            dos.writeUTF(descriptor);
            dos.close();
            descStream = bos.toByteArray();
        } catch (IOException ex) {
            throw (IllegalStateException)
                new IllegalStateException("can't happen").initCause(ex);
        }

        final MessageDigest sha1;

        try {
            sha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException ex) {
            throw (IllegalStateException)
                new IllegalStateException("can't happen").initCause(ex);
        }


        byte[] sha = sha1.digest(descStream);

        long result = 0;
        int len = Math.min(sha.length, 8);
        for (int i=0; i < len; i++) {
            result += (long)(sha[i] & 0xFF) << (8 * i);
        }
        return result;
    }

    /**
     * Return the string signature for the given method.
     *
     * @param method the method
     * @return the method signature
     */

    // XXX: deal with constructors
    private static String methodSignature(Method method) {
        if (method == null) { throw new NullPointerException("method"); }

        StringBuffer acc = new StringBuffer();
        acc.append(method.getName());
        acc.append("(");
        final Class[] paramTypes = method.getParameterTypes();

        for (int ii=0; ii<paramTypes.length; ii++) {
            descriptor(paramTypes[ii], acc);
        }
        acc.append(")");
        descriptor(method.getReturnType(), acc);

        int dotIdx = -1;
        do {
            // Since we know that "." cannot occur as the last character, it is
            // safe to assume that dotIdx+1 is within bounds.
            dotIdx = acc.indexOf(".", dotIdx+1);
            if (dotIdx > -1) {
                acc.setCharAt(dotIdx, '/');
            }
        } while (dotIdx > -1);

        return acc.toString();
    }

    /**
     * Write the given types string descriptor into the given {@link
     * StringBuffer}.
     *
     * @param type the type
     * @param acc the buffer to append the descriptor to
     */

    private static void descriptor(Class type, StringBuffer acc) {
        if (Void.TYPE.equals(type)) {
            acc.append("V");
        } else if (Byte.TYPE.equals(type)) {
            acc.append("B");
        } else if (Character.TYPE.equals(type)) {
            acc.append("C");
        } else if (Double.TYPE.equals(type)) {
            acc.append("D");
        } else if (Float.TYPE.equals(type)) {
            acc.append("F");
        } else if (Integer.TYPE.equals(type)) {
            acc.append("I");
        } else if (Long.TYPE.equals(type)) {
            acc.append("J");
        } else if (Short.TYPE.equals(type)) {
            acc.append("S");
        } else if (Boolean.TYPE.equals(type)) {
            acc.append("Z");
        } else if (type.isArray()) {
            acc.append("[");
            descriptor(type.getComponentType(), acc);
        } else {
            acc.append("L").append(type.getName()).append(";");
        }
    }

}
