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
package com.mycom;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.gui.frame.FrameMediator;

public class HelloWorldAction extends AbstractColumbaAction {
	public HelloWorldAction(FrameMediator controller) {
		super(controller, "Hello, World!");

		putValue(SHORT_DESCRIPTION, "Show me this tooltip, please");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		JOptionPane.showMessageDialog(null, "Hello World!");
	}
}