/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/actions/LookupDispatchAction.java,v 1.18 2003/11/27 19:20:31 dgraham Exp $
 * $Revision: 1.18 $
 * $Date: 2003/11/27 19:20:31 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
 *    any, must include the following acknowledgement:
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
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
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
 
package org.apache.struts.actions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.config.MessageResourcesConfig;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.util.MessageResources;

/**
 *  <p>
 *
 *  An abstract <strong>Action</strong> that dispatches to the subclass mapped
 *  <code>execute</code> method. This is useful in
 *  cases where an HTML form has multiple submit buttons with the same name. The
 *  button name is specified by the <code>parameter</code> property of the
 *  corresponding ActionMapping. To configure the use of this action in your
 *  <code>struts-config.xml</code> file, create an entry like this:</p> <pre>
 *   &lt;action path="/test"
 *           type="org.example.MyAction"
 *           name="MyForm"
 *          scope="request"
 *          input="/test.jsp"
 *      parameter="action"/&gt;
 * </pre> <p>
 *
 *  which will use the value of the request parameter named "action" to locate
 *  the corresponding key in ApplicationResources. For example, you might have
 *  the following ApplicationResources.properties:</p> <pre>
 *    button.add=Add Record
 *    button.delete=Delete Record
 *  </pre><p>
 *
 *  And your JSP would have the following format for submit buttons:</p> <pre>
 *   &lt;html:form action="/test"&gt;
 *    &lt;html:submit property="action"&gt;
 *      &lt;bean:message key="button.add"/&gt;
 *    &lt;/html:submit&gt;
 *    &lt;html:submit property="action"&gt;
 *      &lt;bean:message key="button.delete"/&gt;
 *    &lt;/html:submit&gt;
 *  &lt;/html:form&gt;
 *  </pre> <p>
 *
 *  Your subclass must implement both getKeyMethodMap and the
 *  methods defined in the map. An example of such implementations are:</p>
 * <pre>
 *  protected Map getKeyMethodMap() {
 *      Map map = new HashMap();
 *      map.put("button.add", "add");
 *      map.put("button.delete", "delete");
 *      return map;
 *  }
 *
 *  public ActionForward add(ActionMapping mapping,
 *          ActionForm form,
 *          HttpServletRequest request,
 *          HttpServletResponse response)
 *          throws IOException, ServletException {
 *      // do add
 *      return mapping.findForward("success");
 *  }
 *
 *  public ActionForward delete(ActionMapping mapping,
 *          ActionForm form,
 *          HttpServletRequest request,
 *          HttpServletResponse response)
 *          throws IOException, ServletException {
 *      // do delete
 *      return mapping.findForward("success");
 *  }
 *  <p>
 *
 *  <strong>Notes</strong> - If duplicate values exist for the keys returned by
 *  getKeys, only the first one found will be returned. If no corresponding key
 *  is found then an exception will be thrown. You can override the
 *  method <code>unspecified</code> to provide a custom handler. If the submit
 *  was cancelled (a <code>html:cancel</code> button was pressed), the custom
 *  handler <code>cancelled</code> will be used instead.
 *
 * @author Erik Hatcher
 * @author Scott Carlson
 * @author David Graham
 * @author Leonardo Quijano
 * @author Rob Leland
 * @author Ted Husted
 */
public abstract class LookupDispatchAction extends DispatchAction {

    /**
     * Reverse lookup map from resource value to resource key.
     */
    protected Map localeMap = new HashMap();

    /**
     * Resource key to method name lookup.
     */
    protected Map keyMethodMap = null;

    // ---------------------------------------------------------- Public Methods

