/*
 * $Header: /tmp/cvs-vintage/struts/contrib/struts-chain/src/java/org/apache/struts/chain/servlet/PopulateActionForm.java,v 1.4 2004/02/02 13:53:21 germuska Exp $
 * $Revision: 1.4 $
 * $Date: 2004/02/02 13:53:21 $
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
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.chain.AbstractPopulateActionForm;
import org.apache.struts.config.ActionConfig;
import org.apache.struts.util.RequestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Populate the form bean (if any) for this request.  Sets the multipart
 * class from the action config in the request attributes.</p>
 *
 * @version $Revision: 1.4 $ $Date: 2004/02/02 13:53:21 $
 */

public class PopulateActionForm extends AbstractPopulateActionForm {


    private static final Log log = LogFactory.getLog(PopulateActionForm.class);

    // ------------------------------------------------------- Protected Methods


    protected void populate(Context context,
                         ActionConfig actionConfig,
                         ActionForm actionForm) throws Exception
    {
        ServletWebContext swcontext = (ServletWebContext) context;
        RequestUtils.populate(actionForm, actionConfig.getPrefix(), actionConfig.getSuffix(), swcontext.getRequest());
    }

    protected void reset(Context context,
                         ActionConfig actionConfig,
                         ActionForm actionForm) {

        ServletWebContext swcontext = (ServletWebContext) context;
        actionForm.reset((ActionMapping) actionConfig, swcontext.getRequest());

        // Set the multipart class
        if (actionConfig.getMultipartClass() != null) {
            swcontext.getRequestScope().put(Globals.MULTIPART_KEY,
                                 actionConfig.getMultipartClass());
        }

    }


}
