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
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.columba.core.gui.util.CheckBoxWithMnemonic;
import org.columba.core.gui.util.DefaultFormBuilder;
import org.columba.core.gui.util.LabelWithMnemonic;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.Identity;
import org.columba.mail.main.MailInterface;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.parser.ParserException;

import com.jgoodies.forms.layout.FormLayout;

public class IdentityPanel extends DefaultPanel implements ActionListener {
    private JLabel nameLabel;
    private JTextField nameTextField;
    private JLabel addressLabel;
    private JTextField addressTextField;
    private JLabel organisationLabel;
    private JTextField organisationTextField;
    private JLabel replyaddressLabel;
    private JTextField replyaddressTextField;
    private JLabel accountnameLabel;
    private JTextField accountnameTextField;
    private JCheckBox defaultAccountCheckBox;
    private JButton selectSignatureButton;
    private JCheckBox attachsignatureCheckBox;
    private AccountItem account;

    //private ConfigFrame frame;
    public IdentityPanel(AccountItem account) {
        super();

        //this.frame = frame;
        this.account = account;

        initComponents();
        layoutComponents();
        updateComponents(true);
    }

    protected void updateComponents(boolean b) {
        Identity identity = account.getIdentity();
        if (b) {
            accountnameTextField.setText(account.getName());
            Address address = identity.getAddress();
            nameTextField.setText(address.getDisplayName());
            addressTextField.setText(address.getMailAddress());
            address = identity.getReplyToAddress();
            replyaddressTextField.setText(
                    address == null ? "" : address.getMailAddress());
            organisationTextField.setText(identity.getOrganisation());
            File signature = identity.getSignature();
            selectSignatureButton.setText(
                    signature == null ? "~/.signature" : signature.getPath());

            attachsignatureCheckBox.setSelected(signature != null);

            defaultAccountCheckBox.setSelected(
                    MailInterface.config.getAccountList().getDefaultAccountUid()
                        == account.getInteger("uid"));
        } else {
            try {
                Address address = Address.parse(addressTextField.getText());
                if (nameTextField.getText() != null) {
                    address.setDisplayName(nameTextField.getText());
                }
                identity.setAddress(address);
                if (replyaddressTextField.getText().length() > 0) {
                    address = Address.parse(replyaddressTextField.getText());
                } else {
                    address = null;
                }
                identity.setReplyToAddress(address);
            } catch (ParserException pe) {} //does not occur
            identity.setOrganisation(organisationTextField.getText());
            if (attachsignatureCheckBox.isSelected()) {
                identity.setSignature(new File(selectSignatureButton.getText()));
            } else {
                identity.setSignature(null);
            }

            account.setName(accountnameTextField.getText());

            if (defaultAccountCheckBox.isSelected()) {
                MailInterface.config.getAccountList().setDefaultAccount(
                    account.getUid());
            }
        }
    }

    protected void initComponents() {
        accountnameLabel = new LabelWithMnemonic(MailResourceLoader.getString(
                    "dialog", "account", "identity_accountname"));

        accountnameTextField = new JTextField();
        accountnameLabel.setLabelFor(accountnameTextField);
        defaultAccountCheckBox = new CheckBoxWithMnemonic(MailResourceLoader.getString(
                    "dialog", "account", "defaultaccount"));

        nameLabel = new LabelWithMnemonic(MailResourceLoader.getString(
                    "dialog", "account", "yourname"));

        nameTextField = new JTextField();
        nameLabel.setLabelFor(nameTextField);
        addressLabel = new LabelWithMnemonic(MailResourceLoader.getString(
                    "dialog", "account", "address"));

        addressTextField = new JTextField();
        addressLabel.setLabelFor(addressTextField);
        replyaddressLabel = new LabelWithMnemonic(MailResourceLoader.getString(
                    "dialog", "account", "replytoaddress"));

        organisationLabel = new LabelWithMnemonic(MailResourceLoader.getString(
                    "dialog", "account", "organisation"));

        replyaddressTextField = new JTextField();
        replyaddressLabel.setLabelFor(replyaddressTextField);
        organisationTextField = new JTextField();
        organisationLabel.setLabelFor(organisationTextField);

        attachsignatureCheckBox = new CheckBoxWithMnemonic(MailResourceLoader.getString(
                    "dialog", "account", "attachthissignature"));

        selectSignatureButton = new JButton("~/.signature");
        selectSignatureButton.setActionCommand("CHOOSE");
        selectSignatureButton.addActionListener(this);
    }

