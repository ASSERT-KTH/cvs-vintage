// The contents of this file are subject to the Mozilla Public License Version
//1.1
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
//Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.table.action;

import java.awt.event.ActionEvent;

import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.folder.command.ToggleMarkCommand;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.table.selection.TableSelectionChangedEvent;
import org.columba.mail.util.MailResourceLoader;

/**
 * Toggle read/unread flag of selected messages.
 * 
 * @author fdietz
 */
public class ToggleSpamFlagAction extends AbstractColumbaAction implements
        SelectionListener {

    public ToggleSpamFlagAction(FrameMediator frameMediator) {
        super(frameMediator, MailResourceLoader.getString("menu", "mainframe",
                "menu_message_togglespam"));

        // tooltip text
        putValue(SHORT_DESCRIPTION, MailResourceLoader.getString("menu",
                "mainframe", "menu_message_togglespam_tooltip").replaceAll("&",
                ""));

        // icons
        putValue(SMALL_ICON, ImageLoader.getSmallImageIcon("spam-16.png"));
        putValue(LARGE_ICON, ImageLoader.getImageIcon("spam-24.png"));

        setEnabled(false);

        ((MailFrameMediator) frameMediator)
                .registerTableSelectionListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
        FolderCommandReference[] r = ((MailFrameMediator) getFrameMediator())
                .getTableSelection();
        r[0].setMarkVariant(MarkMessageCommand.MARK_AS_SPAM);

        ToggleMarkCommand c = new ToggleMarkCommand(r);

        MainInterface.processor.addOp(c);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.columba.core.gui.util.SelectionListener#selectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent e) {
        setEnabled(((TableSelectionChangedEvent) e).getUids().length > 0);
    }
}