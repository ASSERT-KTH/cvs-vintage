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
package org.columba.mail.gui.tree.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.config.FolderItem;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.folder.MessageFolderInfo;

public class FolderTreeCellRenderer
	extends DefaultTreeCellRenderer //extends JLabel implements TreeCellRenderer
{
	Border unselectedBorder = null;
	Border selectedBorder = null;
	boolean isBordered = true;
	boolean bool;
	//TreeController treeController;

	private ImageIcon image1,
		image2,
//		image3,
//		image4,
//		image5,
//		image6,
//		image7,
//		image8,
		image9,
		image10;

	private Font plainFont, boldFont, italicFont;

	public FolderTreeCellRenderer( boolean bool) {
		super();

		//this.treeController = treeController;
		
		this.bool = bool;

		boldFont = UIManager.getFont("Tree.font");
		boldFont = boldFont.deriveFont(Font.BOLD);
		italicFont = UIManager.getFont("Tree.font");
		italicFont = italicFont.deriveFont(Font.ITALIC);
		plainFont = UIManager.getFont("Tree.font");

		image1 = ImageLoader.getSmallImageIcon("folder.png");
		image2 = ImageLoader.getSmallImageIcon("localhost.png");

		image9 = ImageLoader.getSmallImageIcon("remotehost.png");
		image10 = ImageLoader.getSmallImageIcon("virtualfolder.png");

	}

	public Component getTreeCellRendererComponent(
		JTree tree,
		Object value,
		boolean isSelected,
		boolean expanded,
		boolean leaf,
		int row,
		boolean hasFocus) {

		
		FolderTreeNode treeNode = (FolderTreeNode) value;

		/*
		FolderTreeNode selection = treeController.getSelected();
		if( selection != null )
		{
		if ( treeNode.equals(selection) )
			isSelected = true;
		else
			isSelected = false;
		}
		*/
		
		super.getTreeCellRendererComponent(
			tree,
			value,
			isSelected,
			expanded,
			leaf,
			row,
			hasFocus);

		Folder folder = null;

		/*
		if (value instanceof IMAPRootFolder) {
			setText(((IMAPRootFolder) value).getName());
			setIcon(image9);
			return this;
		
		}
		*/

		try {
			folder = (Folder) value;
		} catch (Exception ex) {
			setText(((FolderTreeNode) value).toString());

			return this;
		}

		if (folder != null) {

			if (((Folder) folder).getMessageFolderInfo().getRecent() > 0) {

				setFont(boldFont);
			} else {
				setFont(plainFont);
			}
		} else {
			setFont(plainFont);
		}

		FolderItem item = folder.getFolderItem();

		if (item != null) {
			//int uid = item.getInteger("uid");

			String name;

			//name = folder.getName();

			name = item.get("property", "name");

			MessageFolderInfo info = ((Folder) folder).getMessageFolderInfo();
			if (folder != null) {
				if (info.getUnseen() > 0)
					name = name + " (" + info.getUnseen() + ") ";

				StringBuffer buf = new StringBuffer();
				buf.append("<html><body>&nbsp;Total: " + info.getExists());
				buf.append("<br>&nbsp;Unseen: " + info.getUnseen());
				buf.append("<br>&nbsp;Recent: " + info.getRecent());
				buf.append("</body></html>");
				setToolTipText(buf.toString());

			} else {
				setToolTipText("");
			}

			setText(name);

			if (expanded) {
				setIcon(folder.getExpandedIcon());
			} else {
				setIcon(folder.getCollapsedIcon());
			}
			
			if (!item.getBoolean("selectable",true)) {
				setFont( italicFont );
				setForeground(Color.darkGray);
			}
			
			
			// FIXME

			/*
			if (item.getType().equals("virtual")) {
				setIcon(image10);
			
			} else if (
				item.getType().equals("imaproot") && !item.isMessageFolder()) {
			
				setIcon(image9);
			} else if (item.getAccessRights().equals("system")) {
				if (item.getUid() == 100)
					setIcon(image2);
				else
					setIcon(image1);
			
			} else if (item.getType().equals("imap")) {
				if (item.getMessageFolder().equals("false")) {
					setForeground(Color.darkGray);
					setFont(italicFont);
			
				}
				setIcon(image1);
			} else {
			
				setIcon(image1);
			}
			*/
		}
		return this;
	}
}
