/*
 * $Revision: 1.1 $
 * $Date: 2004/06/01 16:11:47 $
 * $Author: fdietz $
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
/*--

$Id: SpellException.java,v 1.1 2004/06/01 16:11:47 fdietz Exp $

Copyright (C) 2000 Brett McLaughlin & Jason Hunter.
All rights reserved.

*/
package org.columba.mail.spellcheck.cswilly;

import java.io.PrintStream;
import java.io.PrintWriter;


/**
 * <b><code>SpellException</code></b>
 * <p>
 * This <code>Exception</code> subclass is the top level
 *   <code>Exception</code> that spell classes
 *   can throw.  It's subclasses add specificity to the
 *   problems that can occur using spell, but this single
 *   <code>Exception</code> can be caught to handle all
 *   spell specific problems.
 * <p>
 * <b>This is a copy and paste of the JDOMException, create due.</b>
 * @author Brett McLaughlin
 * @author Jason Hunter
 * @version 1.0
 */
public class SpellException extends Exception {
    /** A wrapped <code>Throwable</code> */
    protected Throwable rootCause;

    /**
 * <p>
 * This will create an <code>Exception</code>.
 * </p>
 */
    public SpellException() {
        super("Error occurred in spell application.");
    }

    /**
 * <p>
 * This will create an <code>Exception</code> with the given message.
 * </p>
 *
 * @param message <code>String</code> message indicating
 *                the problem that occurred.
 */
    public SpellException(String message) {
        super(message);
    }

    /**
 * <p>
 * This will create an <code>Exception</code> with the given message
 *   and wrap another <code>Exception</code>.  This is useful when
 *   the originating <code>Exception</code> should be held on to.
 * </p>
 *
 * @param message <code>String</code> message indicating
 *                the problem that occurred.
 * @param exception <code>Exception</code> that caused this
 *                  to be thrown.
 */
    public SpellException(String message, Throwable rootCause) {
        super(message);
        this.rootCause = rootCause;
    }

    /**
 * <p>
 * This returns the message for the <code>Exception</code>. If
 *   there is a root cause, the message associated with the root
 *   <code>Exception</code> is appended.
 * </p>
 *
 * @return <code>String</code> - message for <code>Exception</code>.
 */
    public String getMessage() {
        if (rootCause != null) {
            return super.getMessage() + ": " + rootCause.getMessage();
        } else {
            return super.getMessage();
        }
    }

    /**
 * <p>
 * This prints the stack trace of the <code>Exception</code>. If
 *   there is a root cause, the stack trace of the root
 *   <code>Exception</code> is printed right after.
 * </p>
 */
    public void printStackTrace() {
        super.printStackTrace();

        if (rootCause != null) {
            System.err.print("Root cause: ");
            rootCause.printStackTrace();
        }
    }

    /**
 * <p>
 * This prints the stack trace of the <code>Exception</code> to the given
 *   PrintStream. If there is a root cause, the stack trace of the root
 *   <code>Exception</code> is printed right after.
 * </p>
 */
    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);

        if (rootCause != null) {
            s.print("Root cause: ");
            rootCause.printStackTrace(s);
        }
    }

    /**
 * <p>
 * This prints the stack trace of the <code>Exception</code> to the given
 *   PrintWriter. If there is a root cause, the stack trace of the root
 *   <code>Exception</code> is printed right after.
 * </p>
 */
    public void printStackTrace(PrintWriter w) {
        super.printStackTrace(w);

        if (rootCause != null) {
            w.print("Root cause: ");
            rootCause.printStackTrace(w);
        }
    }

    /**
 * <p>
 * This will return the root cause <code>Throwable</code>, or null
 *   if one does not exist.
 * </p>
 *
 * @return <code>Throwable</code> - the wrapped <code>Throwable</code>.
 */
    public Throwable getRootCause() {
        return rootCause;
    }
}
