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

package org.columba.addressbook.gui.dialog.contact;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

import org.columba.addressbook.model.Contact;
import org.columba.addressbook.model.VCARD;
import org.columba.addressbook.util.AddressbookResourceLoader;
import org.columba.core.gui.util.ButtonWithMnemonic;
import org.columba.core.gui.util.LabelWithMnemonic;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A dialog for editing a contact's full name.
 */
public class FullNameDialog extends JDialog implements ActionListener {
    private JLabel titleLabel;
    private JTextField titleTextField;
    private JLabel fornameLabel;
    private JTextField fornameTextField;
    private JLabel middlenameLabel;
    private JTextField middlenameTextField;
    private JLabel lastnameLabel;
    private JTextField lastnameTextField;
    private JLabel suffixLabel;
    private JTextField suffixTextField;
    private JButton okButton;
    private JButton cancelButton;
    private IdentityPanel identityPanel;
    
    private Contact contact;
    
    public FullNameDialog(JDialog frame, IdentityPanel identityPanel, Contact contact) {
        super(frame, AddressbookResourceLoader.getString("dialog", "contact",
            "edit_fullname"), true);
        
        this.identityPanel = identityPanel;
        this.contact = contact;
        
        initComponents();
        layoutComponents();
        
        pack();
        setLocationRelativeTo(null);
        updateComponents(true);
    }
    
    public void updateComponents(boolean b) {
        if (b) {
            titleTextField.setText(contact.get(VCARD.N_PREFIX));
            lastnameTextField.setText(contact.get(VCARD.N_FAMILY));
            fornameTextField.setText(contact.get(VCARD.N_GIVEN));
            middlenameTextField.setText(contact.get(VCARD.N_ADDITIONALNAMES));
            suffixTextField.setText(contact.get(VCARD.N_SUFFIX));
        } else {
            contact.set(VCARD.N_PREFIX, titleTextField.getText());
            contact.set(VCARD.N_FAMILY, lastnameTextField.getText());
            contact.set(VCARD.N_GIVEN, fornameTextField.getText());
            contact.set(VCARD.N_ADDITIONALNAMES, middlenameTextField.getText());
            contact.set(VCARD.N_SUFFIX, suffixTextField.getText());
        }
    }
    
    protected void layoutComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        
        FormLayout layout = new FormLayout("right:default, 3dlu, default:grow",
        "");
        
        DefaultFormBuilder b = new DefaultFormBuilder(mainPanel, layout);
        b.setRowGroupingEnabled(true);
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        
        b.append(titleLabel);
        b.append(titleTextField);
        
        b.append(fornameLabel);
        b.append(fornameTextField);
        
        b.append(middlenameLabel);
        b.append(middlenameTextField);
        
        b.append(lastnameLabel);
        b.append(lastnameTextField);
        
        b.append(suffixLabel);
        b.append(suffixTextField);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        
        cancelButton = new ButtonWithMnemonic(AddressbookResourceLoader.getString(
            null, "cancel"));
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);
        okButton = new ButtonWithMnemonic(AddressbookResourceLoader.getString(
            null, "ok"));
        okButton.addActionListener(this);
        buttonPanel.add(okButton);
        
        getRootPane().registerKeyboardAction(this, "CANCEL",
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        getRootPane().setDefaultButton(okButton);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
    }
    
    protected void initComponents() {
        titleLabel = new LabelWithMnemonic(AddressbookResourceLoader.getString(
            "dialog", "contact", "title"));
        titleTextField = new JTextField(20);
        titleLabel.setLabelFor(titleTextField);
        fornameLabel = new LabelWithMnemonic(AddressbookResourceLoader.getString(
            "dialog", "contact", "first_name"));
        fornameTextField = new JTextField(20);
        fornameLabel.setLabelFor(fornameTextField);
        middlenameLabel = new LabelWithMnemonic(AddressbookResourceLoader.getString(
            "dialog", "contact", "middle_name"));
        middlenameTextField = new JTextField(20);
        middlenameLabel.setLabelFor(middlenameTextField);
        lastnameLabel = new LabelWithMnemonic(AddressbookResourceLoader.getString(
            "dialog", "contact", "last_name"));
        lastnameTextField = new JTextField(20);
        lastnameLabel.setLabelFor(lastnameTextField);
        suffixLabel = new LabelWithMnemonic(AddressbookResourceLoader.getString(
            "dialog", "contact", "suffix"));
        suffixTextField = new JTextField(20);
        suffixLabel.setLabelFor(suffixTextField);
    }
    
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == okButton) {
            setFormattedName();
            updateComponents(false);
        }
        setVisible(false);
    }
    
    protected void setFormattedName() {
        StringBuffer buf = new StringBuffer();
        
        if (titleTextField.getText().length() > 0) {
            buf.append(titleTextField.getText() + " ");
        }
        
        if (fornameTextField.getText().length() > 0) {
            buf.append(fornameTextField.getText() + " ");
        }
        
        if (middlenameTextField.getText().length() > 0) {
            buf.append(middlenameTextField.getText() + " ");
        }
        
        if (lastnameTextField.getText().length() > 0) {
            buf.append(lastnameTextField.getText() + " ");
        }
        
        if (suffixTextField.getText().length() > 0) {
            buf.append(suffixTextField.getText() + " ");
        }
        
        identityPanel.setFn(buf.toString());
    }
}
