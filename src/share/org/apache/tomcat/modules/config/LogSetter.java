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

package org.apache.tomcat.modules.config;

import java.io.File;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.io.FileUtil;
import org.apache.tomcat.util.log.Log;
import org.apache.tomcat.util.log.LogManager;
import org.apache.tomcat.util.qlog.LogDaemon;
import org.apache.tomcat.util.qlog.QueueLogger;

/*
  Logging in Tomcat is quite flexible; we can either have a log
  file per module (example: ContextManager) or we can have one
  for Servlets and one for Jasper, or we can just have one
  tomcat.log for both Servlet and Jasper.  Right now there are
  three standard log streams, "tc_log", "servlet_log", and
  "JASPER_LOG".  
  
  Path: 
  
  The file to which to output this log, relative to
  TOMCAT_HOME.  If you omit a "path" value, then stderr or
  stdout will be used.
  
  Verbosity: 
  
  Threshold for which types of messages are displayed in the
  log.  Levels are inclusive; that is, "WARNING" level displays
  any log message marked as warning, error, or fatal.  Default
  level is WARNING.  Note: servlet_log must be level
  INFORMATION in order to see normal servlet log messages.
  
  verbosityLevel values can be: 
  FATAL
  ERROR
  WARNING 
  INFORMATION
  DEBUG

  Timestamps:
  
  By default, logs print a timestamp in the form "yyyy-MM-dd
  hh:mm:ss" in front of each message.  To disable timestamps
  completely, set 'timestamp="no"'. To use the raw
  msec-since-epoch, which is more efficient, set
  'timestampFormat="msec"'.  If you want a custom format, you
  can use 'timestampFormat="hh:mm:ss"' following the syntax of
  java.text.SimpleDateFormat (see Javadoc API).  For a
  production environment, we recommend turning timestamps off,
  or setting the format to "msec".
  
  Custom Output:
  
  "Custom" means "normal looking".  "Non-custom" means
  "surrounded with funny xml tags".  In preparation for
  possibly disposing of "custom" altogether, now the default is
  'custom="yes"' (i.e. no tags)
  
  Per-component Debugging:
  
  Some components accept a "debug" attribute.  This further
  enhances log output.  If you set the "debug" level for a
  component, it may output extra debugging information.
*/


/**
 *  Define a logger with the specified name, using the logger
 *  implementation in org.apache.tomcat.util.log.QueueLogger
 *
 *  Tomcat uses the util.log.Log class - if you want to use
 *  a different logger ( like log4j or jsrXXX ) you need to create a
 *  new interceptor that will use your favorite logger and
 *  create a small adapter ( class extending Log and directing
 *  the output to your favorite logger.
 *
 *  The only contract used in tomcat for logging is the util.Log.
 * 
 */
public class LogSetter extends  BaseInterceptor {
    String name;
    String path;
    String verbosityLevel="INFORMATION";
    boolean servletLogger=false;
    boolean timestamps=true;
    String tsFormat=null;

    QueueLogger ql;
    
    public LogSetter() {
    }

    /** Set the name of the logger.
     *  Predefined names are: tc_log, servlet_log, JASPER_LOG.
     */
    public void setName( String s ) {
	name=s;
    }

    public void setPath( String s ) {
	path=s;
    }

    public void setVerbosityLevel( String s ) {
	verbosityLevel=s;
    }

    /** This logger will be used for servlet's log.
     *  ( if not set, the logger will output tomcat messages )
     */
    public void setServletLogger( boolean b ) {
	servletLogger=b;
    }

    /** Display the time of the event ( log ).
     */
    public void setTimestamps( boolean b ) {
	timestamps=b;
    }

    /** Set the format of the timestamp.
	"msec" will display the raw time ( fastest ),
	otherwise a SimpleDateFormat.
    */
    public void setTimestampFormat( String s ) {
	tsFormat=s;
    }
    
