// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.core.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Timo Stich (tstich@users.sourceforge.net)
 * 
 */
public class CommandCancelledException extends Exception {

	/**
	 * Constructor for CommandCancelledException.
	 */
	public CommandCancelledException() {
		super();
	}

	/**
	 * Constructor for CommandCancelledException.
	 * @param message
	 */
	public CommandCancelledException(String message) {
		super(message);
	}

	/**
	 * Constructor for CommandCancelledException.
	 * @param message
	 * @param cause
	 */
	public CommandCancelledException(String message, Throwable cause) {
		this(message);
		compatibleInitCause(cause);
	}

	/**
	 * Constructor for CommandCancelledException.
	 * @param cause
	 */
	public CommandCancelledException(Throwable cause) {
		this();
		compatibleInitCause(cause);
	}
	
	private void compatibleInitCause(Throwable cause) {
		try{
			Method initCause = getClass().getMethod("initCause", new Class[]{ Throwable.class });
			initCause.invoke(this, new Object[]{ cause });
		}catch(NoSuchMethodException nsme){
		}catch(IllegalAccessException iae){
		}catch(InvocationTargetException ite){}
	}
}
