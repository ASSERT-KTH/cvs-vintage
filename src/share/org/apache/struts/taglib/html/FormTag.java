/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/taglib/html/FormTag.java,v 1.23 2002/06/25 00:13:05 craigmcc Exp $
 * $Revision: 1.23 $
 * $Date: 2002/06/25 00:13:05 $
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
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.config.ApplicationConfig;
import org.apache.struts.config.FormBeanConfig;
import org.apache.struts.util.MessageResources;
import org.apache.struts.util.RequestUtils;
import org.apache.struts.util.ResponseUtils;


/**
 * Custom tag that represents an input form, associated with a bean whose
 * properties correspond to the various fields of the form.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.23 $ $Date: 2002/06/25 00:13:05 $
 */

public class FormTag extends TagSupport {


    // ----------------------------------------------------- Instance Variables


    /**
     * The action URL to which this form should be submitted, if any.
     */
    protected String action = null;


    /**
     * The application configuration for our sub-application.
     */
    protected ApplicationConfig appConfig = null;


    /**
     * The content encoding to be used on a POST submit.
     */
    protected String enctype = null;


    /**
     * The name of the field to receive focus, if any.
     */
    protected String focus = null;


    /**
     * The ActionMapping defining where we will be submitting this form
     */
    protected ActionMapping mapping = null;


    /**
     * The message resources for this package.
     */
    protected static MessageResources messages =
     MessageResources.getMessageResources(Constants.Package + ".LocalStrings");


    /**
     * The request method used when submitting this form.
     */
    protected String method = "POST";


    /**
     * The attribute key under which our associated bean is stored.
     */
    protected String name = null;


    /**
     * The onReset event script.
     */
    protected String onreset = null;


    /**
     * The onSubmit event script.
     */
    protected String onsubmit = null;


    /**
     * The scope (request or session) under which our associated bean
     * is stored.
     */
    protected String scope = "session";


    /**
     * The ActionServlet instance we are associated with (so that we can
     * initialize the <code>servlet</code> property on any form bean that
     * we create).
     */
    protected ActionServlet servlet = null;


    /**
     * The style attribute associated with this tag.
     */
    protected String style = null;


    /**
     * The style class associated with this tag.
     */
    protected String styleClass = null;


    /**
     * The identifier associated with this tag.
     */
    protected String styleId = null;


    /**
     * The window target.
     */
    protected String target = null;


    /**
     * The Java class name of the bean to be created, if necessary.
     */
    protected String type = null;

    /**
     * Temporary copy of the 'name' property, used to ensure we don't
     * modify tag attributes between uses of the same instance.
     */
    private String savedName = null;

    /**
     * Temporary copy of the 'scope' property, used to ensure we don't
     * modify tag attributes between uses of the same instance.
     */
    private String savedScope = null;

    /**
     * Temporary copy of the 'type' property, used to ensure we don't
     * modify tag attributes between uses of the same instance.
     */
    private String savedType = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return the action URL to which this form should be submitted.
     */
    public String getAction() {

	return (this.action);

    }


    /**
     * Set the action URL to which this form should be submitted.
     *
     * @param action The new action URL
     */
    public void setAction(String action) {

	this.action = action;

    }

    /**
     * Return the content encoding used when submitting this form.
     */
    public String getEnctype() {

	return (this.enctype);

    }

    /**
     * Set the content encoding used when submitting this form.
     *
     * @param enctype The new content encoding
     */
    public void setEnctype(String enctype) {

	this.enctype = enctype;

    }

    /**
     * Return the focus field name for this form.
     */
    public String getFocus() {

	return (this.focus);

    }


    /**
     * Set the focus field name for this form.
     *
     * @param focus The new focus field name
     */
    public void setFocus(String focus) {

	this.focus = focus;

    }


    /**
     * Return the request method used when submitting this form.
     */
    public String getMethod() {

	return (this.method);

    }


    /**
     * Set the request method used when submitting this form.
     *
     * @param method The new request method
     */
    public void setMethod(String method) {

	this.method = method;

    }


    /**
     * Return the attribute key name of our bean.
     */
    public String getName() {

	return (this.name);

    }


    /**
     * Set the attribute key name of our bean.
     *
     * @param name The new attribute key name
     */
    public void setName(String name) {

	this.name = name;

    }


