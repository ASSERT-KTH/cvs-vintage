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



/**
 * This is the main class seen by objects that need to log. 
 * 
 * It has a log channel to write to; if it can't find a log with that name,
 * it outputs to the default
 * sink.  Also prepends a descriptive name to each message
 * (usually the toString() of the calling object), so it's easier
 * to identify the source.<p>
 *
 * Intended for use by client classes to make it easy to do
 * reliable, consistent logging behavior, even if you don't
 * necessarily have a context, or if you haven't registered any
 * log files yet, or if you're in a non-Tomcat application.
 * <p>
 * Usage: <pre>
 * class Foo {
 *   Log log = Log.getLog("tc_log", "Foo"); // or...
 *   Log log = Log.getLog("tc_log", this); // fills in "Foo" for you
 *   ...
 *     log.log("Something happened");
 *     ...
 *     log.log("Starting something", Log.DEBUG);
 *     ...
 *     catch (IOException e) {
 *       log.log("While doing something", e);
 *     }
 * </pre>
 * 
 *  As a special feature ( required in tomcat operation ) the
 *  Log can be modified at run-time, by changing and configuring the logging
 *  implementation ( without requiring any special change in the user code ).
 *  That means that the  user can do a Log.getLog() at any time,
 *  even before the logging system is fully configured. The
 *  embeding application can then set up or change the logging details,
 *  without any action from the log user.
 *
 * @deprecated Commons-logging should be used instead.
 * @author Alex Chaffee [alex@jguru.com]
 * @author Costin Manolache
 **/
public class Log implements org.apache.commons.logging.Log {

    /**
     * Verbosity level codes.
     */
    public static final int FATAL = Integer.MIN_VALUE;
    public static final int ERROR = 1;
    public static final int WARNING = 2;
    public static final int INFORMATION = 3;
    public static final int DEBUG = 4;
    

    // name of the logger ( each logger has a unique name,
    // used as a key internally )
    protected String logname;

    // string displayed at the beginning of each log line,
    // to identify the source
    protected String prefix;

    /* The "real" logger. This allows the manager to change the
       sink/logger at runtime, and without requiring the log
       user to do any special action or be aware of the changes
    */
    private LogHandler proxy; // the default

    // Used to get access to other logging channels.
    // Can be replaced with an application-specific impl.
    private static LogManager logManager=new LogManager();


    // -------------------- Various constructors --------------------

    protected Log(String channel, String prefix, LogHandler proxy, Object owner) {
	this.logname=channel;
	this.prefix=prefix;
	this.proxy=proxy;
    }

    /**
     * @param logname name of log to use
     * @param owner object whose class name to use as prefix
     **/
    public static Log getLog( String channel, String prefix ) {
	return logManager.getLog( channel, prefix, null );
    }
    
    /**
     * @param logname name of log to use
     * @param prefix string to prepend to each message
     **/
    public static Log getLog( String channel, Object owner ) {
	return logManager.getLog( channel, null, owner );
    }
    
    // -------------------- Log messages. --------------------
    
    /**
     * Logs the message with level INFORMATION
     **/
    public void log(String msg) 
    {
	log(msg, null, INFORMATION);
    }
    
    /**
     * Logs the Throwable with level ERROR (assumes an exception is
     * trouble; if it's not, use log(msg, t, level))
     **/
    public void log(String msg, Throwable t) 
    {
	log(msg, t, ERROR);
    }
    
    /**
     * Logs the message with given level
     **/
    public void log(String msg, int level) 
    {
	log(msg, null, level);
    }
    
    /**
     * Logs the message and Throwable to its logger or, if logger
     * not found, to the default logger, which writes to the
     * default sink, which is usually System.err
     **/
    public void log(String msg, Throwable t, int level)
    {
	log( prefix, msg, t, level );
    }

    /** 
     */
    public void log( String prefix, String msg, Throwable t, int level ) {
	proxy.log( prefix, msg, t, level );
    }
    
