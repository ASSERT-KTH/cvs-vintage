/*
 * @(#) BasicMultiObject.java	1.0 02/07/15
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
package org.objectweb.carol.jtests.conform.basic.server;


// java import
import java.rmi.Remote;
import java.rmi.RemoteException;

// javax import
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.objectweb.carol.rmi.jrmp.interceptor.JInterceptorHelper;

/**
 * Class <code>BasicMultiObject</code> is a basic remote object with reference to another object
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002 
*/
public class BasicMultiObject extends PortableRemoteObject  implements BasicMultiObjectItf {


    /**
     * Constructor
     */
    public BasicMultiObject() throws Exception {
	super();
    }

    /**
     * Basic multi method
     * this method get the name for the BasicObjectItf
     * from jndi and return the result of getSting added with a multi
     * @return String "string"
     */
    public String getMultiString() throws RemoteException {
	try {
	    // set the object name 
	    String basicName = "basicname"; 
	    InitialContext ic = new InitialContext();
	    BasicObjectItf ob = (BasicObjectItf)PortableRemoteObject.narrow(ic.lookup(basicName), BasicObjectItf.class);

	    return "multi string call: " + ob.getString();
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new RemoteException(""+ e);
	}
    }

    /**
     * Basic multi method 
     * this method get the BasicObjectItf stub 
     * @return BasicObjectItf
     */
    public BasicObjectItf getBasicObject() throws RemoteException {
	try {
	    // set the object name 
	    String basicName = "basicname";
	    InitialContext ic = new InitialContext();
	    return (BasicObjectItf)PortableRemoteObject.narrow(ic.lookup(basicName), BasicObjectItf.class);
	    
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new RemoteException(""+ e);
	}
    }

    /**
     * Basic Ref String, method 
     * this method get the BasicObjectItf stub 
     * @return String of the reference
     */
    public String getBasicRefString() throws RemoteException {
	try {
	    // set the object name 
	    String basicRefName = "basicrefname";
	    InitialContext ic = new InitialContext();
	    return ((BasicObjectRef)PortableRemoteObject.narrow(ic.lookup(basicRefName), BasicObjectRef.class)).toString();	    
	} catch (Exception e) {
	    throw new RemoteException(""+ e);
	}
    }
    
}
