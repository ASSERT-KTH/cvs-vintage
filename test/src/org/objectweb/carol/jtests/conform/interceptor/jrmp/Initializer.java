/**
 * @(#) DummyClientServiceContext.java	1.0 02/07/15
 *
 * Copyright (C) 2002 - INRIA (www.inria.fr)
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
 *
 */
package org.objectweb.carol.jtests.conform.interceptor.jrmp;

// arol import
import org.objectweb.carol.rmi.jrmp.interceptor.JInitInfo;
import org.objectweb.carol.rmi.jrmp.interceptor.JInitializer;

/**
 * Class <code>DummyClientServiceContext</code> is a JRMP Interface for Interceptor initialisation
 * for carol testing
 *
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002
 */
public class Initializer implements JInitializer{

    /**
     * In JRMP the 2 method( per and post init have the same
     * consequences ...
     * @param JInitInfo the JInit Information
     */
    public void pre_init(JInitInfo info){
    try {
        info.add_client_request_interceptor(new DummyClientInterceptor("client interceptor"));
        info.add_server_request_interceptor(new DummyServerInterceptor("server interceptor"));
    } catch (Exception e) {
        System.out.println("could'nt instantiate Interceptor" + e);
        e.printStackTrace();
    }
    }

    /**
     * In JRMP the 2 method( per and post init have the same
     * consequences ...
     * @param JInitInfo the JInit Information
     */
    public void post_init(JInitInfo info){
    // do nothing
    }

}