    protected void layoutComponents() {
        // Create a FormLayout instance.
        FormLayout layout = new FormLayout("10dlu, max(70dlu;default), 3dlu, fill:max(150dlu;default):grow",
                
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
                "account", "account_information"));
        builder.nextLine();

        builder.append(accountnameLabel, 1);
        builder.append(accountnameTextField);
        builder.nextLine();

        builder.append(defaultAccountCheckBox, 3);
        builder.nextLine();

        builder.appendSeparator(MailResourceLoader.getString("dialog",
                "account", "needed_information"));
        builder.nextLine();

        builder.append(nameLabel, 1);
        builder.append(nameTextField);
        builder.nextLine();

        builder.append(addressLabel, 1);
        builder.append(addressTextField);
        builder.nextLine();

        builder.appendSeparator(MailResourceLoader.getString("dialog",
                "account", "optional_information"));
        builder.nextLine();

        builder.append(organisationLabel, 1);
        builder.append(organisationTextField);
        builder.nextLine();

        builder.append(replyaddressLabel, 1);
        builder.append(replyaddressTextField);
        builder.nextLine(1);
        

        JPanel panel = new JPanel();
        FormLayout l = new FormLayout("max(100;default), 3dlu, left:max(50dlu;default)",
                
            // 2 columns
            "fill:default:grow"); // rows are added dynamically (no need to define them here)

        // create a form builder
        DefaultFormBuilder b = new DefaultFormBuilder(panel, l);
        
        b.append(attachsignatureCheckBox, selectSignatureButton);

        //b.append(selectSignatureButton);
        builder.append(panel, 3);
        builder.nextLine();

