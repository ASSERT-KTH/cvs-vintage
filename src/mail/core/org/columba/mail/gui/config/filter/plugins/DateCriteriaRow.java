// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

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