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
package org.columba.core.action;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;

import org.columba.core.gui.frame.AbstractFrameController;

/**
 * Adds an Observable/Observer to {@link BasicAction}. 
 * <p>
 * This makes it possible to notify gui-elements like
 * JCheckBoxMenuItem and JToogleButton, which are created using this
 * action when their selection state changes.
 *
 * @author fdietz
 */
public class CheckBoxAction extends FrameAction {

	private JCheckBoxMenuItem checkBoxMenuItem;
	SelectionStateObservable observable;

	public CheckBoxAction(
		AbstractFrameController frameController,
		String name) {
		super(frameController, name);

		observable = new SelectionStateObservable();
	}

	/**
	 * @param frameController
	 * @param name
	 * @param longDescription
	 * @param actionCommand
	 * @param small_icon
	 * @param big_icon
	 * @param mnemonic
	 * @param keyStroke
	 * 
	 * @deprecated
	 */
	public CheckBoxAction(
		AbstractFrameController frameController,
		String name,
		String longDescription,
		String actionCommand,
		ImageIcon small_icon,
		ImageIcon big_icon,
		int mnemonic,
		KeyStroke keyStroke) {
		super(
			frameController,
			name,
			longDescription,
			actionCommand,
			small_icon,
			big_icon,
			mnemonic,
			keyStroke);

		observable = new SelectionStateObservable();
	}

	/**
	 * @param frameController
	 * @param name
	 * @param longDescription
	 * @param tooltip
	 * @param actionCommand
	 * @param small_icon
	 * @param big_icon
	 * @param mnemonic
	 * @param keyStroke
	 * 
	 * @deprecated
	 */
	public CheckBoxAction(
		AbstractFrameController frameController,
		String name,
		String longDescription,
		String tooltip,
		String actionCommand,
		ImageIcon small_icon,
		ImageIcon big_icon,
		int mnemonic,
		KeyStroke keyStroke) {
		super(
			frameController,
			name,
			longDescription,
			tooltip,
			actionCommand,
			small_icon,
			big_icon,
			mnemonic,
			keyStroke);

		observable = new SelectionStateObservable();
	}

	/**
	 * Returns the checkBoxMenuItem.
	 * @return JCheckBoxMenuItem
	 */
	public JCheckBoxMenuItem getCheckBoxMenuItem() {
		return checkBoxMenuItem;
	}

	/**
	 * Sets the checkBoxMenuItem.
	 * @param checkBoxMenuItem The checkBoxMenuItem to set
	 * 
	 * @deprecated Use Observable instead.
	 */
	public void setCheckBoxMenuItem(JCheckBoxMenuItem checkBoxMenuItem) {
		this.checkBoxMenuItem = checkBoxMenuItem;
		checkBoxMenuItem.setState(getInitState());
	}

	/**
	 * @return
	 * 
	 * @deprecated Use Observable instead.
	 */
	public boolean getState() {
		return checkBoxMenuItem.getState();
	}

	/**
	 * @param value
	 * 
	 * @deprecated Use Observable instead.
	 */
	public void setState(boolean value) {
		checkBoxMenuItem.setState(value);
	}

	/**
	 * @return
	 * 
	 * @deprecated Use Observable instead.
	 */
	protected boolean getInitState() {
		return false;
	}

	/**
	 * @return	selection state observable
	 */
	public SelectionStateObservable getObservable() {
		return observable;
	}

}
