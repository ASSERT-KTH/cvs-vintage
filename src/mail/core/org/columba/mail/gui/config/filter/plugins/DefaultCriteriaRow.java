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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.columba.core.plugin.AbstractPluginHandler;
import org.columba.core.plugin.PluginInterface;
import org.columba.mail.filter.FilterCriteria;
import org.columba.mail.gui.config.filter.CriteriaList;

public class DefaultCriteriaRow implements PluginInterface {
	protected FilterCriteria criteria;
	protected CriteriaList criteriaList;

	protected JPanel panel;

	protected JComboBox conditionComboBox;
	/*
	private JComboBox matchComboBox;
	private JTextField textField;
	*/

	protected JButton removeButton;
	//protected Vector conditionList;

	protected GridBagLayout gridbag = new GridBagLayout();
	protected GridBagConstraints c = new GridBagConstraints();

	AbstractPluginHandler pluginHandler;

	protected int count;

	public DefaultCriteriaRow(
		AbstractPluginHandler pluginHandler,
		CriteriaList criteriaList,
		FilterCriteria c) {
		this.pluginHandler = pluginHandler;

		this.criteria = c;
		this.criteriaList = criteriaList;

		/*
		conditionList = new Vector();
		
		conditionList.add("Subject");
		conditionList.add("From");
		conditionList.add("To");
		conditionList.add("Cc");
		conditionList.add("Bcc");
		conditionList.add("To or Cc");
		conditionList.add("Custom Headerfield");
		conditionList.add("Body");
		conditionList.add("Date");
		conditionList.add("Flags");
		conditionList.add("Priority");
		conditionList.add("Size");
		*/

		panel = new JPanel();

		initComponents();

		updateComponents(true);

		conditionComboBox.addActionListener(criteriaList);

	}

	public void updateComponents(boolean b) {
		if (b) {
			String conditionString = criteria.getType();
			//int index = conditionList.indexOf(conditionString);

			//System.out.println("condition: "+ conditionString );
			conditionComboBox.setSelectedItem(conditionString);

		} else {
			//int conditionIndex = (int) conditionComboBox.getSelectedIndex();
			//String conditionString = (String) conditionList.get(conditionIndex);
			//conditionString = conditionString.toLowerCase();
			String conditionString =
				(String) conditionComboBox.getSelectedItem();

			criteria.setType(conditionString);
		}
	}

	public void initComponents() {

		panel.setLayout(gridbag);

		//conditionComboBox = new JComboBox(pluginList.getFilters());
		conditionComboBox = new JComboBox(pluginHandler.getDefaultNames());
		/*
		for (int i = 0; i < conditionList.size(); i++) {
			String name = (String) conditionList.get(i);
		
			conditionComboBox.addItem(name);
		}
		*/

		//c.fill = GridBagConstraints.HORIZONTAL;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.insets = new Insets(1, 2, 1, 2);
		c.gridx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 1;

		gridbag.setConstraints(conditionComboBox, c);

		panel.add(conditionComboBox);

		count = 0;

	}
	
	public JPanel getContentPane()
	{
		return panel;
	}

	public void addComponent(JComponent component) {
		c.gridx = ++count;
		gridbag.setConstraints(component, c);
		panel.add(component);
	}

	/**
	 * Returns the criteria.
	 * @return FilterCriteria
	 */
	public FilterCriteria getCriteria() {
		return criteria;
	}

	/**
	 * Returns the pluginHandler.
	 * @return AbstractPluginHandler
	 */
	public AbstractPluginHandler getPluginHandler() {
		return pluginHandler;
	}

	/**
	 * Sets the pluginHandler.
	 * @param pluginHandler The pluginHandler to set
	 */
	public void setPluginHandler(AbstractPluginHandler pluginHandler) {
		this.pluginHandler = pluginHandler;
	}

}
