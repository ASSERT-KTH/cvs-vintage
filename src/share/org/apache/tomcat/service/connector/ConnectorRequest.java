/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/service/connector/Attic/ConnectorRequest.java,v 1.2 1999/10/24 17:34:04 costin Exp $
 * $Revision: 1.2 $
 * $Date: 1999/10/24 17:34:04 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */ 


package org.apache.tomcat.service.connector;

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
//import org.apache.tomcat.server.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ConnectorRequest extends Request {
    MsgConnector con;
    Hashtable env_vars;
    
    public ConnectorRequest( MsgConnector con ) {
	super();
	this.con=con;
    }
    
    public void recycle() {
	super.recycle();
    }

    protected int decodeRequest(MsgBuffer msg) throws IOException {

	env_vars=new Hashtable();

	int envCount = msg.getInt();
	
	for( int i=0; i<envCount; i++ ) {
	    String n= msg.getString();
	    String v= msg.getString();
	    env_vars.put( n , v );
	}
	
	int hCount = msg.getInt();
	     
	for( int i=0; i<hCount; i++ ) {
	    String n= msg.getString();
	    String v= msg.getString();
	    headers.putHeader( n.toLowerCase() , v );
	    //System.out.println( "Head: " + n + "=" + v);
	}


	byte initialBodyChunk[] = new byte[msg.getMaxLen()];
	int len=msg.getBytes(initialBodyChunk); // XXX reuse ? 

	ConnectorServletIS in = new ConnectorServletIS(con, initialBodyChunk , len);
	this.in=in;

	
	method= (String)env_vars.get("REQUEST_METHOD");
	protocol=(String)env_vars.get("SERVER_PROTOCOL");
	requestURI=(String)env_vars.get("REQUEST_URI");
	queryString=(String)env_vars.get("QUERY_STRING");
	if ((queryString != null ) && ! "".equals(queryString)) {
            processFormData(queryString);
        }
	if(requestURI==null) requestURI="xxx"; //XXX
	// XXX: fix it!
	if (requestURI.indexOf("?") > -1) {
	    requestURI = requestURI.substring(0, requestURI.indexOf("?"));
        }
	String hostHeader = this.getHeader("host");
		
	if (hostHeader != null) {
	    int i = hostHeader.indexOf(':');
	    if (i > -1) {
		hostHeader = hostHeader.substring(0,i);
	    }
	    this.setServerName(hostHeader);
	} else {
	    // XXX
	    // this is crap having to do this lookup -- we
	    // need a better solution
	    //    InetAddress localAddress = socket.getLocalAddress();
	    //rrequest.setServerName(localAddress.getHostName());
	    this.setServerName("localhost");
	}
	contentLength = headers.getIntHeader("content-length");
	contentType = headers.getHeader("content-type");
	charEncoding = getCharsetFromContentType(contentType);

        String sport=(String)env_vars.get("SERVER_PORT");
	if(sport==null) sport="80";
	serverPort=new Integer(sport).intValue();

        remoteAddr=(String)env_vars.get("REMOTE_ADDR");
	
	// XXX: bug, fix it
	remoteHost=(String)env_vars.get("REMOTE_ADDR");
	
	processCookies();
	 
	return 0;
    }    

}

