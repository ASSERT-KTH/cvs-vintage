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

import org.apache.tomcat.util.collections.SimplePool;

/**
 * A real implementation of the Logger abstraction.
 * It uses a log queue, so that the caller will not
 * have to wait.
 *
 * @author Anil V (akv@eng.sun.com)
 * @since  Tomcat 3.1
 */
public class QueueLogger extends Logger {
    // will be shared by all loggers
    private LogDaemon logDaemon = null;
    // one pool per QueueLogger
    private SimplePool pool=new SimplePool();

    public QueueLogger() {
    }

    public void setLogDaemon(LogDaemon ld ) {
	logDaemon=ld;
    }

    /**
     * Adds a log message and stack trace to the queue and returns
     * immediately. The logger daemon thread will pick it up later and
     * actually print it out.
     *
     * @param	message		the message to log.
     * @param	t		the exception that was thrown.
     * @param	verbosityLevel	what type of message is this?
     * 				(WARNING/DEBUG/INFO etc)
     */
    public void log(String prefix, String message, Throwable t,
		    int verbosityLevel)
    {
	//	System.out.println("XXXZ " + logDaemon +  " " + message  );
	if( ! logDaemon.isStarted() ) {
	    System.out.println("SUPER " + logDaemon +  " " + message  );
	    super.log( prefix, message, t , verbosityLevel );
	    return;
	}

	if (verbosityLevel <= getVerbosityLevel()) {
            // check wheter we are logging to a file
            if (path!= null){
                // If the date has changed, switch log files
                if (day!=getDay(System.currentTimeMillis())) {
                    synchronized (this) {
                        close();
                        open();
                    }
                }
            }

	    LogEntry entry=(LogEntry)pool.get();
	    if( entry == null ) {
		entry=new LogEntry(this);
	    }

	    if( timestamp ) {
		entry.setDate(System.currentTimeMillis());
	    } else {
		entry.setDate( 0 );
	    }
	    entry.setPrefix( prefix );
	    entry.setMessage( message );
	    entry.setThrowable( t );
	    logDaemon.add(entry);
	}
    }

    /** Flush the queue - in a separate thread, so that
	caller doesn't have to wait
    */
    public void flush() {
	// we need to wait for the log thread to finish, there is
	// nothing special we can do ( writing will interfere with the
	// log thread, which logs as soon as it gets an entry )
	//emptyQueue();
    }

    // 	Thread workerThread = new Thread(flusher);
    // 	workerThread.start();
    //     Runnable flusher = new Runnable() {
    // 	    public void run() {
    // 		QueueLogger.emptyQueue();
    // 	    }};

    private static final char[] NEWLINE=Logger.NEWLINE;
    // There is only one thread, so we can reuse this
    char outBuffer[]=new char[512]; // resize
    StringBuffer outSB = new StringBuffer();

    /** Produce output for a log entry, and then recycle it.
	This method is called from a single thread ( the log daemon )
     */
    void log(LogEntry logEntry) {
	if( logEntry==null ) {
	    System.out.println("Null log entry ");
	    return;
	}
	try {
	    outSB.setLength(0);

	    logEntry.print( outSB );
	    outSB.append( NEWLINE );

	    int len=outSB.length();
	    if( len > outBuffer.length ) {
		outBuffer=new char[len];
	    }
	    outSB.getChars(0, len, outBuffer, 0);
	    if (sink != null) {
		sink.write( outBuffer, 0, len );
		sink.flush();
		//System.out.print(sink + " "  + new String(outBuffer,0,len) );
	    } else {
		System.out.println("No writer ");
		System.out.print(new String(outBuffer,0,len) );
	    }
	} catch (Exception ex) { // IOException
	    ex.printStackTrace(); // nowhere else to write it
	}
	pool.put( logEntry );
    }


}

