/**
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 2005 Bull S.A.
 * Contact: jonas-team@objectweb.org
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
 * $Id: RemoteExceptionHelper.java,v 1.1 2005/03/11 14:02:28 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.rmi.exception;

import java.rmi.RemoteException;


/**
 * This class throws RemoteException by using provided exception, and set the
 * detail attribute of the newly created exception. This avoid to forget initial
 * exception.<br>
 * initCause() cannot be used as it is a RemoteException.<br>
 * Use detail attribute as suggested by Vadim Nasardinov
 * @author Florent Benoit (
 */
public class RemoteExceptionHelper {


    /**
     * Utility class, no constructor
     */
    private RemoteExceptionHelper() {
    }

    /**
     * Build a new exception with the given exception by wrapping it in a NoSuchObjectException
     * @return built exception
     * @param message text error for the exception
     * @param originalException original exception
     */
    public static RemoteException create(String message, Exception originalException) {
        return new RemoteException(message, originalException);
    }

    /**
     * Build a new exception with the given Throwable by wrapping it in a NoSuchObjectException
     * @return built exception
     * @param originalThrowable original throwable
     */
    public static RemoteException create(Throwable originalThrowable) {
        if (originalThrowable instanceof RemoteException) {
            return (RemoteException) originalThrowable;
        } else {
            return new RemoteException(originalThrowable.getMessage(), originalThrowable);
        }
    }

    /**
     * Build a new exception with the given Throwable by wrapping it in a NoSuchObjectException
     * @return built exception
     * @param message text error for the exception
     * @param originalThrowable original throwable
     */
    public static RemoteException create(String message, Throwable originalThrowable) {
        if (originalThrowable instanceof RemoteException) {
            return (RemoteException) originalThrowable;
        } else {
            return new RemoteException(message, originalThrowable);
        }
    }
}
