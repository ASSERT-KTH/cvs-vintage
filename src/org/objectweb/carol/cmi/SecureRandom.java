/*
 * Copyright (C) 2002-2003, Simon Nieuviarts
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
 */
package org.objectweb.carol.cmi;

class SecureRandom {
    private static java.security.SecureRandom sr = null;

    private static java.security.SecureRandom getSR() {
        if (sr != null)
            return sr;
        sr = new java.security.SecureRandom();
        sr.setSeed(System.currentTimeMillis());
        return sr;
    }

    public static void doSlowSeed() {
        java.security.SecureRandom sr = getSR();
        sr.setSeed(sr.generateSeed(20));
    }

    public static void setSeed(long rs) {
        getSR().setSeed(rs);
    }

    public static int getInt() {
        return getSR().nextInt();
    }

    public static int getInt(int n) {
        return getSR().nextInt(n);
    }

    public static long getLong() {
        return getSR().nextLong();
    }
}
