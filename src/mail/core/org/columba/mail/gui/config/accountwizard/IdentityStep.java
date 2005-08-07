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

package org.columba.mail.gui.config.accountwizard;

import java.lang.reflect.Method;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.javaprog.ui.wizard.AbstractStep;
import net.javaprog.ui.wizard.DataLookup;
import net.javaprog.ui.wizard.DataModel;
import net.javaprog.ui.wizard.DefaultDataLookup;

import org.columba.core.gui.base.LabelWithMnemonic;
import org.columba.core.gui.base.MultiLineLabel;
import org.columba.core.gui.base.WizardTextField;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.parser.ParserException;

class IdentityStep extends AbstractStep {
    protected DataModel data;
    protected JTextField nameTextField;
    protected JTextField addressTextField;
    protected JTextField accountNameTextField;

    public IdentityStep(DataModel data) {
        super(MailResourceLoader.getString("dialog", "accountwizard", "identity"),
            MailResourceLoader.getString("dialog", "accountwizard",
                "identity_description"));
        this.data = data;
        setCanGoNext(false);
    }

    protected JComponent createComponent() {
        JComponent component = new JPanel();
        component.setLayout(new BoxLayout(component, BoxLayout.Y_AXIS));
        component.add(new MultiLineLabel(MailResourceLoader.getString(
                    "dialog", "accountwizard", "identity_text")));
        component.add(Box.createVerticalStrut(40));

        WizardTextField middlePanel = new WizardTextField();

        LabelWithMnemonic nameLabel = new LabelWithMnemonic(MailResourceLoader.getString(
                    "dialog", "accountwizard", "name"));
        middlePanel.addLabel(nameLabel);
        nameTextField = new JTextField();

        DocumentListener fieldListener = new DocumentListener() {
                public void removeUpdate(DocumentEvent e) {
                    checkFields();
                }

                public void insertUpdate(DocumentEvent e) {
                    checkFields();
                }

                protected void checkFields() {
                    String s = addressTextField.getText();
                    setCanGoNext((nameTextField.getDocument().getLength() > 0) &&
                        (s.length() > 0) && isEmailAddress(s) &&
                        (accountNameTextField.getDocument().getLength() > 0));
                }

                protected boolean isEmailAddress(String s) {
                    int at = s.indexOf('@');
                    int lastDot = s.lastIndexOf('.');

                    return (at > 0) && (lastDot > (at + 1)) &&
                    (lastDot < (s.length() - 1));
                }

                public void changedUpdate(DocumentEvent e) {
                }
            };

        nameTextField.getDocument().addDocumentListener(fieldListener);
        nameLabel.setLabelFor(nameTextField);
        middlePanel.addTextField(nameTextField);

        JLabel exampleLabel = new JLabel(MailResourceLoader.getString(
                    "dialog", "accountwizard", "example") + "Bill Gates");
        middlePanel.addExample(exampleLabel);

        LabelWithMnemonic addressLabel = new LabelWithMnemonic(MailResourceLoader.getString(
                    "dialog", "accountwizard", "address"));
        middlePanel.addLabel(addressLabel);
        addressTextField = new JTextField();
        addressTextField.getDocument().addDocumentListener(fieldListener);
        addressLabel.setLabelFor(addressTextField);
        middlePanel.addTextField(addressTextField);
        middlePanel.addExample(new JLabel(MailResourceLoader.getString(
                    "dialog", "accountwizard", "example") +
                "BillGates@microsoft.com"));

        LabelWithMnemonic accountNameLabel = new LabelWithMnemonic(MailResourceLoader.getString(
                    "dialog", "accountwizard", "accountname"));
        middlePanel.addLabel(accountNameLabel);
        accountNameTextField = new JTextField();
        Method method = null;
        try {
            method = accountNameTextField.getClass().getMethod("getText", null);
        } catch (NoSuchMethodException nsme) {}
        data.registerDataLookup("Identity.accountName",
            new DefaultDataLookup(accountNameTextField, method, null));
        accountNameTextField.getDocument().addDocumentListener(fieldListener);
        accountNameLabel.setLabelFor(accountNameTextField);
        middlePanel.addTextField(accountNameTextField);
        middlePanel.addExample(new JLabel(MailResourceLoader.getString(
                    "dialog", "accountwizard", "example") +
                "Bill's private mail"));
        component.add(middlePanel);

        data.registerDataLookup("Identity.address", new DataLookup() {
            public Object lookupData() {
                try {
                    Address address = Address.parse(addressTextField.getText());
                    address.setDisplayName(nameTextField.getText());
                    return address;
                } catch (ParserException pe) {
                    return null;
                }
            }
        });
        return component;
    }

    public void prepareRendering() {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    nameTextField.requestFocus();
                }
            });
    }
}