    /**
     * Return the onReset event script.
     */
    public String getOnreset() {

	return (this.onreset);

    }


    /**
     * Set the onReset event script.
     *
     * @param onReset The new event script
     */
    public void setOnreset(String onReset) {

	this.onreset = onReset;

    }


    /**
     * Return the onSubmit event script.
     */
    public String getOnsubmit() {

	return (this.onsubmit);

    }


    /**
     * Set the onSubmit event script.
     *
     * @param onSubmit The new event script
     */
    public void setOnsubmit(String onSubmit) {

	this.onsubmit = onSubmit;

    }


    /**
     * Return the attribute scope of our bean.
     */
    public String getScope() {

	return (this.scope);

    }


    /**
     * Set the attribute scope of our bean.
     *
     * @param scope The new attribute scope
     */
    public void setScope(String scope) {

	this.scope = scope;

    }


    /**
     * Return the style attribute for this tag.
     */
    public String getStyle() {

	return (this.style);

    }


    /**
     * Set the style attribute for this tag.
     *
     * @param style The new style attribute
     */
    public void setStyle(String style) {

	this.style = style;

    }


    /**
     * Return the style class for this tag.
     */
    public String getStyleClass() {

	return (this.styleClass);

    }


    /**
     * Set the style class for this tag.
     *
     * @param styleClass The new style class
     */
    public void setStyleClass(String styleClass) {

	this.styleClass = styleClass;

    }


    /**
     * Return the style identifier for this tag.
     */
    public String getStyleId() {

        return (this.styleId);

    }


    /**
     * Set the style identifier for this tag.
     *
     * @param styleId The new style identifier
     */
    public void setStyleId(String styleId) {

        this.styleId = styleId;

    }
    


    /**
     * Return the window target.
     */
    public String getTarget() {

	return (this.target);

    }


    /**
     * Set the window target.
     *
     * @param target The new window target
     */
    public void setTarget(String target) {

	this.target = target;

    }


    /**
     * Return the Java class of our bean.
     */
    public String getType() {

	return (this.type);

    }


