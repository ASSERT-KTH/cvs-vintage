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

package org.apache.tomcat.util.log;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;


/**
 * Log destination ( or channel ). This is the base class that will be
 * extended by log handlers - tomcat uses util.qlog.QueueLogger,
 * in future we'll use log4j or java.util.logger adapters.
 *
 * The base class writes to a (default) writer, and it can
 * be used for very simple logging.
 * 
 * @author Anil Vijendran (akv@eng.sun.com)
 * @author Alex Chaffee (alex@jguru.com)
 * @author Ignacio J. Ortega (nacho@siapi.es)
 * @author Costin Manolache
 */
public  class LogHandler {

    protected PrintWriter sink = defaultSink;
    protected int level = Log.INFORMATION;

    
    /**
     * Prints log message and stack trace.
     * This method should be overriden by real logger implementations
     *
     * @param	prefix		optional prefix. 
     * @param	message		the message to log. 
     * @param	t		the exception that was thrown.
     * @param	verbosityLevel	what type of message is this?
     * 				(WARNING/DEBUG/INFO etc)
     */
    public void log(String prefix, String msg, Throwable t,
		    int verbosityLevel)
    {
	if( sink==null ) return;
	// default implementation ( in case no real logging is set up  )
	if( verbosityLevel > this.level ) return;
	
	if (prefix != null) 
	    sink.println(prefix + ": " + msg );
	else 
	    sink.println(  msg );
	
	if( t!=null )
	    t.printStackTrace( sink );
    }

    /**
     * Flush the log. 
     */
    public void flush() {
	if( sink!=null)
	    sink.flush();
    }


    /**
     * Close the log. 
     */
    public synchronized void close() {
	this.sink = null;
    }
    
    /**
     * Set the verbosity level for this logger. This controls how the
     * logs will be filtered. 
     *
     * @param	level		one of the verbosity level codes. 
     */
    public void setLevel(int level) {
	this.level = level;
    }
    
    /**
     * Get the current verbosity level.
     */
    public int getLevel() {
	return this.level;
    }


    // -------------------- Default sink
    
    protected static PrintWriter defaultSink =
	new PrintWriter( new OutputStreamWriter(System.err), true);

    /**
     * Set the default output stream that is used by all logging
     * channels.
     *
     * @param	w		the default output stream.
     */
    public static void setDefaultSink(Writer w) {
	if( w instanceof PrintWriter )
	    defaultSink=(PrintWriter)w;
	else 
	    defaultSink = new PrintWriter(w);
    }

    // -------------------- General purpose utilitiy --------------------

    

}
