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
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.columba.core.action.CheckBoxAction;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.frame.TableOwner;
import org.columba.mail.gui.table.model.TableModelChangedEvent;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ThreadedViewAction
	extends CheckBoxAction
	implements SelectionListener {

	/**
	 * Constructor for ThreadedViewAction.
	 * @param frameController
	 * @param name
	 * @param longDescription
	 * @param actionCommand
	 * @param small_icon
	 * @param big_icon
	 * @param mnemonic
	 * @param keyStroke
	 */
	public ThreadedViewAction(AbstractFrameController frameController) {
		super(
			frameController,
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_view_viewthreaded"),
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_view_viewthreaded_tooltip"),
			"VIEW_THREADED",
			null,
			null,
			'0',
			KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));

		(
			(
				AbstractMailFrameController) frameController)
					.registerTableSelectionListener(
			this);

		setEnabled(true);
	}

	public void tableChanged(TableModelChangedEvent e) {
		ColumbaLogger.log.info("event=" + e);

		if (e.getEventType() == TableModelChangedEvent.UPDATE) {
			FolderCommandReference[] r =
				(FolderCommandReference[])
					((AbstractMailFrameController) frameController)
					.getTreeSelection();

			Folder folder = (Folder) r[0].getFolder();
			final boolean enableThreadedView =
				folder.getFolderItem().getBoolean(
					"property",
					"enable_threaded_view",
					false);

			updateTable(enableThreadedView);

			
			
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						getCheckBoxMenuItem().setSelected(enableThreadedView);
					}
				});
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
		}

	}

	public void actionPerformed(ActionEvent e) {

		if ((frameController instanceof TableOwner) == false)
			return;

		JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();

		FolderCommandReference[] r =
			(FolderCommandReference[])
				((AbstractMailFrameController) frameController)
				.getTreeSelection();

		Folder folder = (Folder) r[0].getFolder();

		boolean enableThreadedView = item.isSelected();
		folder.getFolderItem().set(
			"property",
			"enable_threaded_view",
			enableThreadedView);

		updateTable(getState());

	}

	protected void updateTable(boolean enableThreadedView) {

		if ((frameController instanceof TableOwner) == false)
			return;

		((TableOwner) frameController)
			.getTableController()
			.getView()
			.enableThreadedView(enableThreadedView);

		((TableOwner) frameController)
			.getTableController()
			.getTableModelThreadedView()
			.toggleView(enableThreadedView);

		((TableOwner) frameController)
			.getTableController()
			.getUpdateManager()
			.update();
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.util.SelectionListener#selectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent e) {
		/*
		Folder[] selection = ((TreeSelectionChangedEvent) e).getSelected();
		if (selection.length == 1)
			setEnabled(true);
		else
			setEnabled(false);
		*/
	}
}
