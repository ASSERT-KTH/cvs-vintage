/*
 * $Id: HttpSessionContext.java,v 1.1 1999/10/09 00:20:30 duncan Exp $
 * 
 * Copyright (c) 1997-1999 Sun Microsystems, Inc. All Rights Reserved.
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
 */


package javax.servlet.http;

import java.util.Enumeration;

/**
 *
 * @author		Various
 * @version		$Version$
 *
 * @deprecated		As of Java(tm) Servlet API 2.1
 *			for security reasons, with no replacement.
 *			This interface will be removed in a future
 *			version of this API.
 *
 * @see			HttpSession
 * @see			HttpSessionBindingEvent
 * @see			HttpSessionBindingListener
 *
 */


public interface HttpSessionContext {

    /**
     *
     * @deprecated 	As of Java Servlet API 2.1 with
     *			no replacement. This method must 
     *			return null and will be removed in
     *			a future version of this API.
     *
     */

    public HttpSession getSession(String sessionId);
    
    
    
  
    /**
     *
     * @deprecated	As of Java Servlet API 2.1 with
     *			no replacement. This method must return 
     *			an empty <code>Enumeration</code> and will be removed
     *			in a future version of this API.
     *
     */

    public Enumeration getIds();
}





