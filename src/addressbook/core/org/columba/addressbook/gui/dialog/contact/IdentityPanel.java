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
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.columba.addressbook.model.Contact;
import org.columba.addressbook.model.VCARD;
import org.columba.addressbook.util.AddressbookResourceLoader;
import org.columba.core.gui.util.DefaultFormBuilder;

import com.jgoodies.forms.layout.FormLayout;


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
    private List emailList;
    
    
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
        emailComboBox.updateComponents( b);

        if (b == true) {
            nameTextField.setText(contact.formatGet(VCARD.FN)); //$NON-NLS-1$
            
            organisationTextField.setText(contact.get(VCARD.ORG)); //$NON-NLS-1$
            displayNameTextField.setText(contact.get(VCARD.DISPLAYNAME)); //$NON-NLS-1$
            nickNameTextField.setText(contact.get(VCARD.NICKNAME)); //$NON-NLS-1$
            positionTextField.setText(contact.get(VCARD.ROLE)); //$NON-NLS-1$
            urlTextField.setText(contact.get(VCARD.URL)); //$NON-NLS-1$
        } else {
        	contact.formatSet(VCARD.FN, nameTextField.getText()); //$NON-NLS-1$
            
        	contact.set(VCARD.ORG, organisationTextField.getText()); //$NON-NLS-1$
        	contact.set(VCARD.DISPLAYNAME, displayNameTextField.getText()); //$NON-NLS-1$
        	contact.set(VCARD.NICKNAME, nickNameTextField.getText()); //$NON-NLS-1$
        	contact.set(VCARD.ROLE, positionTextField.getText()); //$NON-NLS-1$
        	contact.set(VCARD.URL, urlTextField.getText()); //$NON-NLS-1$
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
    
        //LOCALIZE
        nameButton = new JButton(AddressbookResourceLoader.getString("dialog",
                    "contact", "full_name")); //$NON-NLS-1$
        nameButton.setActionCommand("NAME"); //$NON-NLS-1$
        nameButton.addActionListener(this);
        nameTextField = new JTextField(20);
       

        //LOCALIZE
        nickNameLabel = new JLabel(AddressbookResourceLoader.getString(
                    "dialog", "contact", "nickname")); //$NON-NLS-1$
        nickNameTextField = new JTextField(20);
        

        //LOCALIZE
        displayNameLabel = new JLabel(AddressbookResourceLoader.getString(
                    "dialog", "contact", "sorting_displayname")); //$NON-NLS-1$
        displayNameTextField = new JTextField(20);
        

        //b.appendSeparator();
        //LOCALIZE
        positionLabel = new JLabel(AddressbookResourceLoader.getString(
                    "dialog", "contact", "position")); //$NON-NLS-1$
        positionTextField = new JTextField(20);
       

        //LOCALIZE
        organisationLabel = new JLabel(AddressbookResourceLoader.getString(
                    "dialog", "contact", "organisation")); //$NON-NLS-1$
        organisationTextField = new JTextField(20);
      

        //b.appendSeparator();
        //LOCALIZE
        urlLabel = new JLabel(AddressbookResourceLoader.getString("dialog",
                    "contact", "website")); //$NON-NLS-1$
        urlTextField = new JTextField(20);
     
        emailList = new Vector();
        emailList.add(VCARD.EMAIL_TYPE_INTERNET); //$NON-NLS-1$
        emailList.add(VCARD.EMAIL_TYPE_X400); //$NON-NLS-1$
        emailList.add(VCARD.EMAIL_TYPE_PREF); //$NON-NLS-1$
        emailTextField = new JTextField(20);
        emailComboBox = new AttributComboBox(VCARD.EMAIL, emailList, emailTextField, contact); //$NON-NLS-1$
        
    }

    public void actionPerformed(ActionEvent ev) {
        String action = ev.getActionCommand();

        if (action.equals("NAME")) { //$NON-NLS-1$
            dialog.setVisible(true);
        }
    }
}
