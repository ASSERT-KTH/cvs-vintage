
/*
 * $Id: HttpSession.java,v 1.1 1999/10/09 00:20:30 duncan Exp $
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
 * Provides a way to identify a user across more than one page
 * request or visit to a Web site and to store information about that user.
 *
 * <p>The servlet container uses this interface to create a session
 * between an HTTP client and an HTTP server. The session persists
 * for a specified time period, across more than one connection or
 * page request from the user. A session usually corresponds to one 
 * user, who may visit a site many times. The server can maintain a 
 * session in many ways such as using cookies or rewriting URLs.
 *
 * <p>This interface allows servlets to 
 * <ul>
 * <li>View and manipulate information about a session, such as
 *     the session identifier, creation time, and last accessed time
 * <li>Bind objects to sessions, allowing user information to persist 
 *     across multiple user connections
 * </ul>
 *
 * <p>When an application stores an object in or removes an object from a
 * session, the session checks whether the object implements
 * {@link HttpSessionBindingListener}. If it does, 
 * the servlet notifies the object that it has been bound to or unbound 
 * from the session.
 * 
 * <p>A servlet should be able to handle cases in which
 * the client does not choose to join a session, such as when cookies are
 * intentionally turned off. Until the client joins the session,
 * <code>isNew</code> returns <code>true</code>.  If the client chooses 
 * not to join
 * the session, <code>getSession</code> will return a different session
 * on each request, and <code>isNew</code> will always return
 * <code>true</code>.
 *
 * <p>Session information is scoped only to the current web application
 * (<code>ServletContext</code>), so information stored in one context
 * will not be directly visible in another.
 *
 * @author	Various
 * @version	$Version$
 *
 *
 * @see 	HttpSessionBindingListener
 * @see 	HttpSessionContext
 *
 */

public interface HttpSession {




    /**
     *
     * Returns the time when this session was created, measured
     * in milliseconds since midnight January 1, 1970 GMT.
     *
     * @return				a <code>long</code> specifying
     * 					when this session was created,
     *					expressed in 
     *					milliseconds since 1/1/1970 GMT
     *
     * @exception IllegalStateException	if this method is called on an
     *					invalidated session
     *
     */

    public long getCreationTime();
    
    
    
    
    /**
     *
     * Returns a string containing the unique identifier assigned 
     * to this session. The identifier is assigned 
     * by the servlet container and is implementation dependent.
     * 
     * @return				a string specifying the identifier
     *					assigned to this session
     *
     * @exeption IllegalStateException	if this method is called on an
     *					invalidated session
     *
     */

    public String getId();
    
    
    

    /**
     *
     * Returns the last time the client sent a request associated with
     * this session, as the number of milliseconds since midnight
     * January 1, 1970 GMT. 
     *
     * <p>Actions that your application takes, such as getting or setting
     * a value associated with the session, do not affect the access
     * time.
     *
     * @return				a <code>long</code>
     *					representing the last time 
     *					the client sent a request associated
     *					with this session, expressed in 
     *					milliseconds since 1/1/1970 GMT
     *
     * @exeption IllegalStateException	if this method is called on an
     *					invalidated session
     *
     */

    public long getLastAccessedTime();
    
    
    
    /**
     *
     * Specifies the time, in seconds, between client requests before the 
     * servlet container will invalidate this session.  A negative time
     * indicates the session should never timeout.
     *
     * @param interval		An integer specifying the number
     * 				of seconds 
     *
     */
    
    public void setMaxInactiveInterval(int interval);




   /**
    * Returns the maximum time interval, in seconds, that 
    * the servlet container will keep this session open between 
    * client accesses. After this interval, the servlet container
    * will invalidate the session.  The maximum time interval can be set
    * with the <code>setMaxInactiveInterval</code> method.
    * A negative time indicates the session should never timeout.
    *  
    *
    * @return		an integer specifying the number of
    *			seconds this session remains open
    *			between client requests
    *
    * @see		#setMaxInactiveInterval
    *
    *
    */

    public int getMaxInactiveInterval();
    
    


   /**
    *
    * @deprecated 	As of Version 2.1, this method is
    *			deprecated and has no replacement.
    *			It will be removed in a future
    *			version of the Java Servlet API.
    *
    */

