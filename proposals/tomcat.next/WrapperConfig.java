/*
 * $Header: /tmp/cvs-vintage/tomcat/proposals/tomcat.next/Attic/WrapperConfig.java,v 1.1 2000/01/08 03:54:03 craigmcc Exp $
 * $Revision: 1.1 $
 * $Date: 2000/01/08 03:54:03 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
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
 * [Additional notices, if required by prior licensing conditions]
 *
 */ 


// package org.apache.tomcat;


/**
 * A <b>WrapperConfig</b> encapsulates the configuration properties related
 * to a specific <code>&lt;servlet%gt;</code> element from the deployment
 * descriptor of a web application.  As such, implementations of this
 * interface may be used both in the servlet engine itself, and administrative
 * tools used to construct deployment descriptors.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2000/01/08 03:54:03 $
 */

public interface WrapperConfig {


    // ------------------------------------------------------------- Properties


    /**
     * Return the description of this servlet.
     */
    public String getDescription();


    /**
     * Set the description of this servlet.
     *
     * @param description The new description
     */
    public void setDescription(String description);


    /**
     * Return the display name of this servlet.
     */
    public String getDisplayName();


    /**
     * Set the display name of this servlet.
     *
     * @param name The new display name
     */
    public void setDisplayName(String name);


    /**
     * Return the path of the JSP file for this servlet.
     */
    public String getJspFile();


    /**
     * Set the path of the JSP file for this servlet.
     *
     * @param path The JSP file path
     */
    public void setJspFile();


    /**
     * Return the path of the large icon file for this servlet.
     */
    public String getLargeIcon();


    /**
     * Set the path of the large icon file for this servlet.
     *
     * @param path The new icon path
     */
    public void setLargeIcon(String path);


    /**
     * Return the load-on-startup order identifier for this servlet, or
     * zero if this is not a load-on-startup servlet.
     */
    public int getLoadOnStartup();


    /**
     * Set the load-on-startup order identifier for this servlet, or
     * zero if this is not a load-on-startup servlet.
     *
     * @param order The new load-on-startup order identifier
     */
    public void setLoadOnStartup(int order);


    /**
     * Return the name of the servlet class for this servlet.
     */
    public String getServletClass();


    /**
     * Set the name of the servlet class for this servlet.
     *
     * @param servletClass The new servlet class
     */
    public void setServletClass(String servletClass);


    /**
     * Return the canonical name of this servlet.
     */
    public String getServletName();


    /**
     * Set the canonical name of this servlet.
     *
     * @param name Canonical name of this servlet
     */
    public void setServletName(String name);


    /**
     * Return the path of the small icon file for this servlet.
     */
    public String getSmallIcon();


    /**
     * Set the path of the small icon file for this servlet.
     *
     * @param path The new icon path
     */
    public void setSmallIcon(String path);


    // --------------------------------------------------------- Public Methods


    /**
     * Add the specified initialization parameter for this servlet.
     *
     * @param param The new initialization parameter
     */
    public void addParameter(WrapperParam param);


    /**
     * Add the specified security role reference for this servlet.
     *
     * @param role The new role reference
     */
    public void addRoleRef(WrapperRole role);


    /**
     * Return the initialization parameter with the specified name, if any;
     * otherwise return <code>null</code>.
     *
     * @param name Parameter name to look up
     */
    public WrapperParam findParameter(String name);


    /**
     * Return the set of initialization parameters for this servlet.  If there
     * are no defined parameters, a zero-length array is returned.
     */
    public WrapperParam[] findParameters();


    /**
     * Return the security role reference for the specified security role
     * name (as used by the servlet), if any; otherwise return
     * <code>null</code>.
     *
     * @param name Security role name (as used by the servlet) to look up
     */
    public WrapperRole findRoleRef(String name);


    /**
     * Return the set of security role references for this servlet.  If there
     * are no defined role references, a zero-length array is returned.
     */
    public WrapperRole[] findRoleRefs();


    /**
     * Remove any initialization parameter with the defined name
     * from this servlet.
     *
     * @param name Name of the parameter to be removed
     */
    public void removeParameter(String name);


    /**
     * Remove any security role reference for the specified role name,
     * as used within the servlet.
     *
     * @param name Security role name to be removed
     */
    public void removeRoleRef(String name);


}


