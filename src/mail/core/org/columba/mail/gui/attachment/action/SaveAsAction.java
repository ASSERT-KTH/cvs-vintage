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
package org.columba.mail.gui.attachment.action;

import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.main.MainInterface;

import org.columba.mail.gui.attachment.AttachmentSelectionChangedEvent;
import org.columba.mail.gui.attachment.command.SaveAttachmentAsCommand;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.util.MailResourceLoader;

import java.awt.event.ActionEvent;


/**
 * @author frdietz
 */
public class SaveAsAction extends AbstractColumbaAction implements SelectionListener {

    /**
     * Creates a SaveAs Action.
     * @param frameMediator frame mediator.
     */
    public SaveAsAction(FrameMediator frameMediator) {
        super(frameMediator,
            MailResourceLoader.getString("menu", "mainframe", "attachmentsaveas"));

        // tooltip text
        putValue(SHORT_DESCRIPTION,
            MailResourceLoader.getString("menu", "mainframe",
                "attachmentsaveas_tooltip").replaceAll("&", ""));

        // icons
        putValue(SMALL_ICON,
            ImageLoader.getSmallImageIcon("stock_save_as-16.png"));
        putValue(LARGE_ICON, ImageLoader.getImageIcon("stock_save_as.png"));

        if (frameMediator.getSelectionManager() != null) {
            ((MailFrameMediator) frameMediator).registerAttachmentSelectionListener(this);
        }
    }

    /** {@inheritDoc} */
    public void actionPerformed(ActionEvent evt) {
        MainInterface.processor.addOp(new SaveAttachmentAsCommand(
                getFrameMediator().getSelectionManager()
                    .getHandler("mail.attachment").getSelection()));
    }

    /** {@inheritDoc} */
    public void selectionChanged(SelectionChangedEvent e) {
        if (((AttachmentSelectionChangedEvent) e).getAddress() != null) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }
    }
}
