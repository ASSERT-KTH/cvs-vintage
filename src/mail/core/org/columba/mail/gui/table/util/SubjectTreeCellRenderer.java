// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.mail.gui.table.util;

import java.awt.Component;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.gui.table.TableView;
import org.columba.mail.message.HeaderInterface;

/**
 * TITLE:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class SubjectTreeCellRenderer extends DefaultTreeCellRenderer {
	private ImageIcon image1;
	private ImageIcon image2;
	private Font plainFont, boldFont, font;

	private TableView headerTable;

	private JTree tree1;

	public SubjectTreeCellRenderer(JTree tree) {
		super();
		this.tree1 = tree;

		image1 = ImageLoader.getSmallImageIcon("folder.png");

		//image2 = ImageLoader.getImageIcon("table.unread", "");
		image2 = null;

		boldFont = UIManager.getFont("Label.font");
		boldFont = boldFont.deriveFont(Font.BOLD);

		plainFont = UIManager.getFont("Label.font");
		
		font = plainFont;

	}

	public Component getTreeCellRendererComponent(
		JTree tree,
		Object value,
		boolean isSelected,
		boolean expanded,
		boolean leaf,
		int row,
		boolean hasFocus) {

		
		super.getTreeCellRendererComponent(
			tree,
			value,
			isSelected,
			expanded,
			leaf,
			row,
			hasFocus);
		
		
		/*
		TreePath path = tree1.getPathForRow(row);
		if (path == null)
			return this;
		*/

		//MessageNode messageNode = (MessageNode) path.getLastPathComponent();
		MessageNode messageNode = (MessageNode) value;

		
		if ( messageNode.getUserObject().equals("root") )
			{
				setText("...");
				setIcon(null);
				return this;
			}
		
		HeaderInterface header = messageNode.getHeader();
		if (header == null) {
			System.out.println("header is null");
			return this;
		}

		if (header.getFlags() != null) {
			if (header.getFlags().getRecent()) {
				font = boldFont;
				setFont(boldFont);
			} else {
				font = plainFont;
				setFont(plainFont);
			}
		}

		String subject = (String) header.get("subject");
		if (subject != null)
			setText(subject);
		else
			setText("");

		setIcon(null);

		return this;
	}
	/**
	 * @see java.awt.MenuContainer#getFont()
	 */
	public Font getFont() {
		return font;
	}

}