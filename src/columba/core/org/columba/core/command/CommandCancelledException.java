//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
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
