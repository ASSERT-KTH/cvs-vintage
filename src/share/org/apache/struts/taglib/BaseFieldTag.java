/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/taglib/Attic/BaseFieldTag.java,v 1.6 2000/07/16 22:29:04 craigmcc Exp $
 * $Revision: 1.6 $
 * $Date: 2000/07/16 22:29:04 $
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
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.struts.util.BeanUtils;
import org.apache.struts.util.MessageResources;


/**
 * Convenience base class for the various input tags for text fields.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.6 $ $Date: 2000/07/16 22:29:04 $
 */

public abstract class BaseFieldTag extends BaseInputTag {


    // ----------------------------------------------------- Instance Variables


    /**
     * The type of input field represented by this tag (text, password, or
     * hidden).
     */
    protected String type = null;


    // --------------------------------------------------------- Public Methods


    /**
     * Generate the required input tag.
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doStartTag() throws JspException {

	// Create an appropriate "input" element based on our parameters
	StringBuffer results = new StringBuffer("<input type=\"");
	results.append(type);
	results.append("\" name=\"");
	results.append(property);
	results.append("\"");
	if (accessKey != null) {
	    results.append(" accesskey=\"");
	    results.append(accessKey);
	    results.append("\"");
	}
	if (maxlength != null) {
	    results.append(" maxlength=\"");
	    results.append(maxlength);
	    results.append("\"");
	}
	if (cols != null) {
	    results.append(" size=\"");
	    results.append(cols);
	    results.append("\"");
	}
	if (tabIndex != null) {
	    results.append(" tabindex=\"");
	    results.append(tabIndex);
	    results.append("\"");
	}
	results.append(" value=\"");
	if (value != null) {
	    results.append(BeanUtils.filter(value));
	} else {
	    Object bean = pageContext.findAttribute(Constants.BEAN_KEY);
	    if (bean == null)
		throw new JspException
		    (messages.getMessage("getter.bean", Constants.BEAN_KEY));
	    try {
		value = BeanUtils.getScalarProperty(bean, property);
		if (value == null)
		    value = "";
		results.append(BeanUtils.filter(value));
	    } catch (NoSuchMethodException e) {
		throw new JspException
		    (messages.getMessage("getter.method", property));
	    } catch (Exception e) {
		throw new JspException
		    (messages.getMessage("getter.result",
					 property, e.toString()));
	    }
	}
	results.append("\"");
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


}
