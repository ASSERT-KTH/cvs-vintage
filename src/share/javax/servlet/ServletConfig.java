/*
 * $Id: ServletConfig.java,v 1.1 1999/10/09 00:20:29 duncan Exp $
 * 
 * Copyright (c) 1995-1999 Sun Microsystems, Inc. All Rights Reserved.
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

package javax.servlet;

import java.util.Enumeration;



/**
 * 
 * A servlet configuration object used by a servlet container
 * used to pass information to a servlet during initialization. 
 *
 * <p>The configuration information contains initialization parameters,
 * which are a set of name/value pairs, and a {@link ServletContext} object,
 * which gives the servlet information about the server.
 *
 * @author 	Various
 * @version 	$Version$
 *
 * @see 	ServletContext
 *
 */
 
public interface ServletConfig {




    /**
     * Returns a reference to the {@link ServletContext} in which the servlet
     * is executing.
     *
     *
     * @return		a {@link ServletContext} object, used
     *			by the servlet to interact with its servlet 
     *                  container
     * 
     * @see		ServletContext
     *
     */

    public ServletContext getServletContext();
    
    
    
    

    /**
     * Returns a <code>String</code> containing the value of the 
     * named initialization parameter, or <code>null</code> if 
     * the parameter does not exist.
     *
     * @param name	a <code>String</code> specifying the name
     *			of the initialization parameter
     *
     * @return		a <code>String</code> containing the value 
     *			of the initialization parameter
     *
     */

    public String getInitParameter(String name);
    
    

    /**
     * Returns the names of the servlet's initialization parameters
     * as an <code>Enumeration</code> of <code>String</code> objects, 
     * or an empty <code>Enumeration</code> if the servlet has
     * no initialization parameters.
     *
     * @return		an <code>Enumeration</code> of <code>String</code> 
     *			objects containing the names of the servlet's 
     *			initialization parameters
     *
     *
     *
     */

    public Enumeration getInitParameterNames();
    

    /**
     * Returns the name of this servlet instance.
     * The name may be provided via server administration, assigned in the 
     * web application deployment descriptor, or for an unregistered (and thus
     * unnamed) servlet instance it will be the servlet's class name.
     *
     * @return		the name of the servlet instance
     *
     *
     *
     */

    public String getServletName();

}
