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
package org.columba.mail.folder.command;

import org.columba.core.command.Command;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.WorkerStatusController;

import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.folder.AbstractFolder;
import org.columba.mail.main.MailInterface;

import javax.swing.tree.TreeNode;


/**
 * A Command for moving a folder to another folder.
 * <p>
 * The command reference should be inserted as these:
 * <ol>
 * <li> A <code>Folder</code> that is going to be moved.
 * <li> A <code>FolderTreeNode</code> that the above folder is moved to.
 * </ol>
 * @author redsolo
 */
public class MoveFolderCommand extends Command {
    private TreeNode parentDestFolder;
    private TreeNode parentSourceFolder;

    /**
 * @param references the folder references.
 */
    public MoveFolderCommand(DefaultCommandReference[] references) {
        super(references);
    }

    /** {@inheritDoc} */
    public void updateGUI() throws Exception {
        // update treemodel
        if (parentDestFolder != null) {
            MailInterface.treeModel.nodeStructureChanged(parentDestFolder);
        }

        if (parentSourceFolder != null) {
            MailInterface.treeModel.nodeStructureChanged(parentSourceFolder);
        }
    }

    /** {@inheritDoc} */
    public void execute(WorkerStatusController worker)
        throws Exception {
        // get folder that is going to be moved
        MessageFolder movedFolder = (MessageFolder) ((FolderCommandReference) getReferences()[0]).getFolder();
        parentSourceFolder = movedFolder.getParent();

        // get destination folder
        AbstractFolder destFolder = ((FolderCommandReference) getReferences()[1]).getFolder();
        parentDestFolder = destFolder.getParent();

        //System.out.println("Removing leaf from parent. Leaf=" + movedFolder.getName() +", parent=" + parentSourceFolder);
        movedFolder.removeFromParent();

        //System.out.println("Appending leaf to parent. Leaf=" + movedFolder.getName() +", parent=" + destFolder);
        destFolder.append(movedFolder);
    }
}
