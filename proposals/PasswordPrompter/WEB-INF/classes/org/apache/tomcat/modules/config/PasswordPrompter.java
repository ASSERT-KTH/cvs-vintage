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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.io.Prompter;
import org.apache.tomcat.util.io.PrompterException;
import org.apache.tomcat.util.log.Log;

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
 * @version   $Revision: 1.4 $ $Date: 2003/09/29 07:39:49 $
 */
public class PasswordPrompter extends BaseInterceptor {

    // ----------------------------------------------------------------- Fields

    /** The name of the module */
    public static final String MOD_NAME = "PasswordPrompter";

    /** The current version number */
    public static final String MOD_VERSION = "1.0";

    /** Number of lines to scroll to hide password */
    private int scroll = 25;

    /** Delay (milliseconds) before prompting */
    private int delay = 100;

    private Vector prompts = new Vector();

    // ------------------------------------------------------------ Constructor

    /** Default constructor. */
    public PasswordPrompter() {
    }

    // ------------------------------------------------------------- Attributes

    /** Set number of lines to scroll display to hide the password */
    public void setScroll( int lines ) {
        scroll = lines;
    }

    /** Set delay (in milliseconds) prior to outputting prompt
        to give queue output to reach the console */
    public void setDelay( int delay ) {
        this.delay = delay;
    }

    /** Set a module prompt string */
    public void setProperty( String name, String value ) {
        if ( name.startsWith("prompt") ) {
            UserPrompt p = new UserPrompt( value );
            prompts.add(p);
            if ( debug > 0 ) {
                log("Prompt specification " + value + " added");
            }
        }
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

    /**
     * Perform the prompts if appropriate for any of the
     * specified interceptors.
     */
    public void doPrompts( Object[] modules, Context ctx ) {
        for ( int idx = 0; idx < prompts.size(); idx++ ) {
            UserPrompt p =
                    (UserPrompt)prompts.elementAt(idx);
            // perform this prompt for any applicable modules in the array
            p.prompt( modules, ctx, delay, scroll );
        }
    }

    // -------------------------------------------------------------- Callbacks

    /**
     * This callback is automatically executed by the startup process each time
     * an interceptor is added to a context.
     *
     * This method will experience two rounds of calls.  In the first round,
     * the ContextManager will be in the STATE_NEW state and this module will
     * have been loaded in a "trial" classloader.  In the second round,
     * the ContextManager will be in the STATE_CONFIG state and this
     * module will be loaded in its "final" classloader, which could be
     * configured differently from the "trial" classloader used in the
     * first round.
     *
     * In each round, the first call to this method will be with itself as
     * the module being added.  Any number of modules could have been previosly
     * added. This method will be called again when any additional modules
     * are added.
     *
     * The Prompting is performed the second round.
     *
     * @param cm    the <code>ContextManager</code> for which the interceptor
     *              is being added
     * @param ctx   the <code>Context</code> for which the interceptor is being
     *              added. Will be null for global modules.  For add-on modules
     *              it will always be null since they are always added as global
     *              modules.
     * @param i     the interceptor being added to the Context
     */
    public void addInterceptor( ContextManager cm, Context ctx,
                                BaseInterceptor i ) throws TomcatException
    {
        // If executing config generation, don't prompt for passwords
        if ( cm.getProperty("jkconf") != null ) {
            return;
        }

        // if time to prompt
        if ( cm.getState() != ContextManager.STATE_CONFIG ) {
            Object[] modules;

            // if adding ourselves (i.e. the first call )
            if ( i == this ) {
                // prepare all of the prompts
                for ( int idx = 0; idx < prompts.size(); idx++ )
                {
                    UserPrompt p = (UserPrompt)prompts.elementAt(idx);
                    p.prepare( (Hashtable)cm.getNote("modules"),
                                getLog() , debug );
                }

                // check all previously added modules
                modules =
                        cm.getContainer().getInterceptors();
                // do prompts on these modules
                doPrompts( modules, ctx );

                // check local context modules
                Enumeration enum = cm.getContexts();
                while (enum.hasMoreElements()) {
                    ctx = (Context)enum.nextElement();
                    modules = ctx.getContainer().getHooks().getModules();
                    if ( modules.length > 0 )
                        doPrompts( modules, ctx );
                }
            } else {
                // check just the module being added
                modules = new BaseInterceptor[] { i };
                // do prompts on this module
                doPrompts( modules, ctx );
            }

        }
    }

}

class UserPrompt
{
    boolean valid=false;
    private String spec;
    private String module;
    private String test;
    private String isSet;
    private String set;
    private String prompt;
    Log logger;
    int debug;

    private Class moduleClass;
    private UserPromptMethod testMethod;
    private UserPromptMethod isSetMethod;
    private UserPromptMethod setMethod;

    Class [] paramTypes1 = new Class[] { String.class };
    Class [] paramTypes2 = new Class[] { String.class, Object.class };

    public UserPrompt( String spec ) {
        if ( spec == null )
            return;

        this.spec = spec;
        // commented NO-OP 
        // this.logger = logger;
        StringTokenizer st = new StringTokenizer(spec,"|");
        try {
            module = st.nextToken();
            test = st.nextToken();
            isSet = st.nextToken();
            set = st.nextToken();
            prompt = st.nextToken();
            valid=true;
        } catch ( NoSuchElementException nsee ) {
            // leave as invalid
        }
    }

