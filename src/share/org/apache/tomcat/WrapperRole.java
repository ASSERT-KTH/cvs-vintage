/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/Attic/WrapperRole.java,v 1.1 2000/01/09 03:20:02 craigmcc Exp $
 * $Revision: 1.1 $
 * $Date: 2000/01/09 03:20:02 $
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


package org.apache.tomcat;


/**
 * Representation of a servlet security role reference, corresponding to a
 * specific <code>&lt;security-role-ref&gt;</code> element from the deployment
 * descriptor of a web application.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2000/01/09 03:20:02 $
 */

public final class WrapperRole {



    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new instance with default parameter values.
     */
    public WrapperRole() {

	this(null, null, null);

    }


    /**
     * Construct a new instance with specified parameter values.
     *
     * @param name Name of role used within the servlet
     * @param link Name of the application security role to map to
     * @param description Role reference description
     */
    public WrapperRole(String name, String link, String description) {

	super();
	setName(name);
	setLink(link);
	setDescription(description);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The role reference description.
     */
    private String description = null;


    /**
     * The linked-to name of the application security role.
     */
    private String link = null;


    /**
     * The role name used within the servlet.
     */
    private String name = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return the description of this security role reference.
     */
    public String getDescription() {

	return (description);

    }


    /**
     * Set the description of this security role reference.
     *
     * @param description The new description
     */
    public void setDescription(String description) {

	this.description = description;

    }


    /**
     * Return the application name of this security role.
     */
    public String getLink() {

	return (link);

    }


    /**
     * Set the application name of this security role.
     *
     * @param link The new link
     */
    public void setLink(String link) {

	this.link = link;

    }


    /**
     * Return the servlet name of this security role.
     */
    public String getName() {

	return (name);

    }


    /**
     * Set the servlet name of this security role.
     *
     * @param name The new name
     */
    public void setName(String name) {

	this.name = name;

    }


}
