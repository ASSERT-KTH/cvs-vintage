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
 * $Id: Decision.java,v 1.1 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

/**
 * Returned to the cluster stub by methods of a <code>Distributor</code> to
 * tell what to do.
 *
 * @author Simon Nieuviarts
 */
public class Decision {

    /**
     * Decision : return a value
     */
    public static final int RETURN = 0;

    /**
     * Decision : retry
     */
    public static final int RETRY = 1;

    /**
     * Decision : throw an exception
     */
    public static final int THROW = 2;

    /**
     * Chosen Decision
     */
    public int decision;

    /**
     * Value associated with return decision
     */
    public Object retVal;

    /**
     * Creates a return decision
     * @param retVal the value to return
     */
    private Decision(Object retVal) {
        this.decision = RETURN;
        this.retVal = retVal;
    }

    /**
     * Creates a decision
     * @param decision value of the decision
     */
    private Decision(int decision) {
        this.decision = decision;
    }

    /**
     * Creates a retry decision
     * @return retry
     */
    public static Decision doRetry() {
        return new Decision(RETRY);
    }

    /**
     * Creates a throw decision
     * @return throw
     */
    public static Decision doThrow() {
        return new Decision(THROW);
    }

    /**
     * Creates a return decision
     * @param retVal the value to return
     * @return return
     */
    public static Decision doReturn(Object retVal) {
        return new Decision(retVal);
    }
}