    public String getSpec() {
        return spec;
    }

    public boolean isValid() {
        return valid;
    }

    /** Prepare needed prompt fields.
     */
    public void prepare( Hashtable modules, Log logger, int debug ) {
        this.debug = debug;
        if ( valid && logger != null )
        {
            this.logger = logger;

            String className = module;
            // if no package, assume module name
            if ( module.indexOf(".") < 0 ) {
                String cn = (String)modules.get( module );
                if ( cn != null ) {
                    if ( debug > 0 )
                        logger.log("Translating module name "
                                + module + " to " + cn);
                    className = cn;
                }
            }
            try {
                moduleClass = Class.forName( className );
                testMethod = new UserPromptMethod( moduleClass, test, false );
                isSetMethod = new UserPromptMethod( moduleClass, isSet, false );
                setMethod = new UserPromptMethod( moduleClass, set, true );
                if ( debug > 0 ) {
                    logger.log("Prompt for " + module + " prepared");
                }
            } catch ( Exception ex ) {
                valid = false;
                logger.log( "Exception occurred while preparing UserPrompt."
                        + " Treating as invalid", ex );
            }
        }
    }

    /** Perform the prompt for any modules that match the module for this
     *  prompt instance.  For each match, if the "test" and "is already set"
     *  methods succeed, the prompt is displayed and the response is
     *  set on the module using the "set" method.
     */
    public void prompt( Object [] modules, Context ctx, int delay, int scroll ) {
        if ( !valid )
            return;

        for ( int i = 0; i < modules.length; i++ ) {
            BaseInterceptor module = (BaseInterceptor)modules[i];
            if ( !moduleClass.isInstance( module ) ) {
                continue;
            }
            if ( !testMethod.invokeBool( module, true ) ) {
                if ( debug > 0 ) {
                    logger.log("Module " + module + " failed test method "
                            + testMethod.getName());
                }
                continue;
            }
            if ( !isSetMethod.invokeBool( module, false ) ) {
                if ( debug > 0 ) {
                    logger.log("Module " + module + " attribute already "
                            + "set according to " + isSetMethod.getName() + " method");
                }
                continue;
            }

           // insure log flushed
           logger.flush();
           // delay in case using QueueLogger, which doesn't flush
           try {
               Thread.currentThread().sleep(delay);
           } catch ( InterruptedException ie ) {
               // ignore
           }

            // go ahead with the prompting
            String response = null;
            try {
                String temp = prompt;
                if ( ctx != null ) {
                    temp = "Context " + ctx.getName() + "\n" + prompt;
                }
                response = Prompter.promptForInput( temp, scroll );
                setMethod.invokeSet( module, response );
            } catch (IOException ioe) {
                logger.log( "IO problem with command line: " + ioe.toString() );
            } catch (PrompterException pe) {
                logger.log( "Prompter problem: " + pe.toString() );
            } catch ( InvocationTargetException ite ) {
                logger.log( "Error setting the response in the module: "
                        + ite.toString() );
            } catch ( IllegalAccessException iae ) {
                logger.log( "Problem setting the response in the module: "
                        + iae.toString() );
            }
        }
    }
}


class UserPromptMethod
{
    boolean skip=true;
    boolean not=false;
    Method method;
    String param;

    Class [] paramTypes1 = new Class[] { String.class };
    Class [] paramTypes2 = new Class[] { String.class, Object.class };

    public UserPromptMethod( Class moduleClass, String spec , boolean set )
            throws NoSuchMethodException {
        if ( moduleClass != null && !"always".equals( spec )  ) {

            if ( spec.startsWith("!") ) {
                not=true;
                spec = spec.substring(1);
            }

            int idx = spec.indexOf(":");
            if ( idx < 0 ) {
                method = moduleClass.getMethod( spec,
                        set ? paramTypes1 : null );
                param = null;
                skip = false;
            } else if ( idx > 0 ) {
                method = moduleClass.getMethod( spec.substring( 0, idx ),
                        set ? paramTypes2 : paramTypes1 );
                param = spec.substring( idx + 1);
                skip = false;
            } else {
                throw new NoSuchMethodException("Empty method specified");
            }
        }
    }

    public String getName() {
        if ( method != null ) {
            return method.getName();
        } else if ( skip ) {
            return "<skipped>";
        } else {
            return "<invalid>";
        }
    }

    /** Return success if boolean method returns the desired
     *  target (or !target).  If skipping, always return true.
     *  If error, always return false.
     */
    public boolean invokeBool( BaseInterceptor module, boolean target ) {
        if ( skip ) {
            return true;
        }

        Object [] params = null;
        if ( param != null ) {
            params = new Object [] { param };
        }

        try {
            Object result = method.invoke( module, params );
            if ( result instanceof Boolean ) {
                if ( not ) {
                    target = !target;
                }
                return ( ((Boolean)result).booleanValue() == target );
            } else {
                if ( not ) {
                    target = !target;
                }
                return !target;
            }
        } catch ( Exception ex ) {
            return false;
        }
    }

    /** Invoke setter method with specified value
     */
    public void invokeSet( BaseInterceptor module, String value )
            throws IllegalAccessException, InvocationTargetException {
        Object [] params;
        if ( param != null ) {
            params = new Object [] { param, value };
        } else {
            params = new Object [] { value };
        }

        method.invoke( module, params );
    }
}