/*
 * $Id: ServletException.java,v 1.1 1999/10/09 00:20:29 duncan Exp $
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


/**
 * Defines a general exception a servlet can throw when it
 * encounters difficulty.
 *
 * @author 	Various
 * @version 	$Version$
 *
 */


public class ServletException extends Exception {

    private Throwable rootCause;





    /**
     * Constructs a new servlet exception.
     *
     */

    public ServletException() {
	super();
    }
    
   

    

    /**
     * Constructs a new servlet exception with the
     * specified message. The message can be written 
     * to the server log and/or displayed for the user. 
     *
     * @param message 		a <code>String</code> 
     *				specifying the text of 
     *				the exception message
     *
     */

    public ServletException(String message) {
	super(message);
    }
    
   
   
    

    /**
     * Constructs a new servlet exception when the servlet 
     * needs to throw an exception and include a message 
     * about the "root cause" exception that interfered with its 
     * normal operation, including a description message.
     *
     *
     * @param message 		a <code>String</code> containing 
     *				the text of the exception message
     *
     * @param rootCause		the <code>Throwable</code> exception 
     *				that interfered with the servlet's
     *				normal operation, making this servlet
     *				exception necessary
     *
     */
    
    public ServletException(String message, Throwable rootCause) {
	super(message);
	this.rootCause = rootCause;
    }





    /**
     * Constructs a new servlet exception when the servlet 
     * needs to throw an exception and include a message
     * about the "root cause" exception that interfered with its
     * normal operation.  The exception's message is based on the localized
     * message of the underlying exception.
     *
     * <p>This method calls the <code>getLocalizedMessage</code> method
     * on the <code>Throwable</code> exception to get a localized exception
     * message. When subclassing <code>ServletException</code>, 
     * this method can be overridden to create an exception message 
     * designed for a specific locale.
     *
     * @param rootCause 	the <code>Throwable</code> exception
     * 				that interfered with the servlet's
     *				normal operation, making the servlet exception
     *				necessary
     *
     */

    public ServletException(Throwable rootCause) {
	super(rootCause.getLocalizedMessage());
	this.rootCause = rootCause;
    }
  
  
 
 
    
    /**
     * Returns the exception that caused this servlet exception.
     *
     *
     * @return			the <code>Throwable</code> 
     *				that caused this servlet exception
     *
     */
    
    public Throwable getRootCause() {
	return rootCause;
    }
}





