//The contents of this file are subject to the Mozilla Public License Version
//1.1
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
//Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.core.nativ;

import java.awt.Component;
import java.awt.Window;

import javax.swing.JPopupMenu;
import javax.swing.JWindow;

import com.jniwrapper.DefaultLibraryLoader;
import com.jniwrapper.win32.Msg;
import com.jniwrapper.win32.gdi.Icon;
import com.jniwrapper.win32.shell.TrayIcon;
import com.jniwrapper.win32.shell.TrayIconListener;
import com.jniwrapper.win32.shell.TrayMessage;
import com.jniwrapper.win32.ui.AWTWindowDecorator;
import com.jniwrapper.win32.ui.Wnd;

/**
 * Wrapper for jniwrapper library.
 * <p>
 * TODO: add all ballon message types, including warning/error/info
 * 
 * @author fdietz
 */
public class Win32Wrapper implements NativeWrapper, TrayIconListener {

	private TrayIcon trayIcon;
	private JPopupMenu menu;
	private Window invoker;
	private NativeWrapperHandler handler;
	
	/**
	 * default constructor
	 */
	public Win32Wrapper(NativeWrapperHandler handler) {
		super();

		this.handler = handler;
		
		// load all win32 dlls
		DefaultLibraryLoader.getInstance().addPath("native/win32/JNI-wrapper");

		// init trayicon
		trayIcon = new TrayIcon();

		// init icon
		Icon icon = new Icon();
		icon.loadFromFile("res/org/columba/core/images/Columba.ico");
		trayIcon.setIcon(icon);

		trayIcon.addTrayListener(this);

	}

	/**
	 * @see org.columba.core.nativ.NativeWrapper#setTrayIconToolTip(java.lang.String)
	 */
	public void setTrayIconToolTip(String toolTip) {
		trayIcon.setToolTip(toolTip);

	}

	/**
	 * @see com.jniwrapper.win32.shell.TrayIconListener#trayActionPerformed(long,
	 *      int, int)
	 */
	public void trayActionPerformed(long message, int x, int y) {

		if (message == Msg.WM_RBUTTONUP) {
			showPopup(x, y);
		}

		menu = handler.getPopupMenu();
		getInvokerWindow(); // force to create frame

	}

	private void showPopup(final int x, final int y) {
		Component invoker = getInvokerWindow();
		menu.show(invoker, x, y);
	}

	private Component getInvokerWindow() {
		if (invoker == null) {
			invoker = new JWindow();
			invoker.setLocation(0, 0);
			invoker.setVisible(true);
			invoker.add(menu);
			AWTWindowDecorator windowDecorator = new AWTWindowDecorator(invoker);
			long windowStyle = windowDecorator.getWindowStyle();
			windowStyle &= ~Wnd.WS_VISIBLE;
			windowDecorator.setWindowStyle(windowStyle);
		}
		return invoker;
	}

	/**
	 * @see org.columba.core.nativ.NativeWrapper#setTrayIconVisible(boolean)
	 */
	public void setTrayIconVisible(boolean visible) {
		trayIcon.setVisible(visible);

	}

	/**
	 * @see org.columba.core.nativ.NativeWrapper#displayBallonTip(java.lang.String)
	 */
	public void displayBallonTip(String text) {
		trayIcon.showMessage(new TrayMessage(text));

	}
}