    /**
     *  Process the specified HTTP request, and create the corresponding HTTP
     *  response (or forward to another web component that will create it).
     *  Return an <code>ActionForward</code> instance describing where and how
     *  control should be forwarded, or <code>null</code> if the response has
     *  already been completed.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @param form The optional ActionForm bean for this request (if any)
     * @return Describes where and how control should be forwarded.
     * @exception Exception if an error occurs
     */
    public ActionForward execute(
        ActionMapping mapping,
        ActionForm form,
        HttpServletRequest request,
        HttpServletResponse response)
        throws Exception {

        if (isCancelled(request)) {
            ActionForward af = cancelled(mapping, form, request, response);
            if (af != null) {
                return af;
            }
        }
        // Identify the request parameter containing the method name
        String parameter = mapping.getParameter();
        if (parameter == null) {
            String message = messages.getMessage("dispatch.handler", mapping.getPath());
            throw new ServletException(message);
        }

        // Identify the string to lookup
        String methodName = getMethodName(mapping, form, request, response, parameter);

        return dispatchMethod(mapping, form, request, response, methodName);

    }

    /**
     * This is the first time this Locale is used so build the reverse lookup Map.
     * Search for message keys in all configured MessageResources for
     * the current module.
     */
    private Map initLookupMap(HttpServletRequest request, Locale userLocale) {
        Map lookupMap = new HashMap();
        this.keyMethodMap = this.getKeyMethodMap();

        ModuleConfig moduleConfig =
                (ModuleConfig) request.getAttribute(Globals.MODULE_KEY);

        MessageResourcesConfig[] mrc = moduleConfig.findMessageResourcesConfigs();

        // Look through all module's MessageResources
        for (int i = 0; i < mrc.length; i++) {
            MessageResources resources = this.getResources(request, mrc[i].getKey());

            // Look for key in MessageResources
            Iterator iter = this.keyMethodMap.keySet().iterator();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                String text = resources.getMessage(userLocale, key);

                // Found key and haven't added to Map yet, so add the text
                if ((text != null) && !lookupMap.containsKey(text)) {
                    lookupMap.put(text, key);
                }
            }
        }

        return lookupMap;
    }

    /**
     * Provides the mapping from resource key to method name.
     *
     * @return Resource key / method name map.
     */
    protected abstract Map getKeyMethodMap();

    /**
     * Lookup the method name corresponding to the client request's locale.
     *
     * @param request The HTTP request we are processing
     * @param keyName The parameter name to use as the properties key
     * @param mapping The ActionMapping used to select this instance
     *
     * @return The method's localized name.
     * @throws ServletException if keyName cannot be resolved
     * @since Struts 1.2.0
     */ 
    protected String getLookupMapName(
        HttpServletRequest request,
        String keyName,
        ActionMapping mapping)
        throws ServletException {

        // Based on this request's Locale get the lookupMap
        Map lookupMap = null;

        synchronized(localeMap) {
            Locale userLocale = this.getLocale(request);
            lookupMap = (Map) this.localeMap.get(userLocale);

            if (lookupMap == null) {
                lookupMap = this.initLookupMap(request, userLocale);
                this.localeMap.put(userLocale, lookupMap);
            }
        }

        // Find the key for the resource
        String key = (String) lookupMap.get(keyName);
        if (key == null) {
            String message = messages.getMessage(
                    "dispatch.resource", mapping.getPath(), keyName);
            throw new ServletException(message);
        }

        // Find the method name
        String methodName = (String) keyMethodMap.get(key);
        if (methodName == null) {
            String message = messages.getMessage(
                    "dispatch.lookup", mapping.getPath(), key);
            throw new ServletException(message);
        }

        return methodName;
    }

    /**
     * Returns the method name, given a parameter's value.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @param parameter The <code>ActionMapping</code> parameter's name
     *
     * @return The method's name.
     * @since Struts 1.2.0
     */
    protected String getMethodName(
        ActionMapping mapping,
        ActionForm form,
        HttpServletRequest request,
        HttpServletResponse response,
        String parameter)
        throws Exception {

        // Identify the method name to be dispatched to.
        // dispatchMethod() will call unspecified() if name is null
        String keyName = request.getParameter(parameter);
        if (keyName == null || keyName.length() == 0) {
            return null;
        }

        String methodName = getLookupMapName(request, keyName, mapping);

        return methodName;
    }


}
