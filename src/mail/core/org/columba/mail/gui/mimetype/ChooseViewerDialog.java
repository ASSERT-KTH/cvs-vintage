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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.columba.core.gui.util.DialogStore;

public class ChooseViewerDialog implements ActionListener {
	private JTextField viewerName;
	private String viewer = null;
	private JCheckBox saveCButton;
	private JDialog dialog;

	public ChooseViewerDialog(
		String contentType,
		String contentSubtype,
		boolean save) {
		//LOCALIZE
		// super(owner,"Select Viewer for "+contentType+"/"+contentSubtype,true);

		dialog =
			DialogStore.getDialog(
				"Select Viewer for " + contentType + "/" + contentSubtype);
		init(save);
		dialog.setSize(300, 150);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	private void init(boolean save) {
		JPanel contentPane = new JPanel(new BorderLayout(0, 0));
		contentPane.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
		JPanel viewerPanel = new JPanel();
		viewerPanel.setLayout(new BoxLayout(viewerPanel, BoxLayout.X_AXIS));
		//LOCALIZE
		JLabel label = new JLabel("Viewer:");
		viewerPanel.add(label);
		viewerPanel.add(Box.createHorizontalStrut(5));
		viewerName = new JTextField();
		viewerName.setActionCommand("OK");
		viewerName.addActionListener(this);
		label.setLabelFor(viewerName);
		viewerPanel.add(viewerName);
		viewerPanel.add(Box.createHorizontalStrut(10));
		JButton searchButton = new JButton("...");
		searchButton.addActionListener(this);
		searchButton.setActionCommand("SEARCH");
		viewerPanel.add(searchButton);
		contentPane.add(viewerPanel, BorderLayout.NORTH);
		//LOCALIZE
		saveCButton = new JCheckBox("Always open with this Viewer", save);
		saveCButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 17, 0));
		contentPane.add(saveCButton, BorderLayout.CENTER);
		JPanel bottomPanel = new JPanel(new BorderLayout(0, 0));
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
		//LOCALIZE
		JButton okButton = new JButton("OK");
		okButton.addActionListener(this);
		okButton.setActionCommand("OK");
		buttonPanel.add(okButton);
		//LOCALIZE
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("CANCEL");
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		contentPane.add(bottomPanel, BorderLayout.SOUTH);
		dialog.setContentPane(contentPane);
		dialog.getRootPane().setDefaultButton(okButton);
		dialog.getRootPane().registerKeyboardAction(
			this,
			"CANCEL",
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
		if (command.equals("SEARCH")) {
			JFileChooser fileChooser = new JFileChooser();
			//LOCALIZE
			fileChooser.setDialogTitle("Choose Program");
			int returnVal = fileChooser.showDialog(dialog, "OK");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				viewerName.setText(fileChooser.getSelectedFile().toString());
			}
		} else if (command.equals("OK")) {
			viewer = viewerName.getText();
			dialog.dispose();
		} else if (command.equals("CANCEL")) {
			dialog.dispose();
		}
	}
}
