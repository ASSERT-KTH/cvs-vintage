package org.columba.mail.gui.tree.menu;

import org.columba.mail.gui.tree.*;
import org.columba.mail.gui.tree.action.*;
import org.columba.core.gui.statusbar.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

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