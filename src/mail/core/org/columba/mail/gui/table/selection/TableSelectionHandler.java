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
package org.columba.mail.gui.table.selection;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.gui.selection.SelectionHandler;

import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.gui.table.TableView;
import org.columba.mail.gui.table.model.MessageNode;

import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreePath;


/**
 * TableSelectionHandler adds another abstraction layer to the
 * swing JTable selection model.
 * <p>
 * It is responsible for providing a mapping between swing
 * table rows or tree nodes into message UIDs and back.
 * <p>
 * Additionally it is able to encapsulate a message object transparently
 * for every action in Columba. This means actions don't need to care
 * about if this message is actually from a folder-/table-selection, as
 * it usually is (example: user selects a message in the table and does
 * a move operation on it), or if it is just a temporary message (
 * example: pgp-decrypted message ).
 * <p>
 * For this reason it uses a temporary folder to save such a message and
 * provide actions with the correctly mapped FolderCommandReference[] object.
 *
 *
 *
 * @author fdietz
 */
public class TableSelectionHandler extends SelectionHandler
    implements ListSelectionListener {
    private final static MessageNode[] messageNodeArray = { null };
    private TableView view;
    private LinkedList messages;
    private MessageFolder folder;

    // if this is set to true, we use the local selection, instead
    // of using the table selection
    private boolean useLocalSelection;
    private FolderCommandReference[] local;

    /**
 * @param id
 */
    public TableSelectionHandler(TableView view) {
        super("mail.table");
        this.view = view;

        view.getSelectionModel().addListSelectionListener(this);

        messages = new LinkedList();

        useLocalSelection = false;
    }

    /* (non-Javadoc)
 * @see org.columba.core.gui.util.SelectionHandler#getSelection()
 */
    public DefaultCommandReference[] getSelection() {
        if (useLocalSelection == true) {
            return local;
        }

        FolderCommandReference[] references = new FolderCommandReference[1];

        references[0] = new FolderCommandReference(folder, getUidArray());

        return references;
    }

    /* (non-Javadoc)
 * @see org.columba.core.gui.util.SelectionHandler#setSelection(org.columba.core.command.DefaultCommandReference[])
 */
    public void setSelection(DefaultCommandReference[] selection) {
        FolderCommandReference ref = (FolderCommandReference) selection[0];

        folder = (MessageFolder) ref.getFolder();

        useLocalSelection = false;
    }

    /**
 * Sets the folder.
 * @param folder The folder to set
 */
    public void setFolder(MessageFolder folder) {
        this.folder = folder;
    }

    private Object[] getUidArray() {
        Object[] result = new Object[messages.size()];
        ListIterator it = messages.listIterator();

        int i = 0;

        while (it.hasNext()) {
            result[i++] = ((MessageNode) it.next()).getUid();
        }

        return result;
    }

    /* (non-Javadoc)
 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
 */
    public void valueChanged(ListSelectionEvent e) {
        useLocalSelection = false;

        // user is still manipulating the selection
        if (e.getValueIsAdjusting() == true) {
            return;
        }

        messages = new LinkedList();

        ListSelectionModel lsm = (ListSelectionModel) e.getSource();

        if (lsm.isSelectionEmpty()) {
            //no rows are selected
        } else {
            int[] rows = view.getSelectedRows();

            for (int i = 0; i < rows.length; i++) {
                TreePath path = view.getTree().getPathForRow(rows[i]);
                MessageNode node = (MessageNode) path.getLastPathComponent();
                messages.add(node);
            }
        }

        fireSelectionChanged(new TableSelectionChangedEvent(folder,
                getUidArray()));
    }

    public void setLocalReference(FolderCommandReference[] r) {
        this.local = r;

        useLocalSelection = true;
    }
}
