/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/action/ActionServlet.java,v 1.85 2002/01/15 18:51:26 craigmcc Exp $
 * $Revision: 1.85 $
 * $Date: 2002/01/15 18:51:26 $
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


package org.apache.struts.action;


import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.FastHashMap;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.struts.config.ApplicationConfig;
import org.apache.struts.config.ConfigRuleSet;
import org.apache.struts.config.ControllerConfig;
import org.apache.struts.config.DataSourceConfig;
import org.apache.struts.config.MessageResourcesConfig;
import org.apache.struts.taglib.html.Constants;
import org.apache.struts.upload.MultipartRequestWrapper;
import org.apache.struts.util.GenericDataSource;
import org.apache.struts.util.MessageResources;
import org.apache.struts.util.MessageResourcesFactory;
import org.apache.struts.util.RequestUtils;
import org.apache.struts.util.ServletContextWriter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * <p><strong>ActionServlet</strong> represents the "controller" in the
 * Model-View-Controller (MVC) design pattern for web applications that is
 * commonly known as "Model 2".  This nomenclature originated with a
 * description in the JavaServerPages Specification, version 0.92, and has
 * persisted ever since (in the absence of a better name).</p>
 *
 * <p>Generally, a "Model 2" application is architected as follows:</p>
 * <ul>
 * <li>The user interface will generally be created with JSP pages, which
 *     will not themselves contain any business logic.  These pages represent
 *     the "view" component of an MVC architecture.</li>
 * <li>Forms and hyperlinks in the user interface that require business logic
 *     to be executed will be submitted to a request URI that is mapped to the
 *     controller servlet.</li>
 * <li>There will be one instance of this servlet class,
 *     which receives and processes all requests that change the state of
 *     a user's interaction with the application.  This component represents
 *     the "controller" component of an MVC architecture.</li>
 * <li>The controller servlet will select and invoke an action class to perform
 *     the requested business logic.</li>
 * <li>The action classes will manipulate the state of the application's
 *     interaction with the user, typically by creating or modifying JavaBeans
 *     that are stored as request or session attributes (depending on how long
 *     they need to be available).  Such JavaBeans represent the "model"
 *     component of an MVC architecture.</li>
 * <li>Instead of producing the next page of the user interface directly,
 *     action classes will generally use the
 *     <code>RequestDispatcher.forward()</code> facility of the servlet API
 *     to pass control to an appropriate JSP page to produce the next page
 *     of the user interface.</li>
 * </ul>
 *
 * <p>The standard version of <code>ActionServlet</code> implements the
 *    following logic for each incoming HTTP request.  You can override
 *    some or all of this functionality by subclassing this servlet and
 *    implementing your own version of the processing.</p>
 * <ul>
 * <li>Identify, from the incoming request URI, the substring that will be
 *     used to select an action procedure.</li>
 * <li>Use this substring to map to the Java class name of the corresponding
 *     action class (an implementation of the <code>Action</code> interface).
 *     </li>
 * <li>If this is the first request for a particular action class, instantiate
 *     an instance of that class and cache it for future use.</li>
 * <li>Optionally populate the properties of an <code>ActionForm</code> bean
 *     associated with this mapping.</li>
 * <li>Call the <code>perform()</code> method of this action class, passing
 *     on a reference to the mapping that was used (thereby providing access
 *     to the underlying ActionServlet and ServletContext, as well as any
 *     specialized properties of the mapping itself), and the request and
 *     response that were passed to the controller by the servlet container.
 *     </li>
 * </ul>
 *
 * <p>The standard version of <code>ActionServlet</code> is configured based
 * on the following servlet initialization parameters, which you will specify
 * in the web application deployment descriptor (<code>/WEB-INF/web.xml</code>)
 * for your application.  Subclasses that specialize this servlet are free to
 * define additional initialization parameters.</p>
 * <ul>
 * <li><strong>application</strong> - Java class name of the application
 *     resources bundle base class.  [NONE]
 *     <em>DEPRECATED - Configure this using the "parameter" attribute
 *     of the &lt;message-resources&gt; element.</em></li>
 * <li><strong>bufferSize</strong> - The size of the input buffer used when
 *     processing file uploads.  [4096]
 *     <em>DEPRECATED - Configure this using the "bufferSize" attribute
 *     of the &lt;controller&gt; element.</em></li>
 * <li><strong>config</strong> - Context-relative path to the XML resource
 *     containing the configuration information for the default application.
 *     [/WEB-INF/struts-config.xml]</li>
 * <li><strong>config/foo</strong> - Context-relative path to the XML resource
 *     containing the configuration information for the sub-application that
 *     will be at prefix "/foo".  This can be repeated as many times as
 *     required for multiple sub-applications.</li>
 * <li><strong>content</strong> - Default content type and character encoding
 *     to be set on each response; may be overridden by a forwarded-to
 *     servlet or JSP page.  [text/html]
 *     <em>DEPRECATED - Configure this using the "contentType" attribute
 *     of the &lt;controller&gt; element.</em></li>
 * <li><strong>debug</strong> - The debugging detail level for this
 *     servlet, which controls how much information is logged.  [0]
 * <li><strong>detail</strong> - The debugging detail level for the Digester
 *     we utilize to process the application configuration files.</li>
 * <li><strong>factory</strong> - The Java class name of the
 *     <code>MessageResourcesFactory</code> used to create the application
 *     <code>MessageResources</code> object.
 *     [org.apache.struts.util.PropertyMessageResourcesFactory]
 *     <em>DEPRECATED - Configure this using the "factory" attribute
 *     of the &lt;message-resources&gt; element.</em></li>
 * <li><strong>formBean</strong> - The Java class name of the ActionFormBean
 *     implementation to use [org.apache.struts.action.ActionFormBean].
 *     <em>DEPRECATED - Configure this using the "className" attribute
 *     of each &lt;form-bean&gt; element.</em></li>
 * <li><strong>forward</strong> - The Java class name of the ActionForward
 *     implementation to use [org.apache.struts.action.ActionForward].
 *     Two convenient classes you may wish to use are:
 *     <ul>
 *     <li><em>org.apache.struts.action.ForwardingActionForward</em> -
 *         Subclass of <code>org.apache.struts.action.ActionForward</code>
 *         that defaults the <code>redirect</code> property to
 *         <code>false</code> (same as the ActionForward default value).
 *     <li><em>org.apache.struts.action.RedirectingActionForward</em> -
 *         Subclass of <code>org.apache.struts.action.ActionForward</code>
 *         that defaults the <code>redirect</code> property to
 *         <code>true</code>.
 *     </ul>
 *     <em>DEPRECATED - Configure this using the "className" attribute of
 *     each &lt;forward&gt; element.</em></li>
 * <li><strong>locale</strong> - If set to <code>true</code>, and there is a
 *     user session, identify and store an appropriate
 *     <code>java.util.Locale</code> object (under the standard key
 *     identified by <code>Action.LOCALE_KEY</code>) in the user's session
 *     if there is not a Locale object there already. [true]
 *     <em>DEPRECATED - Configure this using the "locale" attribute of
 *     the &lt;controller&gt; element.</em></li>
 * <li><strong>mapping</strong> - The Java class name of the ActionMapping
 *     implementation to use [org.apache.struts.action.ActionMapping].
 *     Two convenient classes you may wish to use are:
 *     <ul>
 *     <li><em>org.apache.struts.action.RequestActionMapping</em> - Subclass
 *         of <code>org.apache.struts.action.ActionMapping</code> that
 *         defaults the <code>scope</code> property to "request".
 *     <li><em>org.apache.struts.action.SessionActionMapping</em> - Subclass
 *         of <code>org.apache.struts.action.ActionMapping</code> that
 *         defaults the <code>scope</code> property to "session".  (Same
 *         as the ActionMapping default value).
 *     </ul>
 *     <em>DEPRECATED - Configure this using the "className" attribute of
 *     each &lt;action&gt; element.</em></li>
 * <li><strong>maxFileSize</strong> - The maximum size (in bytes) of a file
 *     to be accepted as a file upload.  Can be expressed as a number followed
 *     by a "K" "M", or "G", which are interpreted to mean kilobytes,
 *     megabytes, or gigabytes, respectively.  [250M]
 *     <em>DEPRECATED - Configure this using the "maxFileSize" attribute of
 *     the &lt;controller&gt; element.</em></li>
 * <li><strong>multipartClass</strong> - The fully qualified name of the
 *     MultiplartRequestHandler implementation class to be used for processing
 *     file uploads. If set to <code>none</code>, disables Struts multipart
 *     request handling.  [org.apache.struts.upload.DiskMultipartRequestHandler]
 *     FIXME - check this</li>
 * <li><strong>nocache</strong> - If set to <code>true</code>, add HTTP headers
 *     to every response intended to defeat browser caching of any response we
 *     generate or forward to.  [false]
 *     <em>DEPRECATED - Configure this using the "nocache" attribute of
 *     the &lt;controller&gt; element.</em></li>
 * <li><strong>null</strong> - If set to <code>true</code>, set our application
 *     resources to return <code>null</code> if an unknown message key is used.
 *     Otherwise, an error message including the offending message key will
 *     be returned.  [true]
 *     <em>DEPRECATED - Configure this using the "null" attribute of
 *     the &lt;message-resources&gt; element.</em></li>
 * <li><strong>tempDir</strong> - The temporary working directory to use when
 *     processing file uploads.  [The working directory provided to this web
 *     application as a servlet context attribute]
 *     <em>DEPRECATED - Configure this using the "tempDir" attribute of
 *     the &lt;controller&gt; element.</em></li>
 * <li><strong>validating</strong> - Should we use a validating XML parse to
 *     process the configuration file (strongly recommended)? [true]</li>
 * </ul>
 *
 * @author Craig R. McClanahan
 * @author Ted Husted
 * @version $Revision: 1.85 $ $Date: 2002/01/15 18:51:26 $
 */

public class ActionServlet
    extends HttpServlet {


    // ----------------------------------------------------- Instance Variables


    /**
     * The context-relative path to our configuration resource for the
     * default sub-application.
     */
    protected String config = "/WEB-INF/struts-config.xml";


    /**
     * The Digester used to produce ApplicationConfig objects from a
     * Struts configuration file.
     */
    protected Digester configDigester = null;


    /**
     * The JDBC data sources that has been configured for this application,
     * if any, keyed by the servlet context attribute under which they are
     * stored.
     */
    protected FastHashMap dataSources = new FastHashMap();


    /**
     * The debugging detail level for this servlet.
     */
    protected int debug = 0;


    /**
     * The debugging detail level for configuration file parsing.
     */
    protected int detail = 0;


    /**
     * The resources object for our internal resources.
     */
    protected MessageResources internal = null;


    /**
     * The Java base name of our internal resources.
     */
    protected String internalName = "org.apache.struts.action.ActionResources";


    /**
     * The <code>RequestProcessor</code> instance we will use to process
     * all incoming requests.
     */
    protected RequestProcessor processor = null;


    /**
     * The set of public identifiers, and corresponding resource names, for
     * the versions of the configuration file DTDs that we know about.  There
     * <strong>MUST</strong> be an even number of Strings in this list!
     */
    protected String registrations[] = {
        "-//Apache Software Foundation//DTD Struts Configuration 1.0//EN",
        "/org/apache/struts/resources/struts-config_1_0.dtd",
        "-//Apache Software Foundation//DTD Struts Configuration 1.1//EN",
        "/org/apache/struts/resources/struts-config_1_1.dtd",
        "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN",
        "/org/apache/struts/resources/web-app_2_2.dtd",
        "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN",
        "/org/apache/struts/resources/web-app_2_3.dtd"
    };


    /**
     * The URL pattern to which we are mapped in our web application
     * deployment descriptor.  FIXME - multiples???
     */
    protected String servletMapping = null;


    /**
     * The servlet name under which we are registered in our web application
     * deployment descriptor.
     */
    protected String servletName = null;


    /**
     * Should we use a validating XML parser to read the configuration file?
     */
    protected boolean validating = true;



    // ---------------------------------------------------- HttpServlet Methods


    /**
     * Gracefully shut down this controller servlet, releasing any resources
     * that were allocated at initialization.
     */
    public void destroy() {

    if (debug >= 1)
        log(internal.getMessage("finalizing"));

        destroyDataSources();
        destroyApplications();
        destroyInternal();

        // FIXME - destroy ApplicationConfig and message resource instances

    }


    /**
     * Initialize this servlet.  Most of the processing has been factored into
     * support methods so that you can override particular functionality at a
     * fairly granular level.
     *
     * @exception ServletException if we cannot configure ourselves correctly
     */
    public void init() throws ServletException {

        initInternal();
        initOther();
        initServlet();

        // Initialize sub-applications as needed
        ApplicationConfig ac = initApplicationConfig("", config);
        initApplicationMessageResources(ac);
        initApplicationDataSources(ac);
        Enumeration names = getServletConfig().getInitParameterNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            if (!name.startsWith("config/")) {
                continue;
            }
            String prefix = name.substring(6);
            ac = initApplicationConfig
                (prefix, getServletConfig().getInitParameter(name));
            initApplicationMessageResources(ac);
            initApplicationDataSources(ac);
        }
        destroyConfigDigester();

    }


    /**
     * Process an HTTP "GET" request.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public void doGet(HttpServletRequest request,
              HttpServletResponse response)
        throws IOException, ServletException {

        process(request, response);

    }


    /**
     * Process an HTTP "POST" request.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public void doPost(HttpServletRequest request,
               HttpServletResponse response)
        throws IOException, ServletException {

        process(request, response);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Remember a servlet mapping from our web application deployment
     * descriptor, if it is for this servlet.
     *
     * @param servletName The name of the servlet being mapped
     * @param urlPattern The URL pattern to which this servlet is mapped
     */
    public void addServletMapping(String servletName, String urlPattern) {

        if (debug >= 1)
            log("Process servletName=" + servletName +
                ", urlPattern=" + urlPattern);
        if (servletName == null)
            return;
        if (servletName.equals(this.servletName))
            this.servletMapping = urlPattern;

    }


    /**
     * Return a JDBC data source associated with this application, if any.
     *
     * @param key The servlet context attribute key under which this data
     *  source is stored, or <code>null</code> for the default.
     *
     * @deprecated Look up data sources directly in servlet context attributes
     */
    public DataSource findDataSource(String key) {

        if (key == null)
            return ((DataSource) dataSources.get(Action.DATA_SOURCE_KEY));
        else
            return ((DataSource) dataSources.get(key));

    }


    /**
     * Return the global ActionForward for the specified logical name, for
     * the default sub-application.
     *
     * @param name Logical name of the requested forwarding
     *
     * @deprecated Call ActionMapping.findForward() to get the forwarding
     *  that is local to the current sub-application
     */
    public ActionForward findForward(String name) {

        ApplicationConfig appConfig = (ApplicationConfig)
            getServletContext().getAttribute(Action.APPLICATION_KEY);
        return ((ActionForward) appConfig.findForwardConfig(name));

    }


    /**
     * Return the ActionMapping for the specified path, for the default
     * sub-application.
     *
     * @param path Request path for which a mapping is requested
     *
     * @deprecated Get the ApplicationConfig object from request attribute
     *  key Action.APPLIATION_KEY and call findActionConfig() to get
     *  mappings for the current sub-application
     */
    public ActionMapping findMapping(String path) {

        ApplicationConfig appConfig = (ApplicationConfig)
            getServletContext().getAttribute(Action.APPLICATION_KEY);
        return ((ActionMapping) appConfig.findActionConfig(path));
    }


    /**
     * Return the debugging detail level for this servlet.
     */
    public int getDebug() {

        return (this.debug);

    }


    /**
     * Return the <code>MessageResources</code> instance containing our
     * internal message strings.
     */
    public MessageResources getInternal() {

        return (this.internal);

    }


    /**
     * Log the specified message if the current debugging detail level for
     * this servlet has been set to an equal or higher value.  Otherwise,
     * ignore this message.
     *
     * @param message Message to be logged
     * @param level Debugging detail level of this message
     */
    public void log(String message, int level) {

        if (debug >= level)
            log(message);

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Gracefully terminate use of any sub-applications associated with this
     * application (if any).
     */
    protected void destroyApplications() {

        Enumeration names = getServletContext().getAttributeNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            Object value = getServletContext().getAttribute(name);
            if (value instanceof ApplicationConfig) {
                try {
                    ((ApplicationConfig) value).getProcessor().destroy();
                } catch (Throwable t) {
                    ;
                }
            }
        }

    }


    /**
     * Gracefully release any configDigester instance that we have created.
     */
    protected void destroyConfigDigester() {

        configDigester = null;

    }


    /**
     * Gracefully terminate use of the data source associated with this
     * application (if any).
     *
     * @deprecated Will no longer be required with multi-application support
     */
    protected void destroyDataSources() {

        synchronized (dataSources) {
            Iterator keys = dataSources.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                getServletContext().removeAttribute(key);
                DataSource dataSource = findDataSource(key);
                if (dataSource instanceof GenericDataSource) {
                    if (debug >= 1)
                        log(internal.getMessage("dataSource.destroy", key));
                    try {
                        ((GenericDataSource) dataSource).close();
                    } catch (SQLException e) {
                        log(internal.getMessage("destroyDataSource", key), e);
                    }
                }
            }
            dataSources.clear();
        }

    }


    /**
     * Gracefully terminate use of the internal MessageResources.
     */
    protected void destroyInternal() {

        internal = null;

    }


    /**
     * Return the application configuration object for the currently selected
     * sub-application.
     *
     * @param request The servlet request we are processing
     */
    protected ApplicationConfig getApplicationConfig
        (HttpServletRequest request) {

        ApplicationConfig config = (ApplicationConfig)
            request.getAttribute(Action.APPLICATION_KEY);
        if (config == null) {
            config = (ApplicationConfig)
                getServletContext().getAttribute(Action.APPLICATION_KEY);
        }
        return (config);

    }


    /**
     * <p>Initialize the application configuration information for the
     * specified sub-application.</p>
     *
     * @param prefix Application prefix for this application
     * @param path Context-relative resource path for this application's
     *  configuration resource
     *
     * @exception ServletException if initialization cannot be performed
     */
    protected ApplicationConfig initApplicationConfig
        (String prefix, String path) throws ServletException {

        if (debug >= 1) {
            log("Initializing application path '" + prefix +
                "' configuration from '" + path + "'");
        }

        // Parse the application configuration for this application
        ApplicationConfig config = null;
        InputStream input = null;
        String value = null;
        try {
            config = new ApplicationConfig(prefix, this);
            Digester digester = initConfigDigester();
            digester.push(config);
            input = getServletContext().getResourceAsStream(path);
            digester.parse(input);
            input.close();
            getServletContext().setAttribute
                (Action.APPLICATION_KEY + prefix, config);
        } catch (Throwable t) {
            log(internal.getMessage("configParse", path), t);
            throw new UnavailableException
                (internal.getMessage("configParse", path));
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    ;
                }
            }
        }

        // Is this the configuration for the default sub-application?
        if (prefix.length() > 0) {
            config.freeze();
            return (config);
        }

        // Special handling for the default app's ControllerConfig
        ControllerConfig cc = config.getControllerConfig();
        value = getServletConfig().getInitParameter("bufferSize");
        if (value != null) {
            cc.setBufferSize(Integer.parseInt(value));
        }
        value = getServletConfig().getInitParameter("content");
        if (value != null) {
            cc.setContentType(value);
        }
        value = getServletConfig().getInitParameter("locale");
        if (value != null) {
            if (value.equalsIgnoreCase("true") ||
                value.equalsIgnoreCase("yes")) {
                cc.setLocale(true);
            } else {
                cc.setLocale(false);
            }
        }
        value = getServletConfig().getInitParameter("maxFileSize");
        if (value != null) {
            cc.setMaxFileSize(value);
        }
        value = getServletConfig().getInitParameter("nocache");
        if (value != null) {
            if (value.equalsIgnoreCase("true") ||
                value.equalsIgnoreCase("yes")) {
                cc.setNocache(true);
            } else {
                cc.setNocache(false);
            }
        }
        value = getServletConfig().getInitParameter("tempDir");
        if (value != null) {
            cc.setTempDir(value);
        }

        // Special handling for the default app's MessageResourcesConfig
        MessageResourcesConfig mrc = config.getMessageResourcesConfig();
        value = getServletConfig().getInitParameter("application");
        if (value != null) {
            mrc.setParameter(value);
        }
        value= getServletConfig().getInitParameter("factory");
        if (value != null) {
            mrc.setFactory(value);
        }
        value = getServletConfig().getInitParameter("null");
        if (value != null) {
            if (value.equalsIgnoreCase("true") ||
                value.equalsIgnoreCase("yes")) {
                mrc.setNull(true);
            } else {
                mrc.setNull(false);
            }
        }
        config.freeze();
        return (config);

    }


    /**
     * <p>Initialize the application data sources for the specified
     * sub-application.</p>
     *
     * @param config ApplicationConfig information for this application
     *
     * @exception ServletException if initialization cannot be performed
     */
    protected void initApplicationDataSources
        (ApplicationConfig config) throws ServletException {

        if (debug >= 1) {
            log("Initializing application path '" + config.getPrefix() +
                "' data sources");
        }

        ServletContextWriter scw =
            new ServletContextWriter(getServletContext());
        DataSourceConfig dscs[] = config.findDataSourceConfigs();
        if (dscs == null) {
            return;
        }
        dataSources.setFast(false);
        for (int i = 0; i < dscs.length; i++) {
            if (debug >= 1) {
                log("Initializing application path '" + config.getPrefix() +
                    "' data source '" + dscs[i].getKey() + "'");
            }
            DataSource ds = null;
            try {
                // FIXME - Support user-specified data source classes again
                ds = new GenericDataSource();
                PropertyUtils.copyProperties(ds, dscs[i]);
                ds.setLogWriter(scw);
                if (ds instanceof GenericDataSource) {
                    ((GenericDataSource) ds).open();
                }
            } catch (Throwable t) {
                log(internal.getMessage
                    ("dataSource.init", dscs[i].getKey()), t);
                throw new UnavailableException
                    (internal.getMessage("dataSource.init", dscs[i].getKey()));
            }
            getServletContext().setAttribute(dscs[i].getKey(), ds);
            dataSources.put(dscs[i].getKey(), ds);
        }
        dataSources.setFast(true);

    }


    /**
     * <p>Initialize the application MessageResources for the specified
     * sub-application.</p>
     *
     * @param config ApplicationConfig information for this application
     *
     * @exception ServletException if initialization cannot be performed
     */
    protected void initApplicationMessageResources
        (ApplicationConfig config) throws ServletException {

        MessageResourcesConfig mrc = config.getMessageResourcesConfig();
        if ((mrc.getFactory() == null) || (mrc.getParameter() == null)) {
            return;
        }
        if (debug >= 1) {
            log("Initializing application path '" + config.getPrefix() +
                "' message resources from '" + mrc.getParameter() + "'");
        }

        try {
            String factory = mrc.getFactory();
            MessageResourcesFactory.setFactoryClass(factory);
            MessageResourcesFactory factoryObject =
                MessageResourcesFactory.createFactory();
            MessageResources resources =
                factoryObject.createResources(mrc.getParameter());
            resources.setReturnNull(mrc.getNull());
            getServletContext().setAttribute
                (Action.MESSAGES_KEY + config.getPrefix(), resources);
        } catch (Throwable t) {
            log(internal.getMessage
                ("applicationResources", mrc.getParameter()), t);
            throw new UnavailableException
                (internal.getMessage
                 ("applicationResources", mrc.getParameter()));
        }

    }


    /**
     * <p>Create (if needed) and return a new Digester instance that has been
     * initialized to process Struts application configuraiton files and
     * configure a corresponding ApplicationConfig object (which must be
     * pushed on to the evaluation stack before parsing begins).</p>
     */
    protected Digester initConfigDigester() {

        // Do we have an existing instance?
        if (configDigester != null) {
            return (configDigester);
        }

        // Create and return a new Digester instance
        configDigester = new Digester();
        configDigester.setDebug(detail);
        configDigester.setNamespaceAware(true);
        configDigester.setValidating(validating);
        configDigester.addRuleSet(new ConfigRuleSet());
        for (int i = 0; i < registrations.length; i += 2) {
            URL url = this.getClass().getResource(registrations[i+1]);
            if (url != null)
                configDigester.register(registrations[i], url.toString());
        }
        return (configDigester);
    }


    /**
     * Initialize our internal MessageResources bundle.
     *
     * @exception ServletException if we cannot initialize these resources
     */
    protected void initInternal() throws ServletException {

        try {
            internal = MessageResources.getMessageResources(internalName);
        } catch (MissingResourceException e) {
            log("Cannot load internal resources from '" + internalName + "'",
                e);
            throw new UnavailableException
                ("Cannot load internal resources from '" + internalName + "'");
        }

    }


    /**
     * Initialize other global characteristics of the controller servlet.
     *
     * @exception ServletException if we cannot initialize these resources
     */
    protected void initOther() throws ServletException {

        String value = null;
        value = getServletConfig().getInitParameter("config");
        if (value != null) {
            config = value;
        }
        try {
            value = getServletConfig().getInitParameter("debug");
            debug = Integer.parseInt(value);
        } catch (Throwable t) {
            debug = 0;
        }
        try {
            value = getServletConfig().getInitParameter("detail");
            detail = Integer.parseInt(value);
        } catch (Throwable t) {
            detail = 0;
        }
        value = getServletConfig().getInitParameter("validating");
        if (value != null) {
            if (value.equalsIgnoreCase("true") ||
                value.equalsIgnoreCase("yes"))
                validating = true;
            else
                validating = false;
        }

    }


    /**
     * Initialize the servlet mapping under which our controller servlet
     * is being accessed.  This will be used in the <code>&html:form&gt;</code>
     * tag to generate correct destination URLs for form submissions.
     */
    protected void initServlet() throws ServletException {

        // Remember our servlet name
        this.servletName = getServletConfig().getServletName();

        // Prepare a Digester to scan the web application deployment descriptor
        Digester digester = new Digester();
        digester.push(this);
        digester.setDebug(this.debug);
        digester.setNamespaceAware(true);
        digester.setValidating(false);

        // Register our local copy of the DTDs that we can find
        for (int i = 0; i < registrations.length; i += 2) {
            URL url = this.getClass().getResource(registrations[i+1]);
            if (url != null)
                digester.register(registrations[i], url.toString());
        }

        // Configure the processing rules that we need
        digester.addCallMethod("web-app/servlet-mapping",
                               "addServletMapping", 2);
        digester.addCallParam("web-app/servlet-mapping/servlet-name", 0);
        digester.addCallParam("web-app/servlet-mapping/url-pattern", 1);

        // Process the web application deployment descriptor
        if (debug >= 1)
            log("Scanning web.xml for controller servlet mapping");
        InputStream input= null;
        try {
            input =
                getServletContext().getResourceAsStream("/WEB-INF/web.xml");
            digester.parse(input);
        } catch (Throwable e) {
            log(internal.getMessage("configWebXml"), e);
        } finally {
            if (input != null)
                input = null;
        }

        // Record a servlet context attribute (if appropriate)
        if (debug >= 1)
            log("Mapping for servlet '" + servletName + "' = '" +
                servletMapping + "'");
        if (servletMapping != null)
            getServletContext().setAttribute(Action.SERVLET_KEY,
                                             servletMapping);

    }


    /**
     * Perform the standard request processing for this request, and create
     * the corresponding response.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception is thrown
     */
    protected void process(HttpServletRequest request,
                           HttpServletResponse response)
        throws IOException, ServletException {

        RequestUtils.selectApplication(request, getServletContext());
        getApplicationConfig(request).getProcessor().process
            (request, response);

    }


}
