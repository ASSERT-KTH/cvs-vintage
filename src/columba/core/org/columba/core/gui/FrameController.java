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

import java.awt.event.MouseAdapter;

import org.columba.core.config.ViewItem;
import org.columba.core.gui.menu.Menu;
import org.columba.core.gui.statusbar.StatusBar;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.mail.gui.frame.TooltipMouseHandler;

/**
 * @author Timo Stich (tstich@users.sourceforge.net)
 * 
 */
public abstract class FrameController {

	protected StatusBar statusBar;
	protected MouseAdapter mouseTooltipHandler;
	protected String id;
	protected ViewItem item;
	protected DefaultFrameModel model;
	protected FrameView view;
	protected SelectionManager selectionManager;
	
	
	/**
	 * Constructor for FrameController.
	 * 
	 * Warning: Never do any inits in the constructor -> use init() instead!
	 */
	public FrameController( String id, DefaultFrameModel model ) {
		this.id = id;
		this.model =model;	
		statusBar = new StatusBar( MainInterface.processor.getTaskManager() );
		
		mouseTooltipHandler = new TooltipMouseHandler( statusBar );
		
		model.register(id, this);

		selectionManager = new SelectionManager();
		init();
				
		view = createView();
		
		initInternActions();
	}
	
	/**
	 * - create all additional controllers
	 * - register SelectionHandlers
	 */
	protected abstract void init();

	protected abstract void initInternActions();

	public StatusBar getStatusBar() {
		return statusBar;
	}

	/**
	 * Returns the mouseTooltipHandler.
	 * @return MouseAdapter
	 */
	public MouseAdapter getMouseTooltipHandler() {
		return mouseTooltipHandler;
	}
	
	public void close()
	{
		ColumbaLogger.log.info("closing FrameController");
		
		view.saveWindowPosition();
		model.unregister(id);

		//getView().setVisible(false);	
	}

	abstract protected FrameView createView();
	
	/**
	 * @return ViewItem
	 */
	public ViewItem getItem() {
		return item;
	}

	/**
	 * Sets the item.
	 * @param item The item to set
	 */
	public void setItem(ViewItem item) {
		this.item = item;
	}
	
	
	/**
	 * @return FrameView
	 */
	public FrameView getView() {
		return view;
	}

	public Menu getMenu() {
		return view.getMenu();
	}

	/**
	 * @return SelectionManager
	 */
	public SelectionManager getSelectionManager() {
		return selectionManager;
	}

	/**
	 * Sets the selectionManager.
	 * @param selectionManager The selectionManager to set
	 */
	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	/**
	 * @return DefaultFrameModel
	 */
	public DefaultFrameModel getModel() {
		return model;
	}

}
