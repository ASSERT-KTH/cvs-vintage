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
package org.columba.addressbook.gui.action;

import org.columba.addressbook.main.AddressbookInterface;

import org.columba.core.action.FrameAction;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class AddressbookActionListener implements ActionListener {
    public FrameAction cutAction;
    public FrameAction copyAction;
    public FrameAction pasteAction;
    public FrameAction selectAllAction;
    public FrameAction deleteAction;
    public FrameAction closeAction;
    public FrameAction addContactAction;
    public FrameAction addGroupAction;
    public FrameAction removeAction;
    public FrameAction propertiesAction;
    public FrameAction addAddressbookAction;
    public FrameAction addressbookImportAction;
    public FrameAction addvcardAction;
    public FrameAction savevcardAction;
    private AddressbookInterface addressbookInterface;

    public AddressbookActionListener(AddressbookInterface i) {
        this.addressbookInterface = i;
        initAction();
    }

    public void changeActions() {
        Object[] items = addressbookInterface.table.getSelectedItems();

        if (items.length > 0) {
            // enable
            removeAction.setEnabled(true);
            propertiesAction.setEnabled(true);
        } else {
            // disable
            removeAction.setEnabled(false);
            propertiesAction.setEnabled(false);
        }

        /*
        Folder folder = addressbookInterface.tree.getSelectedFolder();
        FolderItem item = folder.getFolderItem();
        if (item != null)
        {
                if (item.getType().equals("addressbook"))
                {

                        // enable

                        addContactAction.setEnabled(true);
                        addGroupAction.setEnabled(true);
                        addvcardAction.setEnabled(true);

                }
                else
                {
                        // disable
                        addContactAction.setEnabled(false);
                        addGroupAction.setEnabled(false);
                        addvcardAction.setEnabled(false);

                        }
        }
        */
    }

    public void initAction() {
        /*
                        cutAction =
                                new BasicAction(
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_edit_cut"),
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_edit_cut"),
                                        "CUT_FOR_FUN",
                                        ImageLoader.getSmallImageIcon("stock_cut-16.png"),
                                        ImageLoader.getImageIcon("stock_cut.png"),
                                        'T',
                                        KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
                        cutAction.setEnabled(true);

                        copyAction =
                                new BasicAction(
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_edit_copy"),
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_edit_copy"),
                                        "COPY_FOR_FUN",
                                        ImageLoader.getSmallImageIcon("stock_copy-16.png"),
                                        ImageLoader.getImageIcon("stock_copy.png"),
                                        'C',
                                        KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
                        copyAction.setEnabled(true);

                        pasteAction =
                                new BasicAction(
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_edit_paste"),
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_edit_paste"),
                                        "PASTE",
                                        ImageLoader.getSmallImageIcon("stock_paste-16.png"),
                                        ImageLoader.getImageIcon("stock_paste.png"),
                                        'V',
                                        KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
                        pasteAction.setEnabled(true);

                        deleteAction =
                                new BasicAction(
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_edit_delete"),
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_edit_delete"),
                                        "DELETE",
                                        ImageLoader.getSmallImageIcon("stock_paste-16.png"),
                                        ImageLoader.getImageIcon("stock_paste.png"),
                                        'D',
                                        KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
                        deleteAction.setEnabled(true);

                        selectAllAction =
                                new BasicAction(
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_edit_selectall"),
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_edit_selectall"),
                                        "SELECTALL",
                                        null,
                                        null,
                                        'A',
                                        KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
                        selectAllAction.setEnabled(true);

                        closeAction =
                                new BasicAction(
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_file_close"),
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_file_close"),
                                        "CLOSE",
                                        ImageLoader.getSmallImageIcon("stock_exit-16.png"),
                                        ImageLoader.getImageIcon("stock_exit.png"),
                                        '0',
                                        null);
                        closeAction.setEnabled(true);
                        closeAction.addActionListener(this);

                        addContactAction =
                                new BasicAction(
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_file_addcontact"),
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_file_addcontact"),
                                        "ADDCONTACT",
                                        ImageLoader.getSmallImageIcon("contact_small.png"),
                                        ImageLoader.getImageIcon("contact.png"),
                                        '0',
                                        null);
                        addContactAction.setEnabled(true);
                        addContactAction.addActionListener(this);

                        addGroupAction =
                                new BasicAction(
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_file_addgroup"),
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_file_addgroup"),
                                        "ADDGROUP",
                                        ImageLoader.getSmallImageIcon("group_small.png"),
                                        ImageLoader.getImageIcon("group.png"),
                                        '0',
                                        null);
                        addGroupAction.setEnabled(true);
                        addGroupAction.addActionListener(this);

                        removeAction =
                                new BasicAction(
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_file_remove"),
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_file_remove"),
                                        "REMOVE",
                                        ImageLoader.getSmallImageIcon("stock_delete-16.png"),
                                        ImageLoader.getImageIcon("stock_delete.png"),
                                        '0',
                                        null);
                        removeAction.setEnabled(true);
                        removeAction.addActionListener(this);

                        propertiesAction =
                                new BasicAction(
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_file_properties"),
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_file_properties"),
                                        "PROPERTIES",
                                        ImageLoader.getSmallImageIcon("stock_edit-16.png"),
                                        ImageLoader.getImageIcon("stock_edit.png"),
                                        '0',
                                        null);
                        propertiesAction.setEnabled(true);
                        propertiesAction.addActionListener(this);

                        addAddressbookAction =
                                new BasicAction(
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_file_addaddressbook"),
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_file_addaddressbook"),
                                        "ADDADDRESSBOOK",
                                        ImageLoader.getSmallImageIcon("stock_book-16.png"),
                                        ImageLoader.getImageIcon("stock_book.png"),
                                        '0',
                                        null);
                        addAddressbookAction.setEnabled(true);
                        addAddressbookAction.addActionListener(this);

                        addressbookImportAction =
                                new BasicAction(
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_utilities_addressbook"),
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_utilities_addressbook"),
                                        "ADDRESSBOOK_IMPORT",
                                        null,
                                        null,
                                        '0',
                                        null);
                        addressbookImportAction.setEnabled(true);
                        addressbookImportAction.addActionListener(this);

                        addvcardAction =
                                new BasicAction(
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_file_addvcard"),
                                        AddressbookResourceLoader.getString(
                                                "menu",
                                                "mainframe",
                                                "menu_file_addvcard"),
                                        "ADD_VCARD",
                                        null,
                                        null,
                                        '0',
                                        null);
                        addvcardAction.setEnabled(true);
                        addvcardAction.addActionListener(this);
        */
    }

    public void actionPerformed(ActionEvent e) {
        /*
        String command = e.getActionCommand();

        if (command.equals(closeAction.getActionCommand())) {
                addressbookInterface.frame.setVisible(false);
        } else if (command.equals(addAddressbookAction.getActionCommand())) {
                String name = JOptionPane.showInputDialog(
                                addressbookInterface.frame,
                                AddressbookResourceLoader.getString(
                                        "menu", "mainframe", "add_addressbook_message"),
                                AddressbookResourceLoader.getString(
                                        "menu", "mainframe", "add_addressbook_title"),
                                JOptionPane.PLAIN_MESSAGE);

                if (name != null && name.length() != 0) {
                        System.out.println("name:" + name);
                        //TODO: Create new addressbook with the given name
                }
        } else if (command.equals(addContactAction.getActionCommand())) {
                ContactDialog dialog =
                        new ContactDialog(addressbookInterface.frame);

                dialog.setVisible(true);
                if (dialog.getResult()) {
                        System.out.println("saving contact");

                        // Ok

                        ContactCard card = new ContactCard();

                        dialog.updateComponents(card, false);

                        Folder folder = addressbookInterface.tree.getSelectedFolder();

                        folder.add(card);

                        addressbookInterface.table.setFolder(folder);
                }
        } else if (command.equals(addGroupAction.getActionCommand())) {
                Folder folder =
                        (Folder) addressbookInterface.tree.getSelectedFolder();
                if (folder == null) return;

                EditGroupDialog dialog =
                        new EditGroupDialog(
                                addressbookInterface.frame,
                                addressbookInterface,
                                null);

                dialog.setHeaderList(folder.getHeaderItemList());

                dialog.setVisible(true);

                if (dialog.getResult()) {
                        // Ok
                        GroupListCard card = new GroupListCard();

                        dialog.updateComponents(card, null, false);

                        folder.add(card);
                        addressbookInterface.table.setFolder(folder);
                }
        } else if (command.equals(removeAction.getActionCommand())) {
                Object[] uids = addressbookInterface.table.getSelectedUids();
                AddressbookFolder folder =
                        (AddressbookFolder) addressbookInterface
                                .tree
                                .getSelectedFolder();

                for (int i = 0; i < uids.length; i++) {
                        folder.remove(uids[i]);
                }
                addressbookInterface.table.setFolder(folder);

        } else if (command.equals(propertiesAction.getActionCommand())) {
                Object uid = addressbookInterface.table.getSelectedUid();
                if (uid == null) return;
                HeaderItem item = addressbookInterface.table.getSelectedItem();

                AddressbookFolder folder =
                        (AddressbookFolder) addressbookInterface
                                .tree
                                .getSelectedFolder();

                if (item.isContact()) {
                        ContactCard card = (ContactCard) folder.get(uid);
                        System.out.println("card:" + card);

                        ContactDialog dialog =
                                new ContactDialog(addressbookInterface.frame);

                        dialog.updateComponents(card, true);
                        dialog.setVisible(true);

                        if (dialog.getResult()) {
                                System.out.println("saving contact");

                                // Ok

                                dialog.updateComponents(card, false);
                                folder.modify(card, uid);

                                addressbookInterface.table.setFolder(folder);
                        }
                } else {
                        GroupListCard card = (GroupListCard) folder.get(uid);

                        EditGroupDialog dialog =
                                new EditGroupDialog(
                                        addressbookInterface.frame,
                                        addressbookInterface,
                                        null);

                        dialog.setHeaderList(folder.getHeaderItemList());
                        Object[] uids = card.getUids();
                        HeaderItemList members = folder.getHeaderItemList(uids);
                        dialog.updateComponents(card, members, true);

                        dialog.setVisible(true);

                        if (dialog.getResult()) {
                                dialog.updateComponents(card, null, false);
                                folder.modify(card, uid);
                                addressbookInterface.table.setFolder(folder);
                        }
                }

        } else if (
                command.equals(addressbookImportAction.getActionCommand())) {
                ImportWizard dialog = new ImportWizard(addressbookInterface);

        } else if (command.equals(addvcardAction.getActionCommand())) {
                addvcard();

        }
        */
    }

    /*
    protected void addvcard() {
            Folder destinationFolder =
                    (Folder) addressbookInterface.tree.getSelectedFolder();

            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setMultiSelectionEnabled(true);
            int returnVal = fc.showOpenDialog(addressbookInterface.frame);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File[] files = fc.getSelectedFiles();

                    for (int i = 0; i < files.length; i++) {
                            try {
                                    StringBuffer strbuf = new StringBuffer();

                                    BufferedReader in =
                                            new BufferedReader(new FileReader(files[i]));
                                    String str;

                                    while ((str = in.readLine()) != null) {
                                            strbuf.append(str + "\n");
                                    }

                                    in.close();

                                    ContactCard card = VCardParser.parse(strbuf.toString());

                                    destinationFolder.add(card);
                            } catch (Exception ex) {
                                    ex.printStackTrace();
                            }
                    }

            }

            addressbookInterface.table.setFolder(destinationFolder);

    }
    */
}
