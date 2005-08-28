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
package org.columba.mail.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.nio.charset.Charset;

import javax.swing.KeyStroke;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.charset.CharsetOwnerInterface;
import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.core.gui.selection.ISelectionListener;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.resourceloader.GlobalResourceLoader;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.folder.command.PrintMessageCommand;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.table.selection.TableSelectionChangedEvent;

public class PrintAction extends AbstractColumbaAction implements
		ISelectionListener {
	public PrintAction(IFrameMediator controller) {
		super(controller, GlobalResourceLoader.getString("global", "global",
				"menu_message_print"));

		// tooltip text
		putValue(SHORT_DESCRIPTION, GlobalResourceLoader.getString("global",
				"global", "menu_message_print_tooltip").replaceAll("&", ""));

		// small icon for menu
		putValue(SMALL_ICON, ImageLoader
				.getSmallImageIcon("stock_print-16.png"));

		// large icon for toolbar
		putValue(LARGE_ICON, ImageLoader.getImageIcon("stock_print.png"));

		// shortcut key
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P,
				ActionEvent.CTRL_MASK));

		// *20030614, karlpeder* In main view only enabled when
		// message(s) selected
		if (frameMediator instanceof AbstractMailFrameController) {
			((AbstractMailFrameController) frameMediator)
					.registerTableSelectionListener(this);
		}

		setEnabled(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		IMailFolderCommandReference r = ((MailFrameMediator) getFrameMediator())
				.getTableSelection();

		Charset charset = ((CharsetOwnerInterface) getFrameMediator())
				.getCharset();
		PrintMessageCommand c = new PrintMessageCommand(r, charset);
		CommandProcessor.getInstance().addOp(c);
	}

	/**
	 * Ensures that the action is only enabled when at least one message is
	 * selected in the GUI.
	 * 
	 * @see org.columba.core.gui.util.ISelectionListener#selectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent e) {
		setEnabled(((TableSelectionChangedEvent) e).getUids().length > 0);
	}
}
