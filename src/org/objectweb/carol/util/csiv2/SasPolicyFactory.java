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
 * $Id: SasPolicyFactory.java,v 1.1 2004/12/13 16:24:13 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.csiv2;

import org.omg.CORBA.Any;
import org.omg.CORBA.Policy;
import org.omg.CORBA.PolicyError;
import org.omg.PortableInterceptor.PolicyFactory;

/**
 * Make csiv2 policy objects
 * @author Florent Benoit
 */
public class SasPolicyFactory extends org.omg.CORBA.LocalObject implements PolicyFactory {

    /**
     * Default constructor
     */
    public SasPolicyFactory() {
    }

    /**
     * Returns an instance of the appropriate interface derived from
     * <code>CORBA.Policy</code> whose value corresponds to the specified any.
     * @param type An int specifying the type of policy being created.
     * @param value An any containing data with which to construct the
     *        <code>CORBA.Policy</code>.
     * @return A <code>CORBA.Policy<code> object of the specified type and
     *     value.
     * @throws PolicyError if the policy type is invalid
     */
    public Policy create_policy(int type, Any value) throws PolicyError {
        // Can create only known policy objects
        if (type != SasPolicy.POLICY_TYPE) {
            throw new PolicyError();
        }

        // Get component stored
        SasComponent sasComponent = (SasComponent) value.extract_Value();

        // build a new policy object
        return new SasPolicy(sasComponent);

    }
}

