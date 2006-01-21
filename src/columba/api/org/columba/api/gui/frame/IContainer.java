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
package org.columba.api.gui.frame;

import java.io.InputStream;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.columba.api.statusbar.IStatusBar;

/**
 * A container is actually a JFrame, which encapsulates a component called
 * {@link IFrameMediator}in our case.
 * <p>
 * The basic default container is a JFrame, with a core menu, toolbar and
 * statusbar. Additionally, it has a content pane in the center.
 * <p>
 * FrameMeditator extends the menu and toolbar. It also places its main ui
 * components in the content pane.
 * 
 * @author fdietz
 */
public interface IContainer {

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
	void setFrameMediator(IFrameMediator m);

	/**
	 * Switch to new framemediator. This also ensures that the menu, toolbar,
	 * infobar, etc. get also replaced correctly.
	 * 
	 * 
	 * @param m
	 *            new framemediator
	 */
	void switchFrameMediator(IFrameMediator m);

	/**
	 * Get current framemediator this container encapsulates.
	 * 
	 * @return current container
	 */
	IFrameMediator getFrameMediator();

	/**
	 * Get statusbar.
	 * 
	 * @return current statusbar
	 */
	public IStatusBar getStatusBar();

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
	void setToolBar(JToolBar toolbar);

	/**
	 * @return
	 */
	JToolBar getToolBar();

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
	void setContentPane(JPanel view);

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
	//
	/**
	 * Get the menubar of this container.
	 * 
	 * @return current menubar
	 */
	JMenuBar getJMenuBar();

	/**
	 * Set the menubar of this container.
	 * 
	 * @param menuBar
	 *            new menubar
	 */
	void setJMenuBar(JMenuBar menuBar);

	/**
	 * Extend current Columba menu from xml file.
	 * 
	 * @param mediator
	 *            current framemediator
	 * @param fileUrl
	 *            path to xml file
	 */
	void extendMenu(IFrameMediator mediator, InputStream is);

	/**
	 * Extend current toolbar from xml element.
	 * 
	 * @param mediator
	 *            current framemediator
	 * @param is
	 *            xml element
	 */
	void extendToolbar(IFrameMediator mediator, InputStream is);

	/**
	 * Sets the window name which is displayed in the title.
	 * 
	 * @param name
	 */
	void setWindowName(String name);

	/**
	 * Window closing action
	 * 
	 * @param close
	 *            if true, close window. Otherwise, don't close window
	 *            automatically.
	 */
	void setCloseOperation(boolean close);

}