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
 * $Id: ObjectIdFactory.java,v 1.1 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

/**
 * This is a monotonic counter that generate IDs for cluster remote objects.
 *
 * @author Simon Nieuviarts
 */
public class ObjectIdFactory {

    /**
     * Server id
     */
    private ClusterId serverId;

    /**
     * work var
     */
    private byte nextIdArray[];

    /**
     * Creates an ew factory from a server id
     * @param serverId server id
     */
    public ObjectIdFactory(ClusterId serverId) {
        this.serverId = serverId;
        nextIdArray = new byte[1];
        nextIdArray[0] = 0;
    }

    /**
     * Get the next cluster id
     * @return ClusterId
     */
    private synchronized ClusterId getNext() {
        int l = nextIdArray.length;
        for (int i = 0; i < l; i++) {
            byte c = ++nextIdArray[i];
            if (c != 0) {
                return new ClusterId(nextIdArray);
            }
        }
        nextIdArray = new byte[l + 1];
        nextIdArray[0] = 1;
        for (int i = 1; i <= l; i++) {
            nextIdArray[i] = 0;
        }
        return new ClusterId(nextIdArray);
    }

    /**
     * @return on object id.
     * @throws ClusterException
     */
    public synchronized ObjectId getId() {
        return new ObjectId(serverId, getNext());
    }

    /**
     * For test only.
     * @param args program args
     * @throws ServerConfigException if exception
     */
    public static void main(String args[]) throws ServerConfigException {
        ServerIdFactory sidf = new ServerIdFactory();
        ObjectIdFactory oidf = new ObjectIdFactory(sidf.getLocalId());
        long t0 = System.currentTimeMillis();
        System.out.println(oidf.getId()); // 1
        for (int i = 2; i < 255; i++) {
            oidf.getId();
        }
        System.out.println(oidf.getId()); // 255
        System.out.println(oidf.getId()); // 1-0
        System.out.println(oidf.getId()); // 2-0
        for (int i = 3; i < 65534; i++) {
            oidf.getId();
        }
        System.out.println(oidf.getId()); // 254-255
        System.out.println(oidf.getId()); // 255-255
        System.out.println(oidf.getId()); // 1-0-0
        System.out.println(oidf.getId()); // 2-0-0
        System.out.println("t = " + (System.currentTimeMillis() - t0));
    }
}