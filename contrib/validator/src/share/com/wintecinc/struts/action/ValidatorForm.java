/*
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
 */
 
 
package com.wintecinc.struts.action;

import java.io.Serializable;
import java.util.Locale;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import com.wintecinc.struts.validation.Validator;
import com.wintecinc.struts.validation.ValidatorException;
import com.wintecinc.struts.validation.ValidatorResources;
import com.wintecinc.struts.validation.ValidatorUtil;


/**
 * <p>This class extends <strong>ActionForm</strong> and provides 
 * basic field validation based on an XML file.  The key passed into the 
 * validator is the action element's 'name' attribute from the 
 * struts-config.xml which should match the form element's name attribute 
 * in the validation.xml.</p>
 *
 * <ul><li>See /WEB-INF/validation.xml for validation rules.</li></ul>
 *
 * @author David Winterfeldt
 * @see org.apache.struts.action.ActionForm
*/

public class ValidatorForm extends ActionForm implements Serializable {
    /**
     * Used to indicate the current page of a multi-page form.
    */
    protected int page = 0;

    /**
     * Gets page.
    */
    public int getPage() {
       return page;	
    }

    /**
     * Sets page.
    */
    public void setPage(int page) {
       this.page = page;	
    }

    /**
     * Validate the properties that have been set from this HTTP request,
     * and return an <code>ActionErrors</code> object that encapsulates any
     * validation errors that have been found.  If no errors are found, return
     * <code>null</code> or an <code>ActionErrors</code> object with no
     * recorded error messages.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public ActionErrors validate(ActionMapping mapping,
                                 HttpServletRequest request) {

        ActionErrors errors = new ActionErrors();	
	Validator validator = initValidator(mapping.getAttribute(), request, errors);
	
	try {
	   validator.validate();
        } catch (ValidatorException e) {
	   log("ValidatorForm::validate() - " + e.getMessage(), e);
	}

        return errors;
    }

    /**
     * Initialize the <code>Validator</code> to perform validation.
     *
     * @param 	key		The key that the validation rules are under 
     *				(the form elements name attribute).
     * @param 	request		The current request object.
     * @param 	errors		The object any errors will be stored in.
    */
    protected Validator initValidator(String key, HttpServletRequest request, ActionErrors errors) {
	ServletContext application = getServlet().getServletContext();
	ValidatorResources resources = ValidatorUtil.getValidatorResources(application);
	Locale locale = ValidatorUtil.getLocale(request);
	
	Validator validator = new Validator(resources, key);

	//validator.setDebug(getServlet().getDebug());
	validator.setPage(getPage());

 	validator.addResource(Validator.SERVLET_CONTEXT_KEY, application);
	validator.addResource(Validator.HTTP_SERVLET_REQUEST_KEY, request);
	validator.addResource(Validator.LOCALE_KEY, locale);
	validator.addResource(Validator.ACTION_ERRORS_KEY, errors);
	validator.addResource(Validator.BEAN_KEY, this);
        
        return validator;    	
    }

    /**
     * Convenience method that call the comparable servlet log method and writes 
     * an explanatory message and a stack trace for a given Throwable exception to the 
     * servlet log file.
     *
     * @param 	message		String that describes the error or exception
    */
    protected void log(String message) {
       if (getServlet().getDebug() >= 1)
           getServlet().log(message);
    }

    /**
     * Convenience method that call the comparable servlet log method and writes 
     * an explanatory message and a stack trace for a given Throwable exception to the 
     * servlet log file.
     *
     * @param 	message		String that describes the error or exception
     * @param 	throwable	Throwable error or exception
    */
    protected void log(String message, Throwable throwable) {
       if (getServlet().getDebug() >= 1)
           getServlet().log(message, throwable);
    }

    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
       super.reset(mapping, request);
       page = 0;
    }

}
