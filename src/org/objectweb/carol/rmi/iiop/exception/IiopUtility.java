/**
 * Copyright (C) 2004-2005 - Bull S.A.
 *
 * CAROL: Common Architecture for RMI ObjectWeb Layer
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
 * $Id: IiopUtility.java,v 1.2 2005/03/10 09:51:46 benoitf Exp $
 * --------------------------------------------------------------------------
 */

package org.objectweb.carol.rmi.iiop.exception;

import org.objectweb.carol.util.configuration.TraceCarol;


/**
 * Utility class for rmi/iiop exceptions management
 *
 * @author Benoit Pelletier
 */

public class IiopUtility {

    /**
     * private constructor mandatory for utilities class
     *
     */
    private IiopUtility() {
    }

    /**
     * Map a java exception to a corba exception
     *
     * @param e exception to process
     */
    public static void rethrowCorbaException(Exception e) {

        TraceCarol.debugRmiCarol("");

        if (e instanceof java.rmi.MarshalException) {
            throw new org.omg.CORBA.MARSHAL(e.toString());
        } else if (e instanceof java.rmi.NoSuchObjectException) {
            throw new org.omg.CORBA.OBJECT_NOT_EXIST(e.toString());
        } else if (e instanceof java.rmi.AccessException) {
            throw new org.omg.CORBA.NO_PERMISSION(e.toString());
        } else if (e instanceof javax.transaction.TransactionRequiredException) {
            throw new org.omg.CORBA.TRANSACTION_REQUIRED(e.toString());
        } else if (e instanceof javax.transaction.TransactionRolledbackException) {
            throw new org.omg.CORBA.TRANSACTION_ROLLEDBACK(e.toString());
        } else if (e instanceof javax.transaction.InvalidTransactionException) {
            throw new org.omg.CORBA.INVALID_TRANSACTION(e.toString());
        }
    }
}
