/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/config/ActionConfig.java,v 1.1 2001/12/26 19:16:25 craigmcc Exp $
 * $Revision: 1.1 $
 * $Date: 2001/12/26 19:16:25 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
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


package org.apache.struts.config;


import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections.FastHashMap;


/**
 * <p>A JavaBean representing the configuration information of an
 * <code>&lt;action&gt;</code> element from a Struts application
 * configuration file.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2001/12/26 19:16:25 $
 * @since Struts 1.1
 */

public class ActionConfig {


    // ----------------------------------------------------- Instance Variables


    /**
     * The set of local forward configurations for this action, if any,
     * keyed by the <code>name</code> property.
     */
    protected FastHashMap forwards = new FastHashMap();


    // ------------------------------------------------------------- Properties


    /**
     * The request-scope or session-scope attribute name under which our
     * form bean is accessed, if it is different from the form bean's
     * specified <code>name</code>.
     */
    protected String attribute = null;

    public String getAttribute() {
        return (this.attribute);
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }


    /**
     * Context-relative path of the web application resource that will process
     * this request via RequestDispatcher.forward(), instead of instantiating
     * and calling the <code>Action</code> class specified by "type".
     * Exactly one of <code>forward</code>, <code>include</code>, or
     * <code>type</code> must be specified.
     */
    protected String forward = null;

    public String getForward() {
        return (this.forward);
    }

    public void setForward(String forward) {
        this.forward = forward;
    }


    /**
     * Context-relative path of the web application resource that will process
     * this request via RequestDispatcher.include(), instead of instantiating
     * and calling the <code>Action</code> class specified by "type".
     * Exactly one of <code>forward</code>, <code>include</code>, or
     * <code>type</code> must be specified.
     */
    protected String include = null;

    public String getInclude() {
        return (this.include);
    }

    public void setInclude(String include) {
        this.include = include;
    }


    /**
     * Context-relative path of the input form to which control should be
     * returned if a validation error is encountered.  Required if "name"
     * is specified and the input bean returns validation errors.
     */
    protected String input = null;

    public String getInput() {
        return (this.input);
    }

    public void setInput(String input) {
        this.input = input;
    }


    /**
     * Fully qualified Java class name of the
     * <code>MultipartRequestHandler</code> implementation class used to
     * process multi-part request data for this Action.
     */
    protected String multipartClass = null;

    public String getMultipartClass() {
        return (this.multipartClass);
    }

    public void setMultipartClass(String multipartClass) {
        this.multipartClass = multipartClass;
    }


    /**
     * Name of the form bean, if any, associated with this Action.
     */
    protected String name = null;

    public String getName() {
        return (this.name);
    }

    public void setName(String name) {
        this.name = name;
    }


    /**
     * Context-relative path of the submitted request, starting with a
     * slash ("/") character, and omitting any filename extension if
     * extension mapping is being used.
     */
    protected String path = null;

    public String getPath() {
        return (this.path);
    }

    public void setPath(String path) {
        this.path = path;
    }


    /**
     * General purpose configuration parameter that can be used to pass
     * extra iunformation to the Action instance selected by this Action.
     * Struts does not itself use this value in any way.
     */
    protected String parameter = null;

    public String getParameter() {
        return (this.parameter);
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }


    /**
     * Prefix used to match request parameter names to form ben property
     * names, if any.
     */
    protected String prefix = null;

    public String getPrefix() {
        return (this.prefix);
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }


    /**
     * Comma-delimited list of security role names allowed to request
     * this Action.
     */
    protected String roles = null;

    public String getRoles() {
        return (this.roles);
    }

    public void setRoles(String roles) {
        this.roles = roles;
        if (roles == null) {
            roleNames = new String[0];
            return;
        }
        ArrayList list = new ArrayList();
        while (true) {
            int comma = roles.indexOf(',');
            if (comma < 0)
                break;
            list.add(roles.substring(1, comma).trim());
            roles = roles.substring(comma + 1);
        }
        roles = roles.trim();
        if (roles.length() > 0)
            list.add(roles);
        roleNames = (String[]) list.toArray(new String[list.size()]);
    }