    /**
     *  The log will be added and opened as soon as the module is
     *  added to the server
     */
    public void addInterceptor(ContextManager cm, Context ctx,
			       BaseInterceptor module)
	throws TomcatException
    {
	if( module!=this ) return;

	LogManager logManager=(LogManager)cm.getNote("tc.LogManager");
	
	// Log will redirect all Log.getLog to us
	if( logManager==null ) {
	    logManager=new TomcatLogManager();
	    cm.setNote("tc.LogManager", logManager);
	    Log.setLogManager( logManager );
	}

	LogDaemon logDaemon=(LogDaemon)cm.getNote("tc.LogDaemon");
	if( logDaemon==null ) {
	    logDaemon=new LogDaemon();
	    cm.setNote( "tc.LogDaemon", logDaemon );
	    logDaemon.start();
	}
	
	if( name==null ) {
	    if( servletLogger )
		name="org/apache/tomcat/facade";
	    else
		name="org/apache/tomcat/core";
	}

	if( path!=null && ! FileUtil.isAbsolute( path ) ) {
	    File wd= new File(cm.getHome(), path);
	    path= wd.getAbsolutePath();
	}
	
	// workarounds for legacy log names
	if( "tc_log".equals( name ) )
	    name="org/apache/tomcat/core";
	if( servletLogger || "servlet_log".equals( name ) )
	    name="org/apache/tomcat/facade";

	if( ctx != null ) {
	    // this logger is local to a context
	    name=name +  "/"  + ctx.getId();
	}

	createLogger(logManager, logDaemon );
	
    }

    public void engineInit( ContextManager cm )
	throws TomcatException
    {
	// make sure it's started
	LogDaemon logDaemon=(LogDaemon)cm.getNote("tc.LogDaemon");
	logDaemon.start();
    }

    public void engineShutdown(ContextManager cm)
	throws TomcatException
    {
	if( getContext() != null )
	    return;

	if( debug > 0 ) log( "Stopping the logger " + name);
	cm.getLog().flush();
	if( ql!=null ) ql.flush();
	// engineShutdown shouldn't be called on local modules anyway !

	LogDaemon logDaemon=(LogDaemon)cm.getNote("tc.LogDaemon");
	if( logDaemon!=null ) {
	    try{ 
		logDaemon.stop();
	    } catch( Exception ex ) {
		ex.printStackTrace();
	    }
	    //	    cm.setNote( "tc.LogDaemon", null );
	}

    }



    
    /** Set default ServletLog for Context if necessary
     */

    public void addContext( ContextManager cm, Context ctx )
	throws TomcatException
    {
	if( "org/apache/tomcat/facade".equals( name ) &&
		    ctx.getServletLog() == null ) {
	    ctx.setServletLog( Log.getLog( name, ctx.getId() ) );
	}
    }

    /** Adapter and registry for QueueLoggers
     */
    static class TomcatLogManager extends LogManager {
	TomcatLogManager() {
	    // can't be changed after this
	    LogManager olm=Log.setLogManager( this ); 
	    this.loggers=olm.getLoggers();
	    this.channels=olm.getChannels();
	}

    }

    private void createLogger(LogManager logManager, LogDaemon logDaemon) {
	
	if( debug>0) 
	    log( "Constructing logger " + name + " " + path + " " + ctx );
	
	ql=new QueueLogger();
	ql.setLogDaemon( logDaemon );
	if( ! timestamps )
	    ql.setTimestamp( "false" );
	if( tsFormat!=null )
	    ql.setTimestampFormat( tsFormat );
	
	if( path!=null )
	    ql.setPath(path);
	if( verbosityLevel!= null )
	    ql.setVerbosityLevel(verbosityLevel);

	ql.open();

	logManager.addChannel( name, ql );

	if( "org/apache/tomcat/core".equals( name ) ) {
	    // this will be the Log interface to the log we just created
	    // ( the way logs and channels are created is a bit
	    // complicated - work for later )
	    cm.setLog( Log.getLog( name, "ContextManager"));
	}

	if( ctx!=null ) {
	    if( servletLogger ) {
		ctx.setServletLog( Log.getLog( name, ctx.getId() ) );
	    } else {
		ctx.setLog( Log.getLog( name, ctx.getId() ) );
	    }
	}  
    }
}
