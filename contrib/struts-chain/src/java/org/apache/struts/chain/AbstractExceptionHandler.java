/*
 * $Header: /tmp/cvs-vintage/struts/contrib/struts-chain/src/java/org/apache/struts/chain/AbstractExceptionHandler.java,v 1.1 2003/08/31 21:53:00 craigmcc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/08/31 21:53:00 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Struts", and "Apache Software
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

package org.apache.struts.chain;


import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.chain.Constants;
import org.apache.struts.config.ActionConfig;
import org.apache.struts.config.ExceptionConfig;
import org.apache.struts.config.ForwardConfig;
import org.apache.struts.config.ModuleConfig;


/**
 * <p>Invoke the local or global exception handler configured for the
 * exception class that occurred.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2003/08/31 21:53:00 $
 */

public abstract class AbstractExceptionHandler implements Command {


    // ------------------------------------------------------ Instance Variables


    private String actionConfigKey = Constants.ACTION_CONFIG_KEY;
    private String exceptionKey = Constants.EXCEPTION_KEY;
    private String forwardConfigKey = Constants.FORWARD_CONFIG_KEY;
    private String moduleConfigKey = Constants.MODULE_CONFIG_KEY;

    private static final Log log =
        LogFactory.getLog(AbstractExceptionHandler.class);


    // -------------------------------------------------------------- Properties


    /**
     * <p>Return the context attribute key under which the
     * <code>ActionConfig</code> for the currently selected application
     * action is stored.</p>
     */
    public String getActionConfigKey() {

        return (this.actionConfigKey);

    }


    /**
     * <p>Set the context attribute key under which the
     * <code>ActionConfig</code> for the currently selected application
     * action is stored.</p>
     *
     * @param actionConfigKey The new context attribute key
     */
    public void setActionConfigKey(String actionConfigKey) {

        this.actionConfigKey = actionConfigKey;

    }


    /**
     * <p>Return the context attribute key under which any
     * thrown exception will be stored.</p>
     */
    public String getExceptionKey() {

        return (this.exceptionKey);

    }


    /**
     * <p>Set the context attribute key under which any
     * thrown exception will be stored.</p>
     *
     * @param exceptionKey The new context attribute key
     */
    public void setExceptionKey(String exceptionKey) {

        this.exceptionKey = exceptionKey;

    }


    /**
     * <p>Return the context attribute key under which the
     * <code>ForwardConfig</code> for the currently selected application
     * action is stored.</p>
     */
    public String getForwardConfigKey() {

        return (this.forwardConfigKey);

    }


    /**
     * <p>Set the context attribute key under which the
     * <code>ForwardConfig</code> for the currently selected application
     * action is stored.</p>
     *
     * @param forwardConfigKey The new context attribute key
     */
    public void setForwardConfigKey(String forwardConfigKey) {

        this.forwardConfigKey = forwardConfigKey;

    }


    /**
     * <p>Return the context attribute key under which the
     * <code>ModuleConfig</code> for the currently selected application
     * action is stored.</p>
     */
    public String getModuleConfigKey() {

        return (this.moduleConfigKey);

    }


    /**
     * <p>Set the context attribute key under which the
     * <code>ModuleConfig</code> for the currently selected application
     * action is stored.</p>
     *
     * @param moduleConfigKey The new context attribute key
     */
    public void setModuleConfigKey(String moduleConfigKey) {

        this.moduleConfigKey = moduleConfigKey;

    }


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Invoke the appropriate <code>Action</code> for this request, and cache
     * the returned <code>ActionForward</code>.</p>
     *
     * @param context The <code>Context</code> for the current request
     *
     * @exception InvalidPathException if no valid
     *  action can be identified for this request
     *
     * @return <code>false</code> if a <code>ForwardConfig</code> is returned,
     *  else <code>true</code> to complete processing
     */
    public boolean execute(Context context) throws Exception {

        // Look up the exception that was thrown
        Exception exception = (Exception)
            context.getAttributes().get(getExceptionKey());
        if (exception == null) {
            log.warn("No Exception found under key '" +
                     getExceptionKey() + "'");
            return (true);
        }

        // Look up the local or global exception handler configuration
        ExceptionConfig exceptionConfig = null;
        ActionConfig actionConfig = (ActionConfig)
            context.getAttributes().get(getActionConfigKey());
        ModuleConfig moduleConfig = (ModuleConfig)
            context.getAttributes().get(getModuleConfigKey());
        if (actionConfig != null) {
            exceptionConfig =
                actionConfig.findExceptionConfig(exception.getClass().getName());
        } else {
            exceptionConfig =
                moduleConfig.findExceptionConfig(exception.getClass().getName());
        }

        // Handle the exception in the configured manner
        if (exceptionConfig == null) {
            log.warn("Unhandled exception", exception);
            throw exception;
        }
        ForwardConfig forwardConfig =
            handle(context, exception, exceptionConfig,
                   actionConfig, moduleConfig);
        if (forwardConfig != null) {
            context.getAttributes().put(getForwardConfigKey(), forwardConfig);
            return (false);
        } else {
            return (true);
        }

    }


    // ------------------------------------------------------- Protected Methods


    /**
     * <p>Perform the required handling of the specified exception.</p>
     *
     * @param context The <code>Context</code> for this request
     * @param exception The exception being handled
     * @param exceptionConfig The corresponding {@link ExceptionConfig}
     * @param actionConfig The {@link ActionConfig} for this request
     * @param moduleConfig The {@link ModuleConfig} for this request
     *
     * @return the <code>ForwardConfig</code> to be processed next (if any),
     *  or <code>null</code> if processing has been completed
     */
    protected abstract ForwardConfig handle(Context context,
                                            Exception exception,
                                            ExceptionConfig exceptionConfig,
                                            ActionConfig actionConfig,
                                            ModuleConfig moduleConfig)
        throws Exception;


}
