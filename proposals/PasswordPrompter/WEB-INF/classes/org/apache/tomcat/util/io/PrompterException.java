/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights 
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
 */ 

package org.apache.tomcat.util.io;

/** 
 * A general purpose exception class for the <code>Prompter</code> utility.
 * An exception of this type indicates an internal <code>Prompter</code>
 * problem.
 *
 * @author    Christopher Cain
 * @version   $Revision: 1.1 $ $Date: 2001/10/08 05:23:57 $
 */
public class PrompterException extends Exception {

    // -------------------------------------------------------- Instance Fields

    /** The error message passed (if any) */
    protected String message = null;

    /** The underlying exception passed (if any) */
    protected Throwable throwable = null;

    // ----------------------------------------------------------- Constructors

    /**
     * Construct a new <code>PrompterException</code> with no additional
     * information.
     */
    public PrompterException() {

    }

    /**
     * Construct a new <code>PrompterException</code> with the specified
     * message.
     *
     * @param message   message describing the exception
     */
    public PrompterException( String message ) {

        this(message, null);

    }

    /**
     * Construct a new <code>PrompterException</code> with the specified
     * throwable.
     *
     * @param throwable   throwable that caused the exception
     */
    public PrompterException( Throwable throwable ) {

        this(null, throwable);

    }

    /**
     * Construct a new <code>PrompterException</code> with the specified
     * message and throwable.
     *
     * @param message     message describing the exception
     * @param throwable   throwable that caused the exception
     */
    public PrompterException( String message, Throwable throwable ) {

	super();
	this.message = message;
	this.throwable = throwable;

    }

    //---------------------------------------------------------- Public Methods

    /**
     * Return the message associated with this exception.
     *
     * @return   the exception message (if any)
     */
    public String getMessage() {

        return (message);

    }

    /**
     * Return the throwable that caused this exception.
     *
     * @return   the underlying exception (if any)
     */
    public Throwable getThrowable() {

        return (throwable);

    }

    /**
     * Return a formatted string that describes this exception.
     *
     * @return   a description of the exception
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("PrompterException: ");

        if (message != null) {
            sb.append(message);

            if (throwable != null)
                sb.append(":  ");
        }

        if (throwable != null)
            sb.append(throwable.toString());

	return (sb.toString());

    }
}
