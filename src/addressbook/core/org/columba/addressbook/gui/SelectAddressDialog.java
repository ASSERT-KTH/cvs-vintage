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
package org.columba.addressbook.gui;

import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

import org.columba.addressbook.folder.Folder;
import org.columba.addressbook.folder.GroupListCard;
import org.columba.addressbook.folder.HeaderItem;
import org.columba.addressbook.folder.HeaderItemList;
import org.columba.addressbook.gui.tree.util.SelectAddressbookFolderDialog;
import org.columba.addressbook.gui.util.AddressbookDNDListView;
import org.columba.addressbook.gui.util.AddressbookListModel;
import org.columba.addressbook.util.AddressbookResourceLoader;

import org.columba.core.gui.util.ButtonWithMnemonic;
import org.columba.core.main.MainInterface;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.SwingConstants;


public class SelectAddressDialog extends JDialog implements ActionListener {
    private AddressbookDNDListView toList;
    private AddressbookDNDListView ccList;
    private AddressbookDNDListView bccList;
    private AddressbookDNDListView addressbook;
    private JButton toButton;
    private JButton ccButton;
    private JButton bccButton;
    private JButton toRemoveButton;
    private JButton ccRemoveButton;
    private JButton bccRemoveButton;
    private JButton chooseButton;
    private JLabel chooseLabel;
    private AddressbookListModel[] dialogList;
    private HeaderItemList[] headerItemList;

    public SelectAddressDialog(JFrame frame, HeaderItemList[] list) {
        super(frame, true);

        this.headerItemList = list;

        dialogList = new AddressbookListModel[3];

        init();
    }

    public HeaderItemList[] getHeaderItemLists() {
        return headerItemList;
    }

