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

package org.columba.core.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.columba.core.action.FrameAction;
import org.columba.core.charset.CharsetOwnerInterface;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.command.PrintMessageCommand;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.table.selection.TableSelectionChangedEvent;
import org.columba.mail.util.MailResourceLoader;

public class PrintAction 
		extends FrameAction 
		implements SelectionListener {

	public PrintAction(AbstractFrameController controller) {
		super(
			controller,
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_message_print"),
			null,
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_message_print_tooltip"),
			"PRINT",
			ImageLoader.getSmallImageIcon("stock_print-16.png"),
			ImageLoader.getImageIcon("stock_print.png"),
			'0',
			KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));

			// *20030614, karlpeder* In main view only enabled when 
			// message(s) selected
			if (frameController instanceof AbstractMailFrameController) {
				setEnabled(false);
				((AbstractMailFrameController) frameController)
						.registerTableSelectionListener(this);
			} else {
				setEnabled(false); // disables it in other views
			}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		FolderCommandReference[] r =
			(FolderCommandReference[]) getFrameController()
				.getSelectionManager()
				.getSelection("mail.table");

		// GetCharset() added
		String charset =
			((CharsetOwnerInterface) getFrameController())
				.getCharsetManager()
				.getSelectedCharset();

		PrintMessageCommand c = new PrintMessageCommand(r, charset);

		MainInterface.processor.addOp(c);
	}

	/**
	 * Ensures that the action is only enabled when at least 
	 * one message is selected in the GUI.
	 * @see org.columba.core.gui.util.SelectionListener#selectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent e) {

		if (((TableSelectionChangedEvent) e).getUids().length > 0)
			setEnabled(true);
		else
			setEnabled(false);

	}
}
