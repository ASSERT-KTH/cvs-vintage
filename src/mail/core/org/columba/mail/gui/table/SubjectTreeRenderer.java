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
package org.columba.mail.gui.table;

import org.columba.mail.gui.table.model.MessageNode;
import org.columba.mail.message.ColumbaHeader;

import org.columba.ristretto.message.Flags;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Renderer for the JTree in the JTable, which is responsible for displaying the
 * Subject: headerfield.
 * <p>
 * I'm still not convinced which method to calculate the bounds of the table
 * column is faster. <br>
 * The first one overwrites paint() and layout(), the other just overwrites
 * setBounds() only, using the passed JTableColumn. Personally, I prefer the
 * second version, because it should be much faster than calculating the column
 * size, based on the text and font settings.
 * 
 * 
 * @author fdietz
 */
public class SubjectTreeRenderer extends DefaultTreeCellRenderer {
	private Font plainFont;
	private Font boldFont;
	private Font underlinedFont;
	private JTable table;
	private TableColumn tc;

	/**
	 * @param table
	 */
	public SubjectTreeRenderer(JTable table) {
		super();

		this.table = table;

		boldFont = UIManager.getFont("Label.font");
		boldFont = boldFont.deriveFont(Font.BOLD);

		plainFont = UIManager.getFont("Label.font");

		underlinedFont = UIManager.getFont("Tree.font");
		underlinedFont = underlinedFont.deriveFont(Font.ITALIC);

		setOpaque(true);

		setBackground(null);
		setBackgroundNonSelectionColor(null);
	}

	public void setBounds(int x, int y, int w, int h) {
		if (tc == null) {
			tc = table.getColumn("Subject");
		}

		super.setBounds(x, y, tc.getWidth() - x, h);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
	 *      java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, selected, expanded,
				leaf, row, hasFocus);

		MessageNode messageNode = (MessageNode) value;

		if (messageNode.getUserObject().equals("root")) {
			setText("...");
			setIcon(null);

			return this;
		}

		ColumbaHeader header = messageNode.getHeader();

		if (header == null) {
			return this;
		}

		/*
		if (selected) {
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		} else {
			setBackground(table.getBackground());
			setForeground(table.getForeground());
		}
		*/

		Flags flags = ((ColumbaHeader) header).getFlags();

		if (flags != null) {
			if (!flags.getSeen()) {
				if (!getFont().equals(boldFont)) {
					setFont(boldFont);
				}
			} else if (messageNode.isHasRecentChildren()) {
				if (!getFont().equals(underlinedFont)) {
					setFont(underlinedFont);
				}
			} else {
				if (!getFont().equals(plainFont)) {
					setFont(plainFont);
				}
			}
		}

		String subject = (String) header.get("columba.subject");

		if (subject != null) {
			setText(subject);
		} else {
			setText("null");
		}

		setIcon(null);

		return this;
	}

}