    protected void init() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ) );
        JPanel panel = new JPanel(new BorderLayout());

        JPanel leftPanel = new JPanel();
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        leftPanel.add(Box.createVerticalGlue());

        JPanel list1Panel = new JPanel(new BorderLayout());
        JPanel list1TopPanel = new JPanel(new BorderLayout());
        list1TopPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JLabel label1 = new JLabel(AddressbookResourceLoader.getString("dialog", "selectaddressdialog", "to")); //$NON-NLS-1$
        list1TopPanel.add(label1, BorderLayout.WEST);
        list1Panel.add(list1TopPanel, BorderLayout.NORTH);

        /*
        JPanel list1EastPanel = new JPanel();
        list1EastPanel.setLayout( new GridLayout(2,1) );
        toButton = new JButton("<- To");
        toButton.addActionListener(this);
        toButton.setActionCommand("TO");
        list1EastPanel.add( toButton );
        toRemoveButton = new JButton("To ->");
        toRemoveButton.addActionListener(this);
        toRemoveButton.setActionCommand("TO_REMOVE");
        list1EastPanel.add( toRemoveButton );

        list1Panel.add( list1EastPanel, BorderLayout.EAST );
        */
        dialogList[0] = new AddressbookListModel();
        dialogList[0].setHeaderList(headerItemList[0]);
        toList = new AddressbookDNDListView(dialogList[0]);

        //toList.setCellRenderer(new AddressbookListRenderer());
        JScrollPane toPane = new JScrollPane(toList);
        toPane.setPreferredSize(new Dimension(250, 150));
        list1Panel.add(toPane);
        leftPanel.add(list1Panel);

        leftPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel list2Panel = new JPanel(new BorderLayout());
        JPanel list2TopPanel = new JPanel(new BorderLayout());
        list2TopPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JLabel label2 = new JLabel(AddressbookResourceLoader.getString("dialog", "selectaddressdialog", "cc")); //$NON-NLS-1$
        list2TopPanel.add(label2, BorderLayout.WEST);
        list2Panel.add(list2TopPanel, BorderLayout.NORTH);
        dialogList[1] = new AddressbookListModel();
        dialogList[1].setHeaderList(headerItemList[1]);
        ccList = new AddressbookDNDListView(dialogList[1]);

        //ccList.setCellRenderer(new AddressbookListRenderer());
        JScrollPane ccPane = new JScrollPane(ccList);
        ccPane.setPreferredSize(new Dimension(250, 150));
        list2Panel.add(ccPane);
        leftPanel.add(list2Panel);

        leftPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel list3Panel = new JPanel(new BorderLayout());
        JPanel list3TopPanel = new JPanel(new BorderLayout());
        list3TopPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JLabel label3 = new JLabel(AddressbookResourceLoader.getString("dialog", "selectaddressdialog", "bcc")); //$NON-NLS-1$

        list3TopPanel.add(label3, BorderLayout.WEST);
        list3Panel.add(list3TopPanel, BorderLayout.NORTH);

        dialogList[2] = new AddressbookListModel();
        dialogList[2].setHeaderList(headerItemList[2]);
        bccList = new AddressbookDNDListView(dialogList[2]);

        //bccList.setCellRenderer(new AddressbookListRenderer());
        JScrollPane bccPane = new JScrollPane(bccList);
        bccPane.setPreferredSize(new Dimension(250, 150));
        list3Panel.add(bccPane);
        leftPanel.add(list3Panel);

        /*
        for (int i = 0; i < 3; i++)
        {
                Object[] array = list[i].toArray();

                for (int j = 0; j < array.length; j++)
                {
                        dialogList[i].addElement(array[j]);
                }

        }
        */
        leftPanel.add(Box.createVerticalGlue());

        //panel.add( Box.createRigidArea( new Dimension(20,0) ) );
        panel.add(leftPanel);

        mainPanel.add(panel, BorderLayout.WEST);

        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));

        middlePanel.add(Box.createVerticalGlue());

        toButton = new JButton(AddressbookResourceLoader.getString("dialog", "selectaddressdialog", "left_arrow_to")); //$NON-NLS-1$
        toButton.addActionListener(this);
        toButton.setActionCommand("TO"); //$NON-NLS-1$
        middlePanel.add(toButton);
        toRemoveButton = new JButton(AddressbookResourceLoader.getString("dialog", "selectaddressdialog", "right_arrow_to")); //$NON-NLS-1$
        toRemoveButton.addActionListener(this);
        toRemoveButton.setActionCommand("TO_REMOVE"); //$NON-NLS-1$
        middlePanel.add(toRemoveButton);

        //middlePanel.add( Box.createRigidArea( new Dimension(0,20) ) );
        middlePanel.add(Box.createVerticalGlue());

        ccButton = new JButton(AddressbookResourceLoader.getString("dialog", "selectaddressdialog", "left_arrow_cc")); //$NON-NLS-1$
        ccButton.addActionListener(this);
        ccButton.setActionCommand("CC"); //$NON-NLS-1$
        middlePanel.add(ccButton);
        ccRemoveButton = new JButton(AddressbookResourceLoader.getString("dialog", "selectaddressdialog", "right_arrow_cc")); //$NON-NLS-1$
        ccRemoveButton.addActionListener(this);
        ccRemoveButton.setActionCommand("CC_REMOVE"); //$NON-NLS-1$
        middlePanel.add(ccRemoveButton);

        //middlePanel.add( Box.createRigidArea( new Dimension(0,20) ) );
        middlePanel.add(Box.createVerticalGlue());

        bccButton = new JButton(AddressbookResourceLoader.getString("dialog", "selectaddressdialog", "left_arrow_bcc")); //$NON-NLS-1$
        bccButton.addActionListener(this);
        bccButton.setActionCommand("BCC"); //$NON-NLS-1$
        middlePanel.add(bccButton);
        bccRemoveButton = new JButton(AddressbookResourceLoader.getString("dialog", "selectaddressdialog", "right_arrow_bcc")); //$NON-NLS-1$
        bccRemoveButton.addActionListener(this);
        bccRemoveButton.setActionCommand("BCC_REMOVE"); //$NON-NLS-1$
        middlePanel.add(bccRemoveButton);

        middlePanel.add(Box.createVerticalGlue());

        //panel.add( Box.createRigidArea( new Dimension(20,0) ) );
        panel.add(middlePanel, BorderLayout.EAST);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JPanel rightTopPanel = new JPanel();

        //rightTopPanel.setLayout( new GridBagLayout() );
        chooseLabel = new JLabel(AddressbookResourceLoader.getString("dialog", "selectaddressdialog", "addressbook")); //$NON-NLS-1$
        rightTopPanel.add(chooseLabel);
        chooseButton = new JButton(AddressbookResourceLoader.getString("dialog", "selectaddressdialog", "personal_addressbook")); //$NON-NLS-1$
        chooseButton.setActionCommand("CHOOSE"); //$NON-NLS-1$
        chooseButton.addActionListener(this);
        rightTopPanel.add(chooseButton);
        rightPanel.add(rightTopPanel, BorderLayout.NORTH);

        addressbook = new AddressbookDNDListView();
        addressbook.setAcceptDrop(false);

        //addressbook.setCellRenderer(new AddressbookListRenderer());
        JScrollPane scrollPane = new JScrollPane(addressbook);
        scrollPane.setPreferredSize(new Dimension(250, 200));
        rightPanel.add(scrollPane);

        mainPanel.add(rightPanel);

        getContentPane().add(mainPanel);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(17, 12, 11, 11));

        ButtonWithMnemonic okButton = new ButtonWithMnemonic(AddressbookResourceLoader.getString(
                    "global", "ok")); //$NON-NLS-1$ //$NON-NLS-2$
        okButton.setActionCommand("OK"); //$NON-NLS-1$
        okButton.addActionListener(this);
        buttonPanel.add(okButton);

        ButtonWithMnemonic cancelButton = new ButtonWithMnemonic(AddressbookResourceLoader.getString(
                    "global", "cancel")); //$NON-NLS-1$ //$NON-NLS-2$
        cancelButton.setActionCommand("CANCEL"); //$NON-NLS-1$
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(okButton);
        getRootPane().registerKeyboardAction(this, "CANCEL", //$NON-NLS-1$
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
        pack();
        setLocationRelativeTo(null);
    }

    public void setHeaderList(HeaderItemList list) {
        //members = new DefaultListModel();

        /*
        for ( int i=0; i<list.count(); i++ )
        {
                HeaderItem item = list.get(i);
                //members.addElement( item );
        }
        */
        List v = list.getVector();

        addressbook.setListData((Vector) v);
    }

    public void updateComponents(GroupListCard card, HeaderItemList list,
        boolean b) {
        /*
        if ( b == true )
        {
                // gettext
                nameTextField.setText( card.get("displayname") );


                members = new DefaultListModel();
                for ( int i=0; i<list.count(); i++ )
                {
                        HeaderItem item = list.get(i);
                        members.addElement(item);
                }

                this.list.setModel( members );

        }
        else
        {
                // settext
                card.set("displayname",nameTextField.getText() );

                // remove all children
                card.removeMembers();

                // add children
                for ( int i=0; i<members.size(); i++ )
                {
                        HeaderItem item = (HeaderItem) members.get(i);
                        Object uid = item.getUid();
                        card.addMember( ((Integer) uid).toString() );
                }
        }
        */
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals("CANCEL")) { //$NON-NLS-1$
            setVisible(false);
        } else if (command.equals("OK")) { //$NON-NLS-1$
            setVisible(false);

            for (int i = 0; i < 3; i++) {
                Object[] array = dialogList[i].toArray();
                headerItemList[i].clear();

                System.out.println("array-size=" + array.length); //$NON-NLS-1$

                for (int j = 0; j < array.length; j++) {
                    HeaderItem item = (HeaderItem) array[j];

                    if (item.isContact()) {
                        String address = (String) item.get("email;internet"); //$NON-NLS-1$
                        System.out.println("old address:" + address); //$NON-NLS-1$

                        if (address == null) {
                            address = ""; //$NON-NLS-1$
                        }

                        /*
                        String newaddress = AddressParser.quoteAddress(address);
                        System.out.println("new address:" + newaddress);
                        item.add("email;internet", newaddress);
                        */
                    }

                    if (i == 0) {
                        item.add("field", "To"); //$NON-NLS-1$ //$NON-NLS-2$
                    } else if (i == 1) {
                        item.add("field", "Cc"); //$NON-NLS-1$ //$NON-NLS-2$
                    } else if (i == 2) {
                        item.add("field", "Bcc"); //$NON-NLS-1$ //$NON-NLS-2$
                    }

                    headerItemList[i].add((HeaderItem) item.clone());

                    //headerItemList[i].add(item);
                }
            }
        } else if (command.equals("TO")) { //$NON-NLS-1$
            int[] array = addressbook.getSelectedIndices();
            ListModel model = addressbook.getModel();
            HeaderItem item;

            for (int j = 0; j < array.length; j++) {
                item = (HeaderItem) model.getElementAt(array[j]);
                dialogList[0].addElement((HeaderItem) item.clone());
            }
        } else if (command.equals("CC")) { //$NON-NLS-1$
            int[] array = addressbook.getSelectedIndices();
            ListModel model = addressbook.getModel();
            HeaderItem item;

            for (int j = 0; j < array.length; j++) {
                item = (HeaderItem) model.getElementAt(array[j]);
                dialogList[1].addElement((HeaderItem) item.clone());
            }
        } else if (command.equals("BCC")) { //$NON-NLS-1$
            int[] array = addressbook.getSelectedIndices();
            ListModel model = addressbook.getModel();
            HeaderItem item;

            for (int j = 0; j < array.length; j++) {
                item = (HeaderItem) model.getElementAt(array[j]);
                dialogList[2].addElement((HeaderItem) item.clone());
            }
        } else if (command.equals("TO_REMOVE")) { //$NON-NLS-1$
            Object[] array = toList.getSelectedValues();

            for (int j = 0; j < array.length; j++) {
                dialogList[0].removeElement(array[j]);
            }
        } else if (command.equals("CC_REMOVE")) { //$NON-NLS-1$
            Object[] array = ccList.getSelectedValues();

            for (int j = 0; j < array.length; j++) {
                dialogList[1].removeElement(array[j]);
            }
        } else if (command.equals("BCC_REMOVE")) { //$NON-NLS-1$
            Object[] array = bccList.getSelectedValues();

            for (int j = 0; j < array.length; j++) {
                dialogList[2].removeElement(array[j]);
            }
        } else if (command.equals("CHOOSE")) { //$NON-NLS-1$
            SelectAddressbookFolderDialog dialog = MainInterface.addressbookTreeModel.getSelectAddressbookFolderDialog();

            Folder selectedFolder = dialog.getSelectedFolder();

            if (selectedFolder != null) {
                HeaderItemList list = selectedFolder.getHeaderItemList();
                setHeaderList(list);
                chooseButton.setText(selectedFolder.getName());
            }
        }
    }
}
