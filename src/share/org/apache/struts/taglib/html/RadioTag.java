/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/taglib/html/RadioTag.java,v 1.13 2002/07/07 23:27:57 husted Exp $
 * $Revision: 1.13 $
 * $Date: 2002/07/07 23:27:57 $
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


package org.apache.struts.taglib.html;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspWriter;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.struts.util.MessageResources;
import org.apache.struts.util.ResponseUtils;


/**
 * Tag for input fields of type "radio".
 *
 * @author Craig R. McClanahan
 * @author Ted Husted
 * @version $Revision: 1.13 $ $Date: 2002/07/07 23:27:57 $
 */

public class RadioTag extends BaseHandlerTag {


    // ----------------------------------------------------- Instance Variables


    /**
     * The message resources for this package.
     */
    protected static MessageResources messages =
     MessageResources.getMessageResources(Constants.Package + ".LocalStrings");


    /**
     * The name of the bean containing our underlying property.
     */
    protected String name = Constants.BEAN_KEY;

    public String getName() {
        return (this.name);
    }

    public void setName(String name) {
        this.name = name;
    }


    /**
     * The property name for this field.
     */
    protected String property = null;


    /**
     * The body content of this tag (if any).
     */
    protected String text = null;


    /**
     * The server value for this option.
     */
    protected String value = null;


    /**
     * Name of the bean (in some scope) that will return the
     * value of the radio tag.
     * <p>
     * If an iterator is used to render a series of radio tags,
     * this field may be used to specify the name of the bean
     * exposed by the iterator. In this case, the value attribute is
     * used as the name of a property on the <code>idName</code> bean
     * that returns the value of the radio tag in this iteration.
     */
    protected String idName = null;



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
     * @param name The new property name
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



    /**
     * Return the idName.
     * @since Struts 1.1
     */
    public String getIdName() {

        return (this.idName);

    }


    /**
     * Set the idName.
     * @since Struts 1.1
     *
     * @param value The new idName
     */
    public void setIdName(String idName) {

        this.idName=idName;

    }



    // --------------------------------------------------------- Public Methods


    /**
     * Generate the required input tag.
     * [Indexed property since Struts 1.1]
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doStartTag() throws JspException {

        // Acquire the current value of the appropriate field
        Object current = null;
        Object bean = pageContext.findAttribute(name);
        if (bean == null)
            throw new JspException
                (messages.getMessage("getter.bean", name));
        try {

            current = BeanUtils.getProperty(bean, property);
            if (current == null)
                current = "";

            // @since Struts 1.1
            if (idName != null) {
                Object idBean = pageContext.findAttribute(idName);
                if (idBean == null)
                   throw new JspException
                    (messages.getMessage("getter.bean", idName));
                value = BeanUtils.getProperty(idBean, value);
                if (value == null) value = "";
            }


            } catch (IllegalAccessException e) {
                throw new JspException
                    (messages.getMessage("getter.access", property, name));
            } catch (InvocationTargetException e) {
                Throwable t = e.getTargetException();
                throw new JspException
                    (messages.getMessage("getter.result",
                                         property, t.toString()));
        } catch (NoSuchMethodException e) {
            throw new JspException
                (messages.getMessage("getter.method", property, name));
        }



        // Create an appropriate "input" element based on our parameters
        StringBuffer results = new StringBuffer("<input type=\"radio\"");
        results.append(" name=\"");
        // @since Struts 1.1
        if( indexed )
                prepareIndex( results, name );
        results.append(this.property);
        results.append("\"");
        if (accesskey != null) {
            results.append(" accesskey=\"");
            results.append(accesskey);
            results.append("\"");
        }
        if (tabindex != null) {
            results.append(" tabindex=\"");
            results.append(tabindex);
            results.append("\"");
        }
        results.append(" value=\"");
        results.append(this.value);
        results.append("\"");
        if (value.equals(current.toString()))
            results.append(" checked=\"checked\"");
        results.append(prepareEventHandlers());
        results.append(prepareStyles());
        results.append(">");

        // Print this field to our output writer
        ResponseUtils.write(pageContext, results.toString());

        // Continue processing this page
        this.text = null;
        return (EVAL_BODY_TAG);

    }


    /**
     * Save the associated label from the body content.
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doAfterBody() throws JspException {

        if (bodyContent != null) {
            String value = bodyContent.getString().trim();
            if (value.length() > 0)
                text = value;
        }
        return (SKIP_BODY);

    }


    /**
     * Optionally render the associated label from the body content.
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doEndTag() throws JspException {

        // Render any description for this radio button
        if (text != null)
            ResponseUtils.write(pageContext, text);

        // Evaluate the remainder of this page
        return (EVAL_PAGE);

    }


    /**
     * Release any acquired resources.
     */
    public void release() {

        super.release();
        name = Constants.BEAN_KEY;
        property = null;
        text = null;
        value = null;

    }


}
