/*
 * @(#)HttpJspPage.java	1.6 99/10/02
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
import javax.servlet.http.*;
import java.io.IOException;

/**
 * This is the interface that a JSP processor-generated class for the
 * HTTP protocol must satisfy.
 */

public interface HttpJspPage extends JspPage {

    /**
     * _jspService corresponds to the body of the JSP page.
     * This method is defined automatically by the JSP processor
     * and should NEVER BE DEFINED BY THE JSP AUTHOR
     */
    public void _jspService(HttpServletRequest request,
				HttpServletResponse response)
       throws ServletException, IOException;
}
