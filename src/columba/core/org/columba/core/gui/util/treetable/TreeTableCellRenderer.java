package org.columba.core.gui.util.treetable;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TreeTableCellRenderer extends JTree implements TableCellRenderer {
	/** Last table/tree row asked to renderer. */
	protected int visibleRow;
	protected JTreeTable treeTable;
	
	public TreeTableCellRenderer(JTreeTable treeTable, TreeModel model) {
		super(model);
		
		this.treeTable = treeTable;

		//setUI(new ColumbaTreeUI());

	}

	/**
	 * updateUI is overridden to set the colors of the Tree's renderer
	 * to match that of the table.
	 */
	public void updateUI() {
		super.updateUI();
		// Make the tree's cell renderer use the table's cell selection
		// colors.
		TreeCellRenderer tcr = getCellRenderer();
		if (tcr instanceof DefaultTreeCellRenderer) {
			DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer) tcr);
			// For 1.1 uncomment this, 1.2 has a bug that will cause an
			// exception to be thrown if the border selection color is
			// null.
			// dtcr.setBorderSelectionColor(null);
			dtcr.setTextSelectionColor(
				UIManager.getColor("Table.selectionForeground"));
			dtcr.setBackgroundSelectionColor(
				UIManager.getColor("Table.selectionBackground"));
		}
	}

	/**
	 * Sets the row height of the tree, and forwards the row height to
	 * the table.
	 */
	public void setRowHeight(int rowHeight) {
		if (rowHeight > 0) {
			super.setRowHeight(rowHeight);
			if (treeTable != null
				&& treeTable.getRowHeight() != rowHeight) {
				treeTable.setRowHeight(this.getRowHeight());
			}
		}
	}

	/**
	 * This is overridden to set the height to match that of the JTable.
	 */
	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, 0, w, treeTable.getHeight());
	}

	/**
	 * Subclassed to translate the graphics such that the last visible
	 * row will be drawn at 0,0.
	 */
	public void paint(Graphics g) {
		
		g.translate(0, -visibleRow * this.getRowHeight());
		super.paint(g);
	}

	/**
	 * TreeCellRenderer method. Overridden to update the visible row.
	 */
	public Component getTableCellRendererComponent(
		JTable table,
		Object value,
		boolean isSelected,
		boolean hasFocus,
		int row,
		int column) {
		
		
		
		if (isSelected)
			this.setBackground(table.getSelectionBackground());
		else
			this.setBackground(table.getBackground());
		
		visibleRow = row;
		
		return this;
	}
	
	
}
