/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * [Additional notices, if required by prior licensing conditions]
 *
 */
package org.apache.tomcat.modules.config;

import java.io.IOException;
import org.apache.tomcat.core.*;
import org.apache.tomcat.modules.aaa.*;
import org.apache.tomcat.modules.server.*;
import org.apache.tomcat.util.log.Log;
import org.apache.tomcat.util.io.*;

/** 
 * <code>PasswordPrompter</code> is a add-on module for the Tomcat 3.3
 * server. By installing this module, any secure connectors listed in the
 * <code>server.xml</code> configuration file can be defined without the
 * "keystorePass" attribute (where the password for the certificate
 * keystore is traditionally set). At startup, each secure connector without
 * a predefined password will result in a command-line prompt for the password.
 * This has the advantage of increased security, as the certificate password
 * need not be stored in the configuration files.
 *
 * Also, any JDBCRealm in <code>server.xml</code> defined without a
 * connectionPassword will result in a command-line prompt for the password. 
 *
 * <p>
 * <strong>Installation</strong>
 * <p>
 *
 * Place the PasswordPrompter.war file in Tomcat 3.3's modules directory
 * prior to starting Tomcat.
 * 
 * <p>
 * <strong>Notes</strong>
 * <p>
 *
 * This module does NOT attempt any additional validation on the supplied
 * password. It will simply pass the value on just as if it had been entered
 * into the <code>server.xml</code> file.
 *
 * Finally, this is primarily a simple example of an add-on module for Tomcat 3.3.
 * It tries to do something usefull as well, but leaves a fair amount of
 * room for improvement. It should not be considered a finished solution to
 * the security issue of protecting passwords.
 *
 * @author    Larry Isaacs
 * @author    Christopher Cain
 * @version   $Revision: 1.1 $ $Date: 2001/10/08 05:23:57 $
 */
public class PasswordPrompter extends BaseInterceptor {

    // ----------------------------------------------------------------- Fields

    /** The name of the module */
    public static final String MOD_NAME = "PasswordPrompter";

    /** The current version number */
    public static final String MOD_VERSION = "1.0";

    /** Number of lines to scroll to hide password */
    private int scroll = 25;

    // ------------------------------------------------------------ Constructor

    /** Default constructor. */
    public PasswordPrompter() {
    }

    // ------------------------------------------------------------- Attributes

    /** Set number of lines to scroll display to hide the password */
    public void setScroll( int lines ) {
        scroll = lines;
    }

    // ------------------------------------------------ Implementation (Public)

    /**
     * Returns the name of this module.
     *
     * @return   the module name
     */
    public static String getModName() {
        return MOD_NAME;
    }

    /**
     * Returns the current verion number of the module.
     *
     * @return   the current version number
     */
    public static String getModVersion() {
        return MOD_VERSION;
    }

    // -------------------------------------------------------------- Callbacks

    /**
     * This callback is automatically executed by the startup process each time
     * an interceptor is added to a context. For the purposes of this
     * particular module, processing is only done when this interceptor itself
     * is added.
     *
     * @param cm    the <code>ContextManager</code> for which the interceptor
     *              is being added
     * @param ctx   the <code>Context</code> for which the interceptor is being
     *              added
     * @param i     the interceptor being added to the Context
     */
    public void addInterceptor( ContextManager cm, Context ctx,
                                BaseInterceptor i )
    {
        // If executing config generation, don't prompt for passwords
        if ( cm.getProperty("jkconf") != null )
            return;

        if ( i == this ) {

            BaseInterceptor[] interceptors = cm.getContainer().getInterceptors(
                Container.H_engineInit
            );

            for ( int idx = 0; idx < interceptors.length; idx++ )
            {

                if ( interceptors[idx] instanceof PoolTcpConnector )
                {
                    // We have an Http connector, set certificate password if appropriate
                    processTcpConnector( (PoolTcpConnector)interceptors[idx] );
                }
                else if ( interceptors[idx] instanceof JDBCRealm )
                {
                    // We have a JDBC realm, set DB connection password if appropriate
                    processJDBCRealm( (JDBCRealm)interceptors[idx] );
                }
            }
        } else {
            if ( i instanceof PoolTcpConnector ) {
                processTcpConnector( (PoolTcpConnector)i );
            }
        }
    }

    private void processTcpConnector( PoolTcpConnector connector )
    {
        // We have an Http connector, check to see if it's secure
        if ( connector.isSecure() )
        {
            // It's secure, now check to see if the keystore pass was
            // already specified in the config file
            if (!connector.isKeypassSet()) {
                // insure log flushed
                Log logger = getLog();
                if (logger != null )
                    logger.flush();

                // Go ahead with the prompting
                String certpwd = null;
                try {
                    certpwd = Prompter.promptForInput(
                            "SSL socket detected, please enter " +
                            "the certificate password:", scroll );
                } catch (IOException ioe) {
                    log( "IO problem with command line: " + ioe.toString() );
                } catch (PrompterException pe) {
                    log( "Prompter problem: " + pe.toString() );
                }

                if ( certpwd != null )
                    connector.setKeypass(certpwd);
            }
        }
    }

    private void processJDBCRealm( JDBCRealm realm )
    {
        // Check to see if the connection password was
        // already specified in the config file
        if (!realm.isConnectionPasswordSet()) {
            // insure log flushed
            Log logger = getLog();
            if (logger != null )
                logger.flush();

            // Go ahead with the prompting
            String connpwd = null;
            try {
                connpwd = Prompter.promptForInput(
                        "JDBC Realm detected, please enter " +
       	                "the connection password:", scroll );
	    } catch (IOException ioe) {
	        log( "IO problem with command line: " + ioe.toString() );
	    } catch (PrompterException pe) {
	        log( "Prompter problem: " + pe.toString() );
	    }

	    if ( connpwd != null )
	        realm.setConnectionPassword(connpwd);
        }
    }
}