    /**
     * The set of security role names used to authorize access to this
     * Action, as an array for faster access.
     */
    protected String[] roleNames = new String[0];

    public String[] getRoleNames() {
        return (this.roleNames);
    }


    /**
     * Identifier of the scope ("request" or "session") within which
     * our form bean is accessed, if any.
     */
    protected String scope = "session";

    public String getScope() {
        return (this.scope);
    }

    public void setScope(String scope) {
        this.scope = scope;
    }


    /**
     * Suffix used to match request parameter names to form bean property
     * names, if any.
     */
    protected String suffix = null;

    public String getSuffix() {
        return (this.suffix);
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }


    /**
     * Fully qualified Java class name of the <code>Action</code> class
     * to be used to process requests for this mapping if the
     * <code>forward</code> and <code>include</code> properties are not set.
     * Exactly one of <code>forward</code>, <code>include</code>, or
     * <code>type</code> must be specified.
     */
    protected String type = null;

    public String getType() {
        return (this.type);
    }

    public void setType(String type) {
        this.type = type;
    }


    /**
     * Should this Action be configured as the default one for this
     * application?
     */
    protected boolean unknown = false;

    public boolean getUnknown() {
        return (this.unknown);
    }

    public void setUnknown(boolean unknown) {
        this.unknown = unknown;
    }

    /**
     * Should the <code>validate()</code> method of the form bean associated
     * with this action be called?
     */
    protected boolean validate = true;

    public boolean getValidate() {
        return (this.validate);
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new <code>ForwardConfig</code> object to the set of global
     * forwards associated with this application.
     *
     * @param config The new configuration object to be added
     *
     * @exception IllegalStateException if this application configuration
     *  has been frozen
     */
    public void addForwardConfig(ForwardConfig config) {

        forwards.put(config.getName(), config);

    }


    /**
     * Return the forward configuration for the specified key, if any;
     * otherwise return <code>null</code>.
     *
     * @param name Name of the forward configuration to return
     */
    public ForwardConfig findForwardConfig(String name) {

        return ((ForwardConfig) forwards.get(name));

    }


    /**
     * Return the form bean configurations for this application.  If there
     * are none, a zero-length array is returned.
     */
    public ForwardConfig[] findForwardConfigs() {

        ForwardConfig results[] = new ForwardConfig[forwards.size()];
        return ((ForwardConfig[]) forwards.values().toArray(results));

    }


    /**
     * Remove the forward configuration for the specified key.
     *
     * @param config ForwardConfig instance to be removed
     *
     * @exception IllegalStateException if this application configuration
     *  has been frozen
     */
    public void removeForwardConfig(ForwardConfig config) {

        forwards.remove(config.getName());

    }


    /**
     * Return a String representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("ActionConfig[");
        sb.append("path=");
        sb.append(path);
        if (attribute != null) {
            sb.append(",attribute=");
            sb.append(attribute);
        }
        if (forward != null) {
            sb.append(",forward=");
            sb.append(forward);
        }
        if (include != null) {
            sb.append(",include=");
            sb.append(include);
        }
        if (input != null) {
            sb.append(",input=");
            sb.append(input);
        }
        if (multipartClass != null) {
            sb.append(",multipartClass=");
            sb.append(multipartClass);
        }
        if (name != null) {
            sb.append(",name=");
            sb.append(name);
        }
        if (parameter != null) {
            sb.append(",parameter=");
            sb.append(parameter);
        }
        if (prefix != null) {
            sb.append(",prefix=");
            sb.append(prefix);
        }
        if (roles != null) {
            sb.append(",roles=");
            sb.append(roles);
        }
        if (scope != null) {
            sb.append(",scope=");
            sb.append(scope);
        }
        if (suffix != null) {
            sb.append(",suffix=");
            sb.append(suffix);
        }
        if (type != null) {
            sb.append(",type=");
            sb.append(type);
        }
        return (sb.toString());

    }


}
