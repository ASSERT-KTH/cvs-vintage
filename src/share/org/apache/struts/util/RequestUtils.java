/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/util/RequestUtils.java,v 1.13 2001/05/12 20:34:01 craigmcc Exp $
 * $Revision: 1.13 $
 * $Date: 2001/05/12 20:34:01 $
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


package org.apache.struts.util;


import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionForwards;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.taglib.html.Constants;
import org.apache.struts.upload.FormFile;
import org.apache.struts.upload.MultipartRequestHandler;


/**
 * General purpose utility methods related to processing a servlet request
 * in the Struts controller framework.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.13 $ $Date: 2001/05/12 20:34:01 $
 */

public class RequestUtils {


    // ------------------------------------------------------- Static Variables


    /**
     * The default Locale for our server.
     */
    private static final Locale defaultLocale = Locale.getDefault();


    /**
     * The message resources for this package.
     */
    private static MessageResources messages =
	MessageResources.getMessageResources
	("org.apache.struts.util.LocalStrings");



    // --------------------------------------------------------- Public Methods


    /**
     * Create and return an absolute URL for the specified context-relative
     * path, based on the server and context information in the specified
     * request.
     *
     * @param request The servlet request we are processing
     * @param path The context-relative path (must start with '/')
     *
     * @exception MalformedURLException if we cannot create an absolute URL
     */
    public static URL absoluteURL(HttpServletRequest request, String path)
        throws MalformedURLException {

        return (new URL(serverURL(request), path));

    }


    /**
     * Compute a set of query parameters that will be dynamically added to
     * a generated URL.  The returned Map is keyed by parameter name, and the
     * values are either null (no value specified), a String (single value
     * specified), or a String[] array (multiple values specified).  Parameter
     * names correspond to the corresponding attributes of the
     * <code>&lt;html:link&gt;</code> tag.  If no query parameters are
     * identified, return <code>null</code>.
     *
     * @param pageContext PageContext we are operating in
     *
     * @param paramId Single-value request parameter name (if any)
     * @param paramName Bean containing single-value parameter value
     * @param paramProperty Property (of bean named by <code>paramName</code>
     *  containing single-value parameter value
     * @param paramScope Scope containing bean named by
     *  <code>paramScope</code>
     *
     * @param name Bean containing multi-value parameters Map (if any)
     * @param property Property (of bean named by <code>name</code>
     *  containing multi-value parameters Map
     * @param scope Scope containing bean named by
     *  <code>name</code>
     *
     * @param transaction Should we add our transaction control token?
     *
     * @exception JspException if we cannot look up the required beans
     * @exception JspException if a class cast exception occurs on a
     *  looked-up bean or property
     */
    public static Map computeParameters(PageContext pageContext,
                                        String paramId, String paramName,
                                        String paramProperty,
                                        String paramScope, String name,
                                        String property, String scope,
                                        boolean transaction)
        throws JspException {

        // Short circuit if no parameters are specified
        if ((paramId == null) && (name == null))
            return (null);

        // Locate the Map containing our multi-value parameters map
        Map map = null;
        try {
            map = (Map) lookup(pageContext, name,
                               property, scope);
        } catch (ClassCastException e) {
            saveException(pageContext, e);
            throw new JspException
                (messages.getMessage("parameters.multi", name,
                                     property, scope));
        } catch (JspException e) {
            saveException(pageContext, e);
            throw e;
        }

        // Create a Map to contain our results from the multi-value parameters
        Map results = null;
        if (map != null)
            results = new HashMap(map);
        else
            results = new HashMap();

        // Add the single-value parameter (if any)
        if (paramId != null) {
            String paramValue = null;
            try {
                paramValue =(String) lookup(pageContext, paramName,
                                            paramProperty, paramScope);
            } catch (ClassCastException e) {
                saveException(pageContext, e);
                throw new JspException
                    (messages.getMessage("parameters.single", paramName,
                                         paramProperty, paramScope));
            } catch (JspException e) {
                saveException(pageContext, e);
                throw e;
            }
            Object mapValue = map.get(paramId);
            if (mapValue == null)
                map.put(paramId, paramValue);
            else if (mapValue instanceof String) {
                String newValues[] = new String[2];
                newValues[0] = (String) mapValue;
                newValues[1] = paramValue;
                map.put(paramId, newValues);
            } else /* if (mapValue instanceof String[]) */ {
                String oldValues[] = (String[]) mapValue;
                String newValues[] = new String[oldValues.length + 1];
                System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
                newValues[oldValues.length] = paramValue;
                map.put(paramId, newValues);
            }
        }

        // Add our transaction control token (if requested)
        if (transaction) {
            HttpSession session = pageContext.getSession();
            String token = null;
            if (session != null)
                token = (String)
                    session.getAttribute(Action.TRANSACTION_TOKEN_KEY);
            if (token != null)
                map.put(Constants.TOKEN_KEY, token);
        }

        // Return the completed Map
        return (results);

    }


