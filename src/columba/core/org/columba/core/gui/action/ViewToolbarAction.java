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

import org.columba.core.action.CheckBoxAction;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.util.GlobalResourceLoader;

import java.awt.event.ActionEvent;

import java.util.Observable;
import java.util.Observer;


public class ViewToolbarAction extends CheckBoxAction implements Observer {
    public ViewToolbarAction(FrameMediator controller) {
        super(controller,
            GlobalResourceLoader.getString(null, null, "menu_view_showtoolbar"));

        // tooltip text
        putValue(SHORT_DESCRIPTION,
            GlobalResourceLoader.getString(null, null, "menu_view_showtoolbar")
                                .replaceAll("&", ""));
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
        frameMediator.getView().showToolbar();
    }

    /**
     * Update checked state of menu item if change occured
     *
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable o, Object arg) {
        // TODO: implement ViewToolbar->update()
    }
}
