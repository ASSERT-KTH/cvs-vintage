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
package org.columba.core.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.columba.core.config.ViewItem;
import org.columba.core.config.WindowItem;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.util.WindowMaximizer;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class FrameView extends JFrame implements WindowListener {
	protected FrameController frameController;
	protected Menu menu;

	protected ToolBar toolbar;

	protected JPanel ToolbarPane;


	public FrameView(FrameController frameController) {
		this.frameController = frameController;

		this.setIconImage(
			ImageLoader.getImageIcon("ColumbaIcon.png").getImage());

		setTitle(
			"Columba - version: "
				+ org.columba.core.main.MainInterface.version);

		JPanel panel = (JPanel) this.getContentPane();
		panel.setLayout(new BorderLayout());
		panel.add(frameController.getStatusBar(), BorderLayout.SOUTH);

		addWindowListener(this);
		
		ToolbarPane = new JPanel();
		ToolbarPane.setLayout(new BoxLayout(ToolbarPane, BoxLayout.Y_AXIS));		
	}

	public void init() {
		menu = new Menu(frameController);
		setJMenuBar(menu);
		
		toolbar = new ToolBar(frameController);

		if (isToolbarVisible() ) {
			ToolbarPane.add(toolbar);
		}		
	}
	
	public boolean isToolbarVisible() {
		return frameController.getItem().getBoolean("toolbar", "visible");
	}

	public void loadWindowPosition() {
		ViewItem viewItem = frameController.getItem();
		int x = viewItem.getInteger("window", "width");
		int y = viewItem.getInteger("window", "height");
		boolean maximized = viewItem.getBoolean("window", "maximized", true);

		if (maximized)
			maximize();
		else {

			Dimension dim = new Dimension(x, y);
			setSize(dim);

			validate();
		}
	}

	public void maximize() {
		WindowMaximizer.maximize(this);

		/*
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(screenSize);
		*/

		// FIXME: this works only with JDK1.4
		// has to be added with org.columba.core.util.Compatibility-class
		//setExtendedState(MAXIMIZED_BOTH);
	}

	public void saveWindowPosition() {

		java.awt.Dimension d = getSize();

		WindowItem item = frameController.getItem().getWindowItem();

		item.set("x", 0);
		item.set("y", 0);
		item.set("width", d.width);
		item.set("height", d.height);

		boolean isMaximized = WindowMaximizer.isWindowMaximized(this);

		item.set("maximized", isMaximized);
	}

	public void showToolbar(boolean b) {
		if (b) {
			ToolbarPane.add(toolbar);
			frameController.getItem().set("toolbar", "visible", "true");
		} else {
			ToolbarPane.removeAll();
			frameController.getItem().set("toolbar", "visible", "false");
		}
		
		validate();
		repaint();		
	}
	/**
	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	public void windowActivated(WindowEvent arg0) {
	}

	/**
	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	public void windowClosed(WindowEvent arg0) {

	}

	/**
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	public void windowClosing(WindowEvent arg0) {
		frameController.close();
	}

	/**
	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
	 */
	public void windowDeactivated(WindowEvent arg0) {
	}

	/**
	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
	 */
	public void windowDeiconified(WindowEvent arg0) {
	}

	/**
	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	public void windowIconified(WindowEvent arg0) {
	}

	/**
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	public void windowOpened(WindowEvent arg0) {
	}
	/**
	 * @return Menu
	 */
	public Menu getMenu() {
		return menu;
	}

}
