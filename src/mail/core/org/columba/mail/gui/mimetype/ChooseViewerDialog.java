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

package org.columba.mail.gui.mimetype;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.columba.core.gui.util.DialogStore;
import org.columba.mail.util.MailResourceLoader;

public class ChooseViewerDialog implements ActionListener {
        public static final String CMD_OK = "OK";
        public static final String CMD_CANCEL = "CANCEL";
        public static final String CMD_SEARCH = "SEARCH";

	private JTextField viewerName;
	private String viewer = null;
	private JCheckBox saveCButton;
        private JButton okButton;
	private JDialog dialog;

	public ChooseViewerDialog(
		String contentType,
		String contentSubtype,
		boolean save) {

		dialog = DialogStore.getDialog(MailResourceLoader.getString("dialog", "mimetypeviewer", "dialog_title") + " " + contentType + "/" + contentSubtype);
		init(save);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	private void init(boolean save) {
		JPanel contentPane = new JPanel(new BorderLayout(0, 0));
		contentPane.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
		JPanel viewerPanel = new JPanel();
		viewerPanel.setLayout(new BoxLayout(viewerPanel, BoxLayout.X_AXIS));
		JLabel label = new JLabel(MailResourceLoader.getString("dialog", "mimetypeviewer", "viewer_label"));
                label.setDisplayedMnemonic(MailResourceLoader.getMnemonic("dialog", "mimetypeviewer", "viewer_label"));
		viewerPanel.add(label);
		viewerPanel.add(Box.createHorizontalStrut(5));
		viewerName = new JTextField(15);
		viewerName.setActionCommand(CMD_OK);
		viewerName.addActionListener(this);
                viewerName.getDocument().addDocumentListener(new DocumentListener() {
                    public void insertUpdate(DocumentEvent e) {
                        refreshOkButton(e);
                    }
                    
                    public void removeUpdate(DocumentEvent e) {
                        refreshOkButton(e);
                    }
                    
                    protected void refreshOkButton(DocumentEvent e) {
                        okButton.setEnabled(e.getDocument().getLength() > 0);
                    }
                    
                    public void changedUpdate(DocumentEvent e) {}
                });
		label.setLabelFor(viewerName);
		viewerPanel.add(viewerName);
		viewerPanel.add(Box.createHorizontalStrut(10));
		JButton searchButton = new JButton("...");
		searchButton.addActionListener(this);
		searchButton.setActionCommand(CMD_SEARCH);
		viewerPanel.add(searchButton);
		contentPane.add(viewerPanel, BorderLayout.NORTH);
		saveCButton = new JCheckBox(MailResourceLoader.getString("dialog", "mimetypeviewer", "save_viewer"), save);
                saveCButton.setMnemonic(MailResourceLoader.getMnemonic("dialog", "mimetypeviewer", "save_viewer"));
		saveCButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 17, 0));
		contentPane.add(saveCButton, BorderLayout.CENTER);
		JPanel bottomPanel = new JPanel(new BorderLayout(0, 0));
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
		okButton = new JButton(MailResourceLoader.getString("global", "ok"));
		okButton.setActionCommand(CMD_OK);
		okButton.addActionListener(this);
                okButton.setEnabled(false);
		buttonPanel.add(okButton);
		JButton cancelButton = new JButton(MailResourceLoader.getString("global", "cancel"));
		cancelButton.setActionCommand(CMD_CANCEL);
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		contentPane.add(bottomPanel, BorderLayout.SOUTH);
		dialog.setContentPane(contentPane);
		dialog.getRootPane().setDefaultButton(okButton);
		dialog.getRootPane().registerKeyboardAction(
			this,
			CMD_CANCEL,
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	public String getViewer() {
		return viewer;
	}

	public boolean saveViewer() {
		return saveCButton.isSelected();
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command == CMD_SEARCH) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle(MailResourceLoader.getString("dialog", "mimetypeviewer", "choose_viewer"));
			if (fileChooser.showDialog(dialog, MailResourceLoader.getString("global", "ok")) == JFileChooser.APPROVE_OPTION) {
				viewerName.setText(fileChooser.getSelectedFile().toString());
			}
		} else if (command == CMD_OK) {
			viewer = viewerName.getText();
			dialog.dispose();
		} else if (command == CMD_CANCEL) {
			dialog.dispose();
		}
	}
}
