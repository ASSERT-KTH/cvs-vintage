/*
 * $Header: /tmp/cvs-vintage/struts/src/example/org/apache/struts/webapp/example/SaveRegistrationAction.java,v 1.3 2001/07/16 00:44:51 craigmcc Exp $
 * $Revision: 1.3 $
 * $Date: 2001/07/16 00:44:51 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
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


package org.apache.struts.webapp.example;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Hashtable;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.util.MessageResources;


/**
 * Implementation of <strong>Action</strong> that validates and creates or
 * updates the user registration information entered by the user.  If a new
 * registration is created, the user is also implicitly logged on.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2001/07/16 00:44:51 $
 */

public final class SaveRegistrationAction extends Action {


    // --------------------------------------------------------- Public Methods


    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param actionForm The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public ActionForward perform(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response)
	throws IOException, ServletException {

	// Extract attributes and parameters we will need
	Locale locale = getLocale(request);
	MessageResources messages = getResources();
	HttpSession session = request.getSession();
	RegistrationForm regform = (RegistrationForm) form;
	String action = request.getParameter("action");
	if (action == null)
	    action = "Create";
	Hashtable database = (Hashtable)
	  servlet.getServletContext().getAttribute(Constants.DATABASE_KEY);
        if (servlet.getDebug() >= 1)
            servlet.log("SaveRegistrationAction:  Processing " + action +
                        " action");

	// Is there a currently logged on user (unless creating)?
	User user = (User) session.getAttribute(Constants.USER_KEY);
	if (!"Create".equals(action) && (user == null)) {
            if (servlet.getDebug() >= 1)
                servlet.log(" User is not logged on in session "
                            + session.getId());
	    return (servlet.findForward("logon"));
        }

	// Was this transaction cancelled?
	if (isCancelled(request)) {
	    if (servlet.getDebug() >= 1)
	        servlet.log(" Transaction '" + action +
	                    "' was cancelled");
	    if (mapping.getAttribute() != null)
	        session.removeAttribute(mapping.getAttribute());
	    session.removeAttribute(Constants.SUBSCRIPTION_KEY);
	    return (mapping.findForward("success"));
	}

        // Validate the transactional control token
	ActionErrors errors = new ActionErrors();
        if (servlet.getDebug() >= 1) {
            servlet.log(" Checking transactional control token");
        }
        if (!isTokenValid(request))
            errors.add(ActionErrors.GLOBAL_ERROR,
                       new ActionError("error.transaction.token"));
        resetToken(request);

	// Validate the request parameters specified by the user
        if (servlet.getDebug() >= 1)
            servlet.log(" Performing extra validations");
	String value = null;
	value = regform.getUsername();
	if (("Create".equals(action)) &&
	    (database.get(value) != null))
            errors.add("username",
                       new ActionError("error.username.unique",
                                       regform.getUsername()));
	if ("Create".equals(action)) {
	    value = regform.getPassword();
	    if ((value == null) || (value.length() <1))
                errors.add("password",
                           new ActionError("error.password.required"));
	    value = regform.getPassword2();
	    if ((value == null) || (value.length() < 1))
                errors.add("password2",
                           new ActionError("error.password2.required"));
	}

	// Report any errors we have discovered back to the original form
	if (!errors.empty()) {
	    saveErrors(request, errors);
            saveToken(request);
	    return (new ActionForward(mapping.getInput()));
	}

	// Update the user's persistent profile information
	if ("Create".equals(action)) {
	    user = new User();
	    user.setUsername(regform.getUsername());
	}
        try {
            String oldPassword = user.getPassword();
            PropertyUtils.copyProperties(user, regform);
            if ((regform.getPassword() == null) ||
                (regform.getPassword().length() < 1))
                user.setPassword(oldPassword);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t == null)
                t = e;
            servlet.log("Registration.populate", t);
            throw new ServletException("Registration.populate", t);
        } catch (Throwable t) {
            servlet.log("Registration.populate", t);
            throw new ServletException("Subscription.populate", t);
        }


        // Log the user in if appropriate
	if ("Create".equals(action)) {
	    database.put(user.getUsername(), user);
	    session.setAttribute(Constants.USER_KEY, user);
	    if (servlet.getDebug() >= 1)
		servlet.log(" User '" + user.getUsername() +
	                    "' logged on in session " + session.getId());
	}

	// Remove the obsolete form bean
	if (mapping.getAttribute() != null) {
            if ("request".equals(mapping.getScope()))
                request.removeAttribute(mapping.getAttribute());
            else
                session.removeAttribute(mapping.getAttribute());
        }

	// Forward control to the specified success URI
        if (servlet.getDebug() >= 1)
            servlet.log(" Forwarding to success page");
	return (mapping.findForward("success"));

    }


}
