/**
 * Copyright (C) 2002,2004 - INRIA (www.inria.fr)
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
 * --------------------------------------------------------------------------
 * $Id: TraceCarol.java,v 1.7 2004/09/01 11:02:41 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.configuration;

import java.util.Properties;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.HandlerFactory;
import org.objectweb.util.monolog.api.LevelFactory;
import org.objectweb.util.monolog.api.Logger;
import org.objectweb.util.monolog.api.LoggerFactory;
import org.objectweb.util.monolog.file.monolog.PropertiesConfAccess;

/**
 * Class <code> TraceCarol </code> for Carol Trace configuration
 */
public class TraceCarol {

    /**
     * prefix used to identify CAROL loggers
     */
    public static final String prefix = "org.objectweb.carol";

    /**
     * the carol logger jndiCarol and rmiCarol logger are children of carol
     * logger
     */
    protected static Logger carolLogger = null;

    protected static Logger jndiCarolLogger = null;

    protected static Logger rmiCarolLogger = null;

    protected static Logger exportCarolLogger = null;

    protected static Logger cmiDesLogger = null;

    protected static Logger cmiJndiLogger = null;

    protected static Logger cmiRegistryLogger = null;

    /**
     * Configure the log for CAROL. Log configuration is stored in a property
     * file, <code>trace.properties</code>, which should be available from
     * the classpath.
     */
    public static void configure() {
        if (carolLogger == null) {
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
    }

    /**
     * Configure the log for CAROL
     * @param <code>lf</code> the LoggerFactory
     */
    public static void configure(LoggerFactory lf) {
        carolLogger = lf.getLogger(prefix);
        jndiCarolLogger = lf.getLogger(prefix + ".jndi");
        rmiCarolLogger = lf.getLogger(prefix + ".rmi");
        exportCarolLogger = lf.getLogger(prefix + ".rmi.export");
        cmiDesLogger = lf.getLogger(prefix + ".cmi.des");
        cmiJndiLogger = lf.getLogger(prefix + ".cmi.jndi");
        cmiRegistryLogger = lf.getLogger(prefix + ".cmi.registry");
    }

    /**
     * Log a verbose message
     * @param <code>msg</code> verbose message
     */
    public static void verbose(String msg) {
        if (carolLogger != null) {
            carolLogger.log(BasicLevel.INFO, msg);
        } else {
            System.out.println("CAROL Verbose message:" + msg);
        }
    }

    /**
     * Log an error message.
     * @param <code>msg </code> error message
     */
    public static void error(String msg) {
        if (carolLogger != null) {
            carolLogger.log(BasicLevel.ERROR, msg);
        } else {
            System.err.println("CAROL Error:" + msg);
        }
    }

    /**
     * Log an error message and a stack trace from a Throwable object.
     * @param <code>msg</code> error message
     * @param <code>th</code> Throwable object
     */
    public static void error(String msg, Throwable th) {
        if (carolLogger != null) {
            carolLogger.log(BasicLevel.ERROR, msg, th);
        } else {
            System.err.println("CAROL Error:" + msg);
            th.printStackTrace();
        }
    }

    /**
     * Test if Carol debug messages are logged.
     * @return boolean <code>true</code> if Carol debug messages are logged,
     *         <code>false</code> otherwise
     */
    static public boolean isDebugCarol() {
        return (carolLogger != null) && carolLogger.isLoggable(BasicLevel.DEBUG);
    }

    /**
     * Log a Carol debug message.
     * @param <code>msg</code> CAROL debug message
     */
    public static void debugCarol(String msg) {
        if (carolLogger != null) {
            carolLogger.log(BasicLevel.DEBUG, msg);
        }
    }

    /**
     * Test if Carol info messages are logged.
     * @return boolean <code>true</code> if Carol debug messages are logged,
     *         <code>false</code> otherwise
     */
    static public boolean isInfoCarol() {
        return (carolLogger != null) && carolLogger.isLoggable(BasicLevel.INFO);
    }

    /**
     * Log a Carol Info message.
     * @param <code>msg</code> CAROL debug message
     */
    public static void infoCarol(String msg) {
        if (carolLogger != null) {
            carolLogger.log(BasicLevel.INFO, msg);
        }
    }

    /**
     * Test if Jndi debug messages are logged.
     * @return boolean <code>true</code> if Jndi debug messages are logged,
     *         <code>false</code> otherwise
     */
    static public boolean isDebugJndiCarol() {
        return (jndiCarolLogger != null) && jndiCarolLogger.isLoggable(BasicLevel.DEBUG);
    }

    /**
     * Log a Jndi debug message.
     * @param <code>msg</code> Jndi debug message
     */
    public static void debugJndiCarol(String msg) {
        if (jndiCarolLogger != null) {
            jndiCarolLogger.log(BasicLevel.DEBUG, msg);
        }
    }

    /**
     * Test if Rmi debug messages are logged.
     * @return boolean <code>true</code> if Rmi debug messages are logged,
     *         <code>false</code> otherwise
     */
    static public boolean isDebugRmiCarol() {
        return (rmiCarolLogger != null) && rmiCarolLogger.isLoggable(BasicLevel.DEBUG);
    }

    /**
     * Log a Rmi debug message.
     * @param <code>msg</code> Rmi debug message
     */
    public static void debugRmiCarol(String msg) {
        if (rmiCarolLogger != null) {
            rmiCarolLogger.log(BasicLevel.DEBUG, msg);
        }
    }

    /**
     * @return boolean true is is debug export
     */
    public static boolean isDebugExportCarol() {
        return (exportCarolLogger != null) && exportCarolLogger.isLoggable(BasicLevel.DEBUG);
    }

    /**
     * @param string
     */
    public static void debugExportCarol(String msg) {
        if (exportCarolLogger != null) {
            exportCarolLogger.log(BasicLevel.DEBUG, msg);
        }
    }

    /**
     * Test if Cmi DES debug messages are logged.
     * @return boolean <code>true</code> if Cmi DES debug messages are logged,
     *         <code>false</code> otherwise
     */
    static public boolean isDebugCmiDes() {
        return (cmiDesLogger != null) && cmiDesLogger.isLoggable(BasicLevel.DEBUG);
    }

    /**
     * Log a Cmi DES debug message.
     * @param <code>msg</code> Cmi DES debug message
     */
    public static void debugCmiDes(String msg) {
        if (cmiDesLogger != null) {
            cmiDesLogger.log(BasicLevel.DEBUG, msg);
        }
    }

    /**
     * Test if Cmi JNDI debug messages are logged.
     * @return boolean <code>true</code> if Cmi JNDI debug messages are
     *         logged, <code>false</code> otherwise
     */
    static public boolean isDebugCmiJndi() {
        return (cmiJndiLogger != null) && cmiJndiLogger.isLoggable(BasicLevel.DEBUG);
    }

    /**
     * Log a Cmi JNDI debug message.
     * @param <code>msg</code> Cmi JNDI debug message
     */
    public static void debugCmiJndi(String msg) {
        if (cmiJndiLogger != null) {
            cmiJndiLogger.log(BasicLevel.DEBUG, msg);
        }
    }

    /**
     * Test if Cmi registry debug messages are logged.
     * @return boolean <code>true</code> if Cmi registry debug messages are
     *         logged, <code>false</code> otherwise
     */
    static public boolean isDebugCmiRegistry() {
        return (cmiRegistryLogger != null) && cmiRegistryLogger.isLoggable(BasicLevel.DEBUG);
    }

    /**
     * Log a Cmi registry debug message.
     * @param <code>msg</code> Cmi registry debug message
     */
    public static void debugCmiRegistry(String msg) {
        if (cmiRegistryLogger != null) {
            cmiRegistryLogger.log(BasicLevel.DEBUG, msg);
        }
    }

    /**
     * Test if Cmi info messages are logged.
     * @return boolean <code>true</code> if Cmi info messages are logged,
     *         <code>false</code> otherwise
     */
    /*
     * static public boolean isInfoCmiCarol() { return (cmiCarolLogger != null) &&
     * cmiCarolLogger.isLoggable(BasicLevel.INFO); }
     */
    /**
     * Log a Cmi info message.
     * @param <code>msg</code> Cmi info message
     */
    /*
     * public static void infoCmiCarol(String msg) { if (cmiCarolLogger != null) {
     * cmiCarolLogger.log(BasicLevel.INFO, msg); } }
     */
}