    /**
     * Set the Java class of our bean.
     *
     * @param type The new Java class
     */
    public void setType(String type) {

	this.type = type;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Render the beginning of this form.
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doStartTag() throws JspException {

        // Save the name, scope and type property values so that we can restore
        // them in doEndTag(). Needed to allow tag reuse to work correctly.
        savedName = name;
        savedScope = scope;
        savedType = type;

        // Look up the form bean name, scope, and type if necessary
        lookup();

	// Create an appropriate "form" element based on our parameters
	HttpServletResponse response =
	  (HttpServletResponse) pageContext.getResponse();
	StringBuffer results = new StringBuffer("<form");
	results.append(" name=\"");
	results.append(name);
	results.append("\"");
	results.append(" method=\"");
	results.append(method);
        results.append("\" action=\"");
        results.append(response.encodeURL(getActionMappingURL()));
        results.append("\"");
        if (styleClass != null) {
            results.append(" class=\"");
            results.append(styleClass);
            results.append("\"");
        }
	if (enctype != null) {
	    results.append(" enctype=\"");
	    results.append(enctype);
	    results.append("\"");
	}
	if (onreset != null) {
	    results.append(" onreset=\"");
	    results.append(onreset);
	    results.append("\"");
        }
        if (onsubmit != null) {
	    results.append(" onsubmit=\"");
	    results.append(onsubmit);
	    results.append("\"");
	}
        if (style != null) {
            results.append(" style=\"");
            results.append(style);
            results.append("\"");
        }
        if (styleId != null) {
            results.append(" id=\"");
            results.append(styleId);
            results.append("\"");
        }
	if (target != null) {
	    results.append(" target=\"");
	    results.append(target);
	    results.append("\"");
	}
	results.append(">");

        // Add a transaction token (if present in our session)
        HttpSession session = pageContext.getSession();
        if (session != null) {
            String token =
                (String) session.getAttribute(Action.TRANSACTION_TOKEN_KEY);
            if (token != null) {
                results.append("<input type=\"hidden\" name=\"");
                results.append(Constants.TOKEN_KEY);
                results.append("\" value=\"");
                results.append(token);
                results.append("\">");
            }
        }

	// Print this field to our output writer
        ResponseUtils.write(pageContext, results.toString());

	// Store this tag itself as a page attribute
	pageContext.setAttribute(Constants.FORM_KEY, this,
                                 PageContext.REQUEST_SCOPE);
        
	// Locate or create the bean associated with our form
	int scope = PageContext.SESSION_SCOPE;
	if ("request".equals(this.scope))
	    scope = PageContext.REQUEST_SCOPE;
	Object bean = pageContext.getAttribute(name, scope);
	if (bean == null) {
            bean = RequestUtils.createActionForm
                ((HttpServletRequest) pageContext.getRequest(),
                 mapping, appConfig, servlet);
            if (bean instanceof ActionForm) {
                ((ActionForm) bean).reset
                    (mapping, (HttpServletRequest) pageContext.getRequest());
            }
            if (bean == null) {
                throw new JspException
                    (messages.getMessage("formTag.create", type));
            }
            pageContext.setAttribute(name, bean, scope);
        }
	pageContext.setAttribute(Constants.BEAN_KEY, bean,
                                 PageContext.REQUEST_SCOPE);

	// Continue processing this page
	return (EVAL_BODY_INCLUDE);

    }


    /**
     * Render the end of this form.
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doEndTag() throws JspException {

	// Remove the page scope attributes we created
	pageContext.removeAttribute(Constants.BEAN_KEY,
                                    PageContext.REQUEST_SCOPE);
	pageContext.removeAttribute(Constants.FORM_KEY,
                                    PageContext.REQUEST_SCOPE);

	// Render a tag representing the end of our current form
	StringBuffer results = new StringBuffer("</form>");

        // Render JavaScript to set the input focus if required
	if (focus != null) {
            String tempFocus = focus;
            StringBuffer refocus = new StringBuffer("[");
            if (tempFocus.indexOf("[") > 0) {
                StringTokenizer st = new StringTokenizer(tempFocus, "[");
                if (st.countTokens() == 2) {
                    tempFocus = st.nextToken();
                    refocus.append(st.nextToken());
                }
            }
	    results.append("\r\n");
	    results.append("<script language=\"JavaScript\"");
            results.append(" type=\"text/javascript\">\r\n");
	    results.append("  <!--\r\n");
            results.append(" if (document.forms[\"");
            results.append(name);
            results.append("\"].elements[\"");
            results.append(tempFocus);
            results.append("\"]");
            if (refocus.length() > 1) {
                results.append(refocus.toString());
            }
            results.append(".type != \"hidden\") \r\n");
	    results.append("    document.forms[\"");
	    results.append(name);
	    results.append("\"].elements[\"");
            results.append(tempFocus);
            results.append("\"]");
            if (refocus.length() > 1) {
                results.append(refocus.toString());
            }
	    results.append(".focus()\r\n");
	    results.append("  // -->\r\n");
	    results.append("</script>\r\n");
	}

	// Print this value to our output writer
	JspWriter writer = pageContext.getOut();
	try {
	    writer.print(results.toString());
	} catch (IOException e) {
	    throw new JspException
	        (messages.getMessage("common.io", e.toString()));
	}

        // Restore original property values used to invoke this tag. Needed
        // for tag reuse to work correctly.
        name = savedName;
        scope = savedScope;
        type = savedType;

	// Continue processing this page
	return (EVAL_PAGE);

    }


    /**
     * Release any acquired resources.
     */
    public void release() {

	super.release();
	action = null;
        appConfig = null;
	enctype = null;
	focus = null;
        mapping = null;
	method = "POST";
	name = null;
	onreset = null;
	onsubmit = null;
	scope = "session";
        servlet = null;
	style = null;
	styleClass = null;
        styleId = null;
	target = null;
	type = null;

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Return the form action converted into an action mapping path.  The
     * value of the <code>action</code> property is manipulated as follows in
     * computing the name of the requested mapping:
     * <ul>
     * <li>Any filename extension is removed (on the theory that extension
     *     mapping is being used to select the controller servlet).</li>
     * <li>If the resulting value does not start with a slash, then a
     *     slash is prepended.</li>
     * </ul>
     */
    protected String getActionMappingName() {

        String value = action;
        int question = action.indexOf("?");
        if (question >= 0)
            value = value.substring(0, question);
        int slash = value.lastIndexOf("/");
        int period = value.lastIndexOf(".");
        if ((period >= 0) && (period > slash))
            value = value.substring(0, period);
        if (value.startsWith("/"))
            return (value);
        else
            return ("/" + value);

    }


    /**
     * Return the form action converted into a server-relative URL.
     */
    protected String getActionMappingURL() {

        HttpServletRequest request =
            (HttpServletRequest) pageContext.getRequest();
        StringBuffer value = new StringBuffer(request.getContextPath());
        ApplicationConfig config = (ApplicationConfig)
            pageContext.getRequest().getAttribute(Action.APPLICATION_KEY);
        if (config != null) {
            value.append(config.getPrefix());
        }
        
        // Use our servlet mapping, if one is specified
        String servletMapping = (String)
            pageContext.getAttribute(Action.SERVLET_KEY,
                                     PageContext.APPLICATION_SCOPE);
        if (servletMapping != null) {
            String queryString = null;
            int question = action.indexOf("?");
            if (question >= 0) {
                queryString = action.substring(question);
            }
            String actionMapping = getActionMappingName();
            if (servletMapping.startsWith("*.")) {
                value.append(actionMapping);
                value.append(servletMapping.substring(1));
            } else if (servletMapping.endsWith("/*")) {
                value.append(servletMapping.substring
                             (0, servletMapping.length() - 2));
                value.append(actionMapping);
            } else if (servletMapping.equals("/")) {
                value.append(actionMapping);
            }
            if (queryString != null) {
                value.append(queryString);
            }
        }

        // Otherwise, assume extension mapping is in use and extension is
        // already included in the action property
        else {
            if (!action.startsWith("/")) {
                value.append("/");
            }
            value.append(action);
        }

        // Return the completed value
        return (value.toString());

    }


    /**
     * Look up values for the <code>name</code>, <code>scope</code>, and
     * <code>type</code> properties if necessary.
     *
     * @exception JspException if a required value cannot be looked up
     */
    protected void lookup() throws JspException {

        // Look up the application configuration information we need
        appConfig = (ApplicationConfig)
            pageContext.getRequest().getAttribute(Action.APPLICATION_KEY);
        if (appConfig == null) { // Backwards compatibility hack
            appConfig = (ApplicationConfig)
                pageContext.getServletContext().getAttribute(Action.APPLICATION_KEY);
        }
        if (appConfig == null) {
            JspException e = new JspException
                (messages.getMessage("formTag.collections"));
            pageContext.setAttribute(Action.EXCEPTION_KEY, e,
                                     PageContext.REQUEST_SCOPE);
            throw e;
        }
        servlet = (ActionServlet)
            pageContext.getServletContext().getAttribute(Action.ACTION_SERVLET_KEY);

        // Look up the action mapping we will be submitting to
        String mappingName = getActionMappingName();
        mapping = (ActionMapping) appConfig.findActionConfig(mappingName);
        if (mapping == null) {
            JspException e = new JspException
                (messages.getMessage("formTag.mapping", mappingName));
            pageContext.setAttribute(Action.EXCEPTION_KEY, e,
                                     PageContext.REQUEST_SCOPE);
            throw e;
        }

        // Were the required values already specified?
        if (name != null) {
            if (scope == null)
                scope = "session";
            if (type == null) {
                JspException e = new JspException
                    (messages.getMessage("formTag.nameType"));
                pageContext.setAttribute(Action.EXCEPTION_KEY, e,
                                         PageContext.REQUEST_SCOPE);
                throw e;
            }
            return;
        }

        // Look up the form bean definition
        FormBeanConfig formBeanConfig =
            appConfig.findFormBeanConfig(mapping.getName());
        if (formBeanConfig == null) {
            JspException e = new JspException
                (messages.getMessage("formTag.formBean", mapping.getName()));
            pageContext.setAttribute(Action.EXCEPTION_KEY, e,
                                     PageContext.REQUEST_SCOPE);
            throw e;
        }

        // Calculate the required values
        name = mapping.getAttribute();
        scope = mapping.getScope();
        type = formBeanConfig.getType();

    }
}