        /*
         * JPanel innerPanel = builder.getPanel();
         * FormDebugUtils.dumpAll(innerPanel); setLayout(new BorderLayout());
         * add(innerPanel, BorderLayout.CENTER);
         */
        /*
         * setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
         *
         * GridBagLayout mainLayout = new GridBagLayout(); GridBagConstraints
         * mainConstraints = new GridBagConstraints();
         *
         * mainConstraints.anchor = GridBagConstraints.NORTHWEST;
         * mainConstraints.fill = GridBagConstraints.HORIZONTAL;
         * mainConstraints.weightx = 1.0;
         *
         * setLayout(mainLayout);
         *
         * JPanel accountPanel = new JPanel(); Border b1 =
         * BorderFactory.createEtchedBorder(); Border b2 =
         * BorderFactory.createTitledBorder( b1, MailResourceLoader.getString(
         * "dialog", "account", "account_information"));
         *
         * Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
         * Border border = BorderFactory.createCompoundBorder(b2, emptyBorder);
         * accountPanel.setBorder(border);
         *
         * GridBagLayout layout = new GridBagLayout(); GridBagConstraints c =
         * new GridBagConstraints(); accountPanel.setLayout(layout);
         *
         * //defaultAccountCheckBox.setEnabled(false);
         *
         * c.fill = GridBagConstraints.HORIZONTAL; c.anchor =
         * GridBagConstraints.NORTHWEST; c.weightx = 0.1; c.gridwidth =
         * GridBagConstraints.RELATIVE; layout.setConstraints(accountnameLabel,
         * c); accountPanel.add(accountnameLabel);
         *
         * c.gridwidth = GridBagConstraints.REMAINDER; c.weightx = 0.9;
         * layout.setConstraints(accountnameTextField, c);
         * accountPanel.add(accountnameTextField);
         *
         * c.gridwidth = GridBagConstraints.REMAINDER;
         * layout.setConstraints(defaultAccountCheckBox, c);
         * accountPanel.add(defaultAccountCheckBox);
         *
         * mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
         *
         * mainLayout.setConstraints(accountPanel, mainConstraints);
         * add(accountPanel);
         *
         * JPanel neededPanel = new JPanel(); b1 =
         * BorderFactory.createEtchedBorder(); b2 =
         * BorderFactory.createTitledBorder( b1, MailResourceLoader.getString(
         * "dialog", "account", "needed_information"));
         *
         * emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5); border =
         * BorderFactory.createCompoundBorder(b2, emptyBorder);
         * neededPanel.setBorder(border);
         *
         * layout = new GridBagLayout(); c = new GridBagConstraints();
         * neededPanel.setLayout(layout);
         *
         * c.fill = GridBagConstraints.HORIZONTAL; c.anchor =
         * GridBagConstraints.NORTHWEST; c.weightx = 0.1;
         *
         * c.gridwidth = GridBagConstraints.RELATIVE;
         * layout.setConstraints(nameLabel, c); neededPanel.add(nameLabel);
         *
         * c.gridwidth = GridBagConstraints.REMAINDER; c.weightx = 0.9;
         * layout.setConstraints(nameTextField, c);
         * neededPanel.add(nameTextField);
         *
         * c.gridwidth = GridBagConstraints.RELATIVE; c.weightx = 0.1;
         * layout.setConstraints(addressLabel, c);
         * neededPanel.add(addressLabel);
         *
         * c.gridwidth = GridBagConstraints.REMAINDER; c.weightx = 0.9;
         * layout.setConstraints(addressTextField, c);
         * neededPanel.add(addressTextField);
         *
         * mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
         *
         * mainLayout.setConstraints(neededPanel, mainConstraints);
         * add(neededPanel);
         *
         * JPanel optionalPanel = new JPanel(); b1 =
         * BorderFactory.createEtchedBorder(); b2 =
         * BorderFactory.createTitledBorder( b1, MailResourceLoader.getString(
         * "dialog", "account", "optional_information"));
         *
         * emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5); border =
         * BorderFactory.createCompoundBorder(b2, emptyBorder);
         *
         * optionalPanel.setBorder(border);
         *
         * layout = new GridBagLayout(); c = new GridBagConstraints();
         * optionalPanel.setLayout(layout);
         *
         * c.fill = GridBagConstraints.HORIZONTAL; c.anchor =
         * GridBagConstraints.NORTHWEST; c.weightx = 0.1;
         *
         * c.gridwidth = GridBagConstraints.RELATIVE;
         * layout.setConstraints(replyaddressLabel, c);
         * optionalPanel.add(replyaddressLabel);
         *
         * c.gridwidth = GridBagConstraints.REMAINDER; c.weightx = 0.9;
         * layout.setConstraints(replyaddressTextField, c);
         * optionalPanel.add(replyaddressTextField);
         *
         * c.gridwidth = GridBagConstraints.RELATIVE; c.weightx = 0.1;
         * layout.setConstraints(organisationLabel, c);
         * optionalPanel.add(organisationLabel);
         *
         * c.gridwidth = GridBagConstraints.REMAINDER; c.weightx = 0.9;
         * layout.setConstraints(organisationTextField, c);
         * optionalPanel.add(organisationTextField);
         *
         * c.gridwidth = GridBagConstraints.RELATIVE; c.weightx = 0.1;
         * layout.setConstraints(attachsignatureCheckBox, c);
         * optionalPanel.add(attachsignatureCheckBox);
         *
         * c.gridwidth = GridBagConstraints.REMAINDER; c.weightx = 0.9;
         * layout.setConstraints(selectSignatureButton, c);
         * optionalPanel.add(selectSignatureButton);
         *
         * mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
         * mainLayout.setConstraints(optionalPanel, mainConstraints);
         * add(optionalPanel);
         *
         * mainConstraints.gridheight = GridBagConstraints.REMAINDER;
         * mainConstraints.weighty = 1.0; mainConstraints.fill =
         * GridBagConstraints.VERTICAL; Component vglue =
         * Box.createVerticalGlue(); mainLayout.setConstraints(vglue,
         * mainConstraints); add(vglue);
         */
    }

    public boolean isFinished() {
        String address = addressTextField.getText();
        if (accountnameTextField.getText().length() == 0) {
            JOptionPane.showMessageDialog(this,
                MailResourceLoader.getString("dialog", "account", "namelabel"));
            return false;
        } else if (address.length() == 0) {
            JOptionPane.showMessageDialog(this,
                MailResourceLoader.getString("dialog", "account", "addresslabel"));
            return false;
        } else {
            try {
                Address.parse(address);
            } catch (ParserException pe) {
                JOptionPane.showMessageDialog(this,
                    MailResourceLoader.getString("dialog", "account", "invalidaddress"));
                return false;
            }
        }
        return true;
    }

    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        if (action.equals("CHOOSE")) {
            JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                selectSignatureButton.setText(file.getPath());
            }
        }
    }
}
