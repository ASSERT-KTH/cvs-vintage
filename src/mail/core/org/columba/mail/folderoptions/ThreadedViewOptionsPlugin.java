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
package org.columba.mail.folderoptions;

import org.columba.core.config.DefaultItem;
import org.columba.core.config.IDefaultItem;
import org.columba.core.xml.XmlElement;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.frame.TableViewOwner;
import org.columba.mail.gui.table.TableController;


/**
 * Handles enabled/disabled state of threaded-view.
 * 
 * @author fdietz
 */
public class ThreadedViewOptionsPlugin extends AbstractFolderOptionsPlugin {
    /**
 * Constructor
 * 
 * @param mediator      mail framemediator
 */
    public ThreadedViewOptionsPlugin(MailFrameMediator mediator) {
        super("threadedview", "ThreadedViewOptions", mediator);
    }

    /**
 * @see org.columba.mail.folderoptions.AbstractFolderOptionsPlugin#saveOptionsToXml(IMailbox)
 */
    public void saveOptionsToXml(IMailbox folder) {
        XmlElement parent = getConfigNode(folder);
        IDefaultItem item = new DefaultItem(parent);

        TableController tableController = ((TableController)((TableViewOwner) getMediator()).getTableController());

        item.setBoolean("enabled",
            tableController.getTableModelThreadedView().isEnabled());
    }

    /**
 * @see org.columba.mail.folderoptions.AbstractFolderOptionsPlugin#loadOptionsFromXml(IMailbox)
 */
    public void loadOptionsFromXml(IMailbox folder) {
        XmlElement parent = getConfigNode(folder);
        IDefaultItem item = new DefaultItem(parent);

        boolean enableThreadedView = item.getBooleanWithDefault("enabled", false);

        TableController tableController = ((TableController)((TableViewOwner) getMediator()).getTableController());

        tableController.enableThreadedView(enableThreadedView, false);
    }

    /**
   * @see org.columba.mail.folderoptions.AbstractFolderOptionsPlugin#createDefaultElement()
   */
    public XmlElement createDefaultElement(boolean global) {
        XmlElement parent = super.createDefaultElement(global);
        parent.addAttribute("enabled", "false");

        return parent;
    }
}
