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
package org.columba.addressbook.gui.table;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.columba.addressbook.gui.table.model.FilterDecorator;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FilterToolbar extends JPanel implements ActionListener {
	public JButton searchButton;

	private JComboBox comboBox;

	private TableController table;

	private ResourceBundle toolbarLabels;

	private JLabel label;

	private JTextField textField;

	private JButton clearButton;

	public FilterToolbar(TableController table) {
		super();

		this.table = table;

		initComponents();
		layoutComponents();

		textField.getDocument().addDocumentListener(new MyDocumentListener());
	}

	protected void initComponents() {
		label = new JLabel("Name or email contains:");

		textField = new JTextField(12);

		clearButton = new JButton("Clear");
		clearButton.setActionCommand("CLEAR");
		clearButton.addActionListener(this);
	}

	protected void layoutComponents() {
		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		FormLayout l = new FormLayout(
				"3dlu, default, 3dlu, fill:default:grow, 3dlu, default, 3dlu",
				"fill:default:grow");
		PanelBuilder b = new PanelBuilder(this, l);

		CellConstraints c = new CellConstraints();

		b.add(label, c.xy(2, 1));
		b.add(textField, c.xy(4, 1));
		b.add(clearButton, c.xy(6, 1));

	}

	public void update() {
		table.getAddressbookModel().update();
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		if (action.equals("CLEAR")) {
			textField.setText("");
		}
	}

	class MyDocumentListener implements DocumentListener {

		public void insertUpdate(DocumentEvent e) {
			update();
		}

		public void removeUpdate(DocumentEvent e) {
			update();
		}

		public void changedUpdate(DocumentEvent e) {
			//Plain text components don't fire these events
		}

		public void update() {
			FilterDecorator model = table.getFilterDecorator();
			model.setPattern(textField.getText());
			table.getAddressbookModel().fireTableDataChanged();
		}
	}
}