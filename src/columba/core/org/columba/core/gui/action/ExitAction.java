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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.main.MainInterface;
import org.columba.core.util.GlobalResourceLoader;


public class ExitAction extends AbstractColumbaAction {
    public ExitAction(FrameMediator controller) {
        super(controller,
            GlobalResourceLoader.getString(null, null, "menu_file_exit"));

        // tooltip text
        putValue(SHORT_DESCRIPTION,
            GlobalResourceLoader.getString(null, null, "menu_file_exit")
                                .replaceAll("&", ""));

        // small icon for menu
        putValue(SMALL_ICON, ImageLoader.getSmallImageIcon("stock_exit-16.png"));

        // large icon for toolbar
        putValue(LARGE_ICON, ImageLoader.getImageIcon("stock_exit.png"));

        // shortcut key
        putValue(ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
    }

    /*
     * Close all open frames, which leads to exiting Columba
     */
    public void actionPerformed(ActionEvent evt) {
    	
    	//
    	// @author: fdietz
    	// using shutdown-manager is wrong here, because this
    	// automatically also calls the FrameModel, which 
    	// also starts a second shutdown thread
    	// -> This leads into two parallel shutdown thread which
    	// -> is why sometimes config-files, etc. get messed up
    	//
        /*
         * ShutdownManager.getShutdownManager().shutdown(0);
         */
    	MainInterface.frameModel.storeViews();
    	
    }
}
