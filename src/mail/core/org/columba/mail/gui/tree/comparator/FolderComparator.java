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

import java.util.Comparator;

import org.columba.mail.folder.AbstractFolder;
import org.columba.mail.folder.LocalFolder;
import org.columba.mail.folder.imap.IMAPFolder;


/**
 * A comparator that can be used to sort Folders.
 * Other folder comparators should extend this class and only implement the
 * sorting in the <code>compareFolders()</code> method. This comparator will always
 * put the Inbox folders at the top of the tree.
 * <p>
 * The folders are by default sorted by their name. Note that the Inbox folder
 * will always be put at the top.
 * @author redsolo
 */
public class FolderComparator implements Comparator {

    private boolean isAscending = true;

    /**
     * @param ascending if the sorting is ascending or not.
     */
    public FolderComparator(boolean ascending) {
        isAscending = ascending;
    }
    /**
     * Returns true if the node is an Inbox folder.
     * @param folder the folder to check.
     * @return true if the node is an Inbox folder; false otherwise.
     */
    private boolean isInboxFolder(AbstractFolder folder) {
        boolean isInbox = false;
        if (folder instanceof LocalFolder) {
            isInbox = ((LocalFolder) folder).isInboxFolder();
        } else if (folder instanceof IMAPFolder) {
            isInbox = ((IMAPFolder) folder).isInboxFolder();
        }
        return isInbox;
    }

    /** {@inheritDoc} */
    public int compare(Object o1, Object o2) {
        int compValue = 0;

        if ((o1 instanceof AbstractFolder) && (o2 instanceof AbstractFolder)) {
            AbstractFolder folder1 = (AbstractFolder) o1;
            AbstractFolder folder2 = (AbstractFolder) o2;
            if (isInboxFolder(folder1)) {
                compValue = -1;
            } else if (isInboxFolder(folder2)) {
                compValue = 1;
            } else if (isInboxFolder(folder2) && isInboxFolder(folder1)) {
                compValue = 0;
            } else {
                compValue = compareFolders(folder1, folder2);
                if (!isAscending) {
                    compValue *= -1;
                }
            }
        } else {
            compValue = o1.toString().toLowerCase().compareTo(o2.toString().toLowerCase());
            if (!isAscending) {
                compValue *= -1;
            }
        }
        return compValue;
    }

    /**
     * Compares the folders.
     * Returns a negative integer, zero, or a positive integer as the first argument is
     * less than, equal to, or greater than the second.
     * @param folder1 the first folder to be compared.
     * @param folder2 the second folder to be compared.
     * @return a negative integer, zero, or a positive integer as the first argument is
     *      less than, equal to, or greater than the second.
     */
    protected int compareFolders(AbstractFolder folder1, AbstractFolder folder2) {
        return folder1.getName().toLowerCase().compareTo(folder2.getName().toLowerCase());
    }

    /**
     * @return Returns if the comparator should sort ascending or not.
     */
    public boolean isAscending() {
        return isAscending;
    }

    /**
     * @param ascending if the comparator should sorted ascending or not.
     */
    public void setAscending(boolean ascending) {
        isAscending = ascending;
    }
}
