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
 * $Id: ClusterStub.java,v 1.6 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.rmi.Remote;

/**
 * Stubs to clustered objects extend this class.
 * We avoid to pollute method namespace by defining no method.
 *
 * @author Simon Nieuviarts
 */
public abstract class ClusterStub implements Remote {
    /**
     * This member is used by StubList to change the stubList reference.
     * This is because a member can not be at the same time protected
     * and visible at the package level.
     */
    final Distributor _distrib;

    /**
     * Distributor class
     */
    protected final Distributor distrib;

    /**
     * Debug mode ?
     */
    protected final boolean stubDebug;

    /**
     * Creates a new cluster stub
     * @param distrib distibutor class associated with this stub
     * @param sl server stub list associated with this stub
     */
    protected ClusterStub(Distributor distrib, ServerStubList sl) {
        distrib.setStubList(sl);
        this.distrib = this._distrib = distrib;
        stubDebug = sl.isStubDebug();
    }
}
