/*
 *  Copyright 2001-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.util.io;

/** 
 * A general purpose exception class for the <code>Prompter</code> utility.
 * An exception of this type indicates an internal <code>Prompter</code>
 * problem.
 *
 * @author    Christopher Cain
 * @version   $Revision: 1.2 $ $Date: 2004/02/26 06:37:28 $
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
