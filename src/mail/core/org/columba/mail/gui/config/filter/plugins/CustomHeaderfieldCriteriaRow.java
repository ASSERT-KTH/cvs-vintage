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

import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.columba.core.plugin.AbstractPluginHandler;
import org.columba.mail.filter.FilterCriteria;
import org.columba.mail.gui.config.filter.CriteriaList;

public class CustomHeaderfieldCriteriaRow extends DefaultCriteriaRow {

	private JComboBox matchComboBox;
	private JTextField patternTextField;
	private JTextField headerTextField;

	public CustomHeaderfieldCriteriaRow(
		AbstractPluginHandler pluginHandler,
		CriteriaList criteriaList,
		FilterCriteria c) {
		super(pluginHandler, criteriaList, c);
	}

	public void updateComponents(boolean b) {
		super.updateComponents(b);

		if (b) {
			matchComboBox.setSelectedItem(criteria.getCriteriaString());
			patternTextField.setText(criteria.getPattern());
			headerTextField.setText(criteria.getHeaderItemString());
		} else {
			criteria.setCriteria((String) matchComboBox.getSelectedItem());
			criteria.setPattern((String) patternTextField.getText());
			criteria.setHeaderItem((String) headerTextField.getText());
		}

	}

	public void initComponents() {
		super.initComponents();

		headerTextField = new JTextField("header", 12);

		addComponent(headerTextField);

		matchComboBox = new JComboBox();
		matchComboBox.addItem("contains");
		matchComboBox.addItem("contains not");
		/*
		matchComboBox.addItem("is");
		matchComboBox.addItem("is not");
		matchComboBox.addItem("begins with");
		matchComboBox.addItem("ends with");
		*/
		
		addComponent(matchComboBox);

		patternTextField = new JTextField("pattern", 12);

		addComponent(patternTextField);

	}

}
