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

package org.apache.tomcat.util.qlog;

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

