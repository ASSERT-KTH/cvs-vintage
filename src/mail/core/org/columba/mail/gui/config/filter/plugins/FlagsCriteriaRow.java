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

import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.columba.core.plugin.AbstractPluginHandler;
import org.columba.mail.filter.FilterCriteria;
import org.columba.mail.gui.config.filter.CriteriaList;

public class FlagsCriteriaRow extends DefaultCriteriaRow {

	private JComboBox matchComboBox;
	private JComboBox flagsComboBox;
	private JTextField textField;

	public FlagsCriteriaRow(
		AbstractPluginHandler pluginHandler,
		CriteriaList criteriaList,
		FilterCriteria c) {
		super(pluginHandler, criteriaList, c);

	}

	public void updateComponents(boolean b) {
		super.updateComponents(b);

		if (b) {
			matchComboBox.setSelectedItem(criteria.getCriteriaString());
			String flag = criteria.getPattern();
			flagsComboBox.setSelectedItem(flag);
		} else {
			criteria.setCriteria((String) matchComboBox.getSelectedItem());
			criteria.setPattern((String) flagsComboBox.getSelectedItem());
		}

	}

	public void initComponents() {
		super.initComponents();

		matchComboBox = new JComboBox();
		matchComboBox.addItem("is");
		matchComboBox.addItem("is not");

		addComponent(matchComboBox);

		flagsComboBox = new JComboBox();
		flagsComboBox.addItem("Answered");
		flagsComboBox.addItem("Deleted");
		flagsComboBox.addItem("Flagged");
		flagsComboBox.addItem("Recent");
		flagsComboBox.addItem("Draft");
		flagsComboBox.addItem("Seen");

		addComponent(flagsComboBox);

	}

}
