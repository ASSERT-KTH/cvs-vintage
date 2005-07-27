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
 * $Id: InetMask.java,v 1.4 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.LinkedList;

/**
 * @author Simon Nieuviarts
 */
public class InetMask {

    /**
     * Byte array
     */
    byte[] bits;

    /**
     * Mask
     */
    int mask;

    /**
     * Generate an IP mask from its textual representation. The special case of a
     * single address is taken into account.
     * @param textual textual representation of the IP mask
     * @throws UnknownHostException if exception
     */
    public InetMask(String textual) throws UnknownHostException {
        int i = textual.indexOf('/');
        String ip = null;
        if (i < 0) {
            InetAddress a = InetAddress.getByName(ip);
            bits = a.getAddress();
            mask = bits.length * 8;
        } else {
            ip = textual.substring(0, i);
            mask = Integer.parseInt(textual.substring(i + 1));
            InetAddress a = InetAddress.getByName(ip);
            bits = a.getAddress();
        }
    }

    /**
     * Test if an IP addr matches with the mask
     * @param a ip addr
     * @return true if matches
     */
    public boolean match(InetAddress a) {
        byte[] b = a.getAddress();
        int l = b.length;
        if (l != bits.length) {
            return false;
        }
        int m = mask;
        for (int i=0; i<l; i++) {
            if (m < 8) {
                int v1 = b[i];
                if (v1 < 0) {
                    v1 += 256;
                }
                int v2 = bits[i];
                if (v2 < 0) {
                    v2 += 256;
                }
                int shift = 8 - m;
                return (v1 >> shift) == (v2 >> shift);
            }
            m -= 8;
            if (b[i] != bits[i]) {
                return false;
            }
        }
        return false;
    }

    /**
     * Use JDK 1.4 methods.
     * @return List of local addresses (java.net.InetAddress) matching this InetMask.
     */
    public LinkedList filterLocal() {
        LinkedList l = new LinkedList();
        try {
            Enumeration en;
            Class cl;
            Object[] obj0 = {
            };
            cl = Class.forName("java.net.NetworkInterface");
            Method meth = cl.getMethod("getNetworkInterfaces", new Class[0]);
            Method getInet = cl.getMethod("getInetAddresses", new Class[0]);
            en = (Enumeration) meth.invoke(cl, obj0);
            while (en.hasMoreElements()) {
                Object o = en.nextElement();
                Enumeration enum2 = (Enumeration) getInet.invoke(o, obj0);
                while (enum2.hasMoreElements()) {
                    InetAddress a = (InetAddress) enum2.nextElement();
                    if (match(a)) {
                        l.add(a);
                    }
                }
            }
        } catch (Exception e) {
            if (l.isEmpty()) {
                return l;
            } else {
                return new LinkedList();
            }
        }
        return l;
    }
}
