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
import javax.swing.JMenuBar;

import org.columba.core.config.ViewItem;
import org.columba.core.gui.menu.ColumbaMenu;
import org.columba.core.gui.statusbar.StatusBar;
import org.columba.core.gui.toolbar.ColumbaToolBar;
import org.columba.core.xml.XmlElement;

/**
 * A container is actually a JFrame, which encapsulates a component called
 * {@link FrameMediator}in our case.
 * <p>
 * The basic default container is a JFrame, with a core menu, toolbar and
 * statusbar. Additionally, it has a content pane in the center.
 * <p>
 * FrameMeditator extends the menu and toolbar. It also places its main ui
 * components in the content pane.
 * 
 * @author fdietz
 */
public interface Container {

	/**
	 * internally used toolbar ID
	 */
	public static final String MAIN_TOOLBAR = "main";

	/**
	 * Set new framemediator this container should encapsulate.
	 * 
	 * @param m
	 *            new framemediator
	 */
	void setFrameMediator(FrameMediator m);

	/**
	 * Get current framemediator this container encapsulates.
	 * 
	 * @return current container
	 */
	FrameMediator getFrameMediator();

	/**
	 * Get view item containing the configuration of this container.
	 * 
	 * @return container configuration
	 */
	ViewItem getViewItem();

	/**
	 * Get mouse tooltip handler. This is a MouseAdapter which is used by the
	 * menu to display menuitem tooltips on the statusbar when moving the mouse
	 * of an menuitem.
	 * 
	 * @return tooltip handler
	 */
	public MouseAdapter getMouseTooltipHandler();

	/**
	 * Get statusbar.
	 * 
	 * @return current statusbar
	 */
	public StatusBar getStatusBar();

	/**
	 * Show/Hide toolbar.
	 * 
	 * @param id
	 *            id of toolbar
	 * @param enable
	 *            if true, show toolbar. Otherwise, hide toolbar.
	 */
	public void enableToolBar(String id, boolean enable);

	/**
	 * Check if toolbar is visible.
	 * 
	 * @param id
	 *            id of toolbar
	 * @return true, if visible. False, otherwise.
	 */
	public boolean isToolBarEnabled(String id);

	/**
	 * Add another toolbar to this container. These are simply JComponent
	 * objects which are appended vertically currently.
	 * 
	 * @param c
	 *            new toolbar-like component
	 */
	public void addToolBar(JComponent c);

	/**
	 * Set toolbar of this container.
	 * 
	 * @param toolbar
	 *            new toolbar
	 */
	void setToolBar(ColumbaToolBar toolbar);

	/**
	 * @return
	 */
	ColumbaToolBar getToolBar();

	/**
	 * Check if infopanel is visible.
	 * 
	 * @return true, if visible. Otherwise, false.
	 */
	public boolean isInfoPanelEnabled();

	/**
	 * Hide/Show current infopanel.
	 * 
	 * @param enable
	 *            If true, show infopanel. Otherwise, hide infopanel.
	 */
	public void enableInfoPanel(boolean enable);

	/**
	 * Get the current infopanel. This is the darkgray panel right below the
	 * toolbar.
	 * 
	 * @return current infopanel
	 */
	ContainerInfoPanel getInfoPanel();

	/**
	 * Set infopanel of this container. This is the darkgray panel right below
	 * the toolbar.
	 * 
	 * @param panel
	 *            new infopanel
	 */
	void setInfoPanel(ContainerInfoPanel panel);

	/**
	 * Loads stored information about the previous size and location of the
	 * view.
	 * 
	 * @param viewItem
	 *            configuration of the container
	 */
	void loadPositions(ViewItem viewItem);

	/**
	 * Saves information about the current size and location of the view
	 * <p>
	 * 
	 * @param viewItem
	 *            configuration of the container
	 */
	void savePositions(ViewItem viewItem);

	/**
	 * Save window properties and close the window. This includes telling the
	 * frame model that this window/frame is closing, so it can be
	 * "unregistered" correctly
	 */
	public void close();

	/**
	 * Set the content pane of this component. This is the center of the JFrame,
	 * right between the menu/toolbar and statusbar.
	 * 
	 * @param view
	 *            new content pane
	 */
	void setContentPane(ContentPane view);

	/**
	 * Get current swing JFrame. This could become handy when directly accessing
	 * JFrame functionality. For example, you don't want to use Columba's menu
	 * or toolbar framework.
	 * 
	 * @return swing JFrame
	 */
	JFrame getFrame();

	/**
	 * Get the current menu. Note, that this is Columba's xml-based JMenuBar
	 * extension.
	 * <p>
	 * This method is only added for convinience. If we would use getJMenuBar()
	 * instead, we would always have to check if its really an instance of
	 * ColumbaMenu.
	 * 
	 * @return current menu
	 */
	ColumbaMenu getMenu();

	/**
	 * Get the menubar of this container.
	 * 
	 * @return current menubar
	 */
	JMenuBar getJMenuBar();

	/**
	 * Set the menubar of this container. The {@link DefaultContainer}is based
	 * on JFrame, so we get this method for free.
	 * 
	 * @see DefaultContainer
	 * @param menuBar
	 *            new menubar
	 */
	void setJMenuBar(JMenuBar menuBar);

	/**
	 * Extend current Columba menu from xml file.
	 * 
	 * @param mediator
	 *            current framemediator
	 * @param file
	 *            path to xml file
	 */
	void extendMenuFromFile(FrameMediator mediator, String file);

	/**
	 * Extend current toolbar from xml element.
	 * 
	 * @param mediator
	 *            current framemediator
	 * @param element
	 *            xml element
	 */
	void extendToolbar(FrameMediator mediator, XmlElement element);

}