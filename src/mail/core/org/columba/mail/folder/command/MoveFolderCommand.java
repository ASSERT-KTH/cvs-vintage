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
import org.columba.mail.folder.AbstractFolder;
import org.columba.mail.folder.MessageFolder;


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

    private AbstractFolder destParentFolder;
    private int[] destChildIndicies;

    private AbstractFolder srcParentFolder;
    private int[] srcChildIndicies;
    private Object[] srcChildObjects;

    /**
     * @param references the folder references.
     */
    public MoveFolderCommand(DefaultCommandReference reference) {
        super(reference);
    }

    /** {@inheritDoc} */
    /*
    public void updateGUI() throws Exception {

        // update treemodel
        if (srcParentFolder != null) {
            MailInterface.treeModel.nodesWereRemoved(srcParentFolder, srcChildIndicies, srcChildObjects);
        }

        if (destParentFolder != null) {
            MailInterface.treeModel.nodesWereInserted(destParentFolder, destChildIndicies);
        }
    }
    */

    /** {@inheritDoc} */
    public void execute(WorkerStatusController worker) throws Exception {
        // get folder that is going to be moved
        MessageFolder movedFolder = (MessageFolder) ((FolderCommandReference) getReference()).getFolder();

        // get destination folder
        destParentFolder = ((FolderCommandReference) getReference()).getFolder();

        srcParentFolder = (AbstractFolder)movedFolder.getParent();
        srcChildIndicies = new int[] {srcParentFolder.getIndex(movedFolder)};
        srcChildObjects = new Object[] {movedFolder};

        movedFolder.removeFromParent();

        destParentFolder.append(movedFolder);

        destChildIndicies = new int[] {destParentFolder.getIndex(movedFolder)};
    }
}
