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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.tomcat.util.log.Log;
import org.apache.tomcat.util.log.LogHandler;

/**
 * Interface for a logging object. A logging object provides mechanism
 * for logging errors and messages that are of interest to someone who
 * is trying to monitor the system.
 * 
 * @author Anil Vijendran (akv@eng.sun.com)
 * @author Alex Chaffee (alex@jguru.com)
 * @author Ignacio J. Ortega (nacho@siapi.es)
 * @since  Tomcat 3.1
 */
public abstract class Logger extends LogHandler {
    // -------------------- Internal fields --------------------

    protected static PrintWriter defaultSink =
	new PrintWriter( new OutputStreamWriter(System.err));

    protected long day;
    
    // Usefull for subclasses
    private static final String separator =
	System.getProperty("line.separator", "\n");
    public static final char[] NEWLINE=separator.toCharArray();

    /**
     * Set the default output stream that is used by all logging
     * channels.
     *
     * @param	w		the default output stream.
     */
    public static void setDefaultSink(Writer w) {
	if( w!=null )
	    defaultSink = new PrintWriter(w);
    }


    // ----- instance (non-static) content -----
    
    protected boolean custom = true;
    //    protected Writer sink = defaultSink;
    protected String path;
    
    /**
     * Should we timestamp this log at all?
     **/
    protected boolean timestamp = true;

    /**
     * true = The timestamp format is raw msec-since-epoch <br>
     * false = The timestamp format is a custom string to pass to
     *         SimpleDateFormat
     **/
    protected boolean timestampRaw = false;

    /**
     * The timestamp format string, default is "yyyy-MM-dd HH:mm:ss"
     **/
    protected String timestampFormat = "yyyy-MM-dd HH:mm:ss";

    protected DateFormat timestampFormatter
	= new FastDateFormat(new SimpleDateFormat(timestampFormat));


    /**
     * Set the path name for the log output file.
     *
     * @param	path		The path to the log file.
     */
    public void setPath(String path) {
        if (File.separatorChar == '/')
            this.path = path.replace('\\', '/');
        else if (File.separatorChar == '\\')
            this.path = path.replace('/', '\\');
    }

    public String getPath() {
	return path;
    }

    /** Open the log - will create the log file and all the parent directories.
     *  You must open the logger before use, or it will write to System.err
     */
    public void open() {
	if (path == null)
            return;
	// use default sink == System.err
        long date=System.currentTimeMillis();
        day=getDay(date);
	try {
	    File file = new File(path);
            String logName=file.getParent()+File.separator+
		getDatePrefix(date,file.getName());
            file=new File(logName);
	    if (!file.exists())
		new File(file.getParent()).mkdirs();
	    this.sink = new PrintWriter( new FileWriter(logName), true);
	} catch (IOException ex) {
	    System.err.print("Unable to open log file: "+path+"! ");
	    System.err.println(" Using stderr as the default.");
	    this.sink = defaultSink;
	}
    }

    

    /**
     * Set the verbosity level for this logger. This controls how the
     * logs will be filtered. 
     *
     * @param	level		one of the verbosity level strings. 
     */
    public void setVerbosityLevel(String level) {
	if ("warning".equalsIgnoreCase(level))
	    this.level = Log.WARNING;
	else if ("fatal".equalsIgnoreCase(level))
	    this.level = Log.FATAL;
	else if ("error".equalsIgnoreCase(level))
	    this.level = Log.ERROR;
	else if ("information".equalsIgnoreCase(level))
	    this.level = Log.INFORMATION;
	else if ("debug".equalsIgnoreCase(level))
	    this.level = Log.DEBUG;
    }

    /**
     * Set the verbosity level for this logger. This controls how the
     * logs will be filtered. 
     *
     * @param	level		one of the verbosity level codes. 
     */
    public void setVerbosityLevel(int level) {
	this.level = level;
    }
    
    /**
     * Get the current verbosity level.
     */
    public int getVerbosityLevel() {
	return this.level;
    }

    /**
     * Get the current verbosity level.
     */
    public int getLevel() {
	return this.level;
    }

    /**
     * Do we need to time stamp this or not?
     *
     * @param	value		"yes/no" or "true/false"
     */
    public void setTimestamp(String value) {
	if ("true".equalsIgnoreCase(value) ||
	    "yes".equalsIgnoreCase(value))
	    timestamp = true;
	else if ("false".equalsIgnoreCase(value) ||
		 "no".equalsIgnoreCase(value))
	    timestamp = false;
    }

    public  boolean isTimestamp() {
	return timestamp;
    }

    /**
     * If we are timestamping at all, what format do we use to print
     * the timestamp? See java.text.SimpleDateFormat.
     *
     * Default = "yyyy-MM-dd hh:mm:ss". Special case: "msec" => raw
     * number of msec since epoch, very efficient but not
     * user-friendly
     **/
    public void setTimestampFormat(String value)
    {
	if (value.equalsIgnoreCase("msec"))
	    timestampRaw = true;
	else {
	    timestampRaw = false;
	    timestampFormat = value;
	    timestampFormatter =
		new FastDateFormat(new SimpleDateFormat(timestampFormat));
	}
    }
    
    public String getTimestampFormat()
    {
	if (timestampRaw)
	    return "msec";
	else
	    return timestampFormat;
    }

