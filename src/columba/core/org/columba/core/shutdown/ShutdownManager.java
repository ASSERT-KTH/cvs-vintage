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
package org.columba.core.shutdown;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ShutdownManager {

	Vector list;

	public ShutdownManager() {
		list = new Vector();
	}

	public void register(ShutdownPluginInterface plugin) {
		list.add(plugin);
	}

	public void shutdown() {
		JFrame dialog = new JFrame("Saving Folders...");

		dialog.getContentPane().add(new JButton("Saving Folders..."), BorderLayout.CENTER);
		dialog.pack();

		java.awt.Dimension dim = new Dimension(300, 50);
		dialog.setSize(dim);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		dialog.setLocation(
			screenSize.width / 2 - dim.width / 2,
			screenSize.height / 2 - dim.height / 2);

		dialog.setVisible(true);

		for (int i = 0; i < list.size(); i++) {
			ShutdownPluginInterface plugin =
				(ShutdownPluginInterface) list.get(i);

			plugin.shutdown();
		}

		dialog.setVisible(false);

		System.exit(1);
	}
}
