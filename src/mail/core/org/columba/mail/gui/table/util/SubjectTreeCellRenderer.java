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
package org.columba.mail.gui.table.util;

import java.awt.Color;
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
	private Font plainFont, boldFont, underlinedFont;

	private TableView headerTable;

	private JTree tree1;

	private Color background;
	private Color foreground;

	private Font font;
	
	public SubjectTreeCellRenderer(JTree tree) {
		super();
		this.tree1 = tree;
		
		image1 = ImageLoader.getSmallImageIcon("folder.png");

		//image2 = ImageLoader.getImageIcon("table.unread", "");
		image2 = null;

		boldFont = UIManager.getFont("Label.font");
		boldFont = boldFont.deriveFont(Font.BOLD);

		plainFont = UIManager.getFont("Label.font");

		underlinedFont = UIManager.getFont("Tree.font");
		underlinedFont = underlinedFont.deriveFont(Font.ITALIC);

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

		if (messageNode.getUserObject().equals("root")) {
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
				if (getFont().equals(boldFont) == false)
					setFont(boldFont);
			} else if (messageNode.isHasRecentChildren()) {
				if (getFont().equals(underlinedFont) == false)
					setFont(underlinedFont);
			} else if (getFont().equals(plainFont) == false) {
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
	 * Returns the background.
	 * @return Color
	 */
	public Color getBackground() {
		return background;
	}

	/**
	 * Returns the foreground.
	 * @return Color
	 */
	public Color getForeground() {
		return foreground;
	}

	/**
	 * Sets the background.
	 * @param background The background to set
	 */
	public void setBackground(Color background) {
		this.background = background;
	}

	/**
	 * Sets the foreground.
	 * @param foreground The foreground to set
	 */
	public void setForeground(Color foreground) {
		this.foreground = foreground;
	}

	/**
	 * @return
	 */
	public Font getFont() {
		return font;
	}

	/**
	 * @param font
	 */
	public void setFont(Font font) {
		this.font = font;
	}

}