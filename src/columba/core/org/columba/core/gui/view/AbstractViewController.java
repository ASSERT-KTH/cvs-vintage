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

package org.columba.core.gui.view;

import org.columba.core.config.ViewItem;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.selection.SelectionManager;
import org.columba.core.gui.statusbar.StatusBar;
import org.columba.core.main.MainInterface;
import org.columba.core.xml.XmlElement;

import org.columba.mail.gui.frame.TooltipMouseHandler;

import java.awt.event.MouseAdapter;

/**
 * The Controller is responsible for creating a view.
 *
 * It provides a selection handler facility through a
 * {@link SelectionManager}.
 *
 * @see org.columba.core.gui.selection.SelectionManager
 *
 * @author Timo Stich (tstich@users.sourceforge.net)
 *
 */
public abstract class AbstractViewController implements FrameMediator {
    
    protected StatusBar statusBar;

    /**
     * Menuitems use this to display a string in the statusbar
     */
    protected MouseAdapter mouseTooltipHandler;

    /**
     * Saves view information
     */
    protected ViewItem viewItem;

    /**
     *
     * View this controller handles
     */
    protected AbstractView view;

    /**
     * Selection handler
     */
    protected SelectionManager selectionManager;

    /**
     * ID of controller
     */
    protected String id;

    /**
     * Constructor for ViewController.
     *
     * Warning: Never do any inits in the constructor -> use init() instead!
     *
     * The problem is that we have some circular dependencies here:
     * The view needs the action to be initializied first.
     * The actions need the controller properly initialized.
     *
     * So, take care when changing the order of initialization
     *
     */
    public AbstractViewController(String id, ViewItem viewItem) {
        this.id = id;
        this.viewItem = viewItem;

        // If no view spec. is given, use default
        if (viewItem == null) {
            this.viewItem = new ViewItem(createDefaultConfiguration(id));
        }

        // register statusbar at global taskmanager
        statusBar = new StatusBar(MainInterface.processor.getTaskManager());

        // add tooltip handler
        mouseTooltipHandler = new TooltipMouseHandler(statusBar);

        // init selection handler
        selectionManager = new SelectionManager();
    }

    /**
     *
     * @see ThreePaneMailFrameController for an example of its usage
     *
     */
    protected void initActions() {
    }

    /**
     *
     * Create default view configuration
     *
     * This is used by implementations of controllers who want
     * to store some more information, which is specific to their
     * domain.
     *
     * @see AbstractMailFrameController for implementation example
     *
     *
     * @param id        ID of controller
     * @return                xml treenode containing the new configuration
     */
    protected abstract XmlElement createDefaultConfiguration(String id);

    /**
     * - create all additional controllers
     * - register SelectionHandlers
     */
    protected abstract void init();

    /**
     *
     * @return        statusbar
     */
    public StatusBar getStatusBar() {
        return statusBar;
    }

    /**
     * Returns the mouseTooltipHandler.
     *
     * @return MouseAdapter
     */
    public MouseAdapter getMouseTooltipHandler() {
        return mouseTooltipHandler;
    }

    /**
     * Perform housekeeping necessary at closing
     */
    public abstract void close();

    /**
     * Create view
     *
     * @return        view object
     */
    protected abstract AbstractView createView();

    /**
     * Open new view.
     *
     */
    public abstract void openView();

    /**
     * @return ViewItem
     */
    public ViewItem getViewItem() {
        return viewItem;
    }

    /**
     * Sets the item.
     * @param item The item to set
     */
    public void setViewItem(ViewItem item) {
        this.viewItem = item;
    }

    /**
     * @return View (Create new view if there is none)
     */
    public AbstractView getBaseView() {
        if (view == null) {
            // initialize the view here
            init();

            // initialize all actions
            initActions();

            // create view
            view = createView();
        }
        return view;
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
}
