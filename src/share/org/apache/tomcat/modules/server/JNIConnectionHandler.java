/*   
 *  Copyright 1999-2004 The Apache Sofware Foundation.
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

