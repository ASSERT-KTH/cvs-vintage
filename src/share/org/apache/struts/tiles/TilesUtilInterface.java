/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/tiles/Attic/TilesUtilInterface.java,v 1.3 2002/12/17 00:57:36 cedric Exp $
 * $Revision: 1.3 $
 * $Date: 2002/12/17 00:57:36 $
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

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

  /**
   * Class containing utilities for Tiles.
   */
public interface TilesUtilInterface
{
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
        throws IOException, ServletException;

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
        throws IOException, ServletException;

    /**
     * Get the default definition factory from appropriate servlet context.
     * @param servletContext Current servlet context
     * @return Definitions factory or null if not found.
     */
  public DefinitionsFactory getDefaultDefinitionsFactory(ServletContext servletContext);

    /**
     * Get definition factory from appropriate servlet context.
     * Implementation can use the request to select the factory.
     * @param response Current page response
     * @param servletContext Current servlet context
     * @return Definitions factory or null if not found.
     */
  public DefinitionsFactory getDefinitionsFactory(ServletRequest request, ServletContext servletContext);

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
    throws DefinitionsFactoryException;

    /**
     * Return the <code>Class</code> object for the specified fully qualified
     * class name, from the underlying class loader.
     *
     * @param className Fully qualified class name to be loaded
     * @return Class object
     * @exception ClassNotFoundException if the class cannot be found
     */
  public Class applicationClass(String className)
    throws ClassNotFoundException;

}