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
 * $Id: StubBuilder.java,v 1.1 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;

/**
 * Used to serialize a cluster stub.
 *
 * @author Simon Nieuviarts
 */
public class StubBuilder implements Externalizable {

    /**
     * Reference to the cluster stub
     */
    private ClusterStub clusterStub;

    /**
     * Default constructor
     */
    public StubBuilder() {
    }

    /**
     * Creates a new StubBuilder with an initial cluster stub
     * @param clusterStub initial cluster stub
     */
    private StubBuilder(ClusterStub clusterStub) {
        this.clusterStub = clusterStub;
    }

    /**
     * Get the Externalizable instance of a ClusterStub instance
     * @param cs ClusterStub instance
     * @return  Externalizable instance
     */
    public static StubBuilder getReplacement(ClusterStub cs) {
        return new StubBuilder(cs);
    }

    /**
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        clusterStub = ServerStubList.read(in);
    }

    /**
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        clusterStub._distrib.getStubList().write(out);
    }

    /**
     * Read the wrapped instance
     * @return wrapped object
     * @throws ObjectStreamException if an exception occurs
     */
    private Object readResolve() throws ObjectStreamException {
        return clusterStub;
    }
}
