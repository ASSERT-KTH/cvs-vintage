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

