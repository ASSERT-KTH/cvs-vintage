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
package org.columba.mail.gui.infopanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.columba.core.gui.util.CInfoPanel;
import org.columba.mail.config.FolderItem;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.folder.MessageFolderInfo;
import org.columba.mail.gui.tree.selection.TreeSelectionListener;

public class FolderInfoPanel
	extends CInfoPanel
	implements TreeSelectionListener {
	private JLabel leftLabel, readLabel, unreadLabel, recentLabel;
	private JPanel rightPanel;

	private MessageFolderInfo info;
	private FolderItem item;

	public void initComponents() {
		super.initComponents();

		leftLabel = new JLabel("Total: Unseen:");
		leftLabel.setForeground(Color.white);
		leftLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		leftLabel.setFont(font);
		leftLabel.setIconTextGap(10);

		leftLabel.setText("Folder");

		gridbagConstraints.gridx = 0;
		gridbagConstraints.weightx = 0.0;
		gridbagConstraints.anchor = GridBagConstraints.WEST;

		gridbagLayout.setConstraints(leftLabel, gridbagConstraints);
		panel.add(leftLabel);

		Component box = Box.createHorizontalGlue();
		gridbagConstraints.gridx = 1;
		gridbagConstraints.weightx = 1.0;
		gridbagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridbagLayout.setConstraints(box, gridbagConstraints);
		panel.add(box);

		gridbagConstraints.gridx = 2;
		gridbagConstraints.weightx = 0.0;
		gridbagConstraints.fill = GridBagConstraints.NONE;

		gridbagConstraints.anchor = GridBagConstraints.EAST;

		GridBagLayout layout = new GridBagLayout();
		rightPanel = new JPanel();
		rightPanel.setLayout(layout);
		rightPanel.setOpaque(false);
		gridbagLayout.setConstraints(rightPanel, gridbagConstraints);
		panel.add(rightPanel);

		readLabel = new JLabel();

		readLabel.setFont(font);
		readLabel.setForeground(Color.white);
		readLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		unreadLabel = new JLabel();

		unreadLabel.setFont(font);
		unreadLabel.setForeground(Color.white);
		unreadLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		recentLabel = new JLabel();
		recentLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		recentLabel.setFont(font);
		recentLabel.setForeground(Color.white);
		recentLabel.setIconTextGap(10);

		gridbagConstraints = new GridBagConstraints();
		gridbagConstraints.gridx = 0;
		gridbagConstraints.weightx = 0.0;
		gridbagConstraints.anchor = GridBagConstraints.SOUTH;
		gridbagConstraints.insets = new Insets(0, 0, 0, 0);

		gridbagConstraints.gridx = 0;
		gridbagConstraints.insets = new Insets(0, 0, 0, 0);
		gridbagConstraints.anchor = GridBagConstraints.WEST;
		layout.setConstraints(readLabel, gridbagConstraints);
		rightPanel.add(readLabel);

		gridbagConstraints.gridx = 1;
		gridbagConstraints.insets = new Insets(0, 0, 0, 0);
		gridbagConstraints.anchor = GridBagConstraints.WEST;
		layout.setConstraints(unreadLabel, gridbagConstraints);
		rightPanel.add(unreadLabel);

		gridbagConstraints.gridx = 2;
		gridbagConstraints.insets = new Insets(0, 0, 0, 0);
		layout.setConstraints(recentLabel, gridbagConstraints);
		rightPanel.add(recentLabel);
	}

	public void resetRenderer() {
		initComponents();
	}

	public void setFolder(Folder newFolder) {

		item = newFolder.getFolderItem();
		if (item == null)
			return;

		info = ((Folder) newFolder).getMessageFolderInfo();
		if (info == null)
			return;

		int total = info.getExists();
		int unread = info.getUnseen();
		int recent = info.getRecent();

		leftLabel.setIcon(newFolder.getCollapsedIcon());

		leftLabel.setText(newFolder.getName() + " ( total: " + total + " )");
		unreadLabel.setText(" unread: " + unread);
		readLabel.setText(" read: " + (total - unread) + "  ");

		if (recent > 0) {
			recentLabel.setText(" recent: " + recent);

		} else {
			recentLabel.setText("");
		}

	}
	/* (non-Javadoc)
	 * @see org.columba.mail.gui.tree.selection.TreeSelectionListener#folderSelectionChanged(org.columba.mail.folder.FolderTreeNode)
	 */
	public void folderSelectionChanged(FolderTreeNode newFolder) {
		if (newFolder == null)
			return;

		setFolder((Folder) newFolder);

	}

}
