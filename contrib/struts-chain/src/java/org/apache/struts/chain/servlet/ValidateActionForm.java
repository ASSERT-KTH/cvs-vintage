/*
 * $Header: /tmp/cvs-vintage/struts/contrib/struts-chain/src/java/org/apache/struts/chain/servlet/ValidateActionForm.java,v 1.1 2003/08/11 04:55:34 craigmcc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/08/11 04:55:34 $
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

package org.apache.struts.chain.servlet;


import org.apache.commons.chain.Context;
import org.apache.commons.chain.web.servlet.ServletWebContext;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.chain.AbstractValidateActionForm;
import org.apache.struts.chain.Constants;
import org.apache.struts.config.ActionConfig;
import org.apache.struts.config.ForwardConfig;
import org.apache.struts.config.ModuleConfig;


/**
 * <p>Validate the properties of the form bean for this request.  If there are
 * any validation errors, execute the child commands in our chain; otherwise,
 * proceed normally.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2003/08/11 04:55:34 $
 */

public class ValidateActionForm extends AbstractValidateActionForm {


    // ------------------------------------------------------- Protected Methods


    /**
     * <p>Create and return a <code>ForwardConfig</code> representing the
     * specified module-relative destination.</p>
     *
     * @param context The context for this request
     * @param moduleConfig The <code>ModuleConfig</code> for this request
     * @param uri The module-relative URI to be the destination
     */
    protected ForwardConfig forward(Context context,
                                    ModuleConfig moduleConfig,
                                    String uri) {

        return (new ActionForward(null, moduleConfig.getPrefix() + uri,
                                  false, true));

    }


    /**
     * <p>Call the <code>validate()</code> method of the specified form bean,
     * and return the resulting <code>ActionErrors</code> object.</p>
     *
     * @param context The context for this request
     * @param actionForm The form bean for this request
     */
    protected ActionErrors validate(Context context,
                                    ActionConfig actionConfig,
                                    ActionForm actionForm) {

        ServletWebContext swcontext = (ServletWebContext) context;
        return (actionForm.validate((ActionMapping) actionConfig,
                                    swcontext.getRequest()));

    }


}
