/*
 * Copyright 2002,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.struts.faces.systest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;


/**
 * <p>Action to process logon attempts.  It accepts only the combination
 * "gooduser" and "goodpass", while rejecting all others.  Successful
 * login requests a logical forward "login1", while unsuccessful login
 * returns to the input form.</p>
 */

public class LogonAction extends Action {


    private static final Log log = LogFactory.getLog(LogonAction.class);


    /**
     * <p>Process an attempted logon.</p>
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws Exception {

        ActionErrors errors = new ActionErrors();
        String username = (String)
            PropertyUtils.getSimpleProperty(form, "username");
        if ((username == null) || ("".equals(username))) {
            errors.add("username",
                         new ActionError("logon.username"));
        }
        String password = (String)
            PropertyUtils.getSimpleProperty(form, "password");
        if ((password == null) || ("".equals(password))) {
            errors.add("password",
                         new ActionError("logon.password"));
        }
        if (log.isTraceEnabled()) {
            log.trace("username=" + username + ",password=" + password);
        }
        if (errors.isEmpty() &&
            (!"gooduser".equals(username) || !"goodpass".equals(password))) {
            errors.add(ActionErrors.GLOBAL_ERROR,
                         new ActionError("logon.mismatch"));
        }
        if (errors.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Successful logon, forwarding to logon1");
            }
            return (mapping.findForward("logon1"));
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Unsuccessful logon, returning to input");
            }
            saveErrors(request, errors);
            return (mapping.getInputForward());
        }



    }


}
