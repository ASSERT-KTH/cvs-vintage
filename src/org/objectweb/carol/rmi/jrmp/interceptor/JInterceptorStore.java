/*
 * @(#) JInterceptorStore.java	1.0 02/07/15
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
package org.objectweb.carol.rmi.jrmp.interceptor;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Class <code>JInterceptorStore</code> is the CAROL JRMP Client and Server Interceptors
 * Storage System
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 10/03/2003
 */
public class JInterceptorStore {

    /**
     * Initilazer class prefix
     */
    public static String INTIALIZER_PREFIX = "org.objectweb.PortableInterceptor.JRMPInitializerClass";

    /**
     * private boolean for intialisation 
     */
    private static boolean init = false;

    /**
     * private Interceptor for Context propagation
     */
    private static JServerRequestInterceptor [] sis = null;

    /**
     * private Interceptor for Context propagation
     */
    private static JClientRequestInterceptor [] cis = null;

    /**
     * private Interceptors Initializers for Context propagation
     */
    private static String [] initializers = null;

    /** 
     * private Remote Client Interceptor for Context propagation
     */
    private static HashMap remoteClientInterceptors = new HashMap();
   
    /**
     * Intialize interceptors for a carol server
     */
    public static void initLocalInterceptors() {
	if (!init) {
	    // Load the Interceptors
	    try {
		JInitInfo jrmpInfo = new JRMPInitInfoImpl();
		String [] ins = getJRMPInitializers();
		for (int i = 0; i < ins.length ; i ++) {
		    JInitializer jinit = (JInitializer) Class.forName(ins[i]).newInstance();
		    jinit.pre_init(jrmpInfo);
		    jinit.post_init(jrmpInfo);
		}	    
		sis = jrmpInfo.getServerRequestInterceptors();
		cis = jrmpInfo.getClientRequestInterceptors();
		init = true;
	    } catch ( Exception e) {
		//we did not found the interceptor do nothing but a trace ?
		TraceCarol.error("JrmpPRODelegate(), No interceptors found", e);
	    }	
	}

    }

    /**
     * get the local server interceptor
     */
    public static JServerRequestInterceptor [] getLocalServerInterceptors() {
	return sis;
    }

    /**
     * get the local client interceptor
     */
    public static JClientRequestInterceptor [] getLocalClientInterceptors() {
	return cis;
    }

     /**
     * Get Intializers method
     * @return JRMP Initializers enuumeration
     */
    public static String [] getJRMPInitializers() {
	if (!init) {
	    ArrayList ins =  new ArrayList();
	    Properties sys = System.getProperties();
	    for (Enumeration e = System.getProperties().propertyNames(); e.hasMoreElements() ;) {
		String pkey = (String)e.nextElement();
		if (pkey.startsWith(INTIALIZER_PREFIX)) {
		    ins.add(pkey.substring(INTIALIZER_PREFIX.length() + 1));
		}
	    }
	    int sz = ins.size();
	    initializers=new String [sz];
	    for (int i = 0; i < sz; i++) {
		initializers[i] = (String)ins.get(i);
	    }	 
	    return initializers;
	} else {
	    return initializers;
	}
    }

 
    /**
     * Set the remote client interceptor
     */
    public static JClientRequestInterceptor [] setRemoteInterceptors(RemoteKey rk, String [] ins) {
	if (remoteClientInterceptors.containsKey(rk)) {
	    return (JClientRequestInterceptor [])remoteClientInterceptors.get(rk);
	} else {	    
	    // Load the Interceptors
	    try {
		JInitInfo jrmpInfo = new JRMPInitInfoImpl();
		for (int i = 0; i < ins.length ; i ++) {
		    JInitializer jinit = (JInitializer) Class.forName(ins[i]).newInstance();
		    jinit.pre_init(jrmpInfo);
		    jinit.post_init(jrmpInfo);
		}
		JClientRequestInterceptor [] rci = jrmpInfo.getClientRequestInterceptors();
		remoteClientInterceptors.put(rk,rci);
		return rci;
	    } catch ( Exception e) {
		//we did not found the interceptor do nothing but a trace ?
		TraceCarol.error("JrmpPRODelegate(), No remote interceptors found", e);
		return null;
	    }
	}
    }
}