    /** Flush any buffers.
     *  Override if needed.
     */
    public void flush() {
	proxy.flush();
    }

    public void close() {
	proxy.close();
    }

    /** The configured logging level for this channel
     */
    public int getLevel() {
	return proxy.getLevel();
    }

    // -------------------- Management --------------------

    // No getter for the log manager ( user code shouldn't be
    // able to control the logger )
    
    /** Used by the embeding application ( tomcat ) to manage
     *  the logging.
     *
     *  Initially, the Log is not managed, and the default
     *  manager is used. The application can find what loggers
     *  have been created from the default LogManager, and 
     *  provide a special manager implemetation.
     */
    public static LogManager setLogManager( LogManager lm ) {
	// can be changed only once - so that user
	// code can't change the log manager in running servers
	if( logManager.getClass() == LogManager.class ) {
	    LogManager oldLM=logManager;
	    logManager=lm;
	    return oldLM;
	}
	return null;
    }

    public String  getChannel( LogManager lm ) {
	if( lm != logManager ) return null;
	return logname;
    }

    public void setProxy( LogManager lm, LogHandler l ) {
	// only the manager can change the proxy
	if( lm!= logManager ) {
	    System.out.println("Attempt to change proxy " + lm + " " + logManager);
	    return;
	}
	proxy=l;
    }


    // -------------------- Common-logging impl --------------------

    
    // -------------------- Log implementation -------------------- 

    public void debug(Object message) {
        log(message.toString(), null, DEBUG);
    }

    public void debug(Object message, Throwable exception) {
        log(message.toString(), exception, DEBUG);
    }

    public void error(Object message) {
        log(message.toString(), null, ERROR);
    }

    public void error(Object message, Throwable exception) {
        log(message.toString(), exception, ERROR);
    }

    public void fatal(Object message) {
        log(message.toString(), null, FATAL);
    }

    public void fatal(Object message, Throwable exception) {
        log(message.toString(), exception, FATAL);
    }

    public void info(Object message) {
        log(message.toString(), null, INFORMATION);
    }

    public void info(Object message, Throwable exception) {
        log(message.toString(), exception, INFORMATION);
    }
    public void trace(Object message) {
        log(message.toString(), null, DEBUG);
    }
    public void trace(Object message, Throwable exception) {
        log(message.toString(), exception, DEBUG);
    }
    public void warn(Object message) {
        log(message.toString(), null, WARNING);
    }
    public void warn(Object message, Throwable exception) {
        log(message.toString(), exception, WARNING);
    }

    public boolean isDebugEnabled() {
        return proxy.getLevel() <= DEBUG;
    }
    public boolean isErrorEnabled() {
        return proxy.getLevel() <= ERROR;
    }
    public boolean isFatalEnabled() {
        return proxy.getLevel() <= FATAL;
    }
    public boolean isInfoEnabled() {
        return proxy.getLevel() <= INFORMATION;
    }
    public boolean isTraceEnabled() {
        return proxy.getLevel() <= DEBUG;
    }
    public boolean isWarnEnabled() {
        return proxy.getLevel() <= WARNING;
    }
    
    /** Security notes:

    Log acts as a facade to an actual logger ( which has setters, etc).
    
    The "manager" ( embeding application ) can set the handler, and
    edit/modify all the properties at run time.

    Applications can log if they get a Log instance. From Log there is
    no way to get an instance of the LogHandler or LogManager.

    LogManager controls access to the Log channels - a 1.2 implementation
    would check for LogPermissions ( or other mechanisms - like
    information about the current thread, etc ) to determine if the
    code can get a Log.
    
    The "managing application" ( tomcat for example ) can control
    any aspect of the logging and change the actual logging
    implementation behind the scenes.

    One typical usage is that various components will use Log.getLog()
    at various moments. The "manager" ( tomcat ) will initialize the
    logging system by setting a LogManager implementation ( which
    can't be changed by user code after that ). It can then set the
    actual implementation of the logger at any time. ( or provide
    access to trusted code to it ).

    Please review.
    */
    
    
}    