    /**
     * Compute a hyperlink URL based on the <code>forward</code>,
     * <code>href</code>, or <code>page</code> parameter that is not null.
     * The returned URL will have already been passed to
     * <code>response.encodeURL()</code> for adding a session identifier.
     *
     * @param pageContext PageContext for the tag making this call
     *
     * @param forward Logical forward name for which to look up
     *  the context-relative URI (if specified)
     * @param href URL to be utilized unmodified (if specified)
     * @param page Context-relative page for which a URL should
     *  be created (if specified)
     *
     * @param params Map of parameters to be dynamically included (if any)
     * @param anchor Anchor to be dynamically included (if any)
     *
     * @param redirect Is this URL for a <code>response.sendRedirect()</code>?
     *
     * @exception MalformedURLException if a URL cannot be created
     *  for the specified parameters
     */
    public static String computeURL(PageContext pageContext, String forward,
                                    String href, String page,
                                    Map params, String anchor,
                                    boolean redirect)
        throws MalformedURLException {

        // Validate that exactly one specifier was included
        int n = 0;
        if (forward != null)
            n++;
        if (href != null)
            n++;
        if (page != null)
            n++;
        if (n != 1)
            throw new MalformedURLException
                (messages.getMessage("computeURL.specifier"));

        // Calculate the appropriate URL
        StringBuffer url = new StringBuffer();
        HttpServletRequest request =
            (HttpServletRequest) pageContext.getRequest();
        if (forward != null) {
            ActionForwards forwards = (ActionForwards)
                pageContext.getAttribute(Action.FORWARDS_KEY,
                                         PageContext.APPLICATION_SCOPE);
            if (forwards == null)
                throw new MalformedURLException
                    (messages.getMessage("computeURL.forwards"));
            ActionForward af = forwards.findForward(forward);
            if (af == null)
                throw new MalformedURLException
                    (messages.getMessage("computeURL.forward", forward));
            if (af.getRedirect())
                redirect = true;
            if (af.getPath().startsWith("/"))
                url.append(request.getContextPath());
            url.append(af.getPath());
        } else if (href != null) {
            url.append(href);
        } else /* if (page != null) */ {
            url.append(request.getContextPath());
            url.append(page);
        }

        // Add anchor if requested (replacing any existing anchor)
        if (anchor != null) {
            String temp = url.toString();
            int hash = temp.indexOf('#');
            if (hash >= 0)
                url.setLength(hash);
            url.append('#');
            url.append(URLEncoder.encode(anchor));
        }
        
        // Add dynamic parameters if requested
        if ((params != null) && (params.size() > 0)) {

            // Save any existing anchor
            String temp = url.toString();
            int hash = temp.indexOf('#');
            if (hash >= 0) {
                anchor = temp.substring(hash + 1);
                url.setLength(hash);
                temp = url.toString();
            } else
                anchor = null;

            // Add the required request parameters
            boolean question = temp.indexOf('?') >= 0;
            Iterator keys = params.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                Object value = params.get(key);
                if (value == null) {
                    if (!question) {
                        url.append('?');
                        question = true;
                    } else
                        url.append('&');
                    url.append(URLEncoder.encode(key));
                    url.append('='); // Interpret null as "no value"
                } else if (value instanceof String) {
                    if (!question) {
                        url.append('?');
                        question = true;
                    } else
                        url.append('&');
                    url.append(URLEncoder.encode(key));
                    url.append('=');
                    url.append(URLEncoder.encode((String) value));
                } else /* if (value instanceof String[]) */ {
                    String values[] = (String[]) value;
                    for (int i = 0; i < values.length; i++) {
                        if (!question) {
                            url.append('?');
                            question = true;
                        } else
                            url.append('&');
                        url.append(URLEncoder.encode(key));
                        url.append('=');
                        url.append(URLEncoder.encode(values[i]));
                    }
                }
            }

            // Re-add the saved anchor (if any)
            if (anchor != null) {
                url.append('#');
                url.append(anchor);
            }

        }

