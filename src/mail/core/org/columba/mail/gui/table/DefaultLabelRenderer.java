package org.columba.mail.gui.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import org.columba.mail.gui.table.util.MessageNode;
import org.columba.mail.message.HeaderInterface;

/**
 * @author frd
 *
 * The is basic class every renderer should inherite
 * 
 * It is responsible for paint the background/foreground and borders
 * and gives us a central place for optimization
 * 
 */
public class DefaultLabelRenderer extends JLabel implements TableCellRenderer {

	private Border unselectedBorder = null;
	private Border selectedBorder = null;

	private Color background;
	private Color foreground;

	private Font plainFont, boldFont;

	private JTree tree;

	private boolean isBordered = true;

	/**
	 * Constructor for DefaultLabelRenderer.
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public DefaultLabelRenderer(String arg0, Icon arg1, int arg2) {
		super(arg0, arg1, arg2);
	}

	/**
	 * Constructor for DefaultLabelRenderer.
	 * @param arg0
	 * @param arg1
	 */
	public DefaultLabelRenderer(String arg0, int arg1) {
		super(arg0, arg1);
	}

	/**
	 * Constructor for DefaultLabelRenderer.
	 * @param arg0
	 */
	public DefaultLabelRenderer(String arg0) {
		super(arg0);
	}

	/**
	 * Constructor for DefaultLabelRenderer.
	 * @param arg0
	 * @param arg1
	 */
	public DefaultLabelRenderer(Icon arg0, int arg1) {
		super(arg0, arg1);
	}

	/**
	 * Constructor for DefaultLabelRenderer.
	 * @param arg0
	 */
	public DefaultLabelRenderer(Icon arg0) {
		super(arg0);
	}

	/**
	 * Constructor for DefaultLabelRenderer.
	 */
	public DefaultLabelRenderer(JTree tree) {
		super();

		this.tree = tree;
		boldFont = UIManager.getFont("Tree.font");
		boldFont = boldFont.deriveFont(Font.BOLD);

		plainFont = UIManager.getFont("Tree.font");
	}

	/**
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(
		JTable table,
		Object value,
		boolean isSelected,
		boolean hasFocus,
		int row,
		int column) {

		if (isBordered) {
			if (isSelected) {
				if (selectedBorder == null) {
					selectedBorder =
						BorderFactory.createMatteBorder(
							2,
							5,
							2,
							5,
							table.getSelectionBackground());
				}
				//setBorder(selectedBorder);
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			} else {
				if (unselectedBorder == null) {
					unselectedBorder =
						BorderFactory.createMatteBorder(
							2,
							5,
							2,
							5,
							table.getBackground());
				}

				setBackground(table.getBackground());
				//setBorder(unselectedBorder);
				setForeground(table.getForeground());
			}
		}

		//TreePath path = tree.getPathForRow(row);
		MessageNode messageNode = (MessageNode) value;

		HeaderInterface header = messageNode.getHeader();
		if (header == null) {
			System.out.println("header is null");

			return this;
		}

		if (header.getFlags() != null) {
			if (header.getFlags().getRecent()) {
				if (getFont().equals(boldFont) == false)
					setFont(boldFont);
			} else if (getFont().equals(plainFont) == false) {
				setFont(plainFont);
			}
		}

		return this;
	}

	public boolean isOpaque() {
		return (background != null);
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

	/*************** optimization *****************/


	// if graphics doesn't seem to work correctly
	//  -> comment the following lines

	/*
	public void paint(Graphics g) {
		ui.update(g, this);
	}
	public void repaint() {
	}
	*/
	

	/*
	protected void firePropertyChange(
		String propertyName,
		Object oldValue,
		Object newValue) {
		// this is only needed when using HTML text labels
	}
	*/
}
