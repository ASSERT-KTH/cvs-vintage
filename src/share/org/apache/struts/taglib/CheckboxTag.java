/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/taglib/Attic/CheckboxTag.java,v 1.8 2000/07/17 16:37:45 craigmcc Exp $
 * $Revision: 1.8 $
 * $Date: 2000/07/17 16:37:45 $
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


package org.apache.struts.taglib;


import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspWriter;
import org.apache.struts.util.BeanUtils;
import org.apache.struts.util.MessageResources;


/**
 * Tag for input fields of type "checkbox".
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.8 $ $Date: 2000/07/17 16:37:45 $
 */

public final class CheckboxTag extends BaseHandlerTag {


    // ----------------------------------------------------- Instance Variables


    /**
     * The message resources for this package.
     */
    protected static MessageResources messages =
	MessageResources.getMessageResources
	("org.apache.struts.taglib.LocalStrings");


    /**
     * The name of the bean containing our underlying property.
     */
    private String name = Constants.BEAN_KEY;

    public String getName() {
	return (this.name);
    }

    public void setName(String name) {
	this.name = name;
    }


    /**
     * The property name for this field.
     */
    private String property = null;


    /**
     * The server value for this option.
     */
    private String value = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return the property name.
     */
    public String getProperty() {

	return (this.property);

    }


    /**
     * Set the property name.
     *
     * @param property The new property name
     */
    public void setProperty(String property) {

	this.property = property;

    }


    /**
     * Return the server value.
     */
    public String getValue() {

	return (this.value);

    }


    /**
     * Set the server value.
     *
     * @param value The new server value
     */
    public void setValue(String value) {

	this.value = value;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Generate the required input tag.
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doStartTag() throws JspException {

	// Create an appropriate "input" element based on our parameters
	StringBuffer results = new StringBuffer("<input type=\"checkbox\"");
	results.append(" name=\"");
	results.append(this.property);
	results.append("\"");
	if (accessKey != null) {
	    results.append(" accesskey=\"");
	    results.append(accessKey);
	    results.append("\"");
	}
	if (tabIndex != null) {
	    results.append(" tabindex=\"");
	    results.append(tabIndex);
	    results.append("\"");
	}
	String value = this.value;
	if (value == null) {
	    Object bean = pageContext.findAttribute(name);
	    if (bean == null)
		throw new JspException
		    (messages.getMessage("getter.bean", name));
	    try {
		value = BeanUtils.getScalarProperty(bean, property);
		if (value == null)
		    value = "";
	    } catch (NoSuchMethodException e) {
		throw new JspException
		    (messages.getMessage("getter.method", property));
	    } catch (Exception e) {
		throw new JspException
		    (messages.getMessage("getter.result",
					 property, e.toString()));
	    }
	}
	if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes")
	    || value.equalsIgnoreCase("on"))
	    results.append(" checked");
	results.append(prepareEventHandlers());
	results.append(prepareStyles());
	results.append(">");

	// Print this field to our output writer
	JspWriter writer = pageContext.getOut();
	try {
	    writer.print(results.toString());
	} catch (IOException e) {
	    throw new JspException
		(messages.getMessage("common.io", e.toString()));
	}

	// Continue processing this page
	return (EVAL_BODY_TAG);

    }



    /**
     * Optionally render the associated label from the body content.
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doEndTag() throws JspException {

	if (bodyContent == null)
	    return (EVAL_PAGE);

	JspWriter writer = pageContext.getOut();
	try {
	    writer.println(bodyContent.getString().trim());
	} catch (IOException e) {
	    throw new JspException
		(messages.getMessage("common.io", e.toString()));
	}

	// Continue evaluating this page
	return (EVAL_PAGE);

    }


    /**
     * Release any acquired resources.
     */
    public void release() {

	super.release();
	name = Constants.BEAN_KEY;
	property = null;
	value = null;

    }


}
