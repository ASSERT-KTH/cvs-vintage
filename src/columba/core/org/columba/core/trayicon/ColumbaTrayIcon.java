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

package org.columba.core.trayicon;

import javax.swing.Icon;
import javax.swing.JPopupMenu;

import org.columba.core.gui.action.AboutDialogAction;
import org.columba.core.gui.action.ExitAction;
import org.columba.core.gui.action.OpenNewAddressbookWindowAction;
import org.columba.core.gui.action.OpenNewMailWindowAction;
import org.columba.core.gui.action.ShowHelpAction;
import org.columba.core.gui.menu.CMenuItem;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.shutdown.ShutdownManager;
import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

/**
 * Uses the JDIC api to add a tray icon to the system default tray.
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class ColumbaTrayIcon {
	/**
	 * Default icon for the TrayIcon.
	 */
	public static final Icon DEFAULT_ICON = ImageLoader.getImageIcon("trayicon.png");

	private static ColumbaTrayIcon instance;
	
	private SystemTray tray;	
	private TrayIcon trayIcon;
	private JPopupMenu menu;
	
	protected ColumbaTrayIcon() {
		tray = SystemTray.getDefaultSystemTray();
		trayIcon = new TrayIcon(DEFAULT_ICON, "Columba");
		trayIcon.setPopupMenu(getPopupMenu());
	}
	
	/**
	 * Gets the instance of the ColumbaTrayIcon.
	 * 
	 * @return singleton instance
	 */
	public static ColumbaTrayIcon getInstance() {
		if( instance == null) {
			instance = new ColumbaTrayIcon();
		}
		
		return instance;
	}
	
	/**
	 * Add the tray icon to the default system tray.
	 *
	 */
	public void addToSystemTray() {
		tray.addTrayIcon(trayIcon);
		
		ShutdownManager.getShutdownManager().register(new Runnable() {
			public void run() {
				ColumbaTrayIcon.getInstance().removeFromSystemTray();
			}
			
		});
	}

	
	/**
	 * Sets the tooltip of the tray icon.
	 * 
	 * @param tooltip
	 */
	public void setTooltip(String tooltip) {
		trayIcon.setToolTip(tooltip);
	}
	
	/**
	 * Sets the icon of the tray icon.
	 * 
	 * @param icon
	 */
	public void setIcon(Icon icon) {
		trayIcon.setIcon(icon);
	}
	
	/**
	 * Resets the icon to the default icon.
	 * 
	 */
	public void resetIcon() {
		trayIcon.setIcon(DEFAULT_ICON);
	}
	
	/**
	 * Removes the tray icon from the system tray.s
	 */
	public void removeFromSystemTray() {
		tray.removeTrayIcon(trayIcon);
	}
	
	
	private JPopupMenu getPopupMenu() {
		if (menu == null) {
			menu = new JPopupMenu();
			menu.add(new CMenuItem(new OpenNewMailWindowAction(null)));
			menu.add(new CMenuItem(new OpenNewAddressbookWindowAction(null)));
			menu.addSeparator();
			menu.add(new CMenuItem(new AboutDialogAction(null)));
			menu.add(new CMenuItem(new ShowHelpAction(null)));
			menu.addSeparator();
			menu.add(new CMenuItem(new ExitAction(null)));
		}
		return menu;
	}
	
	
}
