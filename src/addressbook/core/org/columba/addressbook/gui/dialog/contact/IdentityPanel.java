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
package org.columba.addressbook.gui.dialog.contact;

import com.jgoodies.forms.debug.FormDebugUtils;
import com.jgoodies.forms.layout.FormLayout;

import org.columba.addressbook.folder.ContactCard;
import org.columba.addressbook.util.AddressbookResourceLoader;

import org.columba.core.gui.util.DefaultFormBuilder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class IdentityPanel extends JPanel implements ActionListener {
    JButton nameButton;
    JTextField nameTextField;
    JLabel organisationLabel;
    JTextField organisationTextField;
    JLabel positionLabel;
    JTextField positionTextField;
    JLabel nickNameLabel;
    JTextField nickNameTextField;
    JLabel displayNameLabel;
    JTextField displayNameTextField;
    JLabel urlLabel;
    JTextField urlTextField;
    AttributComboBox emailComboBox;
    JTextField emailTextField;
    List emailList;
    public FullNameDialog dialog;

    public IdentityPanel() {
        initComponent();
    }

    public void setFn(String s) {
        nameTextField.setText(s);
    }

    public boolean fnIsEmpty() {
        return nameTextField.getText().length() == 0;
    }

    protected void set(ContactCard card, String key, JTextField textField) {
        String value = card.get(key);

        if (value != null) {
            textField.setText(value);
        }
    }

    protected void get(ContactCard card, String key, JTextField textField) {
        card.set(key, textField.getText());
    }

    public void updateComponents(ContactCard card, boolean b) {
        emailComboBox.updateComponents(card, b);

        if (b == true) {
            nameTextField.setText(card.formatGet("fn")); //$NON-NLS-1$
            organisationTextField.setText(card.get("org")); //$NON-NLS-1$
            displayNameTextField.setText(card.get("displayname")); //$NON-NLS-1$
            nickNameTextField.setText(card.get("nickname")); //$NON-NLS-1$
            positionTextField.setText(card.get("role")); //$NON-NLS-1$
            urlTextField.setText(card.get("url")); //$NON-NLS-1$
        } else {
            card.formatSet("fn", nameTextField.getText()); //$NON-NLS-1$
            card.set("org", organisationTextField.getText()); //$NON-NLS-1$
            card.set("displayname", displayNameTextField.getText()); //$NON-NLS-1$
            card.set("nickname", nickNameTextField.getText()); //$NON-NLS-1$
            card.set("role", positionTextField.getText()); //$NON-NLS-1$
            card.set("url", urlTextField.getText()); //$NON-NLS-1$
        }
    }

    protected void initComponent() {
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        FormLayout layout = new FormLayout("right:default, 3dlu, default:grow",
                "");

        DefaultFormBuilder b = new DefaultFormBuilder(this, layout);
        b.setRowGroupingEnabled(true);

        //LOCALIZE
        nameButton = new JButton(AddressbookResourceLoader.getString("dialog",
                    "contact", "full_name")); //$NON-NLS-1$
        nameButton.setActionCommand("NAME"); //$NON-NLS-1$
        nameButton.addActionListener(this);
        nameTextField = new JTextField(20);
        b.append(nameButton);
        b.append(nameTextField);

        //LOCALIZE
        nickNameLabel = new JLabel(AddressbookResourceLoader.getString(
                    "dialog", "contact", "nickname")); //$NON-NLS-1$
        nickNameTextField = new JTextField(20);
        b.append(nickNameLabel);
        b.append(nickNameTextField);

        //LOCALIZE
        displayNameLabel = new JLabel(AddressbookResourceLoader.getString(
                    "dialog", "contact", "sorting_displayname")); //$NON-NLS-1$
        displayNameTextField = new JTextField(20);
        b.append(displayNameLabel);
        b.append(displayNameTextField);

        //b.appendSeparator();
        //LOCALIZE
        positionLabel = new JLabel(AddressbookResourceLoader.getString(
                    "dialog", "contact", "position")); //$NON-NLS-1$
        positionTextField = new JTextField(20);
        b.append(positionLabel);
        b.append(positionTextField);

        //LOCALIZE
        organisationLabel = new JLabel(AddressbookResourceLoader.getString(
                    "dialog", "contact", "organisation")); //$NON-NLS-1$
        organisationTextField = new JTextField(20);
        b.append(organisationLabel);
        b.append(organisationTextField);

        //b.appendSeparator();
        //LOCALIZE
        urlLabel = new JLabel(AddressbookResourceLoader.getString("dialog",
                    "contact", "website")); //$NON-NLS-1$
        urlTextField = new JTextField(20);
        b.append(urlLabel);
        b.append(urlTextField);

        emailList = new Vector();
        emailList.add("internet"); //$NON-NLS-1$
        emailList.add("x400"); //$NON-NLS-1$
        emailList.add("x-email2"); //$NON-NLS-1$
        emailList.add("x-email3"); //$NON-NLS-1$
        emailTextField = new JTextField(20);
        emailComboBox = new AttributComboBox("email", emailList, emailTextField); //$NON-NLS-1$
        b.append(emailComboBox);
        b.append(emailTextField);

        FormDebugUtils.dumpAll(this);
    }

    public void actionPerformed(ActionEvent ev) {
        String action = ev.getActionCommand();

        if (action.equals("NAME")) { //$NON-NLS-1$
            dialog.setVisible(true);
        }
    }
}
