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

import javax.swing.JLabel;

class OneSizeLabel extends JLabel {
	
	private OneSizeLabelFactory factory;
	
	/**
	 * @param arg0
	 */
	public OneSizeLabel(OneSizeLabelFactory factory, String arg0) {
		super(arg0);
		this.factory = factory;
		adjustSize();
	}

	/* (non-Javadoc)
	 * @see java.awt.Component#getPreferredSize()
	 */
	public Dimension getPreferredSize() {
		return factory.getPreferredSize();
	}
	
	public void adjustSize() {
		Dimension mySize = super.getPreferredSize();
		if( mySize == null ) return;
		
		if( mySize.width > factory.getPreferredSize().width )
			factory.setPreferredSize( mySize );
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#setPreferredSize(java.awt.Dimension)
	 */
	public void setPreferredSize(Dimension arg0) {
		factory.setPreferredSize(arg0);
	}

}