        // Perform URL rewriting to include our session ID (if any)
        if (pageContext.getSession() != null) {
            HttpServletResponse response =
                (HttpServletResponse) pageContext.getResponse();
            if (redirect)
                return (response.encodeRedirectURL(url.toString()));
            else
                return (response.encodeURL(url.toString()));
        } else
            return (url.toString());

    }


    /**
     * Locate and return the specified bean, from an optionally specified
     * scope, in the specified page context.  If no such bean is found,
     * return <code>null</code> instead.  If an exception is thrown, it will
     * have already been saved via a call to <code>saveException()</code>.
     *
     * @param pageContext Page context to be searched
     * @param name Name of the bean to be retrieved
     * @param scope Scope to be searched (page, request, session, application)
     *  or <code>null</code> to use <code>findAttribute()</code> instead
     *
     * @exception JspException if an invalid scope name
     *  is requested
     */
    public static Object lookup(PageContext pageContext, String name,
    String scope) throws JspException {

        Object bean = null;
        if (scope == null)
            bean = pageContext.findAttribute(name);
        else if (scope.equalsIgnoreCase("page"))
            bean = pageContext.getAttribute(name, PageContext.PAGE_SCOPE);
        else if (scope.equalsIgnoreCase("request"))
            bean = pageContext.getAttribute(name, PageContext.REQUEST_SCOPE);
        else if (scope.equalsIgnoreCase("session"))
            bean = pageContext.getAttribute(name, PageContext.SESSION_SCOPE);
        else if (scope.equalsIgnoreCase("application"))
            bean =
                pageContext.getAttribute(name, PageContext.APPLICATION_SCOPE);
        else {
            JspException e = new JspException
                (messages.getMessage("lookup.scope", scope));
            saveException(pageContext, e);
            throw e;
        }

        return (bean);

    }


    /**
     * Locate and return the specified property of the specified bean, from
     * an optionally specified scope, in the specified page context.  If an
     * exception is thrown, it will have already been saved via a call to
     * <code>saveException()</code>.
     *
     * @param pageContext Page context to be searched
     * @param name Name of the bean to be retrieved
     * @param property Name of the property to be retrieved, or
     *  <code>null</code> to retrieve the bean itself
     * @param scope Scope to be searched (page, request, session, application)
     *  or <code>null</code> to use <code>findAttribute()</code> instead
     *
     * @exception JspException if an invalid scope name
     *  is requested
     * @exception JspException if the specified bean is not found
     * @exception JspException if accessing this property causes an
     *  IllegalAccessException, IllegalArgumentException,
     *  InvocationTargetException, or NoSuchMethodException
     */
    public static Object lookup(PageContext pageContext, String name,
                                String property, String scope)
        throws JspException {

        // Look up the requested bean, and return if requested
        Object bean = lookup(pageContext, name, scope);
        if (property == null)
            return (bean);
        if (bean == null) {
            JspException e = new JspException
                (messages.getMessage("lookup.bean", name, scope));
            saveException(pageContext, e);
            throw e;
        }

        // Locate and return the specified property
        try {
            return (PropertyUtils.getProperty(bean, property));
        } catch (IllegalAccessException e) {
            saveException(pageContext, e);
            throw new JspException
                (messages.getMessage("lookup.access", property, name));
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t == null)
                t = e;
            saveException(pageContext, t);
            throw new JspException
                (messages.getMessage("lookup.target", property, name));
        } catch (NoSuchMethodException e) {
            saveException(pageContext, e);
            throw new JspException
                (messages.getMessage("lookup.method", property, name));
        }

    }


    /**
     * Look up and return a message string, based on the specified parameters.
     *
     * @param pageContext The PageContext associated with this request
     * @param bundle Name of the servlet context attribute for our
     *  message resources bundle
     * @param locale Name of the session attribute for our user's Locale
     * @param key Message key to be looked up and returned
     *
     * @exception JspException if a lookup error occurs (will have been
     *  saved in the request already)
     */
    public static String message(PageContext pageContext, String bundle,
                                 String locale, String key)
        throws JspException {

        return (message(pageContext, bundle, locale, key, null));

    }


    /**
     * Look up and return a message string, based on the specified parameters.
     *
     * @param pageContext The PageContext associated with this request
     * @param bundle Name of the servlet context attribute for our
     *  message resources bundle
     * @param locale Name of the session attribute for our user's Locale
     * @param key Message key to be looked up and returned
     * @param args Replacement parameters for this message
     *
     * @exception JspException if a lookup error occurs (will have been
     *  saved in the request already)
     */
    public static String message(PageContext pageContext, String bundle,
                                 String locale, String key, Object args[])
        throws JspException {

        // Look up the requested MessageResources
        if (bundle == null)
            bundle = Action.MESSAGES_KEY;
        MessageResources resources = (MessageResources)
            pageContext.getAttribute(bundle, PageContext.APPLICATION_SCOPE);
        if (resources == null) {
            JspException e = new JspException
                (messages.getMessage("message.bundle", bundle));
            saveException(pageContext, e);
            throw e;
        }

        // Look up the requested Locale
        if (locale == null)
            locale = Action.LOCALE_KEY;
        Locale userLocale = (Locale)
            pageContext.getAttribute(locale, PageContext.SESSION_SCOPE);
        if (userLocale == null)
            userLocale = defaultLocale;

        // Return the requested message
        if (args == null)
            return (resources.getMessage(userLocale, key));
        else
            return (resources.getMessage(userLocale, key, args));

    }


    /**
     * Populate the properties of the specified JavaBean from the specified
     * HTTP request, based on matching each parameter name against the
     * corresponding JavaBeans "property setter" methods in the bean's class.
     * Suitable conversion is done for argument types as described under
     * <code>convert()</code>.
     *
     * @param bean The JavaBean whose properties are to be set
     * @param request The HTTP request whose parameters are to be used
     *                to populate bean properties
     *
     * @exception ServletException if an exception is thrown while setting
     *            property values
     */
    public static void populate(Object bean,
                                HttpServletRequest request)
        throws ServletException {

        populate(bean, null, null, request);

    }


    /**
     * Populate the properties of the specified JavaBean from the specified
     * HTTP request, based on matching each parameter name (plus an optional
     * prefix and/or suffix) against the corresponding JavaBeans "property
     * setter" methods in the bean's class.  Suitable conversion is done for
     * argument types as described under <code>setProperties()</code>.
     * <p>
     * If you specify a non-null <code>prefix</code> and a non-null
     * <code>suffix</code>, the parameter name must match <strong>both</strong>
     * conditions for its value(s) to be used in populating bean properties.
     * If the request's content type is "multipart/form-data" and the
     * method is "POST", the HttpServletRequest object will be wrapped in
     * a MultipartRequestWrapper object.
     *
     * @param bean The JavaBean whose properties are to be set
     * @param prefix The prefix (if any) to be prepend to bean property
     *               names when looking for matching parameters
     * @param suffix The suffix (if any) to be appended to bean property
     *               names when looking for matching parameters
     * @param request The HTTP request whose parameters are to be used
     *                to populate bean properties
     *
     * @exception ServletException if an exception is thrown while setting
     *            property values
     */
    public static void populate(Object bean, String prefix, String suffix,
                                HttpServletRequest request)
        throws ServletException {

        // Build a list of relevant request parameters from this request
        HashMap properties = new HashMap();
        // Iterator of parameter names
        Enumeration names = null;
        //Hashtable for multipart values
        Hashtable multipartElements = null;

        boolean isMultipart = false;
        String contentType = request.getContentType();
        String method = request.getMethod();
        if ((contentType != null) &&
            (contentType.startsWith("multipart/form-data")) &&
            (method.equalsIgnoreCase("POST"))) {
            isMultipart = true;
            //initialize a MultipartRequestHandler
            MultipartRequestHandler multipart = null;

            //get an instance of ActionServlet
            ActionServlet servlet;

            if (bean instanceof ActionForm) {
                servlet = ((ActionForm) bean).getServlet();
            } else {
                throw new ServletException("bean that's supposed to be " +
                                           "populated from a multipart request is not of type " +
                                           "\"org.apache.struts.action.ActionForm\", but type " +
                                           "\"" + bean.getClass().getName() + "\"");
            }
            String multipartClass = (String)
                request.getAttribute(Action.MULTIPART_KEY);
            request.removeAttribute(Action.MULTIPART_KEY);

            if (multipartClass != null) {
                //try to initialize the mapping specific request handler
                try {
                    multipart = (MultipartRequestHandler) Class.forName(multipartClass).newInstance();
                }
                catch (ClassNotFoundException cnfe) {
                    servlet.log("MultipartRequestHandler class \"" +
                    multipartClass + "\" in mapping class not found, " +
                    "defaulting to global multipart class");
                }
                catch (InstantiationException ie) {
                    servlet.log("InstantiaionException when instantiating " +
                    "MultipartRequestHandler \"" + multipartClass + "\", " +
                    "defaulting to global multipart class, exception: " +
                    ie.getMessage());
                }
                catch (IllegalAccessException iae) {
                    servlet.log("IllegalAccessException when instantiating " +
                    "MultipartRequestHandler \"" + multipartClass + "\", " +
                    "defaulting to global multipart class, exception: " +
                    iae.getMessage());
                }
            }

            if (multipart == null) {
                //try to initialize the global multipart class
                try {
                    multipart = (MultipartRequestHandler) Class.forName(servlet.getMultipartClass()).newInstance();
                }
                catch (ClassNotFoundException cnfe) {
                    throw new ServletException("Cannot find multipart class \"" +
                    servlet.getMultipartClass() + "\"" +
                    ", exception: " + cnfe.getMessage());
                }
                catch (InstantiationException ie) {
                    throw new ServletException("InstantiaionException when instantiating " +
                    "multipart class \"" + servlet.getMultipartClass() +
                    "\", exception: " + ie.getMessage());
                }
                catch (IllegalAccessException iae) {
                    throw new ServletException("IllegalAccessException when instantiating " +
                    "multipart class \"" + servlet.getMultipartClass() +
                    "\", exception: " + iae.getMessage());
                }
            }


            //set the multipart request handler for our ActionForm
            //if the bean isn't an ActionForm, an exception would have been
            //thrown earlier, so it's safe to assume that our bean is
            //in fact an ActionForm
            ((ActionForm) bean).setMultipartRequestHandler(multipart);

            //set servlet and mapping info
            multipart.setServlet(servlet);
            multipart.setMapping((ActionMapping)
                                 request.getAttribute(Action.MAPPING_KEY));
            request.removeAttribute(Action.MAPPING_KEY);

            //initialize request class handler
            multipart.handleRequest(request);

            //retrive form values and put into properties
            multipartElements = multipart.getAllElements();
            names = multipartElements.keys();
        }

        if (!isMultipart) {
            names = request.getParameterNames();
        }


        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            String stripped = name;
            int subscript = stripped.lastIndexOf("[");
            if (prefix != null) {
                if (!stripped.startsWith(prefix))
                    continue;
                stripped = stripped.substring(prefix.length());
            }
            if (suffix != null) {
                if (!stripped.endsWith(suffix))
                    continue;
                stripped =
                    stripped.substring(0, stripped.length() - suffix.length());
            }
            if (isMultipart) {
                properties.put(stripped, multipartElements.get(name));
            }
            else {
                properties.put(stripped, request.getParameterValues(name));
            }
        }

        // Set the corresponding properties of our bean
        try {
            BeanUtils.populate(bean, properties);
        } catch (Exception e) {
            throw new ServletException("BeanUtils.populate", e);
        }

    }


    /**
     * Return true if a message string for the specified message key
     * is present for the specified Locale.
     *
     * @param pageContext The PageContext associated with this request
     * @param bundle Name of the servlet context attribute for our
     *  message resources bundle
     * @param locale Name of the session attribute for our user's Locale
     * @param key Message key to be looked up and returned
     *
     * @exception JspException if a lookup error occurs (will have been
     *  saved in the request already)
     */
    public static boolean present(PageContext pageContext, String bundle,
                                  String locale, String key)
        throws JspException {

        // Look up the requested MessageResources
        if (bundle == null)
            bundle = Action.MESSAGES_KEY;
        MessageResources resources = (MessageResources)
            pageContext.getAttribute(bundle, PageContext.APPLICATION_SCOPE);
        if (resources == null) {
            JspException e = new JspException
                (messages.getMessage("message.bundle", bundle));
            saveException(pageContext, e);
            throw e;
        }

        // Look up the requested Locale
        if (locale == null)
            locale = Action.LOCALE_KEY;
        Locale userLocale = (Locale)
            pageContext.getAttribute(locale, PageContext.SESSION_SCOPE);
        if (userLocale == null)
            userLocale = defaultLocale;

        // Return the requested message presence indicator
        return (resources.isPresent(userLocale, key));

    }


    /**
     * Compute the printable representation of a URL, leaving off the
     * scheme/host/port part if no host is specified.  This will typically
     * be the case for URLs that were originally created from relative
     * or context-relative URIs.
     *
     * @param url URL to render in a printable representation
     */
    public static String printableURL(URL url) {

        if (url.getHost() != null)
            return (url.toString());

        String file = url.getFile();
        String ref = url.getRef();
        if (ref == null)
            return (file);
        else {
            StringBuffer sb = new StringBuffer(file);
            sb.append('#');
            sb.append(ref);
            return (sb.toString());
        }

    }


    /**
     * Return the URL representing the current request.  This is equivalent
     * to <code>HttpServletRequest.getRequestURL()</code> in Servlet 2.3.
     *
     * @param request The servlet request we are processing
     *
     * @exception MalformedURLException if a URL cannot be created
     */
    public static URL requestURL(HttpServletRequest request)
        throws MalformedURLException {

        StringBuffer url = new StringBuffer();
        String scheme = request.getScheme();
        int port = request.getServerPort();
        if (port < 0)
            port = 80; // Work around java.net.URL bug
        url.append(scheme);
        url.append("://");
        url.append(request.getServerName());
        if ((scheme.equals("http") && (port != 80)) ||
            (scheme.equals("https") && (port != 443))) {
            url.append(':');
            url.append(port);
        }
        url.append(request.getRequestURI());
        return (new URL(url.toString()));

    }


    /**
     * Return the URL representing the scheme, server, and port number of
     * the current request.  Server-relative URLs can be created by simply
     * appending the server-relative path (starting with '/') to this.
     *
     * @param request The servlet request we are processing
     *
     * @exception MalformedURLException if a URL cannot be created
     */
    public static URL serverURL(HttpServletRequest request)
        throws MalformedURLException {

        StringBuffer url = new StringBuffer();
        String scheme = request.getScheme();
        int port = request.getServerPort();
        if (port < 0)
            port = 80; // Work around java.net.URL bug
        url.append(scheme);
        url.append("://");
        url.append(request.getServerName());
        if ((scheme.equals("http") && (port != 80)) ||
            (scheme.equals("https") && (port != 443))) {
            url.append(':');
            url.append(port);
        }
        return (new URL(url.toString()));

    }


    /**
     * Save the specified exception as a request attribute for later use.
     *
     * @param pageContext The PageContext for the current page
     * @param exception The exception to be saved
     */
    public static void saveException(PageContext pageContext,
                                     Throwable exception) {

        pageContext.setAttribute(Action.EXCEPTION_KEY, exception,
                                 PageContext.REQUEST_SCOPE);

    }


}
