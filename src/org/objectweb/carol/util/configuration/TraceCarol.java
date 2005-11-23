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
 * $Id: TraceCarol.java,v 1.10 2005/11/23 21:35:39 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class <code> TraceCarol </code> for Carol Trace configuration
 */
public class TraceCarol {

    /**
     * prefix used to identify CAROL loggers
     */
    public static final String PREFIX = "org.objectweb.carol";

    /**
     * the carol logger jndiCarol and rmiCarol logger are children of carol
     * logger
     */
    private static Log carolLogger = null;

    /**
     * Logger for PREFIX + ".jndi"
     */
    private static Log jndiCarolLogger = null;

    /**
     * Logger for PREFIX + ".jndi.enc"
     */
    private static Log jndiEncCarolLogger = null;

    /**
     * Logger for PREFIX + ".rmi"
     */
    private static Log rmiCarolLogger = null;

    /**
     * Logger for PREFIX + ".rmi.export"
     */
    private static Log exportCarolLogger = null;

    /**
     * Utility class, no constructor
     */
    private TraceCarol() {

    }

    /**
     * Configure the log for CAROL.
     */
    public static void configure() {
        carolLogger = LogFactory.getLog(PREFIX);
        jndiCarolLogger = LogFactory.getLog(PREFIX + ".jndi");
        jndiEncCarolLogger = LogFactory.getLog(PREFIX + ".jndi.enc");
        rmiCarolLogger = LogFactory.getLog(PREFIX + ".rmi");
        exportCarolLogger = LogFactory.getLog(PREFIX + ".rmi.export");
    }

    /**
     * Log a verbose message
     * @param msg verbose message
     */
    public static void verbose(String msg) {
        if (carolLogger != null) {
            carolLogger.info(msg);
        } else {
            System.out.println("CAROL Verbose message:" + msg);
        }
    }

    /**
     * Log an error message.
     * @param msg error message
     */
    public static void error(String msg) {
        if (carolLogger != null) {
            carolLogger.error(msg);
        } else {
            System.err.println("CAROL Error:" + msg);
        }
    }

    /**
     * Log an error message and a stack trace from a Throwable object.
     * @param msg error message
     * @param th Throwable object
     */
    public static void error(String msg, Throwable th) {
        if (carolLogger != null) {
            carolLogger.error(msg, th);
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
     public static boolean isDebugCarol() {
        return (carolLogger != null) && carolLogger.isDebugEnabled();
    }

    /**
     * Log a Carol debug message.
     * @param msg CAROL debug message
     */
    public static void debugCarol(String msg) {
        if (carolLogger != null) {
            carolLogger.debug(msg);
        }
    }

    /**
     * Test if Carol info messages are logged.
     * @return boolean <code>true</code> if Carol debug messages are logged,
     *         <code>false</code> otherwise
     */
     public static boolean isInfoCarol() {
        return (carolLogger != null) && carolLogger.isInfoEnabled();
    }

    /**
     * Log a Carol Info message.
     * @param msg CAROL debug message
     */
    public static void infoCarol(String msg) {
        if (carolLogger != null) {
            carolLogger.info(msg);
        }
    }

    /**
     * Test if Jndi debug messages are logged.
     * @return boolean <code>true</code> if Jndi debug messages are logged,
     *         <code>false</code> otherwise
     */
     public static boolean isDebugJndiCarol() {
        return (jndiCarolLogger != null) && jndiCarolLogger.isDebugEnabled();
    }

    /**
     * Log a Jndi debug message.
     * @param msg Jndi debug message
     */
    public static void debugJndiCarol(String msg) {
        if (jndiCarolLogger != null) {
            jndiCarolLogger.debug(msg);
        }
    }

    /**
     * Test if Jndi ENC debug messages are logged.
     * @return boolean <code>true</code> if Jndi debug messages are logged,
     *         <code>false</code> otherwise
     */
     public static boolean isDebugjndiEncCarol() {
        return (jndiEncCarolLogger != null) && jndiEncCarolLogger.isDebugEnabled();
    }

    /**
     * Log a Jndi ENC debug message.
     * @param msg Jndi debug message
     */
    public static void debugjndiEncCarol(String msg) {
        if (jndiEncCarolLogger != null) {
            jndiEncCarolLogger.debug(msg);
        }
    }


    /**
     * Test if Rmi debug messages are logged.
     * @return boolean <code>true</code> if Rmi debug messages are logged,
     *         <code>false</code> otherwise
     */
     public static boolean isDebugRmiCarol() {
        return (rmiCarolLogger != null) && rmiCarolLogger.isDebugEnabled();
    }

    /**
     * Log a Rmi debug message.
     * @param msg Rmi debug message
     */
    public static void debugRmiCarol(String msg) {
        if (rmiCarolLogger != null) {
            rmiCarolLogger.debug(msg);
        }
    }

    /**
     * @return boolean true is is debug export
     */
    public static boolean isDebugExportCarol() {
        return (exportCarolLogger != null) && exportCarolLogger.isDebugEnabled();
    }

    /**
     * Debug export
     * @param msg string
     */
    public static void debugExportCarol(String msg) {
        if (exportCarolLogger != null) {
            exportCarolLogger.debug(msg);
        }
    }
}