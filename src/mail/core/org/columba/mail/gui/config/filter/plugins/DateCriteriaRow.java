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
package org.columba.mail.gui.config.filter.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComboBox;

import org.columba.core.plugin.AbstractPluginHandler;
import org.columba.mail.filter.FilterCriteria;
import org.columba.mail.gui.config.filter.CriteriaList;
import org.columba.mail.gui.util.DateChooserDialog;

public class DateCriteriaRow
	extends DefaultCriteriaRow
	implements ActionListener {

	private JComboBox matchComboBox;
	private JButton dateButton;

	public DateCriteriaRow(
		AbstractPluginHandler pluginHandler,
		CriteriaList criteriaList,
		FilterCriteria c) {
		super(pluginHandler, criteriaList, c);

	}

	public void updateComponents(boolean b) {
		super.updateComponents(b);

		if (b) {
			matchComboBox.setSelectedItem(criteria.getCriteriaString());
			//textField.setText(criteria.getPattern());
			dateButton.setText(criteria.getPattern());
		} else {
			criteria.setCriteria((String) matchComboBox.getSelectedItem());
			//criteria.setPattern((String) textField.getText());
			criteria.setPattern((String) dateButton.getText());
		}

	}

	public void initComponents() {
		super.initComponents();

		matchComboBox = new JComboBox();
		matchComboBox.addItem("before");
		matchComboBox.addItem("after");

		addComponent(matchComboBox);

		dateButton = new JButton("date");
		dateButton.setActionCommand("DATE");
		dateButton.addActionListener(this);

		addComponent(dateButton);

	}

	public void actionPerformed(ActionEvent ev) {
		String action = ev.getActionCommand();

		if (action.equals("DATE")) {
			DateFormat f = DateFormat.getDateInstance();
			Date d = null;
			try {
				d = f.parse(dateButton.getText());
			} catch (Exception ex) {
				//ex.printStackTrace();
			}

			DateChooserDialog dialog =
				new DateChooserDialog();
			if (d != null)
				dialog.setDate(d);
			dialog.setVisible(true);

			if (dialog.success() == true) {
				// Ok
				Date date = dialog.getDate();
				dateButton.setText(f.format(date));
			} else {
				// cancel
			}
		}
	}

}