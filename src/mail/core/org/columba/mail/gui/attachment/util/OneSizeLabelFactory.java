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

package org.columba.mail.gui.attachment.util;

import java.awt.Dimension;

public class OneSizeLabelFactory {

	private Dimension preferredSize;
	private int maxWidth;

	public OneSizeLabelFactory(int maxWidth) {
		this.maxWidth = maxWidth;
		preferredSize = new Dimension(-1, -1);
	}

	public OneSizeLabel getNewLabel(String text) {
		return new OneSizeLabel(this, text);
	}

	/**
	 * @return
	 */
	public int getMaxWidth() {
		return maxWidth;
	}

	/**
	 * @return
	 */
	public Dimension getPreferredSize() {
		return preferredSize;
	}

	/**
	 * @param i
	 */
	public void setMaxWidth(int width) {

		if (width < preferredSize.width) {
			preferredSize.width = width;
		}

		maxWidth = width;
	}
	
	/**
	 * @param dimension
	 */
	public void setPreferredSize(Dimension dimension) {
		preferredSize = dimension;
		if( preferredSize.width > maxWidth ) {
			preferredSize.width = maxWidth;
		}
	}
	
	public void reset() {
		preferredSize = new Dimension(-1,-1);
	}

}

