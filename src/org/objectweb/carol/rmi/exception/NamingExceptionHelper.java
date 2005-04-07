/**
 * Copyright (C) 2005 - Bull S.A.
 *
 * CAROL: Common Architecture for RMI ObjectWeb Layer
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * --------------------------------------------------------------------------
 * $Id: NamingExceptionHelper.java,v 1.2 2005/04/07 15:07:08 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.rmi.exception;

import javax.naming.NamingException;

/**
 * This class throw NamingException by using provided exception, and set the
 * initCause of the newly created exception. This avoid to forget initial
 * exception
 * @author Florent Benoit
 */
public class NamingExceptionHelper {

    /**
     * Utility class, no constructor
     */
    private NamingExceptionHelper() {
    }

    /**
     * Build a new exception with the given exception by wrapping it in a NamingException
     * @return built exception
     * @param message text error for the exception
     * @param originalException original exception
     */
    public static NamingException create(String message, Exception originalException) {
        NamingException ne = new NamingException(message);
        ne.initCause(originalException);
        return ne;
    }

    /**
     * Build a new exception with the given error by wrapping it in a NamingException
     * @return built exception
     * @param message text error for the exception
     * @param t original error
     */
    public static NamingException create(String message, Throwable t) {
        NamingException ne = new NamingException(message);
        ne.initCause(t);
        return ne;
    }

}
