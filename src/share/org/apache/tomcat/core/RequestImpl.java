/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/Attic/RequestImpl.java,v 1.1 1999/10/09 00:30:16 duncan Exp $
 * $Revision: 1.1 $
 * $Date: 1999/10/09 00:30:16 $
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


package org.apache.tomcat.core;

import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author Harish Prabandham
 */

public class RequestImpl extends Request {
    
    protected StringManager sm =
        StringManager.getManager(Constants.Package);
    protected ServletInputStream in;
    protected MimeHeaders headers = new MimeHeaders();
    protected String serverName = "";
    protected int serverPort;
    protected String remoteAddr;
    protected String remoteHost;
    
    public RequestImpl() {
        super();
    }

    public void recycle() {
	super.recycle();
	//	moreRequests = false;
	in = null;
    	headers.clear();
	serverName = "";
    }

    public long getDateHeader(String name) {
        return headers.getDateHeader(name);
    }
    
    public String getHeader(String name) {
        return headers.getHeader(name);
    }

    public Enumeration getHeaders(String name) {
        String[] headers = this.headers.getHeaders(name);
	Vector v = new Vector();

        if (headers != null) {
	    for (int i = 0; i < headers.length; i++) {
	        v.addElement(headers[i]);
	    }
        }

	return v.elements();
    }
    
    public int getIntHeader(String name)  {
        return headers.getIntHeader(name);
    }
    
    public Enumeration getHeaderNames() {
        return headers.names();
    }
    
    public ServletInputStream getInputStream()
    throws IOException {
    	if (in == null) {
            String msg = sm.getString("serverRequest.inputStream.npe");

    	    throw new IOException(msg);
    	}

    	return in;    
    }

    public BufferedReader getReader()
    throws IOException {
        // XXX
	// this won't work in keep alive scenarios. We need to provide
	// a buffered reader that won't try to read in the stream
	// past the content length -- if we don't, the buffered reader
	// will probably try to read into the next request... bad!
        String encoding = getCharacterEncoding();
        if (encoding == null) {
            // Set a default of Latin-1 even for non-Latin-1 systems
            encoding = "ISO-8859-1";
        }
	InputStreamReader r =
            new InputStreamReader(getInputStream(), encoding);
	return new BufferedReader(r);
    }
    
    // XXX
    // the server name should be pulled from a server object of some
    // sort, not just set and got.
    
    public String getServerName() {
	return serverName;
    }

    public void setServerName(String serverName) {
	this.serverName = serverName;
    }
    
    public void processCookies() {
    	String cookieString = headers.getHeader("cookie");
	if (cookieString != null) {
            StringTokenizer tok = new StringTokenizer(cookieString,
                                                      ";", false);
            while (tok.hasMoreTokens()) {
                String token = tok.nextToken();
                int i = token.indexOf("=");
                if (i > -1) {

                    // XXX
                    // the trims here are a *hack* -- this should
                    // be more properly fixed to be spec compliant
                    
                    String name = token.substring(0, i).trim();
                    String value = token.substring(i+1, token.length()).trim();
                    Cookie cookie = new Cookie(name, value);
                    cookies.addElement(cookie);
                } else {
                    // we have a bad cookie.... just let it go
                }
            }
        }	
    }
    
    // XXX
    // general comment -- we've got one form of this method that takes
    // a string, another that takes an inputstream -- they don't work
    // well together. FIX
    
    public void processFormData(String data) {

        // XXX
        // there's got to be a faster way of doing this.
        StringTokenizer tok = new StringTokenizer(data, "&", false);
        while (tok.hasMoreTokens()) {
            String pair = tok.nextToken();
	    int pos = pair.indexOf('=');
	    if (pos != -1) {
		String key = unUrlDecode(pair.substring(0, pos));
		String value = unUrlDecode(pair.substring(pos+1,
							  pair.length()));
		String values[];
		if (parameters.containsKey(key)) {
		    String oldValues[] = (String[])parameters.get(key);
		    values = new String[oldValues.length + 1];
		    for (int i = 0; i < oldValues.length; i++) {
			values[i] = oldValues[i];
		    }
		    values[oldValues.length] = value;
		} else {
		    values = new String[1];
		    values[0] = value;
		}
		parameters.put(key, values);
	    } else {
		// we don't have a valid chunk of form data, ignore
	    }
        }
    }

    public void processFormData(InputStream in, int contentLength) {
        byte[] buf = new byte[contentLength];
        int read = 0;
        try {
            do {
                read += in.read(buf, read, buf.length - read);
            } while (read < contentLength && read != -1);
        } catch (IOException e) {
            
        }
        String s = new String(buf, 0, read);
        processFormData(s);
    }
    
    public String unUrlDecode(String data) {
	StringBuffer buf = new StringBuffer();
	for (int i = 0; i < data.length(); i++) {
	    char c = data.charAt(i);
	    switch (c) {
	    case '+':
		buf.append(' ');
		break;
	    case '%':
		try {
		    buf.append((char) Integer.parseInt(data.substring(i+1,
                        i+3), 16));
		    i += 2;
		} catch (NumberFormatException e) {
                    String msg =
                        sm.getString("serverRequest.urlDecode.nfe", data);

		    throw new IllegalArgumentException(msg);
		} catch (StringIndexOutOfBoundsException e) {
		    String rest  = data.substring(i);
		    buf.append(rest);
		    if (rest.length()==2)
			i++;
		}
		
		break;
	    default:
		buf.append(c);
		break;
	    }
	}
	return buf.toString();
    }           
	
    // XXX This method is duplicated in core/Response.java
    public String getCharsetFromContentType(String type) {
        // Basically return everything after ";charset="
        if (type == null) {
            return null;
        }
        int semi = type.indexOf(";");
        if (semi == -1) {
            return null;
        }
        String afterSemi = type.substring(semi + 1);
        int charsetLocation = afterSemi.indexOf("charset=");
        if (charsetLocation == -1) {
            return null;
        }
        String afterCharset = afterSemi.substring(charsetLocation + 8);
        String encoding = afterCharset.trim();
        return encoding;
    }

    public int getServerPort() {
        return serverPort;
    }
    
    public String getRemoteAddr() {
        return remoteAddr;
    }
    
    public String getRemoteHost() {
	return remoteHost;
    }    
    
    public void setHeaders( MimeHeaders h ) {
	headers=h;
    }

    public void setServletInputStream( ServletInputStream in ) {
	this.in=in;
    }

    public void setServerPort( int port ) {
	serverPort=port;
    }
    
    public void setRemoteAddress(String addr) {
	this.remoteAddr = addr;
    }

    public void setRemoteHost( String host ) {
	this.remoteHost=host;
    }

    public void setMethod( String meth ) {
	this.method=meth;
    }

    public void setProtocol( String protocol ) {
	this.protocol=protocol;
    }

    public void setRequestURI( String r ) {
	this.requestURI=r;
    }

    public void setQueryString( String q ) {
	this.queryString=q;
    }

    public void setParameters( Hashtable h ) {
	this.parameters=h;
    }
        
    public void setContentLength( int  len ) {
	this.contentLength=len;
    }
        
    public void setContentType( String type ) {
	this.contentType=type;
    }

    public void setCharEncoding( String enc ) {
	this.charEncoding=enc;
    }
}
