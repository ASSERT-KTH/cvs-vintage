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

package org.columba.mail.gui.table.action;

import java.awt.event.ActionEvent;

import org.columba.core.action.FrameAction;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.command.SaveMessageSourceAsCommand;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.table.selection.TableSelectionChangedEvent;
import org.columba.mail.util.MailResourceLoader;

/**
 * Action for saving message source, i.e. for saving a message
 * as-is incl. all headers.
 * @author Karl Peder Olesen (karlpeder), 20030615
 */
public class SaveMessageSourceAsAction
	extends FrameAction
	implements SelectionListener {

	public SaveMessageSourceAsAction(AbstractFrameController controller) {
		super(
				controller,
				MailResourceLoader.getString(
					"menu", "mainframe", "menu_file_save"));
		
		// tooltip text
		setTooltipText(
				MailResourceLoader.getString(
					"menu", "mainframe", "menu_file_save_tooltip"));
		
		// action command
		setActionCommand("SAVE_SOURCE");
		
		// icons
		setSmallIcon(ImageLoader.getSmallImageIcon("stock_save_as-16.png"));
		setLargeIcon(ImageLoader.getImageIcon("stock_save.png"));

		setEnabled(false);
		((AbstractMailFrameController) frameController)
				.registerTableSelectionListener(this);
	}

	/**
	 * Executes this action - i.e. saves message source
	 * by invocing the necessary command.
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		FolderCommandReference[] r =
			((AbstractMailFrameController) getFrameController()).getTableSelection();

		ColumbaLogger.log.debug("Save Message Source As... called");
		SaveMessageSourceAsCommand c =
			new SaveMessageSourceAsCommand(r);
			
		MainInterface.processor.addOp(c);
	}

	/**
	 * Handles enabling / disabling of menu/action depending
	 * on selection
	 * @see org.columba.core.gui.util.SelectionListener#selectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent e) {
		setEnabled(((TableSelectionChangedEvent) e).getUids().length > 0);
	}
}
