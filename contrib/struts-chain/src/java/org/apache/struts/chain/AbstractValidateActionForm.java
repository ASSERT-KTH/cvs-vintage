/*
 * $Header: /tmp/cvs-vintage/struts/contrib/struts-chain/src/java/org/apache/struts/chain/AbstractValidateActionForm.java,v 1.2 2003/08/31 22:42:45 craigmcc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/08/31 22:42:45 $
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


import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ChainBase;
import org.apache.commons.chain.web.WebContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.chain.Constants;
import org.apache.struts.config.ActionConfig;


/**
 * <p>Validate the properties of the form bean for this request.  If there are
 * any validation errors, execute the specified command; otherwise,
 * proceed normally.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2003/08/31 22:42:45 $
 */

public abstract class AbstractValidateActionForm implements Command {


    // ------------------------------------------------------ Instance Variables


    private String actionConfigKey = Constants.ACTION_CONFIG_KEY;
    private String actionFormKey = Constants.ACTION_FORM_KEY;
    private String cancelKey = Constants.CANCEL_KEY;
    private String catalogKey = Constants.CATALOG_KEY;
    private String failureCommand = null;

    private static final Log log =
        LogFactory.getLog(AbstractValidateActionForm.class);


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
     * <p>Return the context attribute key under which the
     * <code>ActionForm</code> for the currently selected application
     * action is stored.</p>
     */
    public String getActionFormKey() {

        return (this.actionFormKey);

    }


    /**
     * <p>Set the context attribute key under which the
     * <code>ActionForm</code> for the currently selected application
     * action is stored.</p>
     *
     * @param actionFormKey The new context attribute key
     */
    public void setActionFormKey(String actionFormKey) {

        this.actionFormKey = actionFormKey;

    }


    /**
     * <p>Return the context attribute key under which the
     * cancellation flag for this request is stored.</p>
     */
    public String getCancelKey() {

        return (this.cancelKey);

    }


    /**
     * <p>Set the context attribute key under which the
     * cancellation flag for this request is stored.</p>
     *
     * @param cancelKey The new context attribute key
     */
    public void setCancelKey(String cancelKey) {

        this.cancelKey = cancelKey;

    }


    /**
     * <p>Return the context attribute key under which the
     * <code>Catalog</code> we perform lookups in is stored.</p>
     */
    public String getCatalogKey() {

        return (this.catalogKey);

    }


    /**
     * <p>Set the context attribute key under which the
     * <code>Catalog</code> we perform lookups in is stored.</p>
     *
     * @param catalogKey The new context attribute key
     */
    public void setCatalogKey(String catalogKey) {

        this.catalogKey = catalogKey;

    }


    /**
     * <p>Return the name of the command to be executed
     * if a validation failure occurs.</p>
     */
    public String getFailureCommand() {

        return (this.failureCommand);

    }


    /**
     * <p>Set the name of the command to be executed
     * if a validation failure occurs.</p>
     *
     * @param failureCommand The name of the chain to be executed
     */
    public void setFailureCommand(String failureCommand) {

        this.failureCommand = failureCommand;

    }


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Validate the properties of the form bean for this request.  If
     * there are any validation errors, execute the child commands in our
     * chain; otherwise, proceed normally.</p>
     *
     * @param context The <code>Context</code> for the current request
     *
     * @return <code>false</code> so that processing continues, if there are
     *  no validation errors; otherwise <code>true</code>
     */
    public boolean execute(Context context) throws Exception {

        // Is there a form bean for this request?
        ActionForm actionForm = (ActionForm)
            context.getAttributes().get(getActionFormKey());
        if (actionForm == null) {
            return (false);
        }

        // Was this request cancelled?
        Boolean cancel = (Boolean)
            context.getAttributes().get(getCancelKey());
        if ((cancel != null) && cancel.booleanValue()) {
            return (false);
        }

        // Is validation disabled on this request?
        ActionConfig actionConfig = (ActionConfig)
            context.getAttributes().get(getActionConfigKey());
        if (!actionConfig.getValidate()) {
            return (false);
        }

        // Call the validate() method of this form bean
        ActionErrors errors = validate(context, actionConfig, actionForm);

        // If there were no errors, proceed normally
        if ((errors == null) || (errors.isEmpty())) {
            return (false);
        }

        // Execute the specified validation failure command
        try {
            Catalog catalog = (Catalog)
                context.getAttributes().get(getCatalogKey());
            Command command = catalog.getCommand(getFailureCommand());
            if (log.isTraceEnabled()) {
                log.trace("Calling failure command '" + getFailureCommand()
                          + "'");
            }
            command.execute(context);
        } catch (Exception e) {
            log.warn("Exception from failure command '" +
                     getFailureCommand() + "'", e);
            throw new IllegalStateException("Failure chain threw exception");
        }
        return (true);

    }


    // ------------------------------------------------------- Protected Methods


    /**
     * <p>Call the <code>validate()</code> method of the specified form bean,
     * and return the resulting <code>ActionErrors</code> object.</p>
     *
     * @param context The context for this request
     * @param actionConfig The <code>ActionConfig</code> for this request
     * @param actionForm The form bean for this request
     */
    protected abstract ActionErrors validate(Context context,
                                             ActionConfig actionConfig,
                                             ActionForm actionForm);


}
