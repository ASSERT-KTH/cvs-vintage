/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/tiles/DefinitionsUtil.java,v 1.2 2002/07/11 16:40:34 cedric Exp $
 * $Revision: 1.2 $
 * $Date: 2002/07/11 16:40:34 $
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


package org.apache.struts.tiles;

import javax.servlet.jsp.PageContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletRequest;

import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.struts.tiles.definition.ReloadableDefinitionsFactory;
import org.apache.struts.tiles.definition.ConfigurableDefinitionsFactory;
import org.apache.struts.tiles.definition.ComponentDefinitionsFactoryWrapper;
import org.apache.struts.tiles.xmlDefinition.I18nFactorySet;
import org.apache.struts.taglib.tiles.ComponentConstants;

import org.apache.commons.beanutils.BeanUtils;

/**
 * Utilities class for definitions factory.
 * Also define userDebugLevel property (to be moved from this class ?).
 * (to do).
 */
public class DefinitionsUtil implements ComponentConstants
{

    /** Global user defined debug level */
  public static int userDebugLevel = 0;
    /** User Debug level */
  static public final int NO_DEBUG = 0;

    /**
     * Name of init property carrying debug level
     * @deprecated use DEFINITIONS_CONFIG_USER_DEBUG_LEVEL instead.
     */
  public static final String INSTANCES_CONFIG_USER_DEBUG_LEVEL = "instances-debug";
    /** Name of init property carrying debug level */
  public static final String DEFINITIONS_CONFIG_USER_DEBUG_LEVEL = "definitions-debug";
    /** Name of init property carrying factory class name */
  public static final String DEFINITIONS_FACTORY_CLASSNAME = "definitions-factory-class";
    /** Constant name used to store factory in context */
  public static final String DEFINITIONS_FACTORY = "org.apache.struts.tiles.DEFINITIONS_FACTORY";
    /** Constant name used to store definition in jsp context.
     *  Used to pass definition from a Struts action to servlet forward */
  public static final String ACTION_DEFINITION = "org.apache.struts.tiles.ACTION_DEFINITION";


    /**
     * Set user debug level. This property control level of errors output.
     */
  public static void setUserDebugLevel( int level )
    {
    userDebugLevel = level;
    }

   /**
   * Create Definition factory.
   * If
   * Create MapperCollection, and put it in appropriate servlet context.
   * This method is used to initialize Component Instances. It is usually
   * called by the initialization servlet.
   * @param servletContext
   * @return newly created MapperCollection.
   * @throws DefinitionsFactoryException If an error occur while initializing factory
   * @deprecated Use createDefinitionsFactory instead.
   */
  public static void initUserDebugLevel(ServletConfig servletConfig)
  {
    // Set user debug level
  try {
    String str = servletConfig.getInitParameter(DEFINITIONS_CONFIG_USER_DEBUG_LEVEL);
    if( str == null )
      { // Check if we use old keyword
      str = servletConfig.getInitParameter(INSTANCES_CONFIG_USER_DEBUG_LEVEL);
      }
    if( str != null )
      {
      int level = Integer.parseInt( str );
      setUserDebugLevel( level );
      if( userDebugLevel > 1 )
        System.out.println( "Component Definitions debug level = " +  userDebugLevel );
      }
    }
   catch(Exception ex)
    {  // silently fail
    System.out.println( "Set user level fail" );
    ex.printStackTrace();
    }
  }

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
  public static DefinitionsFactory createDefinitionsFactory(ServletContext servletContext, Map properties, String classname)
    throws DefinitionsFactoryException
  {
    // Create config object
  DefinitionsFactoryConfig factoryConfig = new DefinitionsFactoryConfig();
    // populate it from map.
  try
    {
    factoryConfig.populate( properties );
    }
   catch(Exception ex )
    {
    throw new DefinitionsFactoryException( "Error - createDefinitionsFactory : Can't populate config object from properties map", ex );
    }
    // Add classname
  if( classname != null )
    factoryConfig.setFactoryClassname(classname);
    // Create factory using config object
  return  createDefinitionsFactory( servletContext, factoryConfig );
  }