    public HttpSessionContext getSessionContext();
    
    
    
    
    /**
     *
     * Returns the object bound with the specified name in this session, or
     * <code>null</code> if no object is bound under the name.
     *
     * @param name		a string specifying the name of the object
     *
     * @return			the object with the specified name
     *
     * @exception IllegalStateException	if this method is called on an
     *					invalidated session
     *
     */
  
    public Object getAttribute(String name);
    
    
    
    
    /**
     *
     * @deprecated 	As of Version 2.2, this method is
     * 			replaced by {@link #getAttribute}.
     *
     * @param name		a string specifying the name of the object
     *
     * @return			the object with the specified name
     *
     * @exception IllegalStateException	if this method is called on an
     *					invalidated session
     *
     */
  
    public Object getValue(String name);
    
    
    

    /**
     *
     * Returns an <code>Enumeration</code> of <code>String</code> objects
     * containing the names of all the objects bound to this session. 
     *
     * @return			an <code>Enumeration</code> of 
     *				<code>String</code> objects specifying the
     *				names of all the objects bound to
     *				this session
     *
     * @exception IllegalStateException	if this method is called on an
     *					invalidated session
     *
     */
    
    public Enumeration getAttributeNames();
    
    
    

    /**
     *
     * @deprecated 	As of Version 2.2, this method is
     * 			replaced by {@link #getAttributeNames}
     *
     * @return				an array of <code>String</code>
     *					objects specifying the
     *					names of all the objects bound to
     *					this session
     *
     * @exception IllegalStateException	if this method is called on an
     *					invalidated session
     *
     */
    
    public String[] getValueNames();
    
    
    

    /**
     * Binds an object to this session, using the name specified.
     * If an object of the same name is already bound to the session,
     * the object is replaced.
     *
     * <p>After this method executes, and if the object
     * implements <code>HttpSessionBindingListener</code>,
     * the container calls 
     * <code>HttpSessionBindingListener.valueBound</code>.
     *
     * @param name			the name to which the object is bound;
     *					cannot be null
     *
     * @param value			the object to be bound; cannot be null
     *
     * @exception IllegalStateException	if this method is called on an
     *					invalidated session
     *
     */
 
    public void setAttribute(String name, Object value);
    



    
    /**
     *
     * @deprecated 	As of Version 2.2, this method is
     * 			replaced by {@link #setAttribute}
     *
     * @param name			the name to which the object is bound;
     *					cannot be null
     *
     * @param value			the object to be bound; cannot be null
     *
     * @exception IllegalStateException	if this method is called on an
     *					invalidated session
     *
     */
 
    public void putValue(String name, Object value);





    /**
     *
     * Removes the object bound with the specified name from
     * this session. If the session does not have an object
     * bound with the specified name, this method does nothing.
     *
     * <p>After this method executes, and if the object
     * implements <code>HttpSessionBindingListener</code>,
     * the container calls 
     * <code>HttpSessionBindingListener.valueUnbound</code>.
     * 
     * 
     *
     * @param name				the name of the object to
     *						remove from this session
     *
     * @exception IllegalStateException	if this method is called on an
     *					invalidated session
     */

    public void removeAttribute(String name);





    /**
     *
     * @deprecated 	As of Version 2.2, this method is
     * 			replaced by {@link #setAttribute}
     *
     * @param name				the name of the object to
     *						remove from this session
     *
     * @exception IllegalStateException	if this method is called on an
     *					invalidated session
     */

    public void removeValue(String name);




    /**
     *
     * Invalidates this session and unbinds any objects bound
     * to it.
     *
     * @exception IllegalStateException	if this method is called on an
     *					already invalidated session
     *
     */

    public void invalidate();
    
    
    
    
    /**
     *
     * Returns <code>true</code> if the client does not yet know about the
     * session or if the client chooses not to join the session.  For 
     * example, if the server used only cookie-based sessions, and
     * the client had disabled the use of cookies, then a session would
     * be new on each request.
     *
     * @return 				<code>true</code> if the 
     *					server has created a session, 
     *					but the client has not yet joined
     *
     * @exception IllegalStateException	if this method is called on an
     *					already invalidated session
     *
     */

    public boolean isNew();
}

