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
package org.columba.mail.gui.config.filter.plugins;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.util.ComboMenu;
import org.columba.core.gui.util.NotifyDialog;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.Plugin;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.mail.filter.FilterAction;
import org.columba.mail.gui.config.filter.ActionList;
import org.columba.mail.plugin.FilterActionPluginHandler;

public class DefaultActionRow implements Plugin {
	protected JPanel panel;
	protected FilterAction filterAction;

	protected GridBagLayout gridbag = new GridBagLayout();
	protected GridBagConstraints c = new GridBagConstraints();
	protected ActionList actionList;
	protected int count;
	private FrameMediator mediator;
	private ComboMenu comboMenu;

	public DefaultActionRow(FrameMediator mediator, ActionList list,
			FilterAction action) {
		this.filterAction = action;
		this.actionList = list;
		this.mediator = mediator;

		panel = new JPanel();

		initComponents();

		updateComponents(true);

	}

	public JPanel getContentPane() {
		return panel;
	}

	public void updateComponents(boolean b) {
		if (b) {
			String name = (String) filterAction.getAction();
			comboMenu.setText(name);
		} else {
			String name = (String) comboMenu.getText();
			filterAction.setAction(name);
		}
	}

	public void initComponents() {
		panel.removeAll();

		panel.setLayout(gridbag);

		FilterActionPluginHandler pluginHandler = null;

		try {
			pluginHandler = (FilterActionPluginHandler) MainInterface.pluginManager
					.getHandler("org.columba.mail.filteraction");
		} catch (PluginHandlerNotFoundException ex) {
			NotifyDialog d = new NotifyDialog();
			d.showDialog(ex);
		}

		/*
		 * actionComboBox = new JComboBox(names); actionComboBox.setRenderer(new
		 * ActionComboBoxRenderer());
		 */
		comboMenu = new ComboMenu(pluginHandler.getPluginIdList());
		comboMenu.setText(filterAction.getAction());
		comboMenu.addItemListener(actionList);

		c.fill = GridBagConstraints.VERTICAL;
		c.weightx = 1.0;
		c.insets = new Insets(2, 2, 2, 2);
		c.gridx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 1;

		gridbag.setConstraints(comboMenu, c);
		panel.add(comboMenu);

		count = 0;
	}

	public void addComponent(JComponent component) {
		c.gridx = ++count;
		gridbag.setConstraints(component, c);
		panel.add(component);
	}

	/**
	 * Returns the filterAction.
	 * 
	 * @return FilterAction
	 */
	public FilterAction getFilterAction() {
		return filterAction;
	}

	/**
	 * Sets the filterAction.
	 * 
	 * @param filterAction
	 *            The filterAction to set
	 */
	public void setFilterAction(FilterAction filterAction) {
		this.filterAction = filterAction;
	}
	/**
	 * @return Returns the mediator.
	 */
	public FrameMediator getMediator() {
		return mediator;
	}
}