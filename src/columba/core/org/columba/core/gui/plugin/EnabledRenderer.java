/*
 * Created on 06.08.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.gui.plugin;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class EnabledRenderer extends JCheckBox implements TableCellRenderer {

	public EnabledRenderer() {
		setHorizontalAlignment(SwingConstants.CENTER);
		//setOpaque(true);
	}
	/* (non-Javadoc)
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(
		JTable arg0,
		Object value,
		boolean arg2,
		boolean arg3,
		int arg4,
		int arg5) {

		/*
		PluginNode node = (PluginNode) value;
		
		if ( node.isCategory() )
		{
			// this node is category folder
			// -> don't make it editable
			
			return new DefaultTableCellRenderer();
		}
		
		boolean b = node.isEnabled();
		*/
		
		Boolean b = (Boolean) value;
		
		setSelected( b.booleanValue());

		return this;
	}

}
