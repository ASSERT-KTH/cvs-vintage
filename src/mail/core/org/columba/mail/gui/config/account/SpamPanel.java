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
package org.columba.mail.gui.config.account;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JRadioButton;

import org.columba.core.gui.util.DefaultFormBuilder;
import org.columba.core.gui.util.MultiLineLabel;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.SpamItem;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.gui.tree.util.SelectFolderDialog;
import org.columba.mail.gui.tree.util.TreeNodeList;
import org.columba.mail.main.MailInterface;

import com.jgoodies.forms.layout.FormLayout;

/**
 * Panel for spam options.
 * 
 * @author fdietz
 *  
 */
public class SpamPanel extends DefaultPanel implements ActionListener {

    private JDialog dialog;

    private AccountItem item;

    private JCheckBox enableCheckBox;

    private JCheckBox addressCheckBox;

    private JCheckBox incomingCheckBox;

    private JCheckBox markCheckBox;

    private JRadioButton incomingTrashRadioButton;

    private JRadioButton incomingMoveToRadioButton;

    private JButton incomingChooseFolderButton;

    private JRadioButton markTrashRadioButton;

    private JRadioButton markMoveToRadioButton;

    private JButton markChooseFolderButton;

    private MultiLineLabel label;

    public SpamPanel(JDialog dialog, AccountItem item) {
        this.item = item;
        this.dialog = dialog;

        initComponents();

        updateComponents(true);
        layoutComponents();
    }

    protected void initComponents() {
        enableCheckBox = new JCheckBox("Enable adaptive spam filter");
        enableCheckBox.setActionCommand("ENABLE");
        enableCheckBox.addActionListener(this);

        addressCheckBox = new JCheckBox(
                "Do not mark message as Junk if sender is in addressbook");

        incomingCheckBox = new JCheckBox("Move incoming Junk messages to:");
        incomingCheckBox.setActionCommand("INCOMING");
        incomingCheckBox.addActionListener(this);

        incomingChooseFolderButton = new JButton("Inbox");
        incomingChooseFolderButton.setActionCommand("INCOMING_BUTTON");
        incomingChooseFolderButton.addActionListener(this);
        
        incomingTrashRadioButton = new JRadioButton("Trash folder");
        incomingMoveToRadioButton = new JRadioButton("Move to:");
        ButtonGroup group = new ButtonGroup();
        group.add(incomingTrashRadioButton);
        group.add(incomingMoveToRadioButton);
        
        markCheckBox = new JCheckBox("When marking message as Junk:");
        markCheckBox.setActionCommand("MARK");
        markCheckBox.addActionListener(this);

        markTrashRadioButton = new JRadioButton("Move to Trash folder");
        markMoveToRadioButton = new JRadioButton("Move to:");
        ButtonGroup group2 = new ButtonGroup();
        group2.add(markTrashRadioButton);
        group2.add(markMoveToRadioButton);

        markChooseFolderButton = new JButton("Inbox");
        markChooseFolderButton.setActionCommand("MARK_BUTTON");
        markChooseFolderButton.addActionListener(this);

        label = new MultiLineLabel(
                "Columba can be trained to analyze the contents of your incoming messages"
                        + " and identify those that are most likely to be junk. If enabled, "
                        + "you must first train Columba to identify Junk mail by marking messages"
                        + " as Junk or not. You need to identify both Junk and non Junk messages.");
    }

    protected void layoutComponents() {
        //		Create a FormLayout instance.
        FormLayout layout = new FormLayout(
                "10dlu, max(100;default), 3dlu, fill:max(150dlu;default):grow, 3dlu, fill:max(150dlu;default):grow", 

                // 2 columns
                ""); // rows are added dynamically (no need to define them

        // here)
        DefaultFormBuilder builder = new DefaultFormBuilder(this, layout);
        builder.setLeadingColumnOffset(1);

        // create EmptyBorder between components and dialog-frame
        builder.setDefaultDialogBorder();

        builder.setLeadingColumnOffset(1);

        builder.appendSeparator("Adaptive Spam Filter");

        builder.append(label, 4);
        builder.nextLine();

        builder.append(enableCheckBox, 4);
        builder.nextLine();

        builder.appendSeparator("Filter Options");
        builder.nextLine();

        builder.append(addressCheckBox, 4);
        builder.nextLine();

        builder.append(incomingCheckBox, 4);
        builder.nextLine();

        builder.setLeadingColumnOffset(2);

        builder.append(incomingTrashRadioButton, 3);
        builder.nextLine();
        builder.append(incomingMoveToRadioButton, 2);
        builder.append(incomingChooseFolderButton);

        builder.setLeadingColumnOffset(1);

        builder.append(markCheckBox, 4);
        builder.nextLine();

        builder.setLeadingColumnOffset(2);

        builder.append(markTrashRadioButton, 3);
        builder.nextLine();

        builder.append(markMoveToRadioButton, 2);
        builder.append(markChooseFolderButton);
    }

