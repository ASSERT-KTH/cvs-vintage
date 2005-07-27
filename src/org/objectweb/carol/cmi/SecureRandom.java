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
 * $Id: SecureRandom.java,v 1.4 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Wrapper for java.security.SecureRandom
 * @see java.security.SecureRandom
 *
 * @author Simon Nieuviarts
 */
public class SecureRandom {

    /**
     * java.security.SecureRandom Reference
     */
    private static java.security.SecureRandom sr = null;

    /**
     * Get a SecureRandom reference
     * @return SecureRandom reference
     */
    private static java.security.SecureRandom getSR() {
        if (sr != null) {
            return sr;
        }
        sr = new java.security.SecureRandom();
        sr.setSeed(System.currentTimeMillis());
        try {
            sr.setSeed(InetAddress.getLocalHost().getAddress());
        } catch (UnknownHostException e) {
            //One of the rare cases where there is nothing to do
        }
        return sr;
    }

    /**
     * Reseeds the underlying random object
     * @param rs the seed
     */
    public static void setSeed(long rs) {
        getSR().setSeed(rs);
    }

    /**
     * Reseeds the underlying random object
     * @param rs the seed
     */
    public static void setSeed(byte[] rs) {
        getSR().setSeed(rs);
    }

    /**
     * Get a random number
     * @return the next pseudorandom (int value)
     */
    public static int getInt() {
        return getSR().nextInt();
    }

    /**
     * Returns a random number between 0 (inclusive) and the specified value
     * (exclusive).
     * @param n the bound on the random number. Must be positive.
     * @return the random number (int value)
     */
    public static int getInt(int n) {
        return getSR().nextInt(n);
    }

    /**
     * Get a random number
     * @return the next pseudorandom (long value)
     */
    public static long getLong() {
        return getSR().nextLong();
    }
}
