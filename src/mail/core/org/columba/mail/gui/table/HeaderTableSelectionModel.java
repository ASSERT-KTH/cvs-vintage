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

package org.columba.mail.gui.table;

import javax.swing.DefaultListSelectionModel;

public class HeaderTableSelectionModel extends DefaultListSelectionModel {

	boolean gestureStarted = false;
	
	
	public void setSelectionInterval(int index0, int index1) {
		if (!gestureStarted && !isSelectedIndex(index0)) {
			super.setSelectionInterval(index0, index1);
		}
	}

	public void setValueIsAdjusting(boolean isAdjusting) {
		if (isAdjusting == false)
			gestureStarted = false;
	}
	/**
	 * @return boolean
	 */
	public boolean isGestureStarted() {
		return gestureStarted;
	}

	/**
	 * Sets the gestureStarted.
	 * @param gestureStarted The gestureStarted to set
	 */
	public void setGestureStarted(boolean gestureStarted) {
		this.gestureStarted = gestureStarted;
	}

}
