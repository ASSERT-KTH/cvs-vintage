/*
 * Created on 12.02.2003
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.columba.mail.command;

import org.columba.mail.folder.MessageFolder;


/**
 * Helper class which makes it easy to distinguish the source-, destination-
 * and update-reference.
 * <p>
 * Source reference is usually the source-folder and an UID array representing
 * the selected messages. There can be of course many source-folders with message.
 * UIDs. Think of Virtual Folders which don't contain messages, but references
 * to messages in other folders.
 * <p>
 * Destination reference is usually only one item, containing the destination folder.
 * <p>
 * Update reference is only needed in order to be able to update the Virtual Folder.
 *
 * @author fdietz
 */
public class FolderCommandAdapter {
    protected FolderCommandReference[] c;
    protected int length;

    public FolderCommandAdapter(FolderCommandReference[] c) {
        this.c = c;

        if (c.length < 3) {
            length = 3;
        } else {
            length = c.length;
        }
    }

    /**
 * Get array of source references.
 *
 * @return                source references
 */
    public FolderCommandReference[] getSourceFolderReferences() {
        FolderCommandReference[] result = new FolderCommandReference[length -
            2];

        System.arraycopy(c, 0, result, 0, length - 2);

        return result;
    }

    /**
 * Get destination reference
 *
 * @return                destination reference
 */
    public MessageFolder getDestinationFolder() {
        return (MessageFolder) c[length - 2].getFolder();
    }

    /**
 * Get destination reference
 *
 * @return                destination reference
 */
    public FolderCommandReference getDestinationFolderReference() {
        return c[length - 2];
    }

    /**
 * Get update reference
 *
 * @return                update reference
 */
    public FolderCommandReference getUpdateReferences() {
        // shouldn't this be <=2 ?
        // -> this fixes the VirtualFolder doesn't update bug for me
        if (c.length <= 2) {
            return null;
        }

        //if ( c.length==2 ) return null;
        return c[c.length - 1];
    }
}
