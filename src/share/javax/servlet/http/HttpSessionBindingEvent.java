/*
 * $Id: HttpSessionBindingEvent.java,v 1.1 1999/10/09 00:20:30 duncan Exp $
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

import java.util.EventObject;


/**
 *
 * Sent to an object that implements
 * {@link HttpSessionBindingListener} when the object is
 * bound to or unbound from the session.
 *
 * <p>The session binds the object by a call to
 * <code>HttpSession.putValue</code> and unbinds the object
 * by a call to <code>HttpSession.removeValue</code>.
 *
 *
 *
 * @author		Various
 * @version		$Version$
 *
 * @see 		HttpSession
 * @see 		HttpSessionBindingListener
 *
 */

public class HttpSessionBindingEvent extends EventObject {




    /* The name to which the object is being bound or unbound */

    private String name;
    
  

    /**
     *
     * Constructs an event that notifies an object that it
     * has been bound to or unbound from a session. 
     * To receive the event, the object must implement
     * {@link HttpSessionBindingListener}.
     *
     *
     *
     * @param session 	the session to which the object is bound or unbound
     *
     * @param name 	the name with which the object is bound or unbound
     *
     * @see			#getName
     * @see			#getSession
     *
     */

    public HttpSessionBindingEvent(HttpSession session, String name) {
	super(session);
	this.name = name;
    }
    
    
    
   
  
    
    /**
     *
     * Returns the name with which the object is bound to or
     * unbound from the session.
     *
     *
     * @return		a string specifying the name with which
     *			the object is bound to or unbound from
     *			the session
     *
     *
     */

    public String getName() {
	return name;
    }
    
    
  
  
    

    /**
     *
     * Returns the session to or from which the object is
     * bound or unbound.
     *
     * @return		the session to which the object is
     *			bound or from which the object is
     *			unbound
     *
     *
     *
     */
    
    public HttpSession getSession() {
	return (HttpSession) getSource();
    }
}







