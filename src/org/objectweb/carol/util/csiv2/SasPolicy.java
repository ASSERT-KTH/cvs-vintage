/**
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 2004 Bull S.A.
 * Contact: jonas-team@objectweb.org
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
 * $Id: SasPolicy.java,v 1.1 2004/12/13 16:24:13 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.csiv2;

import org.omg.CORBA.Policy;

/**
 * Csiv2 Policy for binding object
 * @author Florent Benoit
 */
public class SasPolicy extends org.omg.CORBA.LocalObject implements org.omg.CORBA.Policy {

    /**
     * Type
     */
    public static final int POLICY_TYPE = 0xFB79;

    /**
     * Sas component
     */
    private SasComponent sasComponent = null;

    /**
     * Build a csiv2 policy with the given configuration
     * @param sasComponent configuration for CsiV2
     */
    public SasPolicy(SasComponent sasComponent) {
        this.sasComponent = sasComponent;
    }

    /**
     * Returns the constant value that corresponds to the type of the policy
     * object.
     * @return the constant value that corresponds to the type of the policy
     *         object
     */
    public int policy_type() {
        return POLICY_TYPE;
    }

    /**
     * Destroys the <code>Policy</code> object.
     */
    public void destroy() {

    }

    /**
     * Returns a copy of the <code>Policy</code> object.
     * @return a copy of the <code>Policy</code> object
     */
    public Policy copy() {
        return new SasPolicy(sasComponent);
    }

    /**
     * @return the sasComponent.
     */
    public SasComponent getSasComponent() {
        return sasComponent;
    }
}