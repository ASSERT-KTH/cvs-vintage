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
package org.columba.mail.gui.tree.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.columba.core.gui.frame.FrameMediator;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.AbstractFolder;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.tree.TreeModel;
import org.columba.mail.main.MailInterface;
import org.columba.mail.util.MailResourceLoader;

/**
 * Move selected folder down for one row.
 * <p>
 * TODO (@author redsolo):fix re-selection of moved folder
 *
 * @author fdietz
 * @author redsolo
 */
public class MoveDownAction extends AbstractMoveFolderAction {
    /**
     * @param frameMediator the frame controller.
     */
    public MoveDownAction(FrameMediator frameMediator) {
        super(frameMediator,
            MailResourceLoader.getString("menu", "mainframe",
                "menu_folder_movedown"));
        setEnabled(false);

        // shortcut key
        putValue(ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, ActionEvent.ALT_MASK));
    }

        /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0) {
        FolderCommandReference r = (FolderCommandReference) ((MailFrameMediator) frameMediator).getTreeSelection();

        AbstractFolder folder = r.getFolder();

        int newIndex = folder.getParent().getIndex(folder);
        newIndex = newIndex + 1;
        ((AbstractFolder) folder.getParent()).insert(folder, newIndex);

        TreeModel.getInstance().nodeStructureChanged(folder.getParent());
    }

    /** {@inheritDoc} */
    protected boolean isActionEnabledByIndex(int folderIndex) {
        return (folderIndex < (getLastSelectedFolder().getParent().getChildCount() - 1));
    }
}
