/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/tiles/Attic/StrutsModulesTilesUtilImpl.java,v 1.2 2002/11/09 07:11:21 rleland Exp $
 * $Revision: 1.2 $
 * $Date: 2002/11/09 07:11:21 $
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

import org.apache.struts.tiles.definition.ComponentDefinitionsFactoryWrapper;
import org.apache.struts.tiles.TilesRequestProcessor;

import org.apache.struts.util.RequestUtils;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.action.Action;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

  /**
   * Implementation of TilesUtil for Struts multi modules.
   * Methods in this implementation are aware of the Struts module context.
   * <br>
   * <ul>
   * <li>The method getFactory(...) return the factory for the current struts
   * module.</li>
   * <li>Methods doForward() and doInclude() use their counterparts in the
   * current RequestProcessor.</li>
   * <li>The method createFactory(...) creates a factory for the current module and
   * stores it under appropriate property name.</li>
   * </ul>
   */
public class StrutsModulesTilesUtilImpl extends DefaultTilesUtilImpl implements TilesUtilInterface
{
     /** Commons Logging instance.*/
  protected Log log = LogFactory.getLog(StrutsModulesTilesUtilImpl.class);

    /**
     * Do a forward using request dispatcher.
     *
     * This method is used by the Tiles package anytime a forward is required.
     * @param uri Uri or Definition name to forward
     * @param request Current page request
     * @param response Current page response
     * @param servletContext Current servlet context
     */
  public void doForward(String uri, HttpServletRequest request, HttpServletResponse response,
                        ServletContext servletContext)
    throws IOException, ServletException
  {
  request.getRequestDispatcher( uri ).include(request, response);
  }

    /**
     * Do an include using request dispatcher.
     *
     * This method is used by the Tiles package anytime an include is required.
     * @param uri Uri or Definition name to forward
     * @param request Current page request
     * @param response Current page response
     * @param servletContext Current servlet context
     */
  public void doInclude(String uri, HttpServletRequest request, HttpServletResponse response,
                        ServletContext servletContext)
    throws IOException, ServletException
  {
    // modify uri
  request.getRequestDispatcher( uri ).forward(request, response);
  }

    /**
     * Get definition factory from appropriate servlet context.
     * @return Definitions factory or null if not found.
     */
  public DefinitionsFactory getDefinitionsFactory(ServletRequest request, ServletContext servletContext)
  {
  ModuleConfig moduleConfig = getModuleConfig( (HttpServletRequest)request, servletContext);
  return (DefinitionsFactory)servletContext.getAttribute(DEFINITIONS_FACTORY+moduleConfig.getPrefix() );
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
  public DefinitionsFactory createDefinitionsFactory(ServletContext servletContext, DefinitionsFactoryConfig factoryConfig)
    throws DefinitionsFactoryException
  {
      // Create configurable factory
    DefinitionsFactory factory = createDefinitionFactoryInstance(factoryConfig.getFactoryClassname());
    factory.init( factoryConfig, servletContext );
      // Make factory accessible from jsp tags (push it in appropriate context)
    makeDefinitionsFactoryAccessible(factory, servletContext );
    return factory;
  }

  /**
   * Make definition factory accessible to Tags.
   * Factory is stored in servlet context.
   * @param factory Factory to make accessible
   * @param servletContext Current servlet context
   */
 protected void makeDefinitionsFactoryAccessible(DefinitionsFactory factory, ServletContext servletContext)
  {
  String prefix = factory.getConfig().getFactoryName();
  servletContext.setAttribute(DEFINITIONS_FACTORY+prefix, factory);
  }

  /**
   * Get Tiles request processor associated to the current module.
   */
 protected TilesRequestProcessor getRequestProcessor(HttpServletRequest request, ServletContext servletContext)
  {
  ModuleConfig moduleConfig = getModuleConfig( request, servletContext);
  return (TilesRequestProcessor)servletContext.getAttribute(Action.REQUEST_PROCESSOR_KEY+moduleConfig.getPrefix() );
  }

  /**
   * Get the current ModuleConfig.
   * <br>
   * Lookup in the request, and do selectApplication if not found. The side effect
   * is that the Application object is set in the request if it was not present.
   */
 protected ModuleConfig getModuleConfig(HttpServletRequest request, ServletContext servletContext)
  {
  ModuleConfig moduleConfig = RequestUtils.getModuleConfig( request, servletContext);
  if(moduleConfig==null)
    {
      // ModuleConfig not found in current request. Select it.
    RequestUtils.selectApplication(request, servletContext);
    moduleConfig = RequestUtils.getModuleConfig( request, servletContext);
    }

  return moduleConfig;
  }

}