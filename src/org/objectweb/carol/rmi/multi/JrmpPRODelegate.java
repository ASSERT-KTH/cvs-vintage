/*
 * @(#) JUnicastServerRef.java	1.0 02/07/15
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
package org.objectweb.carol.rmi.multi;

// rmi import
import java.util.Properties;
import java.util.Vector;
import java.util.Enumeration;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;

// corba import
import javax.rmi.CORBA.PortableRemoteObjectDelegate;

// carol jrmp import 
import org.objectweb.carol.rmi.jrmp.server.JUnicastRemoteObject;
import org.objectweb.carol.rmi.jrmp.interceptor.JServerRequestInterceptor;
import org.objectweb.carol.rmi.jrmp.interceptor.JClientRequestInterceptor;
import org.objectweb.carol.rmi.jrmp.interceptor.JInitializer;
import org.objectweb.carol.rmi.jrmp.interceptor.JInitInfo;
import org.objectweb.carol.rmi.jrmp.interceptor.JRMPInitInfoImpl;
import org.objectweb.carol.rmi.jrmp.interceptor.ProtocolInterceptor;


/**
 * Class <code>JrmpPRODelegate</code>  for the mapping between Rmi jrmp UnicastRemoteObject and PortableRemoteObject
 */
public class JrmpPRODelegate implements PortableRemoteObjectDelegate {

    /**
     * Initilazer class prefix
     */
    public static String INTIALIZER_PREFIX = "org.objectweb.PortableInterceptor.JRMPInitializerClass";

    /**
     * private Interceptor for Context propagation
     */
    private JServerRequestInterceptor [] sis = null;

    /**
     * private Interceptor for Context propagation
     */
    private JClientRequestInterceptor [] cis = null;

    /**
     * Constructor 
     */ 
    public JrmpPRODelegate() {


	// Load the Interceptors
	try {
	    JInitInfo jrmpInfo = new JRMPInitInfoImpl();	    
	    for (Enumeration e = getJRMPIntializers() ; e.hasMoreElements() ;) {
		JInitializer jinit = (JInitializer) Class.forName((String)e.nextElement()).newInstance();
		jinit.pre_init(jrmpInfo);
		jinit.post_init(jrmpInfo);
	    }	    
	    sis = jrmpInfo.getServerRequestInterceptors();
	    cis = jrmpInfo.getClientRequestInterceptors();
	} catch ( Exception e) {
	    e.printStackTrace();
	    //we did not found the interceptor do nothing but a trace ?
	}	

    }

    /**
     * Export a Remote Object 
     * @param Remote object to export
     * @exception RemoteException exporting remote object problem 
     */
    public void exportObject(Remote obj) throws RemoteException {
	JUnicastRemoteObject.exportObject(obj, sis, cis);
    }

    
    /**
     * Method for unexport object
     * @param Remote obj object to unexport 
     * @exception NoSuchObjectException if the object is not currently exported
     */
    public void unexportObject(Remote obj) throws NoSuchObjectException {
	JUnicastRemoteObject.unexportObject(obj, true);	
    }

    /**
     * Connection method
     * @param target a remote object;
     * @param source another remote object; 
     * @exception RemoteException if the connection fail
     */ 
    public void connect(Remote target,Remote source) throws RemoteException {
	// do nothing
    }

	
    /**
     * Narrow method
     * @param Remote obj the object to narrow 
     * @param Class newClass the expected type of the result  
     * @return an object of type newClass
     * @exception ClassCastException if the obj class is not compatible with a newClass cast 
     */
    public Object narrow(Object obj, Class newClass ) throws ClassCastException {
	if (newClass.isAssignableFrom(obj.getClass())) {
	    return obj;
	} else {
	    throw new ClassCastException("Can't cast "+obj.getClass().getName()+" in "+newClass.getName());
	}
    }

    /**
     * To stub method
     * @return the stub object    
     * @param Remote object to unexport    
     * @exception NoSuchObjectException if the object is not currently exported
     */
    public Remote toStub(Remote obj) throws NoSuchObjectException {
	try {
	    return (Remote)JUnicastRemoteObject.exportObject(obj, sis, cis);
	} catch (java.rmi.server.ExportException e) {
	    return obj;
	} catch (RemoteException re) {
	    throw new NoSuchObjectException(re.toString());
	}
    }

    /**
     * Get Intializers method
     * @return JRMP Initializers enuumeration
     */
    private Enumeration getJRMPIntializers() {
	Vector initializers =  new Vector();
	Properties sys = System.getProperties();
	for (Enumeration e = System.getProperties().propertyNames(); e.hasMoreElements() ;) {
	    String pkey = (String)e.nextElement();
	    if (pkey.startsWith(INTIALIZER_PREFIX)) {
		initializers.add(pkey.substring(INTIALIZER_PREFIX.length() + 1));
	    }
	}
	return initializers.elements();
    }
    
}
