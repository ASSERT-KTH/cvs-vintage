/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/service/connector/Attic/JNIConnectionHandler.java,v 1.19 2000/10/06 05:19:13 costin Exp $
 * $Revision: 1.19 $
 * $Date: 2000/10/06 05:19:13 $
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

import java.io.IOException;
import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
import org.apache.tomcat.helper.*;
import java.util.Vector;
import java.io.File;

public class JNIConnectionHandler {

    ContextManager contextM;

    // Note: I don't really understand JNI and its use of output
    // streams, but this should really be changed to use
    // tomcat.logging.Logger and not merely System.out  -Alex
    
    public JNIConnectionHandler() {
    }

    public void setServer(Object contextM) {
    	this.contextM=(ContextManager)contextM;
    }

    public void setNativeLibrary(String lib) {

        // First try to load from the library path
        try {
            System.loadLibrary(lib);
            System.out.println("Library " + lib + " was loaded from the lib path");
            return;
        } catch(UnsatisfiedLinkError usl) {
            //usl.printStackTrace();
            System.err.println("Failed to loadLibrary() " + lib);
        }
        
        // Loading from the library path failed
        // Try to load assuming lib is a complete pathname.
        try {
        System.load(lib);
        System.out.println("Library " + lib + " loaded");
            return;
        } catch(UnsatisfiedLinkError usl) {
            System.err.println("Failed to load() " + lib);
            //usl.printStackTrace();
        }
        
        // OK, try to load from the default libexec 
        // directory. 
        // libexec directory = tomcat.home + / + libexec
        File f = new File(System.getProperties().getProperty("tomcat.home"), "libexec");
        if(System.getProperty( "os.name" ).toLowerCase().indexOf("windows") >= 0) {
            f = new File(f, "jni_connect.dll");
        } else {
            f = new File(f, "jni_connect.so");
        }
        
        System.load(f.toString());
        System.out.println("Library " + f.toString() + " loaded");
    }

    static Vector pool=new Vector();
    static boolean reuse=true;
    
    public void processConnection(long s, long l) {
	JNIRequestAdapter reqA=null;
	JNIResponseAdapter resA=null;

        try {
	    
	    if( reuse ) {
		synchronized( this ) {
		    if( pool.size()==0 ) {
			reqA=new JNIRequestAdapter( contextM, this);
			resA=new JNIResponseAdapter( this );
			contextM.initRequest( reqA, resA );
		    } else {
			reqA = (JNIRequestAdapter)pool.lastElement();
			resA=(JNIResponseAdapter)reqA.getResponse();
			pool.removeElement( reqA );
		    }
		}
		reqA.recycle();
		resA.recycle();
	    } else  {
		reqA = new JNIRequestAdapter(contextM, this);
		resA =new JNIResponseAdapter(this);
		contextM.initRequest( reqA , resA );
	    }
	    
            resA.setRequestAttr(s, l);
    	    reqA.readNextRequest(s, l);

    	    if(reqA.shutdown )
        		return;
    	    if(resA.getStatus() >= 400) {
        		resA.finish();
    		    return;
    	    }

    	    contextM.service( reqA, resA );
    	} catch(Exception ex) {
    	    ex.printStackTrace();
    	}
	if( reuse ) {
	    synchronized( this ) {
		pool.addElement( reqA );
	    }
	}
    }

    native int readEnvironment(long s, long l, String []env);

    native int getNumberOfHeaders(long s, long l);

    native int readHeaders(long s,
                           long l,
                           String []names,
                           String []values);

    native int read(long s,
                    long l,
                    byte []buf,
                    int from,
                    int cnt);

    native int startReasponse(long s,
                              long l,
                              int sc,
                              String msg,
                              String []headerNames,
                              String []headerValues,
                              int headerCnt);

    native int write(long s,
                     long l,
                     byte []buf,
                     int from,
                     int cnt);
}

class JNIRequestAdapter extends Request {
    static StringManager sm = StringManager.getManager("org.apache.tomcat.resources");
    ContextManager contextM;
    boolean shutdown=false;

    JNIConnectionHandler h;
    long s;
    long l;

