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
 * $Id: RmiUtility.java,v 1.3 2005/04/07 15:07:08 benoitf Exp $
 * --------------------------------------------------------------------------
 */

package org.objectweb.carol.rmi.exception;

import org.objectweb.carol.rmi.iiop.exception.IiopUtility;
import org.objectweb.carol.util.configuration.ConfigurationRepository;
import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Utility class for the rmi exceptions management
 *
 * @author Benoit Pelletier
 */
public class RmiUtility {

    /**
     * private constructor mandatory for utilities class
     *
     */
    private RmiUtility() {
    }

    /**
     * check if the exception have to be mapped to a protocol exception
     * if yes, throw the new one
     *
     * @param e exception to process
     */
    public static void rethrowRmiException(Exception e) {

        // get the current protocol
        String protocol = ConfigurationRepository.getCurrentConfiguration().getProtocol().getName();

        if (TraceCarol.isDebugRmiCarol()) {
            TraceCarol.debugRmiCarol("Current protocol=" + protocol);
        }

        // iiop case
        if (protocol.equals("iiop")) {
            IiopUtility.rethrowCorbaException(e);
        }

        // for others protocols, nothing to do
    }

}