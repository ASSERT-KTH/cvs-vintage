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

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Handle incoming JNI connections. This class will be called from
 * native code to start tomcat and for each request. 
 *
 * @author Gal Shachor <shachor@il.ibm.com>
 */
public class JNIEndpoint {

    JniHandler handler;

    boolean running = false;

    // Note: I don't really understand JNI and its use of output
    // streams, but this should really be changed to use
    // tomcat.logging.Logger and not merely System.out  -Alex
    
    public JNIEndpoint() {
    }

    // -------------------- Configuration --------------------

    // Called back when the server is initializing the handler
    public void setConnectionHandler(JniHandler handler ) {
	this.handler=handler;
	// the handler is no longer useable
    	if( handler==null ) {
	    System.out.println("Shutting down, handler==null ...");
	    running=false;
            synchronized(this) {
                notify();
            }
	    return;
	}

	System.out.println("Running ...");
	running=true;
        synchronized(this) {
            notify();
        }
    }
    
    // We can have a single active JNIEndpoint.
    static JNIEndpoint ep;

    public static void setEndpoint(JNIEndpoint jniep)
    {
        ep = jniep;
    }

    public static JNIEndpoint getEndpoint() {
	return ep;
    }

    public static final int DEFAULT_TIMEOUT=60*1000;
    
    public static int getTimeout() {
	// # 3086
	String to=System.getProperty("JNIEndpoint.timeout");
	if( to!=null ) {
	    try {
		int i=new Integer( to ).intValue();
		return i;
	    } catch( Exception ex ){
		System.out.println("Invalid timeout " + to );
	    }
	}
	return DEFAULT_TIMEOUT;
    }

    // -------------------- JNI Entry points

    /** Called by JNI to start up tomcat.
     */
    public int startup(String cmdLine,
                       String stdout,
                       String stderr)
    {
	if( ep != null ) {
	    System.err.println("ALREADY STARTED, this is the second call to STARTUP ");
	    return 1;
	}
	System.err.println("Mod_jk calling startup() ");
        try {
            if(null != stdout) {
                System.setOut(new PrintStream(new FileOutputStream(stdout)));
            }
            if(null != stderr) {
                System.setErr(new PrintStream(new FileOutputStream(stderr)));
            }
        } catch(Throwable t) {
        }

	// We need to make sure tomcat did start successfully and
	// report this back.
        try {
            JNIEndpoint.setEndpoint(this);
	    // it will call back setHandler !!
            StartupThread startup = new StartupThread(cmdLine);
	    System.err.println("Starting up StartupThread");
            startup.start();
            synchronized (this) {
                wait(getTimeout());
            }
	    System.err.println("End waiting");
        } catch(Throwable t) {
        }

        if(running) {
	    System.err.println("Running fine ");
            return 1;
        }
	System.err.println("Error - why doesn't run ??");
        return 0;
    }

    /** Called by JNI when a new request is received.
     */
    public int service(long s, long l)
    {
        if(running) {
            try {
                handler.processConnection(s, l);
                return 1;
            } catch(Throwable t) {
                // Throwables are not allowed into the native code !!!
                // print it out so that we can debug it later.
                System.out.println("Caught throwable " + t);
                t.printStackTrace();
            }
        }
        return 0;
    }

    public void shutdown(){
        if ( handler != null && running ) {
            handler.shutdown();
        }
    }

    public static interface JniHandler {
	public void processConnection( long s, long l );
	public void shutdown();
    }
}

/** Tomcat is started in a separate thread. It may be loaded on demand,
    and we can't take up the request thread, as it may be affect the server.

    During startup the JNIConnectionHandler will be initialized and
    will configure JNIEndpoint ( static - need better idea )
 */
class StartupThread extends Thread {
    String []cmdLine = null;

    public StartupThread(String cmdLine) {
        if(null == cmdLine) {
        	this.cmdLine = new String[0];
        } else {
            Vector v = new Vector();
            StringTokenizer st = new StringTokenizer(cmdLine);
            while (st.hasMoreTokens()) {
                v.addElement(st.nextToken());
            }
            this.cmdLine = new String[v.size()];
            v.copyInto(this.cmdLine);
        }
    }

    public void run() {
        boolean failed = true;
        try {
	    System.err.println("Calling main" );
            org.apache.tomcat.startup.Main.main(cmdLine);
	    System.err.println("Main returned" );
            failed = false;
        } catch(Throwable t) {
            t.printStackTrace(); // OK
        } finally {
            if(failed) {
		System.err.println("Failed ??");
            }
        }
    }
}
