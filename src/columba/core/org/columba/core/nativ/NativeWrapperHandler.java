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
package org.columba.core.nativ;

import javax.swing.JPopupMenu;

import org.columba.core.gui.action.AboutDialogAction;
import org.columba.core.gui.action.ExitAction;
import org.columba.core.gui.action.OpenNewAddressbookWindowAction;
import org.columba.core.gui.action.OpenNewMailWindowAction;
import org.columba.core.gui.action.ShowHelpAction;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.menu.CMenuItem;
import org.columba.core.gui.util.ErrorDialog;
import org.columba.core.main.MainInterface;
import org.columba.core.shutdown.ShutdownManager;
import org.columba.core.util.OSInfo;

/**
 * Native code wrapper. Based on the OS the appropriate native code handler is
 * loaded.
 * <p>
 * All methods are handled be these specialized handlers. Currently, only the
 * PopupMenu for the trayicon and trayicon tooltips are supported. For further
 * OS-dependent services we should delegate the work into special classes.
 * <p>
 * Using Composition pattern.
 * 
 * @author fdietz
 */
public class NativeWrapperHandler implements NativeWrapper {

	private NativeWrapper wrapper;
	private JPopupMenu menu;
	private FrameMediator mediator;

	public NativeWrapperHandler(FrameMediator mediator) {
		this.mediator = mediator;
		
		if (OSInfo.isWin32Platform()) {
			try {
				wrapper = new Win32Wrapper(this);
			} catch (Exception e) {
				if (MainInterface.DEBUG)
					e.printStackTrace();
				new ErrorDialog(e.getMessage(), e);
			}
		}

		// if error occured
		if (wrapper == null)
			return;

		// show trayicon in traybar
		wrapper.setTrayIconVisible(true);

		// remove trayicon when exiting Columba
		ShutdownManager.getShutdownManager().register(new Runnable() {
			public void run() {
				// disable trayicon
				wrapper.setTrayIconVisible(false);
			}
		});
	}

	/**
	 * @see org.columba.core.nativ.NativeWrapper#setTrayIconTooltip(java.lang.String)
	 */
	public void setTrayIconToolTip(String toolTip) {
		wrapper.setTrayIconToolTip(toolTip);

	}

	/**
	 * @return Returns the wrapper.
	 */
	public NativeWrapper getWrapper() {
		return wrapper;
	}

	/**
	 * @see org.columba.core.nativ.NativeWrapper#setTrayIconVisible(boolean)
	 */
	public void setTrayIconVisible(boolean visible) {
		wrapper.setTrayIconVisible(visible);

	}

	/**
	 * @see org.columba.core.nativ.NativeWrapper#displayBallonTip(java.lang.String)
	 */
	public void displayBallonTip(String text) {
		wrapper.displayBallonTip(text);

	}

	public JPopupMenu getPopupMenu() {
		if (menu == null) {
			menu = new JPopupMenu();
			menu.add(new CMenuItem(new OpenNewMailWindowAction(mediator)));
			menu.add(new CMenuItem(new OpenNewAddressbookWindowAction(mediator)));
			menu.addSeparator();
			menu.add(new CMenuItem(new AboutDialogAction(mediator)));
			menu.add(new CMenuItem(new ShowHelpAction(mediator)));
			menu.addSeparator();
			menu.add(new CMenuItem(new ExitAction(mediator)));
		}
		return menu;
	}
}