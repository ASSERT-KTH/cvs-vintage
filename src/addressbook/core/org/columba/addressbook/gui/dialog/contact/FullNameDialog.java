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

import com.jgoodies.forms.layout.FormLayout;

import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

import org.columba.addressbook.folder.ContactCard;
import org.columba.addressbook.util.AddressbookResourceLoader;

import org.columba.core.gui.util.DefaultFormBuilder;

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


public class FullNameDialog extends JDialog implements ActionListener {
    JLabel titleLabel;
    JTextField titleTextField;
    JLabel fornameLabel;
    JTextField fornameTextField;
    JLabel middlenameLabel;
    JTextField middlenameTextField;
    JLabel lastnameLabel;
    JTextField lastnameTextField;
    JLabel suffixLabel;
    JTextField suffixTextField;
    JButton okButton;
    JButton changeButton;
    IdentityPanel identityPanel;

    public FullNameDialog(JDialog frame, IdentityPanel identityPanel) {
        super(frame, true);
        this.identityPanel = identityPanel;
        initComponents();
        pack();
        setLocationRelativeTo(null);
    }

    public void updateComponents(ContactCard card, boolean b) {
        if (b) {
            titleTextField.setText(card.get("n", "prefix")); //$NON-NLS-1$ //$NON-NLS-2$
            lastnameTextField.setText(card.get("n", "family")); //$NON-NLS-1$ //$NON-NLS-2$
            fornameTextField.setText(card.get("n", "given")); //$NON-NLS-1$ //$NON-NLS-2$
            middlenameTextField.setText(card.get("n", "middle")); //$NON-NLS-1$ //$NON-NLS-2$
            suffixTextField.setText(card.get("n", "suffix")); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            card.set("n", "prefix", titleTextField.getText()); //$NON-NLS-1$ //$NON-NLS-2$
            card.set("n", "family", lastnameTextField.getText()); //$NON-NLS-1$ //$NON-NLS-2$
            card.set("n", "given", fornameTextField.getText()); //$NON-NLS-1$ //$NON-NLS-2$
            card.set("n", "middle", middlenameTextField.getText()); //$NON-NLS-1$ //$NON-NLS-2$
            card.set("n", "suffix", suffixTextField.getText()); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        FormLayout layout = new FormLayout("right:default, 3dlu, default:grow",
                "");

        DefaultFormBuilder b = new DefaultFormBuilder(mainPanel, layout);
        b.setRowGroupingEnabled(true);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mainPanel, BorderLayout.CENTER);

        titleLabel = new JLabel(AddressbookResourceLoader.getString("dialog",
                    "contact", "title")); //$NON-NLS-1$
        titleTextField = new JTextField(20);
        b.append(titleLabel);
        b.append(titleTextField);

        fornameLabel = new JLabel(AddressbookResourceLoader.getString(
                    "dialog", "contact", "first_name")); //$NON-NLS-1$
        fornameTextField = new JTextField(20);
        b.append(fornameLabel);
        b.append(fornameTextField);

        middlenameLabel = new JLabel(AddressbookResourceLoader.getString(
                    "dialog", "contact", "middle_name")); //$NON-NLS-1$
        middlenameTextField = new JTextField(20);
        b.append(middlenameLabel);
        b.append(middlenameTextField);

        lastnameLabel = new JLabel(AddressbookResourceLoader.getString(
                    "dialog", "contact", "last_name")); //$NON-NLS-1$
        lastnameTextField = new JTextField(20);
        b.append(lastnameLabel);
        b.append(lastnameTextField);

        suffixLabel = new JLabel(AddressbookResourceLoader.getString("dialog",
                    "contact", "suffix")); //$NON-NLS-1$
        suffixTextField = new JTextField(20);
        b.append(suffixLabel);
        b.append(suffixTextField);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(0, 2, 10, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        changeButton = new JButton(AddressbookResourceLoader.getString(
                    "dialog", "contact", "change_formatted_name")); //$NON-NLS-1$
        changeButton.setActionCommand("CHANGE"); //$NON-NLS-1$
        changeButton.addActionListener(this);
        buttonPanel.add(changeButton);
        okButton = new JButton("Close"); //$NON-NLS-1$
        okButton.setActionCommand("OK"); //$NON-NLS-1$
        okButton.addActionListener(this);
        buttonPanel.add(okButton);

        getRootPane().registerKeyboardAction(this, "OK", //$NON-NLS-1$
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().setDefaultButton(okButton);

        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();

        if (action.equals("OK")) { //$NON-NLS-1$
            setVisible(false);

            if (identityPanel.fnIsEmpty() == true) {
                setFormattedName();
            }
        } else if (action.equals("CHANGE")) { //$NON-NLS-1$
            setFormattedName();
        }
    }

    protected void setFormattedName() {
        StringBuffer buf = new StringBuffer();

        if (titleTextField.getText().length() > 0) {
            buf.append(titleTextField.getText() + " "); //$NON-NLS-1$
        }

        if (fornameTextField.getText().length() > 0) {
            buf.append(fornameTextField.getText() + " "); //$NON-NLS-1$
        }

        if (middlenameTextField.getText().length() > 0) {
            buf.append(middlenameTextField.getText() + " "); //$NON-NLS-1$
        }

        if (lastnameTextField.getText().length() > 0) {
            buf.append(lastnameTextField.getText() + " "); //$NON-NLS-1$
        }

        if (suffixTextField.getText().length() > 0) {
            buf.append(suffixTextField.getText() + " "); //$NON-NLS-1$
        }

        identityPanel.setFn(buf.toString());
    }
}