   /**
   * Create default Definition factory.
   * @param servletContext Servlet Context passed to newly created factory.
   * @param properties Map of name/property used to initialize factory configuration object.
   * @return newly created factory of type ConfigurableDefinitionsFactory.
   * @throws DefinitionsFactoryException If an error occur while initializing factory
   */
  public static DefinitionsFactory createDefinitionsFactory(ServletContext servletContext, Map properties)
    throws DefinitionsFactoryException
  {
  return createDefinitionsFactory( servletContext, properties, null );
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
  public static DefinitionsFactory createDefinitionsFactory(ServletContext servletContext, ServletConfig servletConfig)
    throws DefinitionsFactoryException
  {
    // Read factory config
  DefinitionsFactoryConfig factoryConfig = readFactoryConfig(servletConfig);
    // Create factory using config object
  return createDefinitionsFactory( servletContext, factoryConfig );
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
  public static DefinitionsFactory createDefinitionsFactory(ServletContext servletContext, ServletConfig servletConfig, boolean checkIfExist)
    throws DefinitionsFactoryException
  {
  if( checkIfExist )
    {
      // Check if already exist in context
    DefinitionsFactory factory = getDefinitionsFactory( servletContext);
    if( factory != null )
      return factory;
    }
    // creation
  return createDefinitionsFactory( servletContext, servletConfig);
  }

    /**
     * Create Definition factory from specified configuration object.
     * Create a ConfigurableDefinitionsFactory and initialize it with the configuration
     * object. This later can contains the factory classname to use.
     * Factory is made accessible from tags.
     * <p>
     * Fallback of several factory creation methods.
     *
     * @param servletContext Servlet Context passed to newly created factory.
     * @param factoryConfig Configuration object passed to factory.
     * @return newly created factory of type ConfigurableDefinitionsFactory.
     * @throws DefinitionsFactoryException If an error occur while initializing factory
     */
  public static DefinitionsFactory createDefinitionsFactory(ServletContext servletContext, DefinitionsFactoryConfig factoryConfig)
    throws DefinitionsFactoryException
  {
      // Set user debug level
    setUserDebugLevel( factoryConfig.getDebugLevel() );
      // Create configurable factory
    DefinitionsFactory factory = new ConfigurableDefinitionsFactory( );
    factory.init( factoryConfig, servletContext );
      // Make factory accessible from jsp tags
    DefinitionsUtil.makeDefinitionsFactoryAccessible(factory, servletContext );
    return factory;
  }

  /**
   * Set definition factory in appropriate servlet context.
   * @param factory Factory to store.
   * @param servletContext Servlet context that will hold factory.
   * @deprecated since 20020708. Replaced by makeFactoryAccessible()
   */
  static protected void setDefinitionsFactory(ComponentDefinitionsFactory factory, ServletContext servletContext)
  {
  servletContext.setAttribute(DEFINITIONS_FACTORY, factory);
  }

  /**
   * Get a definition by its name.
   * First, retrieve definition factory, and then get requested definition.
   * Throw appropriate exception if definition or definition factory is not found.
   * @param name Name of requested definition.
   * @param request Current servelet request
   * @param servletContext current servlet context
   * @throws FactoryNotFoundException Can't find definition factory.
   * @throws DefinitionsFactoryException General error in factory while getting definition.
   * @throws NoSuchDefinitionException No definition found for specified name
   */
  static public ComponentDefinition getDefinition(String definitionName, ServletRequest request, ServletContext servletContext)
    throws FactoryNotFoundException, DefinitionsFactoryException
  {
  try
    {
    return getDefinitionsFactory(servletContext).getDefinition(definitionName,
                                                       (HttpServletRequest)request,
                                                        servletContext);
    }
   catch( NullPointerException ex )
    {  // Factory not found in context
    throw new FactoryNotFoundException( "Can't get definitions factory from context." );
    }
  }

  /**
   * Get a component / template definition by its name.
   * First, retrieve instance factory, and then get requested instance.
   * Throw appropriate exception if definition is not found.
   * @param name Name of requested definition.
   * @param request Current servelet request
   * @param servletContext current servlet context
   * @throws FactoryNotFoundException Can't find definition factory.
   * @throws DefinitionsFactoryException General error in factory while getting definition.
   * @throws NoSuchDefinitionException No definition found for specified name
   */
  static public ComponentDefinition getDefinition(String definitionName, PageContext pageContext)
    throws FactoryNotFoundException, DefinitionsFactoryException
  {
  return getDefinition( definitionName,
                        (HttpServletRequest)pageContext.getRequest(),
                        pageContext.getServletContext());
  }

  /**
   * Get definition factory from appropriate servlet context.
   * @return Definitions factory or null if not found.
   * @since 20020708
   */
 static  public DefinitionsFactory getDefinitionsFactory(ServletContext servletContext)
  {
  return (DefinitionsFactory)servletContext.getAttribute(DEFINITIONS_FACTORY);
  }

  /**
   * Make definition factory accessible to Tags.
   * Factory is stored in servlet context.
   * @param factory Factory to make accessible
   * @param servletContext Current servlet context
   * @since 20020708
   */
 static  public void makeDefinitionsFactoryAccessible(DefinitionsFactory factory, ServletContext servletContext)
  {
  servletContext.setAttribute(DEFINITIONS_FACTORY, factory);
  }

  /**
   * Get Definition stored in jsp context by an action.
   * @return ComponentDefinition or null if not found.
   */
 static  public ComponentDefinition getActionDefinition(ServletRequest request)
  {
  return (ComponentDefinition)request.getAttribute(ACTION_DEFINITION);
  }

  /**
   * Store definition in jsp context.
   * Mainly used by Struts to pass a definition defined in an Action to the forward.
   */
 static  public void setActionDefinition(ServletRequest request, ComponentDefinition definition)
  {
  request.setAttribute(ACTION_DEFINITION, definition);
  }

  /**
   * Remove Definition stored in jsp context.
   * Mainly used by Struts to pass a definition defined in an Action to the forward.
   */
 static  public void removeActionDefinition(ServletRequest request, ComponentDefinition definition)
  {
  request.removeAttribute(ACTION_DEFINITION);
  }

  /**
   * Populate Definition Factory Config from web.xml properties.
   * @param config Definition Factory Config to populate.
   * @param servletContext Current servlet context containing web.xml properties.
   * @exception IllegalAccessException if the caller does not have
   *  access to the property accessor method
   * @exception InvocationTargetException if the property accessor method
   *  throws an exception
   * @see org.apache.commons.beanutils.BeanUtil
   * @since tiles 20020708
   */
  static public void populateDefinitionsFactoryConfig( DefinitionsFactoryConfig factoryConfig, ServletConfig servletConfig)
    throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
  {
  Map properties = new DefinitionsUtil.ServletPropertiesMap( servletConfig );
  factoryConfig.populate( properties);
  }

  /**
   * Create FactoryConfig and initialize it from web.xml.
   *
   * @param servlet ActionServlet that is managing all the sub-applications
   *  in this web application
   * @param config ApplicationConfig for the sub-application with which
   *  this plug in is associated
   * @exception ServletException if this <code>PlugIn</code> cannot
   *  be successfully initialized
   */
  static protected DefinitionsFactoryConfig readFactoryConfig(ServletConfig servletConfig)
      throws DefinitionsFactoryException
  {
    // Create tiles definitions config object
  DefinitionsFactoryConfig factoryConfig = new DefinitionsFactoryConfig();
    // Get init parameters from web.xml files
  try
    {
    DefinitionsUtil.populateDefinitionsFactoryConfig(factoryConfig, servletConfig);
    }
   catch(Exception ex)
    {
    ex.printStackTrace();
    throw new DefinitionsFactoryException( "Can't populate DefinitionsFactoryConfig class from 'web.xml'.", ex );
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
  ServletPropertiesMap( ServletConfig config )
    {
      // This implementation is very simple.
      // It is possible to avoid creation of a new structure, but this need
      // imply writing all Map interface.
    Enumeration enum = config.getInitParameterNames();
    while( enum.hasMoreElements() )
      {
      String key = (String)enum.nextElement();
      put( key, config.getInitParameter( key ) );
      }
    }
}  // end inner class

}

