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

package org.columba.mail.gui.tree.util;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.*;

import org.columba.core.gui.util.DialogStore;
import org.columba.mail.util.MailResourceLoader;

public class EditFolderDialog implements ActionListener {
    protected String name;
    protected JDialog dialog;
    protected boolean bool = false;
    protected JTextField textField;
    protected JButton okButton;
    
    public EditFolderDialog() {
        this(MailResourceLoader.getString("dialog", "folder", "new_folder_name"));
    }
    
    public EditFolderDialog(String name) {
        this.name = name;
    }
    
    public void showDialog() {
        dialog = DialogStore.getDialog(MailResourceLoader.getString(
                "dialog",
                "folder",
                "edit_name"));
        
        JPanel contentPane = (JPanel)dialog.getContentPane();
        contentPane.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
        JLabel label2 = new JLabel(MailResourceLoader.getString("dialog", "folder", "name"));
        centerPanel.add(label2);
        centerPanel.add(Box.createHorizontalStrut(5));
        textField = new JTextField(name, 15);
        centerPanel.add(textField);
        label2.setLabelFor(textField);
        contentPane.add(centerPanel);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(17, 0, 0, 0));
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        okButton = new JButton(MailResourceLoader.getString("", "ok"));
        okButton.setActionCommand("OK");
        okButton.addActionListener(this);
        buttonPanel.add(okButton);
        dialog.getRootPane().setDefaultButton(okButton);
        JButton cancelButton = new JButton(MailResourceLoader.getString("", "cancel"));
        cancelButton.setActionCommand("CANCEL");
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        contentPane.add(bottomPanel, BorderLayout.SOUTH);
        dialog.getRootPane().registerKeyboardAction(this, "CANCEL",
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
    
    public String getName() {
        return name;
    }
    
    public boolean success() {
        return bool;
    }
    
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        
        if (action.equals("OK")) {
            name = textField.getText().trim();
            // fixing bug with id 553176
            if (name.indexOf('/') != -1) {
                // if the character / is found shows the user a error message
                JOptionPane.showMessageDialog(
                    dialog,
                    MailResourceLoader.getString("dialog", "folder", "error_char_text"),
                    MailResourceLoader.getString("dialog", "folder", "error_char_title"),
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            bool = true;
            dialog.dispose();
        } else if (action.equals("CANCEL")) {
            bool = false;
            
            dialog.dispose();
        }
    }
}
