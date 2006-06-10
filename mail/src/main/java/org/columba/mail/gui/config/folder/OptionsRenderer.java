// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.config.folder;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.columba.mail.util.MailResourceLoader;

/**
 * Translated cell values.
 * 
 * @author fdietz
 *
 */
class OptionsRenderer extends DefaultTableCellRenderer {

	public OptionsRenderer() {
		super();
	}

	/**
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
	 *      java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable arg0, Object value,
			boolean arg2, boolean arg3, int arg4, int arg5) {

		super
				.getTableCellRendererComponent(arg0, value, arg2, arg3, arg4,
						arg5);

		setText(MailResourceLoader.getString("dialog", "folderoptions",
				(String) value));

		return this;
	}
}