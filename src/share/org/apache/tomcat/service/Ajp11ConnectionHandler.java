/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/service/Attic/Ajp11ConnectionHandler.java,v 1.18 2000/05/23 20:58:23 costin Exp $
 * $Revision: 1.18 $
 * $Date: 2000/05/23 20:58:23 $
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
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF * SUCH DAMAGE.
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


/*
  Based on java.apache.org - the code to process AJP connection
*/

package org.apache.tomcat.service;

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
import org.apache.tomcat.service.http.*;
import org.apache.tomcat.service.http.HttpResponseAdapter;
import org.apache.tomcat.service.http.HttpRequestAdapter;
import javax.servlet.*;
import javax.servlet.http.*;

/* Deprecated - must be rewriten to the connector model.  
 */
public class Ajp11ConnectionHandler implements  TcpConnectionHandler {
    StringManager sm = StringManager.getManager("org.apache.tomcat.service");
    
    ContextManager contextM;
    
    public Ajp11ConnectionHandler() {
    }

    public Object[] init() {
	return null;
    }

    public void setAttribute(String name, Object value ) {
	if("context.manager".equals(name) ) {
	    contextM=(ContextManager)value;
	}
    }
    
    public void setContextManager( ContextManager contextM ) {
	this.contextM=contextM;
    }

    public void processConnection(TcpConnection connection, Object thData[]) {
	
        try {
	    Socket socket=connection.getSocket();
	    socket.setSoLinger( true, 100);
	    //XXX recycle
	    //	    RequestImpl request=new RequestImpl();
	    
	    AJPRequestAdapter reqA = new AJPRequestAdapter(socket); // todo: clean ConnectionHandler, make it abstract
	    reqA.setContextManager( contextM );
	    //request.setRequestAdapter( reqA );
	    
	    Ajp11ResponseAdapter resA=new Ajp11ResponseAdapter();
	    //	    ResponseImpl response = new ResponseImpl();
            resA.setOutputStream(socket.getOutputStream());
	    //	    response.setResponseAdapter(resA );
	    int count = 1;

	    //	    request.setResponse(response);
	    reqA.setResponse( resA );
	    //	    request.setResponse(response);
	    //	    response.setRequest(request);
	    resA.setRequest(reqA);

	    reqA.readNextRequest();

	    // resolve the server that we are for

	    int contentLength = reqA.getFacade().getIntHeader("content-length");
	    if (contentLength != -1) {
		BufferedServletInputStream sis =
		    (BufferedServletInputStream)reqA.getInputStream();
		sis.setLimit(contentLength);
	    }

	    contextM.service( reqA, resA );

	    resA.finish();
	    socket.close();
	} catch (Exception e) {
            // XXX
	    // this isn't what we want, we want to log the problem somehow
	    System.out.println("HANDLER THREAD PROBLEM: " + e);
	    e.printStackTrace();
	}
    }
}

class AJPRequestAdapter extends RequestImpl {
    StringManager sm = StringManager.getManager("org.apache.tomcat.service");
    Socket socket;
    
    public AJPRequestAdapter(Socket so) {
	this.socket=so;
	in = new BufferedServletInputStream( this );
    }
    
    protected void readNextRequest() throws IOException {
	//	System.out.println("In AJPREquest");
	InputStream sin = socket.getInputStream();

	Hashtable env_vars=new Hashtable();

	readAJPData(sin, env_vars);
	
	// equiv of readRequestLine ( firsts line of the Request )
	method= (String)env_vars.get("REQUEST_METHOD");
	protocol=(String)env_vars.get("SERVER_PROTOCOL");
	requestURI=(String)env_vars.get("REQUEST_URI");
	queryString=(String)env_vars.get("QUERY_STRING");
	
	// lazy
 	// if ((queryString != null ) && ! "".equals(queryString)) {
	//             processFormData(queryString);
	//         }

	// AJP11 will send CGI vars, and REQUEST_URI includes query string 
	int idQ= requestURI.indexOf("?");
	if ( idQ > -1) {
	    requestURI = requestURI.substring(0, idQ);
        }
	// 	System.out.println("Request: " + requestURI );
	// 	System.out.println("Query: " + queryString );
	// 	System.out.println("ENV: " + env_vars );
	// 	System.out.println("HEADERS: " + headers_in );
	// 	System.out.println("PARAMETERS: " + parameters );

	String sport=(String)env_vars.get("SERVER_PORT");
	if(sport==null) sport="80";
	serverPort=new Integer(sport).intValue();

        remoteAddr=(String)env_vars.get("REMOTE_ADDR");
	
	// XXX: bug, fix it
	remoteHost=(String)env_vars.get("REMOTE_ADDR");

	// lazy	processCookies();
	
	contentLength = headers.getIntHeader("content-length");
	contentType = headers.getHeader("content-type");
	// lazy	charEncoding = getCharsetFromContentType(contentType);
	    
	// XXX
	// detect for real whether or not we have more requests
	// coming
	
	// XXX
	// Support persistent connection in AJP21
	//moreRequests = false;	
    }    

    // Ajp11 protocol implementation 
    public static final int CH_REQUEST_DATA=1;

    // UTF8 is a strict superset of ASCII.
    final static String CHARSET = "UTF8";

    void readAJPData(InputStream in, Hashtable env_vars ) throws IOException {
	byte id = 0;
        int index = 0;
        String token1 = null;
        String token2 = null;
        byte[] line = null;
        byte[] hex = new byte[4];
        int maxlen = 0;
        int len = 0;

        try {
            while (true) {
                // Read four bytes from the input stream
		if (in.read(hex) != 4) {
		    throw new IOException("Malformed AJP request: error reading line length");
		}
                
                // Convert the hex length in decimal
                len=HexUtils.convert2Int(hex);

                // if nothing to read return the previous string and let
                // the caller method understand that len = 0 to finish
                if (len == 0) // throw new AllDataRead();
		    break;
		
		//log(CH_REQUEST_DATA, "Will read " + len + " bytes for this line");
    
                // adapt the str buffer to the request length
                if (len > maxlen) {
                    maxlen = len;
                    line = new byte[len];
                }
                
                // Read len bytes from the input stream
		int len1=in.read(line, 0, len);
                if ( len1 != len) {
		    System.out.println( "REQUEST: " + new String(line, 0, len, CHARSET) );
                    throw new IOException("Malformed AJP request: error reading line data " + len1 + " " + len);
                }
                    
		//log(CH_REQUEST_DATA, "Read: " + new String(line, 0, len, CHARSET));

                // Get the identifier from the first character
                id = line[0];
                
                // All id's take one or two pieces of data separated by a tab (09).
                for (index = 1; (index < len) && (line[index] != 9); index++);
                
                token1 = new String(line, 1, index - 1, CHARSET);
                if (index != len) {
                    token2 = new String(line, index + 1, len - index - 1, CHARSET);
                } else {
                    token2 = "";
                }
		
		//		System.out.println(token1 + " = " + token2 );
                // Switch, depending on what the id is
                switch (id) {
                case 0x43: // 'C' --> ServletZone + Servlet request
                    String servletzone = token1;
                    String servletname = token2; // XXX ignored - tomcat will do it's own parsing...
		    //log(CH_REQUEST_DATA, "Servlet Zone: " + token1 + " Servlet: " + token2);
                    break;
                case 0x53: // 'S' --> Host name
                    String hostname = token1;// XXX fix it, we need to use it in tomcat
                    break;
                case 0x45: // 'E' --> Env variable
                    env_vars.put(token1, token2);
                    break;
                case 0x48: // 'H' --> Header
		    headers.putHeader( token1.toLowerCase(), token2 );
                    break;
                case 0x73: // 's' --> Signal
                    String signal = token1; // XXX ignore
		    System.out.println("Signal: " + token1);
                    break;
                default: // What the heck is this?
                    throw new Exception("Received unknown id: " + (char) id + " [" + token1 + "," + token2 + "]");
                }
            }
        } catch (Exception e) {
	    e.printStackTrace();
            throw new IOException("Error " + e.toString());
        }

    }
}

// Ajp use Status: instead of Status 
class Ajp11ResponseAdapter extends HttpResponseAdapter {
    /** Override setStatus
     */
    public void sendStatus( int status, String message) throws IOException {
	printHead("Status: " );
	printHead( String.valueOf( status ));
	printHead("\r\n");
    }
}