    public void updateComponents(boolean b) {
        SpamItem spam = item.getSpamItem();

        if (b) {
            enableCheckBox.setSelected(spam.isEnabled());

            
            incomingCheckBox.setSelected(spam
                    .isMoveIncomingJunkMessagesEnabled());

            MessageFolder folder = (MessageFolder) MailInterface.treeModel.getFolder(spam
                    .getIncomingCustomFolder());
            String treePath = folder.getTreePath();
            incomingChooseFolderButton.setText(treePath);

           
            incomingMoveToRadioButton.setSelected(!spam
                    .isIncomingTrashSelected());

           
            incomingTrashRadioButton.setSelected(spam
                    .isIncomingTrashSelected());

         
            markCheckBox.setSelected(spam.isMoveMessageWhenMarkingEnabled());

            folder = (MessageFolder) MailInterface.treeModel.getFolder(spam
                    .getMoveCustomFolder());
            treePath = folder.getTreePath();
            markChooseFolderButton.setText(treePath);

            
            markMoveToRadioButton.setSelected(!spam.isMoveTrashSelected());

           
            markTrashRadioButton.setSelected(spam.isMoveTrashSelected());
            
            addressCheckBox.setSelected(spam.checkAddressbook());
            
            enableComponents(enableCheckBox.isSelected());
            
            

        } else {
            spam.setEnabled(enableCheckBox.isSelected());

            spam.enableMoveIncomingJunkMessage(incomingCheckBox.isSelected());
            spam.enableMoveMessageWhenMarking(markCheckBox.isSelected());

            spam.selectedIncomingTrash(incomingTrashRadioButton.isSelected());
            spam.selectMoveTrash(markTrashRadioButton.isSelected());

            
            TreeNodeList list = new TreeNodeList(incomingChooseFolderButton.getText());
            MessageFolder folder = (MessageFolder) MailInterface.treeModel.getFolder(list);

            if (folder == null) {
                // user didn't select any folder
                // -> make Inbox the default folder
                folder = (MessageFolder) MailInterface.treeModel.getFolder(101);
            }

            int uid = folder.getUid();
            
            spam.setIncomingCustomFolder(uid);
            
            list = new TreeNodeList(markChooseFolderButton.getText());
            folder = (MessageFolder) MailInterface.treeModel.getFolder(list);

            if (folder == null) {
                // user didn't select any folder
                // -> make Inbox the default folder
                folder = (MessageFolder) MailInterface.treeModel.getFolder(101);
            }

            uid = folder.getUid();
            
            spam.setMoveCustomFolder(uid);
            
            spam.enableCheckAddressbook(addressCheckBox.isSelected());

        }
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0) {
        String action = arg0.getActionCommand();

        if (action.equals("ENABLE")) {
            enableComponents(enableCheckBox.isSelected());
        } else if (action.equals("INCOMING")) {
            enableIncoming(incomingCheckBox.isSelected());

        } else if (action.equals("MARK")) {
            enableMark(markCheckBox.isSelected());
        } else if (action.equals("MARK_BUTTON")) {
            SelectFolderDialog dialog = MailInterface.treeModel.getSelectFolderDialog();

            if (dialog.success()) {
                MessageFolder folder = dialog.getSelectedFolder();

                String treePath = folder.getTreePath();
                markChooseFolderButton.setText(treePath);
            }
        } else if (action.equals("INCOMING_BUTTON")) {
            SelectFolderDialog dialog = MailInterface.treeModel.getSelectFolderDialog();

            if (dialog.success()) {
                MessageFolder folder = dialog.getSelectedFolder();

                String treePath = folder.getTreePath();
                incomingChooseFolderButton.setText(treePath);
            }
        } 

    }

    private void enableComponents(boolean enable) {

        addressCheckBox.setEnabled(enable);

        incomingCheckBox.setEnabled(enable);
        enableIncoming(enable);

        markCheckBox.setEnabled(enable);
        enableMark(enable);
    }

    private void enableIncoming(boolean enable) {
        
        incomingChooseFolderButton.setEnabled(incomingCheckBox.isSelected());
        incomingMoveToRadioButton.setEnabled(incomingCheckBox.isSelected());
        incomingTrashRadioButton.setEnabled(incomingCheckBox.isSelected());
    }

    private void enableMark(boolean enable) {
       
        markChooseFolderButton.setEnabled(markCheckBox.isSelected());
        markMoveToRadioButton.setEnabled(markCheckBox.isSelected());
        markTrashRadioButton.setEnabled(markCheckBox.isSelected());
    }

}
