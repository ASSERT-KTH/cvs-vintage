/*
 * $Header: /tmp/cvs-vintage/struts/src/example/org/apache/struts/example/Attic/EditSubscriptionAction.java,v 1.3 2000/06/16 07:12:16 craigmcc Exp $
 * $Revision: 1.3 $
 * $Date: 2000/06/16 07:12:16 $
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
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.util.MessageResources;


/**
 * Implementation of <strong>Action</strong> that populates an instance of
 * <code>SubscriptionForm</code> from the currently specified subscription.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2000/06/16 07:12:16 $
 */

public final class EditSubscriptionAction extends ActionBase {


    // --------------------------------------------------------- Public Methods


    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
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
    public ActionForward perform(ActionServlet servlet,
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
	String host = request.getParameter("host");

	// Is there a currently logged on user?
	User user = (User) session.getAttribute(Constants.USER_KEY);
	if (user == null) {
	    if (servlet.getDebug() >= 1)
	        servlet.log("EditSubscriptionAction: User is not logged on in session "
	                    + session.getId());
	    return (mapping.findForward("logon"));
	}

	// Identify the relevant subscription
	Subscription subscription = null;
	if (action.equals("Create")) {
	    subscription = new Subscription();
	    subscription.setUser(user);
	} else {
	    subscription = user.findSubscription(host);
	}
	if (subscription == null) {
	    if (servlet.getDebug() >= 1)
		servlet.log("EditSubscriptionAction: No subscription for user " +
			    user.getUsername() + " and host " + host);
	    return (mapping.findForward("failure"));
	}
	session.setAttribute(Constants.SUBSCRIPTION_KEY, subscription);

	// Populate the subscription form
	if (form == null) {
	    form = new SubscriptionForm();
	    session.setAttribute(mapping.getFormAttribute(), form);
	}
	SubscriptionForm subform = (SubscriptionForm) form;
	subform.setAction(action);
	subform.setHost(subscription.getHost());
	subform.setUsername(subscription.getUsername());
	subform.setPassword(subscription.getPassword());
	subform.setType(subscription.getType());

	// Forward control to the edit subscription page
	return (mapping.findForward("success"));

    }


}
