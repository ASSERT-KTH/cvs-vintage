/*
 *  Copyright 2001-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.util.net;


import java.io.IOException;
import java.net.ConnectException;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.tomcat.util.compat.Jdk11Compat;

/**
 */

public final class StreamHandlerFactory implements URLStreamHandlerFactory {

    /** System property for protocol handlers.
     */
    public static final String SYS_PROTOCOLS = "java.protocol.handler.pkgs";
    private String protocolString = null;
    private Vector protocols = null;
    private Jdk11Compat jdk11Compat = Jdk11Compat.getJdkCompat();

    public StreamHandlerFactory() {
	loadProtocols();
    }

    /** Create a <code>URLStreamHandler</code> for this protocol.
     *  This factory differs from the default in that
     *  <ul>
     *  <li>If the protocol is <code>jar</code> 
     *      or <code>file</code>, we decline</li>
     *  <li>We load classes from the <code>ContextClassLoader</code></li>
     *  <li>If no handler is defined, we return a connection-less
     *       <code>URLStreamHandler</code> that allows parsing</li>
     *  </ul>
     */
    public  URLStreamHandler createURLStreamHandler(String protoS) {
	if("jar".equalsIgnoreCase(protoS) || "file".equalsIgnoreCase(protoS) )
	    return null;
	// The following is broken in a sandbox. 
	// Maybe fix with privileged, but probably not. 
	// LoadOnStartups have done their thing before we're set.
	//if(protocolString != System.getProperty(SYS_PROTOCOLS))
	//    loadProtocols();
	ClassLoader acl = jdk11Compat.getContextClassLoader();
	if(acl == null)
	    acl = getClass().getClassLoader();
	Class phldrC = null;
	if(protocols != null) {
	    Enumeration e = protocols.elements();
	    while(e.hasMoreElements()) {
		String phldrPK = (String)e.nextElement();
		try {
		    phldrC = acl.loadClass(phldrPK+"."+protoS+".Handler");
		    break;
		} catch(ClassNotFoundException cnfex){
		}
	    }
	}
	if(phldrC == null){
	    String phldrCN = "sun.net.www.protocol." + protoS + ".Handler";
	    try {
		phldrC = acl.loadClass(phldrCN);
	    } catch(ClassNotFoundException cnfex) {
	    }
	}
	if(phldrC == null) {
	    phldrC = DummyStreamHandler.class;
	}
	URLStreamHandler handler = null;
	try {
	    handler = (URLStreamHandler)phldrC.newInstance();
	} catch(Exception ex) {
	}
	return handler;
    }

    /** Pre-parse the defined protocols.
     *  This follows the rules specified in 
     *  <code>java.net.URL(String,String,int,String)</code>.
     */
    private synchronized void loadProtocols() {
	if(protocolString == System.getProperty(SYS_PROTOCOLS))	
	    return;
	String protocolS = System.getProperty(SYS_PROTOCOLS);
	if(protocolS != null) {
	    protocols = new Vector();
	    StringTokenizer tok = new StringTokenizer(protocolS,"|");
	    while(tok.hasMoreTokens()) {
		String protStr = tok.nextToken();
		protocols.addElement(protStr);
	    }
	}
	protocolString = protocolS;
    }
    /** A connection-less <code>URLStreamHandler</code> to allow parsing-only URLs.
     */
    public class DummyStreamHandler extends URLStreamHandler {
	DummyStreamHandler() {
	}
	protected URLConnection openConnection(java.net.URL xx) throws IOException{
	    throw new ConnectException("Connections are not supported.");
	}
    }
}
