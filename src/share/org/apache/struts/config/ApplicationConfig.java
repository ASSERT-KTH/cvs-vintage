/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/config/Attic/ApplicationConfig.java,v 1.17 2002/09/22 06:13:56 martinc Exp $
 * $Revision: 1.17 $
 * $Date: 2002/09/22 06:13:56 $
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


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;



/**
 * <p>The collection of static configuration information that describes a
 * Struts-based application module.  Multiple application modules
 * are identified by a <em>prefix</em> at the beginning of the context
 * relative portion of the request URI.  If no application prefix can be
 * matched, the default configuration (with a prefix equal to a zero-length
 * string) is selected, which is elegantly backwards compatible with the
 * previous Struts behavior that only supported one application.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.17 $ $Date: 2002/09/22 06:13:56 $
 * @since Struts 1.1
 */

public class ApplicationConfig implements Serializable {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct an ApplicationConfig object according to the specified
     * parameter values.
     *
     * @param prefix Context-relative URI prefix for this module
     */
    public ApplicationConfig(String prefix) {

        super();
        this.prefix = prefix;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The set of action configurations for this module, if any,
     * keyed by the <code>path</code> property.
     */
    protected HashMap actionConfigs = new HashMap();


    /**
     * The set of JDBC data source configurations for this
     * module, if any, keyed by the <code>key</code> property.
     */
    protected HashMap dataSources = new HashMap();


    /**
     * The set of exception handling configurations for this
     * module, if any, keyed by the <code>type</code> property.
     */
    protected HashMap exceptions = new HashMap();


    /**
     * The set of form bean configurations for this module, if any,
     * keyed by the <code>name</code> property.
     */
    protected HashMap formBeans = new HashMap();


    /**
     * The set of global forward configurations for this module, if any,
     * keyed by the <code>name</code> property.
     */
    protected HashMap forwards = new HashMap();


    /**
     * The set of message resources configurations for this
     * module, if any, keyed by the <code>key</code> property.
     */
    protected HashMap messageResources = new HashMap();


    /**
     * The set of configured plug-in Actions for this module,
     * if any, in the order they were declared and configured.
     */
    protected ArrayList plugIns = new ArrayList();


    // ------------------------------------------------------------- Properties


    /**
     * Has this module been completely configured yet.  Once this flag
     * has been set, any attempt to modify the configuration will return an
     * IllegalStateException.
     */
    protected boolean configured = false;

    public boolean getConfigured() {
        return (this.configured);
    }


    /**
     * The controller configuration object for this module.
     */
    protected ControllerConfig controllerConfig = null;

    public ControllerConfig getControllerConfig() {
        if (this.controllerConfig == null) {
            this.controllerConfig = new ControllerConfig();
        }
        return (this.controllerConfig);
    }

    public void setControllerConfig(ControllerConfig cc) {
        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }
        this.controllerConfig = cc;
    }


    /**
     * The prefix of the context-relative portion of the request URI, used to
     * select this configuration versus others supported by the controller
     * servlet.  A configuration with a prefix of a zero-length String is the
     * default configuration for this web application.
     */
    protected String prefix = null;

    public String getPrefix() {
        return (this.prefix);
    }


    /**
     * The default class name to be used when creating action mapping
     * instances.
     */
    protected String actionMappingClass =
        "org.apache.struts.action.ActionMapping";

    public String getActionMappingClass() {
        return this.actionMappingClass;
    }

    public void setActionMappingClass(String actionMappingClass) {
        this.actionMappingClass = actionMappingClass;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new <code>ActionConfig</code> instance to the set associated
     * with this module.
     *
     * @param config The new configuration instance to be added
     *
     * @exception IllegalStateException if this module configuration
     *  has been frozen
     */
    public void addActionConfig(ActionConfig config) {

        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }
        config.setApplicationConfig(this);
        actionConfigs.put(config.getPath(), config);

    }


