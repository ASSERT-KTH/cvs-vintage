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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.columba.addressbook.model.Contact;
import org.columba.addressbook.model.VCARD;
import org.columba.addressbook.util.AddressbookResourceLoader;
import org.columba.core.gui.util.ButtonWithMnemonic;
import org.columba.core.gui.util.LabelWithMnemonic;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The panel shown in the ContactDialog.
 */
public class IdentityPanel extends JPanel implements ActionListener {
    private JButton nameButton;
    private JTextField nameTextField;
    private JLabel organisationLabel;
    private JTextField organisationTextField;
    private JLabel positionLabel;
    private JTextField positionTextField;
    private JLabel nickNameLabel;
    private JTextField nickNameTextField;
    private JLabel displayNameLabel;
    private JTextField displayNameTextField;
    private JLabel urlLabel;
    private JTextField urlTextField;
    private AttributComboBox emailComboBox;
    private JTextField emailTextField;
    
    public FullNameDialog dialog;
    
    private Contact contact;

    public IdentityPanel(Contact contact) {
    	this.contact = contact;
    	
        initComponent();
        layoutComponents();
    }

    public void setFn(String s) {
        nameTextField.setText(s);
    }

    public boolean fnIsEmpty() {
        return nameTextField.getText().length() == 0;
    }

    private void set(Contact card, String key, JTextField textField) {
        String value = card.get(key);

        if (value != null) {
            textField.setText(value);
        }
    }

    private void get(Contact card, String key, JTextField textField) {
        card.set(key, textField.getText());
    }

    public void updateComponents(boolean b) {
        emailComboBox.updateComponents(b);
				
        if (b) {
            nameTextField.setText(contact.formatGet(VCARD.FN));
            
            organisationTextField.setText(contact.get(VCARD.ORG));
            displayNameTextField.setText(contact.get(VCARD.DISPLAYNAME));
            nickNameTextField.setText(contact.get(VCARD.NICKNAME));
            positionTextField.setText(contact.get(VCARD.ROLE));
            urlTextField.setText(contact.get(VCARD.URL));
        } else {
            contact.formatSet(VCARD.FN, nameTextField.getText());

            contact.set(VCARD.ORG, organisationTextField.getText());
            contact.set(VCARD.DISPLAYNAME, displayNameTextField.getText());
            contact.set(VCARD.NICKNAME, nickNameTextField.getText());
            contact.set(VCARD.ROLE, positionTextField.getText());
            contact.set(VCARD.URL, urlTextField.getText());
        }
    }

    protected void layoutComponents() {
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        FormLayout layout = new FormLayout("right:default, 3dlu, default:grow",
                "");

        DefaultFormBuilder b = new DefaultFormBuilder(this, layout);
        b.setRowGroupingEnabled(true);
        
        b.append(nameButton);
        b.append(nameTextField);
        
        b.append(nickNameLabel);
        b.append(nickNameTextField);
        
        b.append(displayNameLabel);
        b.append(displayNameTextField);
        
        b.append(positionLabel);
        b.append(positionTextField);
        
        b.append(organisationLabel);
        b.append(organisationTextField);
        
        b.append(urlLabel);
        b.append(urlTextField);

        b.append(emailComboBox);
        b.append(emailTextField);
    }
    
    protected void initComponent() {
        nameButton = new ButtonWithMnemonic(AddressbookResourceLoader.getString(
                    "dialog", "contact", "full_name"));
        nameButton.addActionListener(this);
        nameTextField = new JTextField(20);
       
        nickNameLabel = new LabelWithMnemonic(AddressbookResourceLoader.getString(
                    "dialog", "contact", "nickname"));
        nickNameTextField = new JTextField(20);
        nickNameLabel.setLabelFor(nickNameTextField);

        displayNameLabel = new LabelWithMnemonic(AddressbookResourceLoader.getString(
                    "dialog", "contact", "sorting_displayname"));
        displayNameTextField = new JTextField(20);
        displayNameLabel.setLabelFor(displayNameTextField);
        
        //b.appendSeparator();
        positionLabel = new LabelWithMnemonic(AddressbookResourceLoader.getString(
                    "dialog", "contact", "position"));
        positionTextField = new JTextField(20);
        positionLabel.setLabelFor(positionTextField);

        organisationLabel = new LabelWithMnemonic(AddressbookResourceLoader.getString(
                    "dialog", "contact", "organisation"));
        organisationTextField = new JTextField(20);
        organisationLabel.setLabelFor(organisationTextField);

        //b.appendSeparator();
        urlLabel = new LabelWithMnemonic(AddressbookResourceLoader.getString("dialog",
                    "contact", "website"));
        urlTextField = new JTextField(20);
        urlLabel.setLabelFor(urlTextField);
     
        Vector emailList = new Vector(3);
        emailList.add(VCARD.EMAIL_TYPE_INTERNET);
        emailList.add(VCARD.EMAIL_TYPE_X400);
        emailList.add(VCARD.EMAIL_TYPE_PREF);
        emailTextField = new JTextField(20);
        emailComboBox = new AttributComboBox(VCARD.EMAIL, emailList, emailTextField,
            contact);
    }

    public void actionPerformed(ActionEvent ev) {
        if (ev.getSource() == nameButton) {
            dialog.setVisible(true);
        }
    }
}
