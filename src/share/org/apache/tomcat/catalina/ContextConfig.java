/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/catalina/Attic/ContextConfig.java,v 1.1 2000/01/26 17:45:08 costin Exp $
 * $Revision: 1.1 $
 * $Date: 2000/01/26 17:45:08 $
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


package org.apache.tomcat.catalina;


/**
 * A <b>ContextConfig</b> encapsulates the configuration properties related
 * to a specific <code>&lt;web-app%gt;</code> element from the deployment
 * descriptor of a web application.  As such, implementations of this
 * interface may be used both in the servlet engine itself, and administrative
 * tools used to construct deployment descriptors.
 * <p>
 * <b>FIXME:  Add the missing configuration elements!</b>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2000/01/26 17:45:08 $
 */

public interface ContextConfig {


    // ------------------------------------------------------------- Properties


    /**
     * Return the description of this web application.
     */
    public String getDescription();


    /**
     * Set the description of this web application.
     *
     * @param description The new description
     */
    public void setDescription(String description);


    /**
     * Return the display name of this web application.
     */
    public String getDisplayName();


    /**
     * Set the display name of this web application.
     *
     * @param name The new display name
     */
    public void setDisplayName(String name);


    /**
     * Return the path of the large icon file for this web application.
     */
    public String getLargeIcon();


    /**
     * Set the path of the large icon file for this web application.
     *
     * @param path The new icon path
     */
    public void setLargeIcon(String path);


    /**
     * Return the path of the small icon file for this web application.
     */
    public String getSmallIcon();


    /**
     * Set the path of the small icon file for this web application.
     *
     * @param path The new icon path
     */
    public void setSmallIcon(String path);


    // --------------------------------------------------------- Public Methods


}
