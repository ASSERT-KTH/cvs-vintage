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
package org.apache.tomcat.util.log;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;


/**
 * Corresponds to a log chanel - this is the main class
 * seen by objects that need to log. 
 * 
 * It has a preferred log name to write to; if
 * it can't find a log with that name, it outputs to the default
 * sink.  Also prepends a descriptive name to each message
 * (usually the toString() of the calling object), so it's easier
 * to identify the source.<p>
 *
 * Intended for use by client classes to make it easy to do
 * reliable, consistent logging behavior, even if you don't
 * necessarily have a context, or if you haven't registered any
 * log files yet, or if you're in a non-Tomcat application.  Not
 * intended to supplant Logger, but to allow client objects a
 * consistent bit of code that prepares log messages before they
 * reach logger (and does the right thing if there is no logger).
 * <p>
 * Usage: <pre>
 * class Foo {
 *   Log log = new Log("tc_log", "Foo"); // or...
 *   Log log = new Log("tc_log", this); // fills in "Foo" for you
 *   ...
 *     log.log("Something happened");
 *     ...
 *     log.log("Starting something", Logger.DEBUG);
 *     ...
 *     catch (IOException e) {
 *       log.log("While doing something", e);
 *     }
 * </pre>
 *
 * @author Alex Chaffee [alex@jguru.com]
 **/
public class Log {

    // name of the logger ( each logger has a unique name,
    // used as a key internally )
    private String logname;
    // string displayed at the beginning of each log line,
    // to identify the source
    private String prefix;

    // The real logger object ( that knows to write to
    // files, optimizations, etc)
    private Logger logger;

    // Do we need that? 
    //    private Log proxy;

    // -------------------- Various constructors --------------------

    public Log() {
    }

    /**
     * Subclass constructor, for classes that want to *be* a
     * LogHelper, and get the log methods for free (like a mixin)
     **/
    public Log(String logname) {
	this.logname = logname;
	String cname=this.getClass().getName();
	this.prefix = cname.substring( cname.lastIndexOf(".") +1);
    }
    
    /**
     * @param logname name of log to use
     * @param owner object whose class name to use as prefix
     **/
    public Log(String logname, Object owner) 
    {
	this.logname = logname;
	String cname = owner.getClass().getName();
	this.prefix = cname.substring( cname.lastIndexOf(".") +1);
    }	
    
    /**
     * @param logname name of log to use
     * @param prefix string to prepend to each message
     **/
    public Log(String logname, String prefix) 
    {
	this.logname = logname;
	this.prefix = prefix;
    }

    // -------------------- Log messages. --------------------
    // That all a client needs to know about logging !
    // --------------------
    
    /**
     * Logs the message with level INFORMATION
     **/
    public void log(String msg) 
    {
	log(msg, null, Logger.INFORMATION);
    }
    
    /**
     * Logs the Throwable with level ERROR (assumes an exception is
     * trouble; if it's not, use log(msg, t, level))
     **/
    public void log(String msg, Throwable t) 
    {
	log(msg, t, Logger.ERROR);
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
	if (prefix != null) {
	    // tuneme
	    msg = prefix + ": " + msg;
	}
	
	// 	    // activate proxy if present
	// 	    if (proxy != null)
	// 		logger = proxy.getLogger();
	
	// activate logname fetch if necessary
	if (logger == null) {
	    if (logname != null)
		logger = Logger.getLogger(logname);
	}
	
	// if all else fails, use default logger (writes to default sink)
	Logger loggerTemp = logger;
	if (loggerTemp == null) {
	    loggerTemp = Logger.defaultLogger;
	}
	loggerTemp.log(msg, t, level);
    }


    // -------------------- Extra configuration stuff --------------------

    
    public Logger getLogger() {
	// 	    if (proxy != null)
	// 		logger = proxy.getLogger();
	return logger;
    }
    
    /**
     * Set a logger explicitly.  Also resets the logname property to
     * match that of the given log.
     *
     * <p>(Note that setLogger(null) will not necessarily redirect log
     * output to System.out; if there is a logger named logname it
     * will fall back to using it, or trying to.)
     **/
    public void setLogger(Logger logger) {
	if (logger != null)
	    setLogname(logger.getName());
	this.logger = logger;
    }
    
    /**
     * Set the logger by name.  Will throw away current idea of what
     * its logger is, and next time it's asked, will locate the global
     * Logger object if the given name.
     **/	
    public void setLogname(String logname) {
	logger = null;	// prepare to locate a new logger
	this.logname = logname;
    }
    
    /**
     * Set the prefix string to be prepended to each message
     **/
    public void setLogPrefix(String prefix) {
	this.prefix = prefix;
    }
    
    // 	/**
    // 	 * Set a "proxy" Log -- whatever that one says its
    // 	 * Logger is, use it
    // 	 **/
    // 	public void setProxy(Log helper) {
    // 	    this.proxy = helper;
    // 	}
    
    
    // ???
    // 	public Log getLog() {
    // 	    return this;
    // 	}
    
    }    
