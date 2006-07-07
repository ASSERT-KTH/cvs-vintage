// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.calendar.store.api;

import org.columba.api.exception.BaseRuntimeException;

public class StoreException extends BaseRuntimeException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 6418062444140007181L;

	/**
	 * Store Exception default constructor
	 */
	public StoreException() {
		super();
	}

	/**
	 * StoreException parameterised constructor
	 * @param message
	 */
	public StoreException(String message) {
		super(message);
	}

	/**
	 * StoreException parameterised constructor
	 * @param message
	 * @param cause
	 */
	public StoreException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * StoreException parameterised constructor
	 * @param cause
	 */
	public StoreException(Throwable cause) {
		super(cause);
	}
}
