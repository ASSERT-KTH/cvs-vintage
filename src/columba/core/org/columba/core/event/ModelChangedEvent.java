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

package org.columba.core.event;

public class ModelChangedEvent {
	
	public static final int CHANGED = 0;
	public static final int ADDED = 1;
	public static final int REMOVED = 2;
	
	private int mode;
	private Object data;
	
	public ModelChangedEvent() {
		mode = CHANGED;
	}
	
	public ModelChangedEvent(int mode) {
		this.mode = mode;
	}	
	
	public ModelChangedEvent(int mode, Object data) {
		this.mode = mode;
		this.data = data;
	}	

	/**
	 * @return int
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * @return Object
	 */
	public Object getData() {
		return data;
	}

	/**
	 * Sets the data.
	 * @param data The data to set
	 */
	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * Sets the mode.
	 * @param mode The mode to set
	 */
	public void setMode(int mode) {
		this.mode = mode;
	}

}
