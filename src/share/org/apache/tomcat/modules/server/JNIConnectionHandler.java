/*
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

package org.apache.tomcat.modules.server;

import java.io.File;
import java.util.Vector;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.TomcatException;

// we'll use the system.out/err until the code is stable, then
// try to logger. Since this is a normal interceptor everything should
// work

/**
 * Connector for a JNI connections using the API in tomcat.service.
 * You need to set a "connection.handler" property with the class name of
 * the JNI connection handler
 * <br>
 * Based on <code>TcpEndpointConnector</code>
 *
 * @author Gal Shachor <shachor@il.ibm.com>
 */
public class JNIConnectionHandler extends BaseInterceptor implements JNIEndpoint.JniHandler {

    public JNIConnectionHandler() {
    }

    // -------------------- Config -------------------- 
    boolean nativeLibLoaded=false;
    String lib;
    boolean exitOnError=true;
    
    /** Location of the jni library
     */
    public void setNativeLibrary(String lib) {
	this.lib=lib;
    }

    public void setExitIfNoLib(boolean b) {
	exitOnError=b;
    }

    JNIEndpoint ep=null;
    
    /** Called when the ContextManger is started
     */
    public void engineInit(ContextManager cm) throws TomcatException {
	ep= JNIEndpoint.getEndpoint();
	if(ep==null ) return;
	super.engineInit( cm );

	if(! nativeLibLoaded ) {
	    initLibrary();
	    if( ! nativeLibLoaded && exitOnError) {
		System.exit(2);
	    }
	}
	try {
	    // notify the jni side that jni is set up corectly
	    ep.setConnectionHandler(this);
	} catch( Exception ex ) {
	    throw new TomcatException( ex );
	}
    }

    public void engineShutdown(ContextManager cm) throws TomcatException {
	if( ep==null ) return;
	try {
	    // notify the jni side that the jni handler is no longer
	    // in use ( we shut down )
	    ep.setConnectionHandler(null);
	} catch( Exception ex ) {
	    throw new TomcatException( ex );
	}
    }

    // ==================== callbacks from web server ====================

    static Vector pool=new Vector();
    static boolean reuse=true;

    /** Called from the web server for each request
     *  You can extend JNIConnectionHandler and implement a different
     *  JNIRequest/JNIResponse. Set the new handler on the JNIEndpoint,
     *  the processConnection will be called.
     *
     *  This is temporary, a new, better and cleaner JNI interface
     *  should be added in j-t-c.
     */
    public void processConnection(long s, long l) {
	JNIRequestAdapter reqA=null;
	JNIResponseAdapter resA=null;

        try {

	    if( reuse ) {
		synchronized( this ) {
		    if( pool.size()==0 ) {
			reqA=new JNIRequestAdapter( cm, this);
			resA=new JNIResponseAdapter( this );
			cm.initRequest( reqA, resA );
		    } else {
			reqA = (JNIRequestAdapter)pool.lastElement();
			resA=(JNIResponseAdapter)reqA.getResponse();
			pool.removeElement( reqA );
		    }
		}
		reqA.recycle();
		resA.recycle();
	    } else  {
		reqA = new JNIRequestAdapter(cm, this);
		resA =new JNIResponseAdapter(this);
		cm.initRequest( reqA , resA );
	    }

            resA.setRequestAttr(s, l);
    	    reqA.readNextRequest(s, l);

	    //     	    if(reqA.shutdown )
	    //         		return;
    	    if(resA.getStatus() >= 400) {
        		resA.finish();
    		    return;
    	    }

    	    cm.service( reqA, resA );
    	} catch(Exception ex) {
    	    ex.printStackTrace();
    	}
	if( reuse ) {
	    synchronized( this ) {
		pool.addElement( reqA );
	    }
	}
    }

    public void shutdown()
    {
        try{
            cm.log("Shutdown from JNI" );
            cm.shutdown();
        } catch (Throwable t){
            cm.log("Exception while JNI shutdown",t);
        }
    }
    // -------------------- Find the native library --------------------

    private void initLibrary()
	throws TomcatException
    {
	if( ep==null ) {
	    if( debug > 0 )
		log("JNI connector disabled, endpoint is null");
	}

	if( lib==null ) {
	    lib="jni_connect.";
	    String os = System.getProperty("os.name").toLowerCase();
	    if(os.indexOf("windows")>=0){
		lib+="dll";
	    } else if(os.indexOf("netware")>=0){
		lib+="nlm";
	    } else {
		lib+="so";
	    }
        }
	log("JNI mode detected, try to load library " + lib );

	File libF=new File(lib);
	if( ! libF.isAbsolute() ) {
	    // not absolute, try first LD_LIB_PATH
	    try {
		System.loadLibrary(lib);
		nativeLibLoaded=true;
		System.out.println("Library " + lib +
				   " was loaded from the lib path");
		return;
	    } catch(UnsatisfiedLinkError usl) {
		System.err.println("loadLibrary(" + lib +
				   ") didn't find the library, try with full path");
		if( debug > 0 )
		    usl.printStackTrace();
	    }
	}

	if( ! libF.isAbsolute() ) {
	    File f1=new File(cm.getInstallDir());
	    // XXX should it be "libexec" ???
	    File f2=new File( f1, "bin" + File.separator + "native" );
	    libF=new File( f2, lib );
	}

	if( ! libF.exists() ) {
	    throw new TomcatException( "Native library doesn't exist " + libF );
	}

        // Loading from the library path failed
        // Try to load assuming lib is a complete pathname.
        try {
	    System.load(libF.getAbsolutePath());
	    nativeLibLoaded=true;
	    System.out.println("Library " + libF.getAbsolutePath() + " loaded");
            return;
        } catch(UnsatisfiedLinkError usl) {
            System.err.println("Failed to load() " + libF.getAbsolutePath());
            if( debug > 0 )
		usl.printStackTrace();
        }        
    }

    // -------------------- Native methods --------------------
    // Calls from tomcat to the web server
    
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

