package org.apache.scaffold.http;


import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;

import org.apache.scaffold.lang.Tokens;
import org.apache.scaffold.lang.ChainedException;


/**
 * Standard Base Action.
 * @author Ted Husted
 * @version $Revision: 1.1 $ $Date: 2002/01/24 15:24:50 $
 */
public class SuperAction extends Action {


    /**
     * Create and return an ActionErrors object.
     * The default method returns a new, empty object.
     * A subclass may examine the parameters and prepopulated
     * the object if circumstances warrant (e.g. user is not
     * authorized).
     * The appropriate forwarding can then be returned by
     * checkErrors (override this too).
     *
     * @param mapping The ActionMapping used to select this instance
     * @param actionForm The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The resonse we are creating
     */
    protected ActionErrors getActionErrors(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request) {

        return new ActionErrors();

    }


     /**
      * Return the appropriate ActionForward for an
      * error condition.
      * The default method returns a forward to input,
      * when there is one, or "error" when not.
      * The application must provide an "error" forward.
      * An advanced implementation could check the errors
      * and provide different forwardings for different
      * @param mapping The ActionMapping used to select this instance
      * @param actionForm The optional ActionForm bean for this request (if any)
      * @param request The HTTP request we are processing
      * @param response The resonse we are creating
      */
     protected ActionForward findError(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response,
            ActionErrors errors) {

        if (errors.empty())
            return null;

        saveErrors(request, errors);

        // If input page, use that
        if (mapping.getInput()!=null)
            return (new ActionForward(mapping.getInput()));

        // If no input page, use error forwarding
        return mapping.findForward(Tokens.ERROR);

    }


    /**
     * Execute the business logic for this Action.
     * @param mapping The ActionMapping used to select this instance
     * @param actionForm The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The resonse we are creating
     * @param errors Our ActionErrors collection
     */
    public void executeLogic(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response,
            ActionErrors errors)
        throws Exception {

        // override to provide functionality, like
        // myBusinessObject.execute(form);

   }


     /**
      * Process the exception handling for this Action.
      * Requires that exception thrown be subclass of
      * ChainedException. Otherwise override.
      * error.general and error.detail need to be
      * defined in ApplicationResources.
      * error.general = An unexpected error has occurred.
      * error.detail = {0}
      * @param mapping The ActionMapping used to select this instance
      * @param actionForm The optional ActionForm bean for this request (if any)
      * @param request The HTTP request we are processing
      * @param response
      */
     protected void catchException(
                    ActionMapping mapping,
                    ActionForm form,
                    HttpServletRequest request,
                    HttpServletResponse response,
                    ActionErrors errors,
                    Exception exception) {

            // Cast as our subclass base type
        ChainedException e = (ChainedException) exception;

            // Log and print to error console
        servlet.log("Action Exception: ", e );
        e.printStackTrace();

            // General error message
        errors.add(ActionErrors.GLOBAL_ERROR,
            new ActionError("error.general"));

            // Generate error messages from exceptions
        errors.add(ActionErrors.GLOBAL_ERROR,
            new ActionError("error.detail",e.getMessage()));
        if (e.isCause()) {
            errors.add(ActionErrors.GLOBAL_ERROR,
                new ActionError("error.detail",e.getCauseMessage()));
        }
    }


    /**
     * Save to the appropriate context any helper objects that
     * may be expected by another component when this Action
     * completes.
     * The default method does nothing.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param actionForm The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The resonse we are creating
     */
    protected void saveHelpers(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request) {

        // override to provide funcionality, like
        // request.setAttribute("yourKey",yourBusinessObject);

    }


     /**
      * Return the appropriate ActionForward for the nominal,
      * non-error state.
      * The default returns mapping.findForward("continue");
      * @param mapping The ActionMapping used to select this instance
      * @param actionForm The optional ActionForm bean for this request (if any)
      * @param request The HTTP request we are processing
      * @param response The response we are creating
      * @param errors Our ActionErrors collection
      */
     protected ActionForward findNominal(
         ActionMapping mapping,
         ActionForm form,
         HttpServletRequest request,
         HttpServletResponse response,
         ActionErrors errors
         ) {

        return mapping.findForward(Tokens.CONTINUE);

    }


    /**
     * Skeleton perform that calls the other "hotspot" methods in
     * this class in turn. Typically, you can override the other
     * methods and leave this one as is.
     * @param mapping The ActionMapping used to select this instance
     * @param actionForm The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @param helper The helper object
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public ActionForward perform(ActionMapping mapping,
                 ActionForm form,
                 HttpServletRequest request,
                 HttpServletResponse response)
    throws IOException, ServletException {

        ActionErrors errors = getActionErrors(mapping,form,request);

        if (!errors.empty())
            return findError(mapping,form,request,response,errors);

        try {

            executeLogic(mapping,form,request,response,errors);

        }
        catch (Exception e) {
            catchException(mapping,form,request,response,errors,e);
        }

        if (!errors.empty())
            return findError(mapping,form,request,response,errors);

        saveHelpers(mapping,form,request);

        return findNominal(mapping,form,request,response,errors);

    }

} // end SuperAction


/*
 * $Header: /tmp/cvs-vintage/struts/contrib/scaffold/src/framework/main/org/apache/scaffold/http/Attic/SuperAction.java,v 1.1 2002/01/24 15:24:50 husted Exp $
 * $Revision: 1.1 $
 * $Date: 2002/01/24 15:24:50 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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
**/




