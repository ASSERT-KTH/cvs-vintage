/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/action/ActionForm.java,v 1.15 2003/04/29 02:55:24 dgraham Exp $
 * $Revision: 1.15 $
 * $Date: 2003/04/29 02:55:24 $
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


import java.io.Serializable;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts.upload.MultipartRequestHandler;


/**
 * <p>An <strong>ActionForm</strong> is a JavaBean optionally associated with
 * one or more <code>ActionMappings</code>.  Such a bean will have had its
 * properties initialized from the corresponding request parameters before
 * the corresonding action's <code>execute</code> method is called.</p>
 *
 * <p>When the properties of this bean have been populated, but before the
 * <code>execute</code> method of the action is called, this bean's
 * <code>validate</code> method will be called, which gives the bean a chance
 * to verify that the properties submitted by the user are correct and valid.
 * If this method finds problems, it returns an error messages object that
 * encapsulates those problems, and the controller servlet will return control
 * to the corresponding input form.  Otherwise, the <code>validate</code>
 * method returns <code>null</code>, indicating that everything is acceptable
 * and the corresponding Action's <code>execute</code> method should be
 * called.</p>
 *
 * <p>This class must be subclassed in order to be instantiated.  Subclasses
 * should provide property getter and setter methods for all of the bean
 * properties they wish to expose, plus override any of the public or
 * protected methods for which they wish to provide modified functionality.
 * </p>
 *
 * <p>Because ActionForms are JavaBeans, subclasses should also implement
 * <code>Serializable</code>, as required by the JavaBean specification.
 * Some containers require that an object meet all JavaBean requirements
 * in order to use the introspection API upon which ActionForms rely.</p>
 *
 * @author Craig R. McClanahan
 * @author Ted Husted
 * @version $Revision: 1.15 $ $Date: 2003/04/29 02:55:24 $
 */

public abstract class ActionForm implements Serializable {


    // ----------------------------------------------------- Instance Variables


    /**
     * The controller servlet instance to which we are attached.
     */
    protected transient ActionServlet servlet = null;


    /**
     * The MultipartRequestHandler for this form, can be
     * <code>null</code>
     */
    protected transient MultipartRequestHandler multipartRequestHandler;


    // ------------------------------------------------------------- Properties


    /**
     * Return the controller servlet instance to which we are attached.
     */
    protected ActionServlet getServlet() {

        return (this.servlet);

    }


    /**
     * Return the controller servlet instance to which we are attached.
     * as an ActionServletWrapper.
     * @see org.apache.struts.action.ActionServletWrapper
     * @since Struts 1.0.1
     */
    public ActionServletWrapper getServletWrapper() {

        return new ActionServletWrapper(getServlet());

    }


    /**
     * Return the MultipartRequestHandler for this form
     * The reasoning behind this is to give form bean developers
     * control over the lifecycle of their multipart requests
     * through the use of the finish() and/or rollback() methods
     * of MultipartRequestHandler.  This method will return
     * <code>null</code> if this form's enctype is not
     * "multipart/request-data".
     * @see org.apache.struts.upload.MultipartRequestHandler
     */
    public MultipartRequestHandler getMultipartRequestHandler() {
        return multipartRequestHandler;
    }


    /**
     * Set the controller servlet instance to which we are attached (if
     * <code>servlet</code> is non-null), or release any allocated resources
     * (if <code>servlet</code> is null).
     *
     * @param servlet The new controller servlet, if any
     */
    public void setServlet(ActionServlet servlet) {

        this.servlet = servlet;

    }


    public void setMultipartRequestHandler(MultipartRequestHandler multipartRequestHandler) {
        this.multipartRequestHandler = multipartRequestHandler;
    }

    // --------------------------------------------------------- Public Methods


    /**
     * Reset all bean properties to their default state.  This method is
     * called before the properties are repopulated by the controller servlet.
     * <p>
     * The default implementation attempts to forward to the HTTP
     * version of this method.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping mapping, ServletRequest request) {

        try {
            reset(mapping, (HttpServletRequest) request);
        } catch (ClassCastException e) {
            ;
        }

    }


    /**
     * Reset all bean properties to their default state.  This method is
     * called before the properties are repopulated by the controller servlet.
     * <p>
     * The default implementation does nothing.  Subclasses should override
     * this method to reset all bean properties to default values.
     * </p>
     * <p>
     * This method is <strong>not</strong> the appropriate place to initialize form values
     * for an "update" type page (this should be done in a setup Action).  You mainly 
     * need to worry about setting checkbox values to false; most of the time you 
     * can leave this method unimplemented.
     * </p>
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {

        ;       // Default implementation does nothing

    }


    /**
     * Validate the properties that have been set for this non-HTTP request,
     * and return an <code>ActionErrors</code> object that encapsulates any
     * validation errors that have been found.  If no errors are found, return
     * <code>null</code> or an <code>ActionErrors</code> object with no
     * recorded error messages.
     * <p>
     * The default implementation attempts to forward to the HTTP version of
     * this method.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public ActionErrors validate(ActionMapping mapping,
                                 ServletRequest request) {

        try {
            return (validate(mapping, (HttpServletRequest) request));
        } catch (ClassCastException e) {
            return (null);
        }

    }


    /**
     * Validate the properties that have been set for this HTTP request,
     * and return an <code>ActionErrors</code> object that encapsulates any
     * validation errors that have been found.  If no errors are found,
     * return <code>null</code> or an <code>ActionErrors</code> object with
     * no recorded error messages.
     * <p>
     * The default ipmlementation performs no validation and returns
     * <code>null</code>.  Subclasses must override this method to provide
     * any validation they wish to perform.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public ActionErrors validate(ActionMapping mapping,
                                 HttpServletRequest request) {

        return (null);

    }


}
