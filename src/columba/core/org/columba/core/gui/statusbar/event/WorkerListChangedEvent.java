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
package org.columba.core.gui.statusbar.event;

public class WorkerListChangedEvent {
	
	public static final int SIZE_CHANGED = 0;
	
	private int type;
	private int oldValue;
	private int newValue;
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getOldValue() {
		return oldValue;
	}

	public void setOldValue(int oldSize) {
		this.oldValue = oldSize;
	}

	public int getNewValue() {
		return newValue;
	}

	public void setNewValue(int newSize) {
		this.newValue = newSize;
	}

}
