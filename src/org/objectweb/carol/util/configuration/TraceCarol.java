/*
 * @(#) TraceCarol.java 
 *
 * Copyright (C) 2002 - INRIA (www.inria.fr)
 *
 * CAROL: Common Architecture for RMI ObjectWeb Layer
 *
 * This library is developed inside the ObjectWeb Consortium,
 * http://www.objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 * 
 * This file is inspired by the JOTM org.objectweb.jotm.TraceTm file
 * (http://www.objectweb.org/jotm)
 *
 */
package org.objectweb.carol.util.configuration;

import java.util.Properties;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.HandlerFactory;
import org.objectweb.util.monolog.api.LevelFactory;
import org.objectweb.util.monolog.api.Logger;
import org.objectweb.util.monolog.api.LoggerFactory;
import org.objectweb.util.monolog.file.monolog.PropertiesConfAccess;

/*
 * Class <code>TraceCarol</code> for Carol Trace configuration 
 */
public class TraceCarol {

    /** 
     * prefix used to identify CAROL loggers 
     */
    public static final String prefix = "org.objectweb.carol";

    /**
     * the carol logger
     * jndiCarol and rmiCarol logger are children of carol logger
     */
    protected static Logger carolLogger = null;  
    protected static Logger jndiCarolLogger = null;
    protected static Logger rmiCarolLogger = null;
    protected static Logger cmiCarolLogger = null;

    /**
    * Configure the log for CAROL.
    * Log configuration is stored in a property file, <code>trace.properties</code>,
    * which should be available from the classpath.
    */
    public static void configure() {
        Properties props = new Properties();
        try {
            props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("trace.properties"));
        } catch (Exception e) {
            System.err.println("Configuration file for log not found. Traces are disabled: " + e);
            return;
        }
        // Instanciate the LoggerFactory
        String lfClassName = props.getProperty("log.config.classname", null);
        if (lfClassName == null) {
            System.err.println("Malformed log configuration file: log.config.classname not available");
            return;
        }
        try {
            LoggerFactory lf = (LoggerFactory) Class.forName(lfClassName).newInstance();
            // Configure the LoggerFactory with the properties
            PropertiesConfAccess.load(props, lf, (HandlerFactory) lf, (LevelFactory) lf);
            TraceCarol.configure(lf);
        } catch (Exception e) {
            System.err.println("Logs are disabled:" + e);
        }
    }


    /**
    * Configure the log for CAROL
    * 
    * @param <code>lf</code> the LoggerFactory
    */
    public static void configure(LoggerFactory lf) {
        carolLogger = lf.getLogger(prefix);
        jndiCarolLogger = lf.getLogger(prefix + ".jndi");
        rmiCarolLogger = lf.getLogger(prefix + ".rmi");
        cmiCarolLogger = lf.getLogger(prefix + ".cmi");
    }


    /**
     * Log a verbose message
     * 
     * @param <code>msg</code> verbose message
     */
    public static void verbose(String msg) {
        if (carolLogger != null) {
            carolLogger.log(BasicLevel.INFO, msg);
        }
    }

    /**
     * Log an error message.
     * 
     * @param <code>msg </code> error message
     */
    public static void error(String msg) {
        if (carolLogger != null) {
            carolLogger.log(BasicLevel.ERROR, msg);
        }
    }

    /**
     * Log an error message and a stack trace from a Throwable object.
     * 
     * @param <code>msg</code> error message
     * @param <code>th</code> Throwable object
     */
    public static void error(String msg, Throwable th) {
        if (carolLogger != null) {
            carolLogger.log(BasicLevel.ERROR, msg, th);
        }
    }

    /**
     * Test if Carol debug messages are logged.
     * 
     * @return boolean  <code>true</code> if Carol debug messages are logged,
     * <code>false</code> otherwise
     */
    static public boolean isDebugCarol() {
        return (carolLogger != null) && carolLogger.isLoggable(BasicLevel.DEBUG);
    }

    /**
     * Log a Carol debug message.
     * 
     * @param <code>msg</code> CAROL debug message
     */
    public static void debugCarol(String msg) {
        if (carolLogger != null) {
            carolLogger.log(BasicLevel.DEBUG, msg);
        }
    }


   /**
     * Test if Jndi debug messages are logged.
     * 
     * @return boolean  <code>true</code> if Jndi debug messages are logged,
     * <code>false</code> otherwise
     */
    static public boolean isDebugJndiCarol() {
        return (jndiCarolLogger != null) && jndiCarolLogger.isLoggable(BasicLevel.DEBUG);
    }

    /**
     * Log a Jndi debug message.
     * 
     * @param <code>msg</code> Jndi debug message
     */
    public static void debugJndiCarol(String msg) {
        if (jndiCarolLogger != null) {
            jndiCarolLogger.log(BasicLevel.DEBUG, msg);
        }
    }

    /**
     * Test if Rmi debug messages are logged.
     *
     * @return boolean  <code>true</code> if Rmi debug messages are logged,
     * <code>false</code> otherwise
     */
    static public boolean isDebugRmiCarol() {
        return (rmiCarolLogger != null) && rmiCarolLogger.isLoggable(BasicLevel.DEBUG);
    }

    /**
     * Log a Rmi debug message.
     *
     * @param <code>msg</code> Rmi debug message
     */
    public static void debugRmiCarol(String msg) {
        if (rmiCarolLogger != null) {
            rmiCarolLogger.log(BasicLevel.DEBUG, msg);
        }
    }

    /**
     * Test if Cmi info messages are logged.
     *
     * @return boolean  <code>true</code> if Cmi info messages are logged,
     * <code>false</code> otherwise
     */
    static public boolean isInfoCmiCarol() {
        return (cmiCarolLogger != null) && cmiCarolLogger.isLoggable(BasicLevel.INFO);
    }

    /**
     * Log a Cmi info message.
     *
     * @param <code>msg</code> Cmi info message
     */
    public static void infoCmiCarol(String msg) {
        if (cmiCarolLogger != null) {
            cmiCarolLogger.log(BasicLevel.INFO, msg);
        }
    }
}
