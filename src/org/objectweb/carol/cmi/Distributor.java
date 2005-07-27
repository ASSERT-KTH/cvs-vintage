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
 * $Id: Distributor.java,v 1.1 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.lang.reflect.Method;
import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
import java.rmi.NoSuchObjectException;
import java.util.Set;

/**
 * Each clustered server object has to be accompanied by a class which
 * extends this one to specify configuration information.
 * It specifies how the replicates will be accessed by the clients,
 * and how replicates are recognized in the cluster.
 * <p>
 * For CMI to load this configuration, the class has to be named under the
 * form : <code><em>full.ServerClass</em>_Cmi</code>
 * <p>
 * It must provide a constructor without parameter. The results should be
 * constant in the cluster so that each server and client uses the same
 * configuration.
 *
 * @author Simon Nieuviarts
 */
public abstract class Distributor {

    /**
     * Stubs list
     */
    private ServerStubList stubList;

    /**
     * Get the current state of the stubs
     * @return set of stubs
     */
    protected synchronized Set getCurrentState() {
        return stubList.getSetOfStubs();
    }

    /**
     * Remove a stub in the list
     * @param sd stub
     */
    protected void removeStub(StubData sd) {
        getStubList().removeStub(sd);
    }

    /**
     * Set a new stub list
     * @param sl stub list
     */
    synchronized void setStubList(ServerStubList sl) {
        stubList = sl;
    }

    /**
     * Get the stub list
     * @return stub list
     */
    synchronized ServerStubList getStubList() {
        return stubList;
    }

    /**
     * To be overriden
     * @return true if the bound object have to be clustered
     */
    public abstract boolean equivAtBind();

    /**
     * To be overriden
     * @return true if the export object have to be clustered
     */
    public abstract boolean equivAtExport();

    /**
     * By default rr algo
     */
    private RoundRobin rr = new RoundRobin();

    /**
     * Choose a stub among the stub list
     * The application deployer can override this method to make its own choices.
     * @param method calling method
     * @param parameters parameters of the calling method
     * @throws NoServerException if no server available
     */
    public StubData choose(Method method, Object[] parameters) throws NoServerException {
        synchronized (rr) {
            rr.update(getStubList().getSetOfStubs());
            return rr.get();
        }
    }

    /**
     * Decison on Exception
     * The application deployer can override this method to make its own choices.
     * @param sd stub data
     * @param ex exception
     * @return decision
     */
    public Decision onException(StubData sd, Exception ex) {
        if ((ex instanceof ConnectException) || (ex instanceof ConnectIOException) || (ex instanceof NoSuchObjectException)) {
            removeStub(sd);
            return Decision.doRetry();
        }
        return Decision.doThrow();
    }

    /**
     * Decision on return
     * The application deployer can override this method to make its own choices.
     * @param sd stub data
     * @param retVal return value
     * @return decision
     */
    public Decision onReturn(StubData sd, Object retVal) {
        return Decision.doReturn(retVal);
    }

    /**
     * Build a readable view of the stub list
     * @return string
     */
    public String toContentsString() {
        return getStubList().toContentsString();
    }

    /**
     * @return readable view of the stub list
     */
    public String toString() {
        return this.getClass().getName() + toContentsString();
    }
}
