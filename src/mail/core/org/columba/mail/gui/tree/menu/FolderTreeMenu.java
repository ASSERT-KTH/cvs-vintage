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
package org.columba.mail.gui.tree.menu;

import java.awt.event.MouseAdapter;

import javax.swing.JPopupMenu;

import org.columba.mail.gui.tree.TreeController;
import org.columba.mail.gui.tree.action.FolderTreeActionListener;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class FolderTreeMenu {
	private JPopupMenu popup;
	private TreeController treeController;

	public FolderTreeMenu(TreeController c) {
		this.treeController = c;

		initContextMenu();
	}

	public JPopupMenu getPopupMenu() {
		return popup;
	}

	protected FolderTreeActionListener getActionListener() {
		return treeController.getActionListener();
	}

	protected void initContextMenu() {

		popup = new JPopupMenu();

		MouseAdapter handler = treeController.getMailFrameController().getMouseTooltipHandler();
		org.columba.core.gui.util.CMenuItem menuItem;

		menuItem =
			new org.columba.core.gui.util.CMenuItem(
				getActionListener().addAction);
		menuItem.addMouseListener(handler);
		popup.add(menuItem);
		menuItem =
			new org.columba.core.gui.util.CMenuItem(
				getActionListener().addVirtualAction);
		menuItem.addMouseListener(handler);
		popup.add(menuItem);

		menuItem =
			new org.columba.core.gui.util.CMenuItem(
				getActionListener().renameAction);
		menuItem.addMouseListener(handler);
		popup.add(menuItem);
		menuItem =
			new org.columba.core.gui.util.CMenuItem(
				getActionListener().removeAction);
		menuItem.addMouseListener(handler);
		popup.add(menuItem);

		/*
		    popup.addSeparator();
		    menuItem =  new org.columba.core.gui.util.CMenuItem(getActionListener().moveupAction);
		menuItem.addMouseListener( handler );
		popup.add( menuItem );
		    menuItem =  new org.columba.core.gui.util.CMenuItem(getActionListener().movedownAction);
		menuItem.addMouseListener( handler );
		popup.add( menuItem );
		*/

		popup.addSeparator();
		menuItem =
			new org.columba.core.gui.util.CMenuItem(
				getActionListener().expungeAction);
		menuItem.addMouseListener(handler);
		popup.add(menuItem);
		menuItem =
			new org.columba.core.gui.util.CMenuItem(
				getActionListener().emptyAction);
		menuItem.addMouseListener(handler);
		popup.add(menuItem);

		/*
		menuItem =  new org.columba.core.gui.util.CMenuItem(getActionListener().compactAction);
		menuItem.addMouseListener( handler );
		popup.add( menuItem );
		*/
		popup.addSeparator();

		menuItem =
			new org.columba.core.gui.util.CMenuItem(
				getActionListener().applyFilterAction);
		menuItem.addMouseListener(handler);
		popup.add(menuItem);
		menuItem =
			new org.columba.core.gui.util.CMenuItem(
				getActionListener().filterPreferencesAction);
		menuItem.addMouseListener(handler);
		popup.add(menuItem);
		popup.addSeparator();

		menuItem =
			new org.columba.core.gui.util.CMenuItem(
				getActionListener().subscribeAction);
		menuItem.addMouseListener(handler);
		popup.add(menuItem);

	}

}