/*
 * Created on 07.09.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.columba.mail.gui.config.export;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.columba.mail.folder.Folder;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class FolderTreeCellRenderer extends DefaultTreeCellRenderer {

	public FolderTreeCellRenderer() {

	}

	public Component getTreeCellRendererComponent(
		JTree tree,
		Object value,
		boolean selected,
		boolean expanded,
		boolean leaf,
		int row,
		boolean hasFocus) {

		super.getTreeCellRendererComponent(
			tree,
			value,
			selected,
			expanded,
			leaf,
			row,
			hasFocus);

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

		if (node instanceof Folder) {
			ImageIcon icon = ((Folder) node).getCollapsedIcon();

			setIcon(icon);

			String name = ((Folder) node).getName();

			setText(name);
		} else {
			String name = (String) node.getUserObject();

			setText(name);
		}
		return this;
	}

	public void paint(Graphics g) {

		Rectangle bounds = g.getClipBounds();
		Font font = getFont();
		FontMetrics fontMetrics = g.getFontMetrics(font);

		int textWidth = fontMetrics.stringWidth(getText());

		int iconOffset = 0;

		//int iconOffset = getHorizontalAlignment() + getIcon().getIconWidth() + 1;

		if (bounds.x == 0 && bounds.y == 0) {
			bounds.width -= iconOffset;
			String labelStr = layout(this, fontMetrics, getText(), bounds);
			setText(labelStr);
		}

		super.paint(g);
	}

	private String layout(
		JLabel label,
		FontMetrics fontMetrics,
		String text,
		Rectangle viewR) {
		Rectangle iconR = new Rectangle();
		Rectangle textR = new Rectangle();
		return SwingUtilities.layoutCompoundLabel(
			fontMetrics,
			text,
			null,
			SwingConstants.RIGHT,
			SwingConstants.RIGHT,
			SwingConstants.RIGHT,
			SwingConstants.RIGHT,
			viewR,
			iconR,
			textR,
			0);
	}
}
