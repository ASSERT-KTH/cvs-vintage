/*
 * Created on 23.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.config.filter.util;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import org.columba.core.gui.util.NotifyDialog;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.mail.plugin.FilterActionPluginHandler;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ActionComboBoxRenderer
	extends JLabel
	implements ListCellRenderer {

	FilterActionPluginHandler pluginHandler;

	/**
	 * 
	 */
	public ActionComboBoxRenderer() {
		super();

		try {
			pluginHandler =
				(
					FilterActionPluginHandler) MainInterface
						.pluginManager
						.getHandler(
					"org.columba.mail.filteraction");
		} catch (PluginHandlerNotFoundException ex) {
			NotifyDialog d = new NotifyDialog();
			d.showDialog(ex);
		}

	}

	/* (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	public Component getListCellRendererComponent(
		JList list,
		Object value,
		int index,
		boolean isSelected,
		boolean arg4) {
		if (cellHasFocus) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		setBorder(
			(cellHasFocus)
				? UIManager.getBorder("List.focusCellHighlightBorder")
				: noFocusBorder);

		// id = org.columba.example.HelloWorld$HelloWorldPlugin
		String id = (String) value;

		String userVisibleName = pluginHandler.getUserVisibleName(id);

		setText(userVisibleName);

		return this;
	}

}
