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
 */ 
package org.apache.tomcat.util.qlog;

import java.io.Writer;
import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.Date;

import org.apache.tomcat.util.collections.SimplePool;
import org.apache.tomcat.util.collections.Queue;

/**
 * The daemon thread that looks in a queue and if it is not empty
 * writes out everything in the queue to the sink.
 */
public final class LogDaemon implements Runnable {
    private boolean shouldStop=false;
    private Thread  logDaemonThread = null;
    private Queue   logQueue  = null;
    
    public LogDaemon() {
    }

    public void start() {
	// already running
	if( logDaemonThread!=null) return;
	
	logQueue = new Queue();
	logDaemonThread=new Thread(this);
	logDaemonThread.setName("QueueLogDaemon");
	// Don't set it as daemon - we don't want tomcat to exit 
	//	    logDaemonThread.setDaemon(true);
	shouldStop=false;
	logDaemonThread.start();
    }


    
    public void stop() {
	if( shouldStop ) return;
	shouldStop=true;
	// wait for it to finish
	logQueue.stop(); // unblock
	logDaemonThread=null;
	logQueue=null;
    }

    public boolean isStarted() {
	return logDaemonThread!=null;
    }
    
    public void add(LogEntry logE ) {
	if( logQueue!=null ) {
	    logQueue.put( logE );
	} else {
	    // We're not started, do it synchronously
	    logE.getLogger().log( logE );
	}
    }
    
    public void run() {
	while (true) {
	    // Will block !
	    LogEntry logEntry =
		(LogEntry)logQueue.pull();
	    if( shouldStop ) return;
	    logEntry.getLogger().log( logEntry );
	    if( shouldStop ) return;
	}
    }
    
}

