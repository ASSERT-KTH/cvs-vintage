/**
 * Copyright (C) 2004 - Bull S.A.
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
 * $Id: RmiMultiUtility.java,v 1.2 2005/04/07 15:07:08 benoitf Exp $
 * --------------------------------------------------------------------------
 */

package org.objectweb.carol.rmi.util;

import java.io.IOException;

import org.objectweb.carol.rmi.iiop.util.RmiIiopUtility;
import org.objectweb.carol.util.configuration.ConfigurationRepository;

/**
 * Utility class for the rmi protocols
 *
 * @author Benoit Pelletier
 */
public class RmiMultiUtility {

    /**
     * private constructor mandatory for utilities class
     *
     */
    private RmiMultiUtility() {
    }

    /**
     * Connect a stub to an ORB
     *
     * @param object object to connect
     * @throws IOException exception
     */
    public static void reconnectStub2Orb(Object object) throws IOException {

        // get the current protocol
        String protocol = ConfigurationRepository.getCurrentConfiguration().getProtocol().getName();

        // iiop case
        if (protocol.equals("iiop")) {
            RmiIiopUtility.reconnectStub2Orb(object);
        }

        // for others protocols, nothing to do
    }

}