/*
 * Created on 07.08.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.gui.plugin;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class VersionRenderer extends JLabel implements TableCellRenderer {

	/**
	 * 
	 */
	public VersionRenderer() {
		super();

		setHorizontalAlignment(SwingConstants.CENTER);

	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(
		JTable table,
		Object value,
		boolean isSelected,
		boolean hasFocus,
		int rowIndex,
		int vColIndex) {

		PluginNode node = (PluginNode) value;

		String version = node.getVersion();
		if ( version == null ) version = " ";
		
		setText(version);

		return this;
	}

	// The following methods override the defaults for performance reasons
	public void validate() {
	}
	public void revalidate() {
	}
	protected void firePropertyChange(
		String propertyName,
		Object oldValue,
		Object newValue) {
	}
	public void firePropertyChange(
		String propertyName,
		boolean oldValue,
		boolean newValue) {
	}

}
