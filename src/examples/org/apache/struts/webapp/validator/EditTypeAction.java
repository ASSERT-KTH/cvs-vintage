/*
 * $Header: /tmp/cvs-vintage/struts/src/examples/org/apache/struts/webapp/validator/EditTypeAction.java,v 1.1 2004/01/08 16:18:19 husted Exp $
 * $Revision: 1.1 $
 * $Date: 2004/01/08 16:18:19 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2003 The Apache Software Foundation.  All rights
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
 */

package org.apache.struts.webapp.validator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.LabelValueBean;

/**
 * Initializes ActionForm.
 *
 * @author Robert Leland
 */
public final class EditTypeAction extends Action {

    /**
     * Commons Logging instance.
     */
    private Log log = LogFactory.getFactory().getInstance(this.getClass().getName());

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @return Action to forward to
     * @exception Exception if an input/output error or servlet exception occurs
     */
    public ActionForward execute(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)
            throws Exception {

        // Was this transaction cancelled?

        initFormBeans(mapping, form, request);

        return mapping.findForward("success");
    }

    /**
     * Convenience method for initializing form bean.
     * @param mapping The ActionMapping used to select this instance
     * @param request The HTTP request we are processing
     */
    protected void initFormBeans(
            ActionMapping mapping, ActionForm form,
            HttpServletRequest request) {

        // Initialize
        ArrayList satisfactionList = new ArrayList();
        satisfactionList.add(new LabelValueBean("Very Satisfied", "4"));
        satisfactionList.add(new LabelValueBean("Satisfied", "3"));
        satisfactionList.add(new LabelValueBean("Not Very Satisfied", "2"));
        satisfactionList.add(new LabelValueBean("Not Satisfied", "1"));
        request.setAttribute("satisfactionList", satisfactionList);

        ArrayList osTypes = new ArrayList();
        osTypes.add(new LabelValueBean("Mac OsX", "OsX"));
        osTypes.add(new LabelValueBean("Windows 95/98/Me", "Win32"));
        osTypes.add(new LabelValueBean("Windows NT/2000/XP/2003", "WinNT"));
        osTypes.add(new LabelValueBean("Linux", "Linux"));
        osTypes.add(new LabelValueBean("BSD NetBSD/FreeBSD/OpenBSD", "BSD"));
        request.setAttribute("osTypes", osTypes);

        ArrayList languageTypes = new ArrayList();
        languageTypes.add(new LabelValueBean("C++", "C++"));
        languageTypes.add(new LabelValueBean("C#", "C#"));
        languageTypes.add(new LabelValueBean("Java", "java"));
        languageTypes.add(new LabelValueBean("Smalltalk", "Smalltalk"));
        request.setAttribute("languageTypes", languageTypes);
    }
}
