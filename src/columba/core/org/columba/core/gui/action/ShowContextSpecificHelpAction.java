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
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.help.HelpManager;
import org.columba.core.util.GlobalResourceLoader;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.help.CSH;

/**
 * @author frd
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ShowContextSpecificHelpAction extends AbstractColumbaAction {
    protected ActionListener target;

    /**
     * @param frameMediator
     * @param name
     */
    public ShowContextSpecificHelpAction(FrameMediator frameMediator) {
        super(frameMediator,
            GlobalResourceLoader.getString(null, null, "menu_help_context"));

        putValue(SMALL_ICON,
            ImageLoader.getSmallImageIcon("stock_extended-help-16.png"));
        putValue(LARGE_ICON, ImageLoader.getImageIcon("stock_extended-help.png"));

        //TODO: i18n
        putValue(SHORT_DESCRIPTION, "Context-specific help".replaceAll("&", ""));

        setShowToolBarText(false);

        target = new CSH.DisplayHelpAfterTracking(
                HelpManager.getHelpManager().getHelpBroker());
    }

    public void actionPerformed(ActionEvent e) {
        target.actionPerformed(e);
    }
}
