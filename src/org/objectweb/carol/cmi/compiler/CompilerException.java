/*
 * Copyright (C) 2002-2003, Simon Nieuviarts
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
 */
package org.objectweb.carol.cmi.compiler;

/**
 * @author nieuviar
 * Any error in the compiler is translated into a CompilerException catched at the
 * highest level
 */
public class CompilerException extends Exception {
    CompilerException() {
        super();
    }

    CompilerException(String message) {
        super(message);
    }

    CompilerException(String message, Throwable cause) {
        super(message + cause.toString());
    }

    CompilerException(Throwable cause) {
        super(cause.toString());
    }
}
