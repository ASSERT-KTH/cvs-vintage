/*
 * $Id: UnavailableException.java,v 1.1 1999/10/09 00:20:30 duncan Exp $
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
 * 
 * CopyrightVersion 1.0
 */

package javax.servlet;


/**
 * Defines an exception that a servlet throws to indicate
 * that it is permanently or temporarily unavailable. 
 *
 * <p>When a servlet is permanently unavailable, something is wrong
 * with the servlet, and it cannot handle
 * requests until some action is taken. For example, the servlet
 * might be configured incorrectly, or its state may be corrupted.
 * A servlet should log both the error and the corrective action
 * that is needed.
 *
 * <p>A servlet is temporarily unavailable if it cannot handle
 * requests momentarily due to some system-wide problem. For example,
 * a third-tier server might not be accessible, or there may be 
 * insufficient memory or disk storage to handle requests. A system
 * administrator may need to take corrective action.
 *
 * <p>Servlet containers can safely treat both types of unavailable
 * exceptions in the same way. However, treating temporary unavailability
 * effectively makes the servlet container more robust. Specifically,
 * the servlet container might block requests to the servlet for a period
 * of time suggested by the servlet, rather than rejecting them until
 * the servlet container restarts.
 *
 *
 * @author 	Various
 * @version 	$Version$
 *
 */

public class UnavailableException
extends ServletException {

    private Servlet     servlet;           // what's unavailable
    private boolean     permanent;         // needs admin action?
    private int         seconds;           // unavailability estimate

    /**
     * 
     * @deprecated	As of Java Servlet API 2.2, use {@link
     * 			#UnavailableException(String)} instead.
     *
     * @param servlet 	the <code>Servlet</code> instance that is
     *                  unavailable
     *
     * @param msg 	a <code>String</code> specifying the
     *                  descriptive message
     *
     */

    public UnavailableException(Servlet servlet, String msg) {
	super(msg);
	this.servlet = servlet;
	permanent = true;
    }
 
    /**
     * @deprecated	As of Java Servlet API 2.2, use {@link
     *			#UnavailableException(String, int)} instead.
     *
     * @param seconds	an integer specifying the number of seconds
     * 			the servlet expects to be unavailable; if
     *			zero or negative, indicates that the servlet
     *			can't make an estimate
     *
     * @param servlet	the <code>Servlet</code> that is unavailable
     * 
     * @param msg	a <code>String</code> specifying the descriptive 
     *			message, which can be written to a log file or 
     *			displayed for the user.
     *
     */
    
    public UnavailableException(int seconds, Servlet servlet, String msg) {
	super(msg);
	this.servlet = servlet;
	if (seconds <= 0)
	    this.seconds = -1;
	else
	    this.seconds = seconds;
	permanent = false;
    }

    /**
     * 
     * Constructs a new exception with a descriptive
     * message indicating that the servlet is permanently
     * unavailable.
     *
     * @param msg 	a <code>String</code> specifying the
     *                  descriptive message
     *
     */

    public UnavailableException(String msg) {
	super(msg);

	permanent = true;
    }

    /**
     * Constructs a new exception with a descriptive message
     * indicating that the servlet is temporarily unavailable
     * and giving an estimate of how long it will be unavailable.
     * 
     * <p>In some cases, the servlet cannot make an estimate. For
     * example, the servlet might know that a server it needs is
     * not running, but not be able to report how long it will take
     * to be restored to functionality. This can be indicated with
     * a negative or zero value for the <code>seconds</code> argument.
     *
     * @param msg	a <code>String</code> specifying the
     *                  descriptive message, which can be written
     *                  to a log file or displayed for the user.
     *
     * @param seconds	an integer specifying the number of seconds
     * 			the servlet expects to be unavailable; if
     *			zero or negative, indicates that the servlet
     *			can't make an estimate
     *
     */
    
    public UnavailableException(String msg, int seconds) {
	super(msg);

	if (seconds <= 0)
	    this.seconds = -1;
	else
	    this.seconds = seconds;

	permanent = false;
    }

    /**
     *
     * Returns a <code>boolean</code> indicating
     * whether the servlet is permanently unavailable.
     * If so, something is wrong with the servlet, and the
     * system administrator must take some corrective action.
     *
     * @return		<code>true</code> if the servlet is
     *			permanently unavailable; <code>false</code>
     *			if the servlet is available or temporarily
     *			unavailable
     *
     */
     
    public boolean isPermanent() {
	return permanent;
    }
  
    /**
     * @deprecated	As of Java Servlet API 2.2, with no replacement.
     *
     * Returns the servlet that is reporting its unavailability.
     * 
     * @return		the <code>Servlet</code> object that is 
     *			throwing the <code>UnavailableException</code>
     *
     */
     
    public Servlet getServlet() {
	return servlet;
    }

    /**
     * Returns the number of seconds the servlet expects to 
     * be temporarily unavailable.  
     *
     * <p>If this method returns a negative number, the servlet
     * is permanently unavailable or cannot provide an estimate of
     * how long it will be unavailable. No effort is
     * made to correct for the time elapsed since the exception was
     * first reported.
     *
     * @return		an integer specifying the number of seconds
     *			the servlet will be temporarily unavailable,
     *			or a negative number if the servlet is permanently
     *			unavailable or cannot make an estimate
     *
     */
     
    public int getUnavailableSeconds() {
	return permanent ? -1 : seconds;
    }
}
