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
 * $Id: ObjectId.java,v 1.1 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Each clustered remote object is uniquely identified by such a number.
 * Two distinct clustered remote object do not have the same
 * <code>ObjectId</code>.
 *
 * @author Simon Nieuviarts
 */
public class ObjectId implements Externalizable, Comparable {

    /**
     * Server id
     */
    private transient ClusterId serverId;

    /**
     * object id
     */
    private transient ClusterId objId;

    /**
     * hashcode
     */
    private transient int hash = 0;

    /**
     * Default constructor
     *
     */
    public ObjectId() {}


    /**
     * Creates a new identifier
     * @param serverId server id
     * @param objId object id
     */
    public ObjectId(ClusterId serverId, ClusterId objId) {
        this.serverId = serverId;
        this.objId = objId;
    }

    /**
     * Compute hashcode
     * @return hashcode
     */
    public int hashCode() {
        if (hash != 0) {
            return hash;
        }
        int h = objId.hashCode() + serverId.hashCode();
        hash = h;
        return h;
    }

    /**
     * Test if two ObjectIds are equals
     * @param o object to compare with this
     * @return true if equals
     */
    public boolean equals(Object o) {
        if (o instanceof ObjectId) {
            ObjectId i = (ObjectId) o;
            return serverId.equals(i.serverId) && objId.equals(i.objId);
        }
        return false;
    }

    /**
     * Compare two ObjectIds
     * @param o object to compare with this
     * @return true if equals
     */
    public int compareTo(Object o) {
        ObjectId i = (ObjectId) o;
        int d = serverId.compareTo(i.serverId);
        if (d != 0) {
            return d;
        }
        return objId.compareTo(i.objId);
    }

    /**
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        serverId = ClusterId.read(in);
        objId = ClusterId.read(in);
    }

    /**
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        write(out);
    }

    /**
     * Read an ObjectId from the input stream
     * @param in input
     * @return ObjectId
     * @throws IOException if an I/O error occurs
     */
    public static ObjectId read(DataInput in) throws IOException {
        ObjectId id = new ObjectId();
        id.serverId = ClusterId.read(in);
        id.objId = ClusterId.read(in);
        return id;
    }

    /**
     * Write an ObjectId on the ouput stream
     * @param out output
     * @throws IOException if an I/O error occurs
     */
    public void write(DataOutput out) throws IOException {
        serverId.write(out);
        objId.write(out);
    }

    /**
     * Get the ServerId associated with the current object
     * @return ClusterId
     */
    public ClusterId getServerId() {
        return serverId;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return serverId.toString() + '/' + objId.toString();
    }

    /**
     * Useful for tests only.
     * @throws Exception Exception
     */
    public static void main(String[] args) throws Exception {
        ServerIdFactory sidf = new ServerIdFactory();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] a = new byte[3];
        a[0] = 1;
        a[1] = 2;
        a[2] = 7;
        ObjectId oid = new ObjectId(sidf.getLocalId(), new ClusterId(a));
        System.out.println(oid);
        outStream.reset();
        CmiOutputStream out;
        out = new CmiOutputStream(outStream);
        out.writeObject(oid);
        out.flush();
        byte[] b = outStream.toByteArray();
        java.io.ByteArrayInputStream ins = new java.io.ByteArrayInputStream(
                b);
        CmiInputStream in = new CmiInputStream(ins);
        Object obj = in.readObject();
        if (!oid.equals(obj)) {
            throw new Exception();
        }
        System.out.println(obj);
    }
}