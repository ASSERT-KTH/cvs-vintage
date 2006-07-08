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
package org.columba.core.gui.toolbar;

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JToolBar;

import org.columba.core.gui.action.AbstractColumbaAction;

/**
 * Extendable toolbar.
 * 
 * @author fdietz
 */

public class ExtendableToolBar extends JToolBar {

	int insertPosition = 0;

	public ExtendableToolBar() {
		super();

		setRollover(true);
		// setFloatable(true);
	}

	public void add(AbstractColumbaAction action) {
		JButton button = ToolBarButtonFactory.createButton(action);

		add(button, insertPosition);

		insertPosition++;
	}

	public void insert(AbstractColumbaAction action, int position) {
		JButton button = ToolBarButtonFactory.createButton(action);

		add(button, position);

		if (position >= insertPosition)
			insertPosition++;
	}

	/**
	 * @see javax.swing.JToolBar#addSeparator()
	 */
	public void addSeparator() {
		JToolBar.Separator s = new JToolBar.Separator(null);
		if (getOrientation() == VERTICAL) {
			s.setOrientation(JSeparator.HORIZONTAL);
		} else {
			s.setOrientation(JSeparator.VERTICAL);
		}

		add(s, insertPosition);

		insertPosition++;
	}

	/**
	 * @see javax.swing.JToolBar#updateUI()
	 */
	public void updateUI() {
		super.updateUI();
		setRollover(true);
	}

}