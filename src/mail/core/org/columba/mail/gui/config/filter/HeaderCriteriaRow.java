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

package org.columba.mail.gui.config.filter;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.columba.mail.filter.FilterCriteria;

public class HeaderCriteriaRow extends DefaultCriteriaRow {

	private JComboBox matchComboBox;
	private JTextField textField;

	public HeaderCriteriaRow(CriteriaList criteriaList, FilterCriteria c) {
		super(criteriaList, c);

	}

	protected void updateComponents(boolean b) {
		super.updateComponents(b);

		if (b) {
			matchComboBox.setSelectedItem(criteria.getCriteriaString());
			textField.setText(criteria.getPattern());
		} else {
			criteria.setCriteria((String) matchComboBox.getSelectedItem());
			criteria.setPattern((String) textField.getText());
		}

	}

	public void initComponents() {
		super.initComponents();

		matchComboBox = new JComboBox();
		matchComboBox.addItem("contains");
		matchComboBox.addItem("contains not");
		matchComboBox.addItem("is");
		matchComboBox.addItem("is not");
		matchComboBox.addItem("begins with");
		matchComboBox.addItem("ends with");
		c.gridx = 1;
		gridbag.setConstraints(matchComboBox, c);
		add(matchComboBox);

		textField = new JTextField("header", 10);
		c.gridx = 2;
		gridbag.setConstraints(textField, c);
		add(textField);

		finishRow();
	}

}
