// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.tree.command;

import java.util.Hashtable;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.WorkerStatusController;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.AbstractFolder;
import org.columba.mail.folder.FolderFactory;
import org.columba.mail.main.MailInterface;

/**
 * @author fdietz
 *  
 */
public class CreateAndSelectSubFolderCommand extends FolderCommand {

    private AbstractFolder parentFolder;

    private boolean success;

    private Hashtable attributes;

    private JTree tree;
    
    private AbstractFolder childFolder;

    public CreateAndSelectSubFolderCommand(JTree tree,
            DefaultCommandReference[] references) {
        super(references);

        success = true;
        this.tree = tree;
    }

    /**
     * @see org.columba.core.command.Command#updateGUI()
     */
    public void updateGUI() throws Exception {
        if (success) {
            MailInterface.treeModel.nodeStructureChanged(parentFolder);
            
            // select node in JTree
            TreeNode[] nodes = childFolder.getPath();
            tree.setSelectionPath(new TreePath(nodes));
        }
    }

    /**
     * @see org.columba.core.command.Command#execute(Worker)
     */
    public void execute(WorkerStatusController worker) throws Exception {
        parentFolder = ((FolderCommandReference) getReferences()[0])
                .getFolder();

        String name = ((FolderCommandReference) getReferences()[0])
                .getFolderName();

        try {
            childFolder = FolderFactory.getInstance()
                    .createDefaultChild(parentFolder, name);

            // if folder creation failed
            //  -> don't update tree ui
            if (childFolder == null) {
                success = false;
            }
        } catch (Exception ex) {
            success = false;
            throw ex;
        }
    }
}