    public int doRead() throws IOException {
        byte []b = new byte[1];
        int rc = doRead(b, 0, 1);

        if(rc <= 0) {
            return -1;
        }

        return ((int)b[0]) & 0x000000FF;
    }

    public  int doRead(byte b[], int off, int len) throws IOException {
        int rc = 0;

        while(0 == rc) {
	        rc = h.read(s, l, b, off, len);
	        if(0 == rc) {
	            Thread.currentThread().yield();
	        }
	    }
	    return rc;
    }

    public JNIRequestAdapter(ContextManager cm,
                             JNIConnectionHandler h) {
    	this.contextM = cm;
    	this.h = h;
    }

    protected void readNextRequest(long s, long l) throws IOException {
        String []env = new String[15];
        int i = 0;

    	this.s = s;
    	this.l = l;

        for(i = 0 ; i < 12 ; i++) {
            env[i] = null;
        }

        /*
         * Read the environment
         */
        if(h.readEnvironment(s, l, env) > 0) {
    		methodMB.setString( env[0] );
    		uriMB.setString( env[1] );
    		queryMB.setString( env[2] );
    		remoteAddr  = env[3];
    		remoteHost  = env[4];
    		serverName  = env[5];
            serverPort  = Integer.parseInt(env[6]);
            authType    = env[7];
            remoteUser  = env[8];
            schemeMB.setString(env[9]);
            protoMB.setString( env[10] );
            // response.setServerHeader(env[11]);
            
            if(schemeMB.equalsIgnoreCase("https")) {
                if(null != env[12]) {
		            attributes.put("javax.servlet.request.X509Certificate",
	                               env[12]);
	            }
	            
                if(null != env[13]) {
		            attributes.put("javax.servlet.request.cipher_suite",
	                               env[13]);
	            }
	            
                if(null != env[14]) {
		            attributes.put("javax.servlet.request.ssl_session",
	                               env[14]);
	            }
            }
            
            
        } else {
            throw new IOException("Error: JNI implementation error");
        }

        /*
         * Read the headers
         */
        int nheaders = h.getNumberOfHeaders(s, l);
        if(nheaders > 0) {
            String []names = new String[nheaders];
            String []values = new String[nheaders];
            if(h.readHeaders(s, l, names, values) > 0) {
                for(i = 0 ; i < nheaders ; i++) {
                    headers.addValue(names[i]).setString(values[i]);
                }
            } else {
                throw new IOException("Error: JNI implementation error");
            }
        }


    }
}


// Ajp use Status: instead of Status
class JNIResponseAdapter extends Response {

    JNIConnectionHandler h;
    long s;
    long l;

    public JNIResponseAdapter(JNIConnectionHandler h) {
    	this.h = h;
    }

    protected void setRequestAttr(long s, long l) throws IOException {
    	this.s = s;
    	this.l = l;
    }

    public void endHeaders() throws IOException {

    	if(request.getProtocol()==null) // HTTP/0.9 
	        return;

        super.endHeaders();
        
	// Servlet Engine header will be set per/adapter - smarter adapters will
	// not send it every time ( have it in C side ), and we may also want
	// to add informations about the adapter used 
	if( request.getContext() != null)
	    setHeader("Servlet-Engine", request.getContext().getEngineHeader());

        int    hcnt = 0;
        String []headerNames = null;
        String []headerValues = null;
        headers.removeHeader("Status");
        hcnt = headers.size();
        headerNames = new String[hcnt];
        headerValues = new String[hcnt];

        for(int i = 0; i < hcnt; i++) {
            headerNames[i] = headers.getName(i).toString();
            headerValues[i] = headers.getValue(i).toString();
        }

        if(h.startReasponse(s, l, status, RequestUtil.getMessage(status), headerNames, headerValues, hcnt) <= 0) {
            throw new IOException("Error: JNI startReasponse implementation error");
        }
    }

    public void doWrite(byte buf[], int pos, int count) throws IOException {
        if(h.write(s, l, buf, pos, count) <= 0) {
            throw new IOException("Error: JNI implementation error");
        }
    }
}

