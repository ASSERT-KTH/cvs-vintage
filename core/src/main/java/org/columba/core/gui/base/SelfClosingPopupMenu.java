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

package org.columba.core.gui.base;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPopupMenu;
import javax.swing.MenuSelectionManager;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Extension of the JPopupMenu so that if nothing is selected, the menu is closed after 1 second.
 * 
 * @author tstich
 */	
public class SelfClosingPopupMenu implements ActionListener, ChangeListener {
	
	protected Timer timer;
	private JPopupMenu popupMenu;
	
	public SelfClosingPopupMenu(JPopupMenu popupMenu) {
		super();
		
		this.popupMenu = popupMenu;
		
		timer = new Timer(1000, this);
		timer.setRepeats(false);
		
		MenuSelectionManager.defaultManager().addChangeListener(this);
		
	}
	
	/**
	 * @see javax.swing.JPopupMenu#show(java.awt.Component, int, int)
	 */
//	public void show(Component invoker, int x, int y) {
//		super.show(invoker, x, y);
//	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		popupMenu.setVisible(false);
	}

	/**
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {
		// The length is 1 if no item is selected
		if( MenuSelectionManager.defaultManager().getSelectedPath().length==1 ) {
			timer.start();
		} else {
			timer.stop();
		}
	}
}
