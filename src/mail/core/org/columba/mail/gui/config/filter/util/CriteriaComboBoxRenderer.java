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

import org.columba.core.gui.util.NotifyDialog;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.mail.plugin.FilterPluginHandler;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CriteriaComboBoxRenderer
	extends JLabel
	implements ListCellRenderer {

	FilterPluginHandler pluginHandler;
	/**
	 * 
	 */
	public CriteriaComboBoxRenderer() {
		super();

		try {

			pluginHandler =
				(FilterPluginHandler) MainInterface.pluginManager.getHandler(
					"org.columba.mail.filter");
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
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		// id = org.columba.example.HelloWorld$HelloWorldPlugin
		String id = (String) value;

		String userVisibleName = pluginHandler.getUserVisibleName(id);

		setText(userVisibleName);

		return this;
	}

}
