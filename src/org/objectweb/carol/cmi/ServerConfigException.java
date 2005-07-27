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
 * $Id: ServerConfigException.java,v 1.1 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

/**
 * Thrown when a bad configuration parameter prevents the operation to
 * succeed
 *
 * @author Simon Nieuviarts
 */
public class ServerConfigException extends Exception {

    /**
     * Creates an exception with a message
     * @param msg message
     */
    public ServerConfigException(String msg) {
        super(msg);
    }

    /**
     * Creates an exception with a message and an exception
     * @param msg message
     * @param ex source exception
     */
    public ServerConfigException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
