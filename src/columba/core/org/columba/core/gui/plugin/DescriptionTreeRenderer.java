/*
 * Created on 06.08.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.gui.plugin;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.columba.core.main.MainInterface;
import org.columba.core.xml.XmlElement;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DescriptionTreeRenderer extends DefaultTreeCellRenderer {

	/* (non-Javadoc)
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
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

		PluginNode node = (PluginNode) value;

		String id = node.getId();
		XmlElement e = MainInterface.pluginManager.getPluginElement(id);
		String description = null;
		if (e == null)
			description = id;
		else
			description = e.getAttribute("name");
		setText(description);

		String tooltip = node.getTooltip();
		setToolTipText(tooltip);

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
