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

package org.columba.mail.gui.config.account;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.columba.core.gui.util.CheckBoxWithMnemonic;
import org.columba.core.gui.util.DefaultFormBuilder;
import org.columba.core.gui.util.LabelWithMnemonic;
import org.columba.mail.config.PGPItem;
import org.columba.mail.util.MailResourceLoader;

import com.jgoodies.forms.layout.FormLayout;

/**
 * Shows PGP-related options.
 */
public class SecurityPanel extends DefaultPanel implements ActionListener {
    private JLabel idLabel;
    private JTextField idTextField;
    private JLabel typeLabel;
    private JComboBox typeComboBox;
    private JLabel pathLabel;
    private JButton pathButton;
    private JCheckBox enableCheckBox;
    private JCheckBox alwaysSignCheckBox;
    private JCheckBox alwaysEncryptCheckBox;
    private PGPItem item;

    public SecurityPanel(PGPItem item) {
        super();
        this.item = item;

        initComponents();
        updateComponents(true);
        layoutComponents();

        //enableCheckBox.setEnabled(false);
    }

    protected void updateComponents(boolean b) {
        if (b) {
            idTextField.setText(item.get("id"));
            pathButton.setText(item.get("path"));

            enableCheckBox.setSelected(item.getBoolean("enabled"));

            alwaysSignCheckBox.setSelected(item.getBoolean("always_sign"));
            alwaysEncryptCheckBox.setSelected(item.getBoolean("always_encrypt"));

            enablePGP(enableCheckBox.isSelected());
        } else {
            item.set("id", idTextField.getText());
            item.set("path", pathButton.getText());

            item.set("enabled", enableCheckBox.isSelected());

            item.set("always_sign", alwaysSignCheckBox.isSelected());
            item.set("always_encrypt", alwaysEncryptCheckBox.isSelected());
        }
    }

    protected void layoutComponents() {
        // Create a FormLayout instance. 
        FormLayout layout = new FormLayout("10dlu, max(70dlu;default), 3dlu, fill:max(150dlu;default):grow ",
                
            // 2 columns
            ""); // rows are added dynamically (no need to define them here)

        // create a form builder
        DefaultFormBuilder builder = new DefaultFormBuilder(this, layout);

        // create EmptyBorder between components and dialog-frame 
        builder.setDefaultDialogBorder();

        // skip the first column
        builder.setLeadingColumnOffset(1);

        // Add components to the panel:
        builder.appendSeparator(MailResourceLoader.getString("dialog",
                "account", "pgp_options"));
        builder.nextLine();

        builder.append(enableCheckBox, 3);
        builder.nextLine();

        builder.append(idLabel, 1);
        builder.append(idTextField);
        builder.nextLine();

        builder.append(alwaysSignCheckBox, 3);
        builder.nextLine();

        //      TODO: reactivate when feature is supported
        /*
        builder.append(alwaysEncryptCheckBox, 3);
        builder.nextLine();
        */
    }

    protected void initComponents() {
        enableCheckBox = new CheckBoxWithMnemonic(MailResourceLoader.getString(
                    "dialog", "account", "enable_PGP_Support"));
        enableCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        enableCheckBox.setActionCommand("ENABLE");
        enableCheckBox.addActionListener(this);

        idLabel = new LabelWithMnemonic(MailResourceLoader.getString(
                    "dialog", "account", "User_ID"));

        typeLabel = new JLabel(MailResourceLoader.getString("dialog",
                    "account", "PGP_Version")); //$NON-NLS-1$

        pathLabel = new JLabel(MailResourceLoader.getString("dialog",
                    "account", "Path_to_Binary")); //$NON-NLS-1$

        idTextField = new JTextField();

        typeComboBox = new JComboBox();

        //typeComboBox.setMargin( new Insets( 0,0,0,0 ) );
        typeComboBox.insertItemAt("GnuPG", 0);
        typeComboBox.insertItemAt("PGP2", 1);
        typeComboBox.insertItemAt("PGP5", 2);
        typeComboBox.insertItemAt("PGP6", 3);
        typeComboBox.setSelectedIndex(0);
        typeComboBox.setEnabled(false);

        pathButton = new JButton();

        //pathButton.setMargin( new Insets( 0,0,0,0 ) );
        pathButton.setActionCommand("PATH");
        pathButton.addActionListener(this);

        alwaysSignCheckBox = new CheckBoxWithMnemonic(MailResourceLoader.getString(
                    "dialog", "account", "Always_sign_when_sending_messages"));
        alwaysSignCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        alwaysSignCheckBox.setEnabled(false);

        alwaysEncryptCheckBox = new CheckBoxWithMnemonic(MailResourceLoader.getString(
                    "dialog", "account", "Always_encrypt_when_sending_messages")); //$NON-NLS-1$
        alwaysEncryptCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        alwaysEncryptCheckBox.setEnabled(false);
    }

    public void enablePGP(boolean b) {
        //typeComboBox.setEnabled(b);
        idTextField.setEnabled(b);
        idLabel.setEnabled(b);
        typeLabel.setEnabled(b);
        pathLabel.setEnabled(b);
        pathButton.setEnabled(b);
        alwaysSignCheckBox.setEnabled(b);
        alwaysEncryptCheckBox.setEnabled(b);
    }

    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        if (action.equals("ENABLE")) {
            enablePGP(enableCheckBox.isSelected());
        } else if (action.equals("PATH")) {
            JFileChooser fileChooser = new JFileChooser();
            File aktFile;

            fileChooser.setDialogTitle(MailResourceLoader.getString("dialog",
                    "account", "PGP_Binary")); //$NON-NLS-1$
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            int returnVal = fileChooser.showDialog(null,
                    MailResourceLoader.getString("dialog", "account",
                        "Select_File")); //$NON-NLS-1$

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                pathButton.setText(file.getPath());
            }
        }
    }

    public boolean isFinished() {
        boolean result = true;

        /*
        String name = getAccountName();
        String address = getAddress();

        if (name.length() == 0) {
            result = false;
            JOptionPane.showMessageDialog(MainInterface.frameModel.getActiveFrame(),
                                           "You have to enter a name for this account!");
        } else if (address.length() == 0) {
            result = false;
            JOptionPane.showMessageDialog(MainInterface.frameModel.getActiveFrame(),
                                           "You have to enter your address!");
        }
        */
        return result;
    }
}
