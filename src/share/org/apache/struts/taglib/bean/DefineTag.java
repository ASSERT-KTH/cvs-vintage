/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/taglib/bean/DefineTag.java,v 1.3 2000/10/12 23:17:15 craigmcc Exp $
 * $Revision: 1.3 $
 * $Date: 2000/10/12 23:17:15 $
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


package org.apache.struts.taglib.bean;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.struts.action.Action;
import org.apache.struts.util.BeanUtils;
import org.apache.struts.util.MessageResources;
import org.apache.struts.util.PropertyUtils;


/**
 * Define a scripting variable based on the value(s) of the specified
 * bean property.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2000/10/12 23:17:15 $
 */

public final class DefineTag extends TagSupport {


    // ------------------------------------------------------------- Properties


    /**
     * The name of the scripting variable that will be exposed as a page
     * scope attribute.
     */
    private String id = null;

    public String getId() {
        return (this.id);
    }

    public void setId(String id) {
        this.id = id;
    }


    /**
     * The message resources for this package.
     */
    private static MessageResources messages =
        MessageResources.getMessageResources
        ("org.apache.struts.taglib.bean.LocalStrings");


    /**
     * The name of the bean owning the property to be exposed.
     */
    private String name = null;

    public String getName() {
        return (this.name);
    }

    public void setName(String name) {
        this.name = name;
    }


    /**
     * The name of the property to be retrieved.
     */
    private String property = null;

    public String getProperty() {
        return (this.property);
    }

    public void setProperty(String property) {
        this.property = property;
    }


    /**
     * The scope within which to search for the specified bean.
     */
    private String scope = null;

    public String getScope() {
        return (this.scope);
    }

    public void setScope(String scope) {
        this.scope = scope;
    }


    /**
     * The fully qualified Java class name of the value to be exposed.
     */
    private String type = null;

    public String getType() {
        return (this.type);
    }

    public void setType(String type) {
        this.type = type;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Retrieve the required property and expose it as a scripting variable.
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doStartTag() throws JspException {

        // Retrieve the required property value
        Object bean = null;
        Object value = null;
        try {

            // Locate the specified bean
	    bean = BeanUtils.lookup(pageContext, name, scope);

            // Locate the specified property
            if (bean == null)
                throw new JspException
                    (messages.getMessage("getter.bean", name));
            if (property == null)
                value = bean;
            else
                value = PropertyUtils.getProperty(bean, property);

        } catch (IllegalAccessException e) {
            pageContext.setAttribute(Action.EXCEPTION_KEY, e,
                                     PageContext.REQUEST_SCOPE);
            throw new JspException
                (messages.getMessage("getter.access", property, name));
	} catch (IllegalArgumentException e) {
            pageContext.setAttribute(Action.EXCEPTION_KEY, e,
                                     PageContext.REQUEST_SCOPE);
	    throw new JspException
	      (messages.getMessage("getter.scope", scope));
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t == null)
                t = e;
            pageContext.setAttribute(Action.EXCEPTION_KEY, t,
                                     PageContext.REQUEST_SCOPE);
            throw new JspException
                (messages.getMessage("getter.invocation",
                                     property, name, t.toString()));
        } catch (NoSuchMethodException e) {
            pageContext.setAttribute(Action.EXCEPTION_KEY, e,
                                     PageContext.REQUEST_SCOPE);
            throw new JspException
                (messages.getMessage("getter.method", property, name));
        }

        // Expose this value as a scripting variable
        pageContext.setAttribute(id, value);
        return (SKIP_BODY);

    }


    /**
     * Reset custom attributes to their default values.
     */
    public void releaseCustomAttributes() {

        id = null;
        name = null;
        property = null;
        scope = null;
        type = null;

    }


}
