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

package org.columba.mail.gui.view;

import org.columba.core.gui.statusbar.StatusBar;
import org.columba.core.gui.view.AbstractView;
import org.columba.mail.gui.infopanel.FolderInfoPanel;
import org.columba.mail.gui.message.MessageView;
import org.columba.mail.gui.table.FilterToolbar;
import org.columba.mail.gui.table.TableView;
import org.columba.mail.gui.tree.TreeView;

/**
 *
 * This interface provides methods and static data
 * that are common to all mail views.
 *
 */
public interface AbstractMailView extends AbstractView {
    public static final String FOLDERINFOPANEL = "folderinfopanel";
    
    public void init(TreeView tree, TableView table,
        FilterToolbar filterToolbar, MessageView message, StatusBar statusBar);

    public void showToolbar();
    
    /**
     * Gets the FilterToolbar of this view
     *
     * @return  the FilterToolbar or null if none
     */
    public FilterToolbar getFilterToolbar();

    /**
     * Sets the FolderInfoPanel of this view
     *
     * @param  the new FolderInfoPanel
     */
    public void setFolderInfoPanel(FolderInfoPanel f);
    
    /**
     * Gets the FolderInfoPanel of this view
     *
     * @return  the FolderInfoPanel or null if none
     */
    public FolderInfoPanel getFolderInfoPanel();
    
    /**
     * Showss the FolderInfoPanel of this view
     */
    public void showFolderInfoPanel();
    
    /* Methods that might be needed later...
    public void setToolBar(ToolBar toolBar);
    public boolean isFolderInfoPanelVisible();
    public void showFilterToolbar();
    public void hideFilterToolbar();
    */
}
