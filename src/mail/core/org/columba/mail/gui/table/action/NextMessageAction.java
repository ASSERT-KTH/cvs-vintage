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
package org.columba.mail.gui.table.action;

import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.gui.util.ImageLoader;

import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.frame.TableViewOwner;
import org.columba.mail.gui.table.TableController;
import org.columba.mail.gui.table.model.MessageNode;
import org.columba.mail.gui.table.selection.TableSelectionChangedEvent;
import org.columba.mail.util.MailResourceLoader;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;


/**
 * @author frd
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class NextMessageAction extends AbstractColumbaAction
    implements SelectionListener {
    public NextMessageAction(FrameMediator frameMediator) {
        super(frameMediator,
            MailResourceLoader.getString("menu", "mainframe",
                "menu_view_nextmessage"));

        // tooltip text
        putValue(SHORT_DESCRIPTION,
            MailResourceLoader.getString("menu", "mainframe",
                "menu_view_nextmessage_tooltip").replaceAll("&", ""));

        // icons
        putValue(LARGE_ICON, ImageLoader.getSmallImageIcon("next-message.png"));

        // shortcut key
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, 0));

        // disable toolbar text
        setShowToolBarText(false);

        setEnabled(false);

        // uncomment to enable action

        /*
        (
                (
                        AbstractMailFrameController) frameMediator)
                                .registerTableSelectionListener(
                this);
        */
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
        FolderCommandReference[] r = ((AbstractMailFrameController) getFrameMediator()).getTableSelection();

        // TODO: fix next-message action
        if (r.length > 0) {
            FolderCommandReference ref = r[0];
            TableController table = ((TableViewOwner) getFrameMediator()).getTableController();
            MessageNode node = table.getView().getSelectedNodes()[0];

            if (node == null) {
                return;
            }

            /*
            MessageNode nextNode = (MessageNode) node.getNextNode();
            Object nextUid = nextNode.getUid();

            Object[] uids = new Object[1];
            uids[0] = nextUid;
            ref.setUids(uids);

            (
                    (AbstractMailFrameController) getFrameController())
                            .setTableSelection(
                    r);
            table.setSelected(uids);

            MainInterface.processor.addOp(
                    new ViewMessageCommand(getFrameController(), r));
            */
        }
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.util.SelectionListener#selectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent e) {
        setEnabled(((TableSelectionChangedEvent) e).getUids().length > 0);
    }
}
