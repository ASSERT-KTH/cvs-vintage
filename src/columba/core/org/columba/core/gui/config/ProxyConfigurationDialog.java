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

package org.columba.core.gui.config;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import org.columba.core.gui.util.*;
import org.columba.core.util.GlobalResourceLoader;

/**
 * A dialog for configurating the use of a proxy server.
 */
public class ProxyConfigurationDialog extends JDialog
implements ActionListener, DocumentListener {
    
    private static final String RESOURCE_PATH = "org.columba.core.i18n.dialog";
    public static final int CANCEL_OPTION = 0;
    public static final int APPROVE_OPTION = 1;
    
    protected int status = CANCEL_OPTION;
    protected JRadioButton directConnectionRadioButton;
    protected JRadioButton useProxyRadioButton;
    protected JLabel proxyHostLabel;
    protected JTextField proxyHostField;
    protected JLabel proxyPortLabel;
    protected JTextField proxyPortField;
    protected JButton okButton;
    
    /**
     * Creates a new dialog.
     */
    public ProxyConfigurationDialog(JFrame parent) {
        super(parent, GlobalResourceLoader.getString(
                RESOURCE_PATH, "proxy", "title"), true);
    }
    
    /**
     * Creates a new dialog.
     */
    public ProxyConfigurationDialog(JDialog parent) {
        super(parent, GlobalResourceLoader.getString(
                RESOURCE_PATH, "proxy", "title"), true);
    }
    
    protected void dialogInit() {
        super.dialogInit();
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                status = CANCEL_OPTION;
            }
        });
        JPanel contentPane = (JPanel)getContentPane();
        contentPane.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        ButtonGroup group = new ButtonGroup();
        directConnectionRadioButton =
                new RadioButtonWithMnemonic(GlobalResourceLoader.getString(
                RESOURCE_PATH, "proxy", "directConnection"));
        directConnectionRadioButton.setSelected(true);
        directConnectionRadioButton.setActionCommand("USE_PROXY");
        directConnectionRadioButton.addActionListener(this);
        group.add(directConnectionRadioButton);
        centerPanel.add(directConnectionRadioButton);
        centerPanel.add(Box.createVerticalStrut(5));
        useProxyRadioButton = 
                new RadioButtonWithMnemonic(GlobalResourceLoader.getString(
                RESOURCE_PATH, "proxy", "useProxy"));
        useProxyRadioButton.setActionCommand("USE_PROXY");
        useProxyRadioButton.addActionListener(this);
        group.add(useProxyRadioButton);
        centerPanel.add(useProxyRadioButton);
        JPanel proxyDataPanel = new JPanel(new GridBagLayout());
        proxyDataPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        GridBagConstraints c = new GridBagConstraints();
        proxyHostLabel = new LabelWithMnemonic(GlobalResourceLoader.getString(
                RESOURCE_PATH, "proxy", "host"));
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 10, 2, 0);
        proxyDataPanel.add(proxyHostLabel, c);
        proxyHostField = new JTextField(10);
        proxyHostField.getDocument().addDocumentListener(this);
        proxyHostLabel.setLabelFor(proxyHostField);
        c.gridwidth = GridBagConstraints.REMAINDER;
        proxyDataPanel.add(proxyHostField, c);
        proxyPortLabel = new LabelWithMnemonic(GlobalResourceLoader.getString(
                RESOURCE_PATH, "proxy", "port"));
        c.gridwidth = 1;
        proxyDataPanel.add(proxyPortLabel, c);
        proxyPortField = new JTextField(5);
        ((AbstractDocument)proxyPortField.getDocument()).setDocumentFilter(
                new DocumentFilter() {
                    public void insertString(DocumentFilter.FilterBypass fb,
                                int offset, String string, AttributeSet attr)
                                throws BadLocationException {
                        if (ensureDigits(string)) {
                            super.insertString(fb, offset, string, attr);
                        } else {
                            Toolkit.getDefaultToolkit().beep();
                        }
                    }
                    
                    public void replace(DocumentFilter.FilterBypass fb,
                            int offset, int length, String string, AttributeSet attr)
                            throws BadLocationException {
                        if (ensureDigits(string)) {
                            super.replace(fb, offset, length, string, attr);
                        } else {
                            Toolkit.getDefaultToolkit().beep();
                        }
                    }
                    
                    private boolean ensureDigits(String string) {
                        for (int i = 0; i < string.length(); i++) {
                            if (!Character.isDigit(string.charAt(i))) {
                                return false;
                            }
                        }
                        return true;
                    }
        });
        proxyPortField.getDocument().addDocumentListener(this);
        proxyPortLabel.setLabelFor(proxyPortField);
        c.gridwidth = GridBagConstraints.REMAINDER;
        proxyDataPanel.add(proxyPortField, c);
        centerPanel.add(proxyDataPanel);
        centerPanel.add(Box.createVerticalGlue());
        contentPane.add(centerPanel);
        JPanel southPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(17, 0, 0, 0));
        okButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(
                "", "global", "ok"));
        okButton.addActionListener(this);
        buttonPanel.add(okButton);
        JButton cancelButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(
                "", "global", "cancel"));
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);
        southPanel.add(buttonPanel, BorderLayout.EAST);
        contentPane.add(southPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(okButton);
        getRootPane().registerKeyboardAction(this, "CANCEL", 
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), 
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        pack();
        setLocationRelativeTo(null);
    }
    
    /**
     * Shows the dialog and returns an integer indicating how the dialog
     * was closed.
     */
    public int showDialog() {
        if (getProxyHost() != null && getProxyPort() > 0) {
            useProxyRadioButton.setSelected(true);
        } else {
            directConnectionRadioButton.setSelected(true);
        }
        updateProxyDataPanel();
        setVisible(true);
        return status;
    }
    
    /**
     * Returns the host name from the text field.
     */
    public String getProxyHost() {
        String text = proxyHostField.getText().trim();
        return text.length() == 0 ? null : text;
    }
    
    /**
     * Sets the value of the host name text field.
     */
    public void setProxyHost(String host) {
        proxyHostField.setText(host);
    }
    
    /**
     * Returns the port number from the text field.
     */
    public int getProxyPort() {
        String text = proxyPortField.getText();
        return text.length() == 0 ? -1 : Integer.parseInt(text);
    }
    
    /**
     * Sets the value of the port number text field.
     */
    public void setProxyPort(int port) {
        if (port > 0) {
            proxyPortField.setText(Integer.toString(port));
        }
    }
    
    /**
     * Updates the enabled state of the components depending on the selection.
     */
    protected void updateProxyDataPanel() {
        boolean enabled = useProxyRadioButton.isSelected();
        proxyHostLabel.setEnabled(enabled);
        proxyHostField.setEnabled(enabled);
        proxyPortLabel.setEnabled(enabled);
        proxyPortField.setEnabled(enabled);
        updateOkButton();
    }
    
    protected void updateOkButton() {
        okButton.setEnabled(directConnectionRadioButton.isSelected() || 
                (getProxyHost() != null && getProxyPort() > 0));
    }
    
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if ("USE_PROXY".equals(command)) {
            updateProxyDataPanel();
        } else if ("OK".equals(command)) {
            status = APPROVE_OPTION;
            if (directConnectionRadioButton.isSelected()) {
                proxyHostField.setText(null);
                proxyPortField.setText(null);
            }
            setVisible(false);
        } else {
            status = CANCEL_OPTION;
            setVisible(false);
        }
    }
    
    public void insertUpdate(DocumentEvent e) {
        updateOkButton();
    }
    
    public void removeUpdate(DocumentEvent e) {
        updateOkButton();
    }
    
    public void changedUpdate(DocumentEvent e) {}
}
