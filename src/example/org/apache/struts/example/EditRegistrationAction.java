/*
 * $Header: /tmp/cvs-vintage/struts/src/example/org/apache/struts/example/Attic/EditRegistrationAction.java,v 1.2 2000/06/16 01:32:21 craigmcc Exp $
 * $Revision: 1.2 $
 * $Date: 2000/06/16 01:32:21 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
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
 */


package org.apache.struts.example;


import java.io.IOException;
import java.util.Locale;
import java.util.Vector;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionBase;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.util.MessageResources;


/**
 * Implementation of <strong>Action</strong> that populates an instance of
 * <code>RegistrationForm</code> from the profile of the currently logged on
 * User (if any).
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2000/06/16 01:32:21 $
 */

public final class EditRegistrationAction extends ActionBase {


    // --------------------------------------------------------- Public Methods


    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     *
     * @param servlet The ActionServlet making this request
     * @param mapping The ActionMapping used to select this instance
     * @param actionForm The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public void perform(ActionServlet servlet,
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response)
	throws IOException, ServletException {


	// Extract attributes we will need
	Locale locale = getLocale(request);
	MessageResources messages = getResources(servlet);
	HttpSession session = request.getSession();
	String action = request.getParameter("action");
	if (action == null)
	    action = "Create";

	// Is there a currently logged on user?
	User user = null;
	if (!"Create".equals(action)) {
	    user = (User) session.getAttribute(Constants.USER_KEY);
	    if (user == null) {
		if (servlet.getDebug() >= 1)
		    servlet.log("EditRegistrationAction: User is not logged on in session "
	                        + session.getId());
	        String uri = Constants.LOGON_PAGE;
	        RequestDispatcher rd =
	          servlet.getServletContext().getRequestDispatcher(uri);
	        rd.forward(request, response);
	        return;
	    }
	}

	// Populate the user registration form
	if (form == null) {
	    form = new RegistrationForm();
	    session.setAttribute(mapping.getFormAttribute(), form);
	}
	RegistrationForm regform = (RegistrationForm) form;
	regform.setAction(action);
	if (user != null) {
	    regform.setUsername(user.getUsername());
	    regform.setPassword(null);
	    regform.setPassword2(null);
	    regform.setFullName(user.getFullName());
	    regform.setFromAddress(user.getFromAddress());
	    regform.setReplyToAddress(user.getReplyToAddress());
	}

	// Forward control to the edit user registration page
	String uri = ((ApplicationMapping) mapping).getSuccess();
	if (servlet.getDebug() >= 1)
	    servlet.log("EditRegistrationAction:  Forwarding to '" + uri + "'");
	RequestDispatcher rd =
	  servlet.getServletContext().getRequestDispatcher(uri);
	rd.forward(request, response);

    }


}
