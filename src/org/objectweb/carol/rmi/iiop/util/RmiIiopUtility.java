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
 * $Id: RmiIiopUtility.java,v 1.2 2005/03/10 09:51:46 benoitf Exp $
 * --------------------------------------------------------------------------
 */

package org.objectweb.carol.rmi.iiop.util;

import java.io.IOException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.CORBA.Stub;

import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.ORB;
import org.omg.CORBA.portable.ObjectImpl;

import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Utility class for the rmi iiop protocol
 *
 * @author Benoit Pelletier
 */
public class RmiIiopUtility {

    /**
     * private constructor mandatory for utilities class
     *
     */
    private RmiIiopUtility() {
    }

    /**
     * Connect a stub to an ORB
     *
     * @param object object to connect
     * @throws IOException exception
     */
    public static void reconnectStub2Orb(Object object) throws IOException {

        if (TraceCarol.isDebugRmiCarol()) {
            TraceCarol.debugRmiCarol("object=" + object);
        }
        // get the current protocol
        if (object instanceof ObjectImpl) {
           try {
              // Check we are still connected
              ObjectImpl objectImpl = (ObjectImpl) object;
              objectImpl._get_delegate();
              if (TraceCarol.isDebugRmiCarol()) {
                  TraceCarol.debugRmiCarol("still connected to ORB");
              }
           } catch (BAD_OPERATION e) {
              try {
                 // Reconnect
                 if (TraceCarol.isDebugRmiCarol()) {
                     TraceCarol.debugRmiCarol("must be reconnect to ORB");
                 }
                 Stub stub = (Stub) object;
                 ORB orb = (ORB) new InitialContext().lookup("java:comp/ORB");
                 // unable to use PortableRemoteObject.connect here because not implemented
                 // in JacOrb
                 stub.connect(orb);
                 if (TraceCarol.isDebugRmiCarol()) {
                     TraceCarol.debugRmiCarol("reconnected");
                 }
              } catch (NamingException ne) {
                 throw new IOException("Unable to lookup java:comp/ORB");
              }
           }
        } else {
           throw new IOException("Not an ObjectImpl " + object.getClass().getName());
        }
    }
}