    /**
     * Add a new <code>DataSourceConfig</code> instance to the set associated
     * with this module.
     *
     * @param config The new configuration instance to be added
     *
     * @exception IllegalStateException if this module configuration
     *  has been frozen
     */
    public void addDataSourceConfig(DataSourceConfig config) {

        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }
        dataSources.put(config.getKey(), config);

    }


    /**
     * Add a new <code>ExceptionConfig</code> instance to the set associated
     * with this application.
     *
     * @param config The new configuration instance to be added
     *
     * @exception IllegalStateException if this module configuration
     *  has been frozen
     */
    public void addExceptionConfig(ExceptionConfig config) {

        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }
        exceptions.put(config.getType(), config);

    }


    /**
     * Add a new <code>FormBeanConfig</code> instance to the set associated
     * with this module.
     *
     * @param config The new configuration instance to be added
     *
     * @exception IllegalStateException if this module configuration
     *  has been frozen
     */
    public void addFormBeanConfig(FormBeanConfig config) {

        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }
        formBeans.put(config.getName(), config);

    }


    /**
     * Add a new <code>ForwardConfig</code> instance to the set of global
     * forwards associated with this module.
     *
     * @param config The new configuration instance to be added
     *
     * @exception IllegalStateException if this module configuration
     *  has been frozen
     */
    public void addForwardConfig(ForwardConfig config) {

        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }
        forwards.put(config.getName(), config);

    }


    /**
     * Add a new <code>MessageResourcesConfig</code> instance to the set
     * associated with this module.
     *
     * @param config The new configuration instance to be added
     *
     * @exception IllegalStateException if this module configuration
     *  has been frozen
     */
    public void addMessageResourcesConfig(MessageResourcesConfig config) {

        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }
        messageResources.put(config.getKey(), config);

    }


    /**
     * Add a newly configured {@link PlugInConfig} instance to the set of
     * plug-in Actions for this module.
     *
     * @param plugInConfig The new configuration instance to be added
     */
    public void addPlugInConfig(PlugInConfig plugInConfig) {

        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }
        plugIns.add(plugInConfig);

    }


    /**
     * Return the action configuration for the specified path, if any;
     * otherwise return <code>null</code>.
     *
     * @param path Path of the action configuration to return
     */
    public ActionConfig findActionConfig(String path) {

        return ((ActionConfig) actionConfigs.get(path));

    }


    /**
     * Return the action configurations for this module.  If there are
     * none, a zero-length array is returned.
     */
    public ActionConfig[] findActionConfigs() {

        ActionConfig results[] = new ActionConfig[actionConfigs.size()];
        return ((ActionConfig[]) actionConfigs.values().toArray(results));

    }


    /**
     * Return the data source configuration for the specified key, if any;
     * otherwise return <code>null</code>.
     *
     * @param key Key of the data source configuration to return
     */
    public DataSourceConfig findDataSourceConfig(String key) {

        return ((DataSourceConfig) dataSources.get(key));

    }


    /**
     * Return the data source configurations for this module.  If there
     * are none, a zero-length array is returned.
     */
    public DataSourceConfig[] findDataSourceConfigs() {

        DataSourceConfig results[] = new DataSourceConfig[dataSources.size()];
        return ((DataSourceConfig[]) dataSources.values().toArray(results));

    }


    /**
     * Return the exception configuration for the specified type, if any;
     * otherwise return <code>null</code>.
     *
     * @param type Exception class name to find a configuration for
     */
    public ExceptionConfig findExceptionConfig(String type) {

        return ((ExceptionConfig) exceptions.get(type));

    }


    /**
     * Return the exception configurations for this module.  If there
     * are none, a zero-length array is returned.
     */
    public ExceptionConfig[] findExceptionConfigs() {

        ExceptionConfig results[] = new ExceptionConfig[exceptions.size()];
        return ((ExceptionConfig[]) exceptions.values().toArray(results));

    }


    /**
     * Return the form bean configuration for the specified key, if any;
     * otherwise return <code>null</code>.
     *
     * @param name Name of the form bean configuration to return
     */
    public FormBeanConfig findFormBeanConfig(String name) {

        return ((FormBeanConfig) formBeans.get(name));

    }


    /**
     * Return the form bean configurations for this module.  If there
     * are none, a zero-length array is returned.
     */
    public FormBeanConfig[] findFormBeanConfigs() {

        FormBeanConfig results[] = new FormBeanConfig[formBeans.size()];
        return ((FormBeanConfig[]) formBeans.values().toArray(results));

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
     * Return the form bean configurations for this module.  If there
     * are none, a zero-length array is returned.
     */
    public ForwardConfig[] findForwardConfigs() {

        ForwardConfig results[] = new ForwardConfig[forwards.size()];
        return ((ForwardConfig[]) forwards.values().toArray(results));

    }


    /**
     * Return the message resources configuration for the specified key,
     * if any; otherwise return <code>null</code>.
     *
     * @param key Key of the data source configuration to return
     */
    public MessageResourcesConfig findMessageResourcesConfig(String key) {

        return ((MessageResourcesConfig) messageResources.get(key));

    }


    /**
     * Return the message resources configurations for this module.
     * If there are none, a zero-length array is returned.
     */
    public MessageResourcesConfig[] findMessageResourcesConfigs() {

        MessageResourcesConfig results[] =
            new MessageResourcesConfig[messageResources.size()];
        return ((MessageResourcesConfig[])
                messageResources.values().toArray(results));

    }


    /**
     * Return the configured plug-in actions for this module.  If there
     * are none, a zero-length array is returned.
     */
    public PlugInConfig[] findPlugInConfigs() {

        PlugInConfig results[] = new PlugInConfig[plugIns.size()];
        return ((PlugInConfig[]) plugIns.toArray(results));

    }


    /**
     * Freeze the configuration of this module.  After this method
     * returns, any attempt to modify the configuration will return
     * an IllegalStateException.
     */
    public void freeze() {

        this.configured = true;

        ActionConfig[] aconfigs = findActionConfigs();
        for (int i = 0; i < aconfigs.length; i++) {
            aconfigs[i].freeze();
        }

        getControllerConfig().freeze();

        DataSourceConfig[] dsconfigs = findDataSourceConfigs();
        for (int i = 0; i < dsconfigs.length; i++) {
            dsconfigs[i].freeze();
        }

        ExceptionConfig[] econfigs = findExceptionConfigs();
        for (int i = 0; i < econfigs.length; i++) {
            econfigs[i].freeze();
        }

        FormBeanConfig[] fbconfigs = findFormBeanConfigs();
        for (int i = 0; i < fbconfigs.length; i++) {
            fbconfigs[i].freeze();
        }

        ForwardConfig[] fconfigs = findForwardConfigs();
        for (int i = 0; i < fconfigs.length; i++) {
            fconfigs[i].freeze();
        }

        MessageResourcesConfig[] mrconfigs = findMessageResourcesConfigs();
        for (int i = 0; i < mrconfigs.length; i++) {
            mrconfigs[i].freeze();
        }

        PlugInConfig[] piconfigs = findPlugInConfigs();
        for (int i = 0; i < piconfigs.length; i++) {
            piconfigs[i].freeze();
        }

    }


    /**
     * Remove the specified action configuration instance.
     *
     * @param config ActionConfig instance to be removed
     *
     * @exception IllegalStateException if this module configuration
     *  has been frozen
     */
    public void removeActionConfig(ActionConfig config) {

        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }
        config.setApplicationConfig(null);
        actionConfigs.remove(config.getPath());

    }


    /**
     * Remove the specified exception configuration instance.
     *
     * @param config ActionConfig instance to be removed
     *
     * @exception IllegalStateException if this module configuration
     *  has been frozen
     */
    public void removeExceptionConfig(ExceptionConfig config) {

        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }
        exceptions.remove(config.getType());

    }


    /**
     * Remove the specified data source configuration instance.
     *
     * @param config DataSourceConfig instance to be removed
     *
     * @exception IllegalStateException if this module configuration
     *  has been frozen
     */
    public void removeDataSourceConfig(DataSourceConfig config) {

        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }
        dataSources.remove(config.getKey());

    }


    /**
     * Remove the specified form bean configuration instance.
     *
     * @param config FormBeanConfig instance to be removed
     *
     * @exception IllegalStateException if this module configuration
     *  has been frozen
     */
    public void removeFormBeanConfig(FormBeanConfig config) {

        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }
        formBeans.remove(config.getName());

    }


    /**
     * Remove the specified forward configuration instance.
     *
     * @param config ForwardConfig instance to be removed
     *
     * @exception IllegalStateException if this module configuration
     *  has been frozen
     */
    public void removeForwardConfig(ForwardConfig config) {

        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }
        forwards.remove(config.getName());

    }


    /**
     * Remove the specified message resources configuration instance.
     *
     * @param config MessageResourcesConfig instance to be removed
     *
     * @exception IllegalStateException if this module configuration
     *  has been frozen
     */
    public void removeMessageResourcesConfig(MessageResourcesConfig config) {

        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }
        messageResources.remove(config.getKey());

    }


    // -------------------------------------------------------- Package Methods


    // ------------------------------------------------------ Protected Methods



}
