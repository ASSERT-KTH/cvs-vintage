/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.commands;

/**
 * Signals that an exception occured within the command architecture.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 */
public abstract class CommandException extends Exception {

    /**
     * Creates a new instance of this class with no specified detail message.
     */
    public CommandException() {
    }

    /**
     * Creates a new instance of this class with the specified detail message.
     * 
     * @param s
     *            the detail message.
     */
    public CommandException(String s) {
        super(s);
    }

    /**
     * Creates a new instance of this class with the specified detail message
     * and cause.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the cause.
     */
    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance of this class with the specified cause.
     * 
     * @param cause
     *            the cause.
     */
    public CommandException(Throwable cause) {
        super(cause);
    }
}
