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
 * $Id: ClusterId.java,v 1.8 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;
import java.io.Externalizable;
import java.util.Arrays;

/**
 * A universally unique identifier for a cluster
 * @author Simon Nieuviarts
 */
public class ClusterId implements Externalizable, Comparable {

    /**
     * Identifier :array of byte
     */
    private transient byte id[];

    /**
     * hashcode of the identifier
     */
    private transient int hash = 0;

    /**
     * Readable format of the identifier
     */
    private transient String str;

    /**
     * Create a new identifier
     */
    public ClusterId() { }

    /**
     * Create a new identifier from an array of byte
     * @param id array of byte
     */
    public ClusterId(byte id[]) {
        if (id.length > Short.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Too long array for a cluster ID");
        }
        this.id = (byte[]) id.clone();
    }

    /**
     * compute the hashcode
     * @return hashcode
     */
    private int redoHash() {
        int h = 0;
        int l = id.length;
        int n = 1;
        for (int i = 0; i < l; i++) {
            h += id[i] * n;
            n *= 31;
        }
        hash = h;
        return h;
    }

    /**
     * get the hascode
     * @return hashcode
     */
    public int hashCode() {
        if (hash != 0) {
            return hash;
        }
        return redoHash();
    }

    /**
     * Compare two identifiers
     * @param ar array of bytes to compare with the identifier
     * @return true is equals, else false
     */
    public boolean match(byte ar[]) {
        return Arrays.equals(id, ar);
    }

    /**
     * Compare two identifier
     * @param o object to compare
     * @return true is equals, else false
     */
    public boolean equals(Object o) {
        if (o instanceof ClusterId) {
            ClusterId i = (ClusterId) o;
            return match(i.id);
        }
        return false;
    }

    /**
     * get the identifier in array of bytes type
     * @return array of bytes
     */
    public byte[] getBytes() {
        return id;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        ClusterId i2 = (ClusterId) o;
        byte[] id1 = id;
        byte[] id2 = i2.id;
        int l1 = id1.length;
        int l2 = id2.length;
        int min = (l1 < l2) ? l1 : l2;
        for (int i = 0; i < min; i++) {
            int d = id1[i] - id2[i];
            if (d != 0) {
                return d;
            }
        }
        if (l1 > min) {
            return 1;
        }
        if (l2 > min) {
            return -1;
        }
        return 0;
    }

    /**
     * Readable format.
     * @return cluster id in a human readable format
     */
    public String toString() {
        if (str != null) return str;
        String s = "";
        int i;
        for (i = 0; i < id.length; i++) {
            int n = id[i];
            if (n < 0) {
                n += 256;
            }
            if (i > 0) {
                s += "-";
            }
            s += n;
        }
        str = s;
        return s;
    }

    /**
     * Read an identifier from an input stream (java.io.ObjectInput)
     * @param in the input stream
     * @throws IOException if exception is encountered
     * @throws ClassNotFoundException if exception is encountered
     **/
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        int l = in.readShort();
        byte[] a = new byte[l];
        in.readFully(a);
        id = a;
    }

    /**
     * Write an identifier on the output stream (java.io.ObjectOutput)
     * @param out the output stream
     * @throws IOException if exception is encountered
     **/
    public void writeExternal(ObjectOutput out) throws IOException {
        write(out);
    }

    /**
     * Read an identifier from an input stream (java.io.DataInput)
     * @param in the input stream
     * @throws IOException if exception is encountered
     **/
     public static ClusterId read(DataInput in) throws IOException {
        ClusterId id = new ClusterId();
        int l = in.readShort();
        byte[] a = new byte[l];
        in.readFully(a);
        id.id = a;
        return id;
    }

     /**
      * Write an identifier on the output stream (java.io.DataOutput)
      * @param out the output stream
      * @throws IOException if exception is encountered
      **/
    public void write(DataOutput out) throws IOException {
        out.writeShort(id.length);
        out.write(id);
    }

    /**
     * Useful for tests only.
     * @param args arguments list
     * @throws Exception if an error occurs
     */
    public static void main(String[] args) throws Exception {
        long t0 = System.currentTimeMillis();
        java.io.ByteArrayOutputStream outStream = new java.io.ByteArrayOutputStream();
        byte[] a = new byte[100];
        byte[] b = new byte[100];
        a[0] = 1;
        a[1] = 2;
        a[2] = 7;
        ClusterId eid = new ClusterId(a);
        System.arraycopy(a, 0, b, 0, 100);
        ClusterId eid2 = new ClusterId(b);
        if (!eid.equals(eid2)) {
            throw new Exception();
        }
        a[2] = 2;
        b[2] = 2;
        eid2 = new ClusterId(b);
        if (eid.equals(eid2)) {
            throw new Exception();
        }
        for (int i = 0; i < 2000; i++) {
            outStream.reset();
            CmiOutputStream out;
            out = new CmiOutputStream(outStream);
            out.writeObject(eid);
            out.flush();
            java.io.ByteArrayInputStream ins = new java.io.ByteArrayInputStream(
                    outStream.toByteArray());
            CmiInputStream in = new CmiInputStream(ins);
            Object obj = in.readObject();
            if (!eid.equals(obj)) {
                throw new Exception();
            }
        }
        System.out.println("total time " + (System.currentTimeMillis() - t0)
                + " ms");
    }
}
