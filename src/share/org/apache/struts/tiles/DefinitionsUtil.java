/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/tiles/DefinitionsUtil.java,v 1.15 2004/01/13 12:48:51 husted Exp $
 * $Revision: 1.15 $
 * $Date: 2004/01/13 12:48:51 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
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

package org.apache.struts.tiles;

import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.taglib.tiles.ComponentConstants;

/**
 * Utilities class for definitions factory.
 * Also define userDebugLevel property (TODO to be moved from this class ?).
 * @deprecated Use {@link TilesUtil#createDefinitionsFactory(ServletContext, DefinitionsFactoryConfig)}
 */
public class DefinitionsUtil extends TilesUtil implements ComponentConstants {

    /** 
     * Commons Logging instance. 
     */
    protected static Log log = LogFactory.getLog(DefinitionsUtil.class);

    /** 
     * Global user defined debug level.
     * @deprecated This will be removed in a release after Struts 1.2. 
     */
    public static int userDebugLevel = 0;

    /** 
     * User Debug level. 
     * @deprecated This will be removed in a release after Struts 1.2. 
     */
    public static final int NO_DEBUG = 0;

    /** 
     * Name of init property carrying debug level. 
     */
    public static final String DEFINITIONS_CONFIG_USER_DEBUG_LEVEL =
        "definitions-debug";

    /** 
     * Name of init property carrying factory class name. 
     */
    public static final String DEFINITIONS_FACTORY_CLASSNAME =
        "definitions-factory-class";

    /** 
     * Constant name used to store factory in context. 
     */
    public static final String DEFINITIONS_FACTORY =
        "org.apache.struts.tiles.DEFINITIONS_FACTORY";

    /** 
     * Constant name used to store definition in jsp context.
     * Used to pass definition from a Struts action to servlet forward. 
     */
    public static final String ACTION_DEFINITION =
        "org.apache.struts.tiles.ACTION_DEFINITION";

    /**
     * Create Definition factory.
     * If a factory class name is provided, a factory of this class is created. Otherwise,
     * default factory is created.
     * @param classname Class name of the factory to create.
     * @param servletContext Servlet Context passed to newly created factory.
     * @param properties Map of name/property used to initialize factory configuration object.
     * @return newly created factory.
     * @throws DefinitionsFactoryException If an error occur while initializing factory
     * @deprecated Use createDefinitionsFactory(ServletContext servletContext, ServletConfig servletConfig)
     */
    public static DefinitionsFactory createDefinitionsFactory(
        ServletContext servletContext,
        Map properties,
        String classname)
        throws DefinitionsFactoryException {

        // Create config object
        DefinitionsFactoryConfig factoryConfig = new DefinitionsFactoryConfig();
        // populate it from map.
        try {
            factoryConfig.populate(properties);

        } catch (Exception ex) {
            throw new DefinitionsFactoryException(
                "Error - createDefinitionsFactory : Can't populate config object from properties map",
                ex);
        }

        // Add classname
        if (classname != null)
            factoryConfig.setFactoryClassname(classname);

        // Create factory using config object
        return createDefinitionsFactory(servletContext, factoryConfig);
    }

    /**
     * Create default Definition factory.
     * @param servletContext Servlet Context passed to newly created factory.
     * @param properties Map of name/property used to initialize factory configuration object.
     * @return newly created factory of type ConfigurableDefinitionsFactory.
     * @throws DefinitionsFactoryException If an error occur while initializing factory
     */
    public static DefinitionsFactory createDefinitionsFactory(
        ServletContext servletContext,
        Map properties)
        throws DefinitionsFactoryException {

        return createDefinitionsFactory(servletContext, properties, null);
    }

    /**
     * Create Definition factory.
     * Create configuration object from servlet web.xml file, then create
     * ConfigurableDefinitionsFactory and initialized it with object.
     * <p>
     * Convenience method. Calls createDefinitionsFactory(ServletContext servletContext, DefinitionsFactoryConfig factoryConfig)
     *
     * @param servletContext Servlet Context passed to newly created factory.
     * @param servletConfig Servlet config containing parameters to be passed to factory configuration object.
     * @return newly created factory of type ConfigurableDefinitionsFactory.
     * @throws DefinitionsFactoryException If an error occur while initializing factory
     */
    public static DefinitionsFactory createDefinitionsFactory(
        ServletContext servletContext,
        ServletConfig servletConfig)
        throws DefinitionsFactoryException {

        DefinitionsFactoryConfig factoryConfig = readFactoryConfig(servletConfig);

        return createDefinitionsFactory(servletContext, factoryConfig);
    }

