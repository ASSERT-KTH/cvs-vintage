/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/taglib/bean/DefineTag.java,v 1.27 2004/04/24 06:37:00 rleland Exp $
 * $Revision: 1.27 $
 * $Date: 2004/04/24 06:37:00 $
 *
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.struts.taglib.bean;


import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.struts.taglib.TagUtils;
import org.apache.struts.util.MessageResources;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Define a scripting variable based on the value(s) of the specified
 * bean property.
 *
 * @version $Revision: 1.27 $ $Date: 2004/04/24 06:37:00 $
 */

public class DefineTag extends BodyTagSupport {

    /**
      * Commons logging instance.
      */
     private static final Log log = LogFactory.getLog(DefineTag.class);

    // ---------------------------------------------------- Protected variables

    /**
     * The message resources for this package.
     */
    protected static MessageResources messages =
        MessageResources.getMessageResources
        ("org.apache.struts.taglib.bean.LocalStrings");


    /**
     * The body content of this tag (if any).
     */
    protected String body = null;


    // ------------------------------------------------------------- Properties


    /**
     * The name of the scripting variable that will be exposed as a page
     * scope attribute.
     */
    protected String id = null;

    public String getId() {
        return (this.id);
    }

    public void setId(String id) {
        this.id = id;
    }


    /**
     * The name of the bean owning the property to be exposed.
     */
    protected String name = null;

    public String getName() {
        return (this.name);
    }

    public void setName(String name) {
        this.name = name;
    }


    /**
     * The name of the property to be retrieved.
     */
    protected String property = null;

    public String getProperty() {
        return (this.property);
    }

    public void setProperty(String property) {
        this.property = property;
    }


    /**
     * The scope within which to search for the specified bean.
     */
    protected String scope = null;

    public String getScope() {
        return (this.scope);
    }

    public void setScope(String scope) {
        this.scope = scope;
    }


    /**
     * The scope within which the newly defined bean will be creatd.
     */
    protected String toScope = null;

    public String getToScope() {
        return (this.toScope);
    }

    public void setToScope(String toScope) {
        this.toScope = toScope;
    }


    /**
     * The fully qualified Java class name of the value to be exposed.
     */
    protected String type = null;

    public String getType() {
        return (this.type);
    }

    public void setType(String type) {
        this.type = type;
    }


    /**
     * The (String) value to which the defined bean will be set.
     */
    protected String value = null;

    public String getValue() {
        return (this.value);
    }

    public void setValue(String value) {
        this.value = value;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Check if we need to evaluate the body of the tag
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doStartTag() throws JspException {
       
        return (EVAL_BODY_TAG);

    }


    /**
     * Save the body content of this tag (if any), or throw a JspException
     * if the value was already defined.
     *
     * @exception JspException if value was defined by an attribute
     */
    public int doAfterBody() throws JspException {

        if (bodyContent != null) {
            body = bodyContent.getString();
            if (body != null) {
                body = body.trim();
            }
            if (body.length() < 1) {
                body = null;
            }
        }
        return (SKIP_BODY);

    }


    /**
     * Retrieve the required property and expose it as a scripting variable.
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doEndTag() throws JspException {

        // Enforce restriction on ways to declare the new value
        int n = 0;
        if (this.body != null) {
            n++;
        }
        if (this.name != null) {
            n++;
        }
        if (this.value != null) {
            n++;
        }
        if (n != 1) {
            JspException e =
                new JspException(messages.getMessage("define.value"));
            TagUtils.getInstance().saveException(pageContext, e);
            throw e;
        }

        // Retrieve the required property value
        Object value = this.value;
        if ((value == null) && (name != null)) {
            value = TagUtils.getInstance().lookup(pageContext, name, property, scope);
        }
        if ((value == null) && (body != null)) {
            value = body;
        }
        if (value == null) {
            JspException e =
                new JspException(messages.getMessage("define.null"));
            TagUtils.getInstance().saveException(pageContext, e);
            throw e;
        }

        // Expose this value as a scripting variable
        int inScope = PageContext.PAGE_SCOPE;
        try {
			if (toScope != null) {
				inScope = TagUtils.getInstance().getScope(toScope);
			}
		} catch (JspException e) {
            log.warn("toScope was invalid name so we default to PAGE_SCOPE",e);
		}
            
        pageContext.setAttribute(id, value, inScope);

        // Continue processing this page
        return (EVAL_PAGE);

    }

    /**
     * Release all allocated resources.
     */
    public void release() {

        super.release();
        body = null;
        id = null;
        name = null;
        property = null;
        scope = null;
        toScope = "page";
        type = null;
        value = null;

    }


}
