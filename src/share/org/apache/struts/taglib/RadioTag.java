/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/taglib/Attic/RadioTag.java,v 1.2 2000/06/12 16:00:53 craigmcc Exp $
 * $Revision: 1.2 $
 * $Date: 2000/06/12 16:00:53 $
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


import java.lang.reflect.Method;
import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;
import org.apache.struts.util.BeanUtils;
import org.apache.struts.util.MessageResources;


/**
 * Tag for input fields of type "radio".
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2000/06/12 16:00:53 $
 */

public final class RadioTag extends BodyTagSupport {


    // ----------------------------------------------------- Instance Variables


    /**
     * The message resources for this package.
     */
    protected static MessageResources messages =
	MessageResources.getMessageResources
	("org.apache.struts.taglib.LocalStrings");


    /**
     * The field name for this field.
     */
    private String name = null;


    /**
     * The server value for this option.
     */
    private String value = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return the field name.
     */
    public String getName() {

	return (this.name);

    }


    /**
     * Set the field name.
     *
     * @param name The new field name
     */
    public void setName(String name) {

	this.name = name;

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

	// Acquire the current value of the appropriate field
	String current = null;
	Object bean = pageContext.findAttribute(Constants.BEAN_KEY);
	if (bean == null)
	    throw new JspException
	        (messages.getMessage("baseFieldTag.missing", name));
	String methodName = "get" + BeanUtils.capitalize(name);
	Class paramTypes[] = new Class[0];
	Method method = null;
	try {
	    method = bean.getClass().getMethod(methodName, paramTypes);
	    current = method.invoke(bean, new Object[0]).toString();
	    if (current == null)
	        current = "";
	} catch (NoSuchMethodException e) {
	    throw new JspException
	        (messages.getMessage("baseFieldTag.method", methodName));
        } catch (Exception e) {
	    throw new JspException
	        (messages.getMessage("baseFieldTag.result",
	                             methodName, e.toString()));
	}

	// Create an appropriate "input" element based on our parameters
	StringBuffer results = new StringBuffer("<input type=\"radio\"");
	results.append(" name=\"");
	results.append(this.name);
	results.append("\" value=\"");
	results.append(this.value);
	results.append("\"");
	if (value.equals(current))
	    results.append(" checked");
	results.append(">");

	// Print this field to our output writer
	JspWriter writer = pageContext.getOut();
	try {
	    writer.print(results.toString());
	} catch (IOException e) {
	    throw new JspException
		(messages.getMessage("baseFieldTag.io", e.toString()));
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
		(messages.getMessage("baseFieldTag.io", e.toString()));
	}

	// Continue evaluating this page
	return (EVAL_PAGE);

    }


}
