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

package org.columba.core.gui.action;

import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.command.*;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.main.MainInterface;
import org.columba.core.util.GlobalResourceLoader;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

public class RedoAction extends AbstractColumbaAction implements TaskManagerListener {
    protected TaskManager taskManager;
    
    public RedoAction(FrameMediator controller) {
        super(controller,
            GlobalResourceLoader.getString(null, null, "menu_edit_redo"));

        // tooltip text
        putValue(SHORT_DESCRIPTION,
            GlobalResourceLoader.getString(null, null, "menu_edit_redo_tooltip")
                                .replaceAll("&", ""));

        // small icon for menu
        putValue(SMALL_ICON, ImageLoader.getSmallImageIcon("stock_redo-16.png"));

        // large icon for toolbar
        putValue(LARGE_ICON, ImageLoader.getImageIcon("stock_redo.png"));

        // disable toolbar text
        setShowToolBarText(false);

        // shortcut key
        putValue(ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));

        setEnabled(false);
        
        taskManager = MainInterface.processor.getTaskManager();
        taskManager.addTaskManagerListener(this);
        MainInterface.focusManager.setRedoAction(this);
    }

    public void actionPerformed(ActionEvent evt) {
        MainInterface.focusManager.redo();
    }

    public void workerAdded(TaskManagerEvent e) {
        setEnabled(taskManager.count() > 0);
    }
    
    public void workerRemoved(TaskManagerEvent e) {
        setEnabled(taskManager.count() > 0);
    }
}
