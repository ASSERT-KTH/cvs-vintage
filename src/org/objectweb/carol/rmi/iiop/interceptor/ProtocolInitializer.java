/*
 * @(#) ProtocolInitializer.java	1.0 02/07/15
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
package org.objectweb.carol.rmi.iiop.interceptor;

// omg import 
import org.objectweb.carol.util.configuration.TraceCarol;
import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;

/**
 * Class <code>ProtocolInitializer</code> is the CAROL JNDI IIOP Interceptor initializer
 * this initializer add an interceptor for the multi rmi management 
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @see org.omg.PortableInterceptor.ORBInitializer
 * @version 1.0, 15/07/2002
 */
public class ProtocolInitializer extends LocalObject implements ORBInitializer {

    /**
     * ORBInitializer pre_init method 
     * here we add the iiop interceptor
     */
    public void pre_init(ORBInitInfo info){
	try {
	    info.add_server_request_interceptor(new ProtocolInterceptor());
	} catch (Exception e) {
	    TraceCarol.error("ProtocolInitializer.pre_init(ORBInitInfo info) could'nt instantiate iiop Interceptor", e);
	}
    }
    
    /**
     * ORBInitializer post_init method 
     * nothing done here
     */
    public void post_init(ORBInitInfo info){
	// do nothing
    }

}
