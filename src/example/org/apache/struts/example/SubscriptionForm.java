/*
 * $Header: /tmp/cvs-vintage/struts/src/example/org/apache/struts/example/Attic/SubscriptionForm.java,v 1.3 2000/06/20 16:33:52 craigmcc Exp $
 * $Revision: 1.3 $
 * $Date: 2000/06/20 16:33:52 $
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


import java.util.Vector;
import org.apache.struts.action.ValidatingActionForm;
import org.apache.struts.util.ErrorMessages;


/**
 * Form bean for the user profile page.  This form has the following fields,
 * with default values in square brackets:
 * <ul>
 * <li><b>action</b> - The maintenance action we are performing (Create, Delete,
 *     or Edit).
 * <li><b>host</b> - The mail host for this subscription.  [REQUIRED]
 * <li><b>password</b> - The password for this subscription.  [REQUIRED]
 * <li><b>submit</b> - The submit button that was pressed.
 * <li><b>type</b> - The subscription type (imap,pop3)
       for this subscription.  [REQUIRED]
 * <li><b>username</b> - The username of this subscription.  [REQUIRED]
 * </ul>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2000/06/20 16:33:52 $
 */

public final class SubscriptionForm implements ValidatingActionForm  {


    // --------------------------------------------------- Instance Variables


    /**
     * The maintenance action we are performing (Create or Edit).
     */
    private String action = "Create";


    /**
     * The host name.
     */
    private String host = "";


    /**
     * The password.
     */
    private String password = "";


    /**
     * The submit button that was pressed.
     */
    private String submit = "";


    /**
     * The subscription type.
     */
    private String type = "";


    /**
     * The username.
     */
    private String username = "";


    // ----------------------------------------------------------- Properties


    /**
     * Return the maintenance action.
     */
    public String getAction() {

	return (this.action);

    }


    /**
     * Set the maintenance action.
     *
     * @param action The new maintenance action.
     */
    public void setAction(String action) {

	if (action == null)
	    this.action = "";
	else
	    this.action = action;

    }


    /**
     * Return the host name.
     */
    public String getHost() {

	return (this.host);

    }


    /**
     * Set the host name.
     *
     * @param host The host name
     */
    public void setHost(String host) {

	if (host == null)
	    this.host = "";
	else
	    this.host = host;

    }


    /**
     * Return the password.
     */
    public String getPassword() {

	return (this.password);

    }


    /**
     * Set the password.
     *
     * @param password The new password
     */
    public void setPassword(String password) {

	if (password == null)
	    this.password = "";
	else
	    this.password = password;

    }


    /**
     * Return the submit button that was pressed.
     */
    public String getSubmit() {

	return (this.submit);

    }


    /**
     * Set the submit button that was pressed.
     *
     * @param submit The new submit button
     */
    public void setSubmit(String submit) {

	if (submit == null)
	    this.submit = "";
	else
	    this.submit = submit;

    }


    /**
     * Return the subscription type.
     */
    public String getType() {

	return (this.type);

    }


    /**
     * Set the subscription type.
     *
     * @param type The subscription type
     */
    public void setType(String type) {

	if (type == null)
	    this.type = "";
	else
	    this.type = type;

    }


    /**
     * Return the username.
     */
    public String getUsername() {

	return (this.username);

    }


    /**
     * Set the username.
     *
     * @param username The new username
     */
    public void setUsername(String username) {

	if (username == null)
	    this.username = "";
	else
	    this.username = username;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Validate the properties of this form bean, and return an array of
     * message keys for any errors we encounter.
     */
    public String[] validate() {

	if ("Cancel".equals(submit))
	    return (null);

	ErrorMessages errors = new ErrorMessages();
	if ((host == null) || (host.length() < 1))
	    errors.addError("error.host.required");
	if ((username == null) || (username.length() < 1))
	    errors.addError("error.username.required");
	if ((password == null) || (password.length() < 1))
	    errors.addError("error.password.required");
	if ((type == null) || (type.length() < 1))
	    errors.addError("error.type.required");
	else if (!"imap".equals(type) && !"pop3".equals(type))
	    errors.addError("error.type.invalid");

	return (errors.getErrors());

    }


}
