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
package org.columba.mail.gui.table.action;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import org.columba.core.action.IMenu;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.command.ColorMessageCommand;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.table.selection.TableSelectionChangedEvent;

/**
 * Creates a menu with a list of colors to select.
 * 
 * @author fdietz
 */
public class ColorMessageMenu
	extends IMenu
	implements ActionListener, SelectionListener {

	// TODO: add central place, which keeps a list of all possible
	//       colors, and provides a custom color configuration possibility
	public static String[] items =
		{ "Black", "Blue", "Gray", "Green", "Red", "Yellow", "Custom" };

	public static Color[] colors =
		{
			Color.black,
			Color.blue,
			Color.gray,
			Color.green,
			Color.red,
			Color.yellow,
			Color.black };
	/**
	 * @param controller
	 * @param caption
	 */
	public ColorMessageMenu(AbstractFrameController controller) {
		super(controller, "Color Message");

		createSubMenu();

		(
			(
				AbstractMailFrameController) controller)
					.registerTableSelectionListener(
			this);
	}

	protected void createSubMenu() {
		// TODO: implement custom menuitem renderer
		for (int i = 0; i < items.length; i++) {
			JMenuItem item = new JMenuItem(items[i]);
			item.setActionCommand(items[i]);
			item.addActionListener(this);
			add(item);
		}

	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		// get current message list selection
		FolderCommandReference[] r =
			((AbstractMailFrameController) getController()).getTableSelection();

		// which menuitem was selected?
		int result = -1;
		for (int i = 0; i < items.length; i++) {
			if (action.equals(items[i])) {
				result = i;
				break;
			}
		}

		// add color selection to reference
		for (int i = 0; i < r.length; i++) {
			r[i].setColorValue(colors[result].getRGB());
		}

		// pass command to scheduler
		MainInterface.processor.addOp(new ColorMessageCommand(r));
	}

	public void selectionChanged(SelectionChangedEvent e) {
		if (((TableSelectionChangedEvent) e).getUids().length > 0) {
			setEnabled(true);
		} else
			setEnabled(false);
	}
}
