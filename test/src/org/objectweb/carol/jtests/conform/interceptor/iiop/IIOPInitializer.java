/**
 * Copyright (C) 2002,2005 - INRIA (www.inria.fr)
 *
 * CAROL: Common Architecture for RMI ObjectWeb Layer
 *
 * This library is developed inside the ObjectWeb Consortium,
 * http://www.objectweb.org
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
 * $Id: IIOPInitializer.java,v 1.4 2005/02/14 09:41:56 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jtests.conform.interceptor.iiop;

import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;

/**
 * Class <code>IIOPInitializer</code> is a IIOP Interceptor initialisation for
 * carol testing
 * @author Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 */
public class IIOPInitializer extends LocalObject implements ORBInitializer {

    /**
     * Init interceptors
     * @param info ORB info
     */
    public void pre_init(ORBInitInfo info) {
        try {
            info.add_client_request_interceptor(new IIOPDummyClientInterceptor("client interceptor"));
            info.add_server_request_interceptor(new IIOPDummyServerInterceptor("server interceptor"));
        } catch (Exception e) {
            System.out.println("could'nt instantiate iiop Interceptor" + e);
            e.printStackTrace();
        }
    }

    /**
     * Called during ORB initialization. If a service must resolve initial
     * references as part of its initialization, it can assume that all initial
     * references will be available at this point.
     * @param info provides initialization attributes and operations by which
     *        Interceptors can be registered.
     */
    public void post_init(ORBInitInfo info) {
        // do nothing
    }

}