/*
 * @(#)JspPage.java	1.4 99/04/26
 * 
 * Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 * 
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 * 
 * CopyrightVersion 1.0
 */
 
package javax.servlet.jsp;

import javax.servlet.*;

/**
 * This is the interface that a JSP processor-generated class must
 * satisfy.
 * <p>
 * The interface defines a protocol with 3 methods; only two of
 * them: jspInit() and jspDestroy() are part of this interface as
 * the signature of the third method: _jspService() depends on
 * the specific protocol used and cannot be expressed in a generic
 * way in Java.
 * <p>
 * A class implementing this interface is responsible for invoking
 * the above methods at the apropriate time based on the
 * corresponding Servlet-based method invocations.
 * <p>
 * The jspInit(0 and jspDestroy() methods can be defined by a JSP
 * author, but the _jspService() method is defined authomatically
 * by the JSP processor based on the contents of the JSP page.
 */

public interface JspPage extends Servlet {

    /**
     * Methods that can be DEFINED BY THE JSP AUTHOR
     * either directly (via a declaration) or via an event handler
     * (in JSP 1.1)
     */

    /**
     * jsp_init() is invoked when the JspPage is initialized.
     * At this point getServletConfig() will return the desired value.
     */
    public void jspInit();

    /**
     * jsp_destroy() is invoked when the JspPage is about to be destroyed.
     */
    public void jspDestroy();

    /**
     * service is the main service entry from the superclass.  It is 
     * responsible from determine if the protocol is valid and to call
     * into the appropriate _jspService(), after the appropriate casting.
     */

    /**
     * _jspService corresponds to the body of the JSP page.
     * This method is defined automatically by the JSP processor
     * and should NEVER BE DEFINED BY THE JSP AUTHOR
     *
     * The specific signature depends on the protocol supported by the JSP page.
     *
     * public void _jspService(<ServletRequestSubtype> request,
     *                             <ServletResponseSubtype> response)
     *        throws ServletException, IOException;
     */
}
