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
package org.columba.mail.gui.tree.comparator;

import org.columba.mail.folder.AbstractFolder;
import org.columba.mail.folder.MessageFolder;
import org.columba.ristretto.message.MessageFolderInfo;


/**
 * Folder comparator that sorts the folders based on the number of unread messages.
 * @author redsolo
 */
public class UnreadFolderComparator extends FolderComparator {

    /**
     * @param ascending if the sorting is ascending or not.
     */
    public UnreadFolderComparator(boolean ascending) {
        super(ascending);
    }

    /** {@inheritDoc} */
    protected int compareFolders(AbstractFolder folder1, AbstractFolder folder2) {
        int compValue;

        if ((folder1 instanceof MessageFolder) && (folder1 instanceof MessageFolder)) {

            MessageFolderInfo info1 = ((MessageFolder) folder1).getMessageFolderInfo();
            MessageFolderInfo info2 = ((MessageFolder) folder2).getMessageFolderInfo();

            if (info1.getUnseen() != info2.getUnseen()) {
                compValue = info2.getUnseen() - info1.getUnseen();
            } else {
                compValue = super.compareFolders(folder1, folder2);
            }
        } else {
            compValue = super.compareFolders(folder1, folder2);
        }

        return compValue;
    }
}