    protected String formatTimestamp(long msec) {
	StringBuffer buf = new StringBuffer();
	formatTimestamp(msec, buf);
	return buf.toString();
    }

    // dummy variable to make SimpleDateFormat work right
    private static java.text.FieldPosition position =
	new java.text.FieldPosition(DateFormat.YEAR_FIELD);

    protected void formatTimestamp(long msec, StringBuffer buf) {
	if (!timestamp)
	    return;
	else if (timestampRaw) {
	    buf.append(Long.toString(msec));
	    return;
	}
	else {
	    Date d = new Date(msec);
	    timestampFormatter.format(d, buf, position);
	    return;
	}
    }

    // ----- utility methods; -----
    static final String START_FORMAT="${";
    static final String END_FORMAT="}";

    protected String getDatePrefix(long millis,String format) {
        try{
            int pos=format.indexOf(Logger.START_FORMAT);
            int lpos=format.lastIndexOf(Logger.END_FORMAT);
            if( pos != -1 && lpos != -1){
                String f="'"+format.substring(0,pos)+"'"
                        +format.substring(pos+2,lpos)
                        +"'"+format.substring(lpos+1)+"'";
                SimpleDateFormat sdf=new SimpleDateFormat(f);
                return sdf.format(new Date(millis));
            }
        }catch(Exception ex){
        }
        return format;
    }

    protected long getDay(long millis){
        return (millis+TimeZone.getDefault().getRawOffset()) /
	    ( 24*60*60*1000 );
    }

    // -------------------- Public Utilities --------------------
    
    /**
     * Converts a Throwable to a printable stack trace, including the
     * nested root cause for a ServletException or TomcatException if
     * applicable
     * TODO: JDBCException too
     *
     * @param t any Throwable, or ServletException, or null
     **/
    public static String throwableToString( Throwable t ) {
	// we could use a StringManager here to get the
	// localized translation of "Root cause:" , but
	// since it's going into a log, no user will see
	// it, and it's desirable that the log file is
	// predictable, so just use English
	return throwableToString( t, "Root cause:" );
    }

    public static final int MAX_THROWABLE_DEPTH=3;

    /**
     * Converts a Throwable to a printable stack trace, including the
     * nested root cause for a ServletException or TomcatException or
     * SQLException if applicable
     *
     * @param t any Throwable, or ServletException, or null
     * @param rootcause localized string equivalent of "Root Cause"
     **/
    public static String throwableToString( Throwable t, String rootcause ) {
	if (rootcause == null)
	    rootcause = "Root Cause:";
	StringWriter sw = new StringWriter();
	PrintWriter w = new PrintWriter(sw);
	printThrowable(w, t, rootcause, MAX_THROWABLE_DEPTH);
	w.flush();
	return sw.toString();
    }

    private static Object[] emptyObjectArray=new Object[0];

    private static void printThrowable(PrintWriter w, Throwable t,
				       String rootcause, int depth )
    {

	if (t != null) {
	    // XXX XXX XXX Something seems wrong - DOS, permissions. Need to
	    // check.
	    t.printStackTrace(w);

	    // Find chained exception using few general patterns
	    
	    Class tC=t.getClass();
	    Method mA[]= tC.getMethods();
	    Method nextThrowableMethod=null;
	    for( int i=0; i< mA.length ; i++  ) {
		if( "getRootCause".equals( mA[i].getName() )
		    || "getNextException".equals( mA[i].getName() )
		    || "getException".equals( mA[i].getName() )) {
		    // check param types
		    Class params[]=mA[i].getParameterTypes();
		    if( params==null || params.length==0 ) {
			nextThrowableMethod=mA[i];
			break;
		    }
		}
	    }

	    if( nextThrowableMethod != null ) {
		try {
		    Throwable nextT=(Throwable)nextThrowableMethod.
			invoke( t , emptyObjectArray );
		    if( nextT != null ) {
			w.println(rootcause);
			if( depth > 0 ) {
			    printThrowable(w, nextT, rootcause, depth-1);
			}
		    }
		} catch( Exception ex ) {
		    // ignore
		}
	    }
	}
    }
    
//     /**
//      * General purpose nasty hack to determine if an exception can be
//      * safely ignored -- specifically, if it's an IOException or
//      * SocketException that is thrown in the normal course of a socket
//      * closing halfway through a connection, or if it's a weird
//      * unknown type of exception.  This is an intractable problem, and
//      * this is a bad solution, but at least it's centralized.
//      **/
//     public static boolean canIgnore(Throwable t) {
// 	String msg = t.getMessage();
// 	if (t instanceof java.io.InterruptedIOException) {
// 	    return true;
// 	}
// 	else if (t instanceof java.io.IOException) {
// 	    // Streams throw Broken Pipe exceptions if their
// 	    // underlying sockets close
// 	    if( "Broken pipe".equals(msg))
// 		return true;
// 	}
// 	else if (t instanceof java.net.SocketException) {
// 	    // TCP stacks can throw SocketExceptions when the client
// 	    // disconnects.  We don't want this to shut down the
// 	    // endpoint, so ignore it. Is there a more robust
// 	    // solution?  Should we compare the message string to
// 	    // "Connection reset by peer"?
// 	    return true;
// 	}
// 	return false;
//     }

}
