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
package org.columba.core.gui.trayicon;

import javax.swing.Icon;
import javax.swing.JPopupMenu;

import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;


public class JDICTrayIcon implements TrayIconInterface {

	private SystemTray tray;

	private TrayIcon trayIcon;
	
	public JDICTrayIcon() {
		tray = SystemTray.getDefaultSystemTray();
	}

	public void addToTray(Icon icon, String name) {
		trayIcon = new TrayIcon(icon, name);
		tray.addTrayIcon(trayIcon);
	}

	public void setIcon(Icon icon) {
		trayIcon.setIcon(icon);
	}

	public void setPopupMenu(JPopupMenu menu) {
		trayIcon.setPopupMenu(menu);
	}

	public void removeFromTray() {
		tray.removeTrayIcon(trayIcon);
	}

	public void setTooltip(String tooltip){
		trayIcon.setToolTip(tooltip);
	}

	
}
