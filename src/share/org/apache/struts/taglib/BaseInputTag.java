/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/taglib/Attic/BaseInputTag.java,v 1.1 2000/05/31 22:28:11 craigmcc Exp $
 * $Revision: 1.1 $
 * $Date: 2000/05/31 22:28:11 $
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
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.struts.util.MessageResources;


/**
 * Abstract base class for the various input tags.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2000/05/31 22:28:11 $
 */

public class BaseInputTag extends TagSupport {


    // ----------------------------------------------------- Instance Variables


    /**
     * The number of character columns for this field, or negative
     * for no limit.
     */
    protected int cols = -1;


    /**
     * The maximum number of characters allowed, or negative for no limit.
     */
    protected int maxlength = -1;


    /**
     * The message resources for this package.
     */
    protected static MessageResources messages =
	MessageResources.getMessageResources
	("org.apache.struts.taglib.LocalStrings");


    /**
     * The name of the field (and associated property) being processed.
     */
    protected String name = null;


    /**
     * The number of rows for this field, or negative for no limit.
     */
    protected int rows = -1;


    /**
     * The value for this field, or <code>null</code> to retrieve the
     * corresponding property from our associated bean.
     */
    protected String value = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return the number of columns for this field.
     */
    public int getCols() {

	return (this.cols);

    }


    /**
     * Set the number of columns for this field.
     *
     * @param cols The new number of columns
     */
    public void setCols(int cols) {

	this.cols = cols;

    }


    /**
     * Set the number of columns for this field.
     *
     * @param cols The new number of columns
     */
    public void setCols(String cols) {

	try {
	    this.cols = Integer.parseInt(cols);
	} catch (NumberFormatException e) {
	    ;
	}

    }


    /**
     * Return the maximum length allowed.
     */
    public int getMaxlength() {

	return (this.maxlength);

    }


    /**
     * Set the maximum length allowed.
     *
     * @param maxlength The new maximum length
     */
    public void setMaxlength(int maxlength) {

	this.maxlength = maxlength;

    }


    /**
     * Set the maximum length allowed.
     *
     * @param maxlength The new maximum length
     */
    public void setMaxlength(String maxlength) {

	try {
	    this.maxlength = Integer.parseInt(maxlength);
	} catch (NumberFormatException e) {
	    ;
	}

    }


    /**
     * Return the field name.
     */
    public String getName() {

	return (this.name);

    }


    /**
     * Set the object name.
     *
     * @param name The new object name
     */
    public void setName(String name) {

	this.name = name;

    }


    /**
     * Return the number of rows for this field.
     */
    public int getRows() {

	return (this.rows);

    }


    /**
     * Set the number of rows for this field.
     *
     * @param rows The new number of rows
     */
    public void setRows(int rows) {

	this.rows = rows;

    }


    /**
     * Set the number of rows for this field.
     *
     * @param rows The new number of rows
     */
    public void setRows(String rows) {

	try {
	    this.rows = Integer.parseInt(rows);
	} catch (NumberFormatException e) {
	    ;
	}

    }


    /**
     * Return the size of this field (synonym for <code>getCols()</code>).
     */
    public int getSize() {

	return (getCols());

    }


    /**
     * Set the size of this field (synonym for <code>setCols()</code>).
     *
     * @param size The new size
     */
    public void setSize(int size) {

	setCols(size);

    }


    /**
     * Set the size of this field (synonym for <code>setCols()</code>).
     *
     * @param size The new size
     */
    public void setSize(String size) {

	setCols(size);

    }


    /**
     * Return the field value (if any).
     */
    public String getValue() {

	return (this.value);

    }


    /**
     * Set the field value (if any).
     *
     * @param value The new field value, or <code>null</code> to retrieve the
     *  corresponding property from the bean
     */
    public void setValue(String value) {

	this.value = value;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Process the start of this tag.  The default implementation does nothing.
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doStartTag() throws JspException {

	return (EVAL_BODY_INCLUDE);

    }



    /**
     * Process the end of this tag.  The default implementation does nothing.
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doEndTag() throws JspException {

	return (EVAL_PAGE);

    }


}