    /**
     * Create Definition factory.
     * Create configuration object from servlet web.xml file, then create
     * ConfigurableDefinitionsFactory and initialized it with object.
     * <p>
     * If checkIfExist is true, start by checking if factory already exist. If yes,
     * return it. If no, create a new one.
     * <p>
     * If checkIfExist is false, factory is always created.
     * <p>
     * Convenience method. Calls createDefinitionsFactory(ServletContext servletContext, DefinitionsFactoryConfig factoryConfig)
     *
     * @param servletContext Servlet Context passed to newly created factory.
     * @param servletConfig Servlet config containing parameters to be passed to factory configuration object.
     * @param checkIfExist Check if factory already exist. If true and factory exist, return it.
     * If true and factory doesn't exist, create it. If false, create it in all cases.
     * @return newly created factory of type ConfigurableDefinitionsFactory.
     * @throws DefinitionsFactoryException If an error occur while initializing factory
     */
    public static DefinitionsFactory createDefinitionsFactory(
        ServletContext servletContext,
        ServletConfig servletConfig,
        boolean checkIfExist)
        throws DefinitionsFactoryException {

        if (checkIfExist) {
            // Check if already exist in context
            DefinitionsFactory factory = getDefinitionsFactory(servletContext);
            if (factory != null)
                return factory;
        }

        return createDefinitionsFactory(servletContext, servletConfig);
    }

    /**
     * Get definition factory from appropriate servlet context.
     * @return Definitions factory or null if not found.
     * @deprecated Use {@link TilesUtil#getDefinitionsFactory(ServletRequest, ServletContext)}
     * @since 20020708
     */
    public static DefinitionsFactory getDefinitionsFactory(ServletContext servletContext) {
        return (DefinitionsFactory) servletContext.getAttribute(DEFINITIONS_FACTORY);
    }

    /**
     * Get Definition stored in jsp context by an action.
     * @return ComponentDefinition or null if not found.
     */
    public static ComponentDefinition getActionDefinition(ServletRequest request) {
        return (ComponentDefinition) request.getAttribute(ACTION_DEFINITION);
    }

    /**
     * Store definition in jsp context.
     * Mainly used by Struts to pass a definition defined in an Action to the forward.
     */
    public static void setActionDefinition(
        ServletRequest request,
        ComponentDefinition definition) {

        request.setAttribute(ACTION_DEFINITION, definition);
    }

    /**
     * Remove Definition stored in jsp context.
     * Mainly used by Struts to pass a definition defined in an Action to the forward.
     */
    public static void removeActionDefinition(
        ServletRequest request,
        ComponentDefinition definition) {

        request.removeAttribute(ACTION_DEFINITION);
    }

    /**
     * Populate Definition Factory Config from web.xml properties.
     * @param factoryConfig Definition Factory Config to populate.
     * @param servletConfig Current servlet config containing web.xml properties.
     * @exception IllegalAccessException if the caller does not have
     *  access to the property accessor method
     * @exception java.lang.reflect.InvocationTargetException if the property accessor method
     *  throws an exception
     * @see org.apache.commons.beanutils.BeanUtils
     * @since tiles 20020708
     */
    public static void populateDefinitionsFactoryConfig(
        DefinitionsFactoryConfig factoryConfig,
        ServletConfig servletConfig)
        throws IllegalAccessException, InvocationTargetException {

        Map properties = new DefinitionsUtil.ServletPropertiesMap(servletConfig);
        factoryConfig.populate(properties);
    }

    /**
     * Create FactoryConfig and initialize it from web.xml.
     *
     * @param servletConfig ServletConfig for the module with which
     *  this plug in is associated
     * @exception DefinitionsFactoryException if this <code>PlugIn</code> cannot
     *  be successfully initialized
     */
    protected static DefinitionsFactoryConfig readFactoryConfig(ServletConfig servletConfig)
        throws DefinitionsFactoryException {

        // Create tiles definitions config object
        DefinitionsFactoryConfig factoryConfig = new DefinitionsFactoryConfig();

        // Get init parameters from web.xml files
        try {
            DefinitionsUtil.populateDefinitionsFactoryConfig(
                factoryConfig,
                servletConfig);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new DefinitionsFactoryException(
                "Can't populate DefinitionsFactoryConfig class from 'web.xml'.",
                ex);
        }

        return factoryConfig;
    }

    /**
     * Inner class.
     * Wrapper for ServletContext init parameters.
     * Object of this class is an hashmap containing parameters and values
     * defined in the servlet config file (web.xml).
     */
    static class ServletPropertiesMap extends HashMap {
        /**
         * Constructor.
         */
        ServletPropertiesMap(ServletConfig config) {
            // This implementation is very simple.
            // It is possible to avoid creation of a new structure, but this need
            // imply writing all Map interface.
            Enumeration e = config.getInitParameterNames();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                put(key, config.getInitParameter(key));
            }
        }
    } // end inner class

}
