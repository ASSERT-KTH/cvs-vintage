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
package org.columba.core.gui.frame;

import java.awt.event.MouseAdapter;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.columba.core.config.ViewItem;
import org.columba.core.gui.menu.Menu;
import org.columba.core.gui.statusbar.StatusBar;
import org.columba.core.gui.toolbar.ToolBar;
import org.columba.core.xml.XmlElement;

/**
 * @author fdietz
 *  
 */
public interface Container {
	
	/**
	 * internally used toolbar ID
	 */
	public static final String MAIN_TOOLBAR = "main";

	void setFrameMediator(FrameMediator m);

	FrameMediator getFrameMediator();

	ViewItem getViewItem();

	public StatusBar getStatusBar();

	public MouseAdapter getMouseTooltipHandler();

	public void enableToolbar(String id, boolean enable);

	public boolean isToolbarEnabled(String id);

	public boolean isInfoPanelEnabled();
	
	public void enableInfoPanel(boolean enable);
	
	/**
	 * Loads stored information about the previous size and location of the
	 * view.
	 */
	void loadPositions(ViewItem viewItem);

	/**
	 * Saves information about the current size and location of the view
	 */
	void savePositions(ViewItem viewItem);

	/**
	 * Save window properties and close the window. This includes telling the
	 * frame model that this window/frame is closing, so it can be
	 * "unregistered" correctly
	 */
	public void close();
	
	public void addToolBar(JComponent c);
	
	void setContentPane(ContentPane view);

	// adapter for the old frame handling stuff
	JFrame getFrame();
	
	Menu getMenu();
	
	void setToolBar(ToolBar toolbar);
	
	ToolBar getToolBar();
	
	ContainerInfoPanel getInfoPanel();
	
	void setInfoPanel(ContainerInfoPanel panel);
	
	void extendMenuFromFile(FrameMediator mediator, String file);

	void extendToolbar(FrameMediator mediator, XmlElement element);
	
}