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

package org.columba.mail.gui.config.filter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.columba.core.filter.Filter;
import org.columba.core.filter.FilterAction;
import org.columba.core.filter.FilterActionList;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.main.Main;
import org.columba.core.plugin.IExtension;
import org.columba.core.plugin.PluginManager;
import org.columba.core.plugin.exception.PluginHandlerNotFoundException;
import org.columba.mail.filter.MailFilterAction;
import org.columba.mail.gui.config.filter.plugins.DefaultActionRow;
import org.columba.mail.gui.config.filter.plugins.MarkActionRow;
import org.columba.mail.plugin.FilterActionExtensionHandler;
import org.columba.mail.plugin.FilterActionUIExtensionHandler;

public class ActionList extends JPanel implements ActionListener, ItemListener {
	private Filter filter;

	private List list;

	private JPanel panel;

	protected GridBagLayout gridbag = new GridBagLayout();

	protected GridBagConstraints c = new GridBagConstraints();

	protected FrameMediator mediator;

	public ActionList(FrameMediator mediator, Filter filter, JFrame frame) {
		super(new BorderLayout());
		this.mediator = mediator;
		this.filter = filter;

		list = new Vector();
		panel = new JPanel();

		JScrollPane scrollPane = new JScrollPane(panel);

		scrollPane.setPreferredSize(new Dimension(500, 50));
		add(scrollPane, BorderLayout.CENTER);

		update();
	}

	public void initComponents() {
	}

	public void updateComponents(boolean b) {
		if (!b) {
			for (Iterator it = list.iterator(); it.hasNext();) {
				DefaultActionRow row = (DefaultActionRow) it.next();
				row.updateComponents(false);
			}

			// for (int i = 0; i < list.size(); i++) {
			// DefaultActionRow row = (DefaultActionRow) list.get(i);
			// row.updateComponents(false);
			// }
		}
	}

	public void add() {
		boolean allowed = true;

		FilterActionList actionList = filter.getFilterActionList();
		for (int i = 0; i < actionList.getChildCount(); i++) {
			FilterAction action = actionList.get(i);
			String name = action.getAction();

			if ((action.equals("move")) || (action.equals("delete"))) {
				allowed = false;
				break;
			}
		}

		if (allowed) {
			updateComponents(false);
			actionList.addEmptyAction();
		}

		update();
	}

	public void remove(int i) {
		FilterActionList actionList = filter.getFilterActionList();

		if (actionList.getChildCount() > 1) {
			updateComponents(false);

			actionList.remove(i);
			update();
		}
	}

	public void update() {
		panel.removeAll();
		list.clear();

		panel.setLayout(gridbag);

		FilterActionExtensionHandler pluginHandler = null;
		FilterActionUIExtensionHandler pluginUIHandler = null;
		try {
			pluginHandler = (FilterActionExtensionHandler) PluginManager
					.getInstance().getHandler(FilterActionExtensionHandler.NAME);
			pluginUIHandler = (FilterActionUIExtensionHandler) PluginManager
			.getInstance().getHandler(FilterActionUIExtensionHandler.NAME);
		} catch (PluginHandlerNotFoundException ex) {
			if (Main.DEBUG) {
				ex.printStackTrace();
			}
		}

		FilterActionList actionList = filter.getFilterActionList();

		for (int i = 0; i < actionList.getChildCount(); i++) {
			FilterAction action = actionList.get(i);

			// int type = action.getActionInt();
			String name = action.getAction();
			DefaultActionRow row = null;

			Object[] args = { mediator, this, action };

			try {
				IExtension extension = pluginHandler.getExtension(name);
				String ui = extension.getMetadata().getAttribute("ui");
				IExtension uiExtension = pluginUIHandler.getExtension(ui);
				
				row = (DefaultActionRow) uiExtension.instanciateExtension(args);
			} catch (Exception ex) {
				ex.printStackTrace();

				// this probably means that the configuration
				// is wrong
				// -> change this to a sane default value
				action.setAction("Mark Message");
				((MailFilterAction) action).setMarkVariant("read");
				row = null;
			}

			if (row == null) {
				// maybe the plugin wasn't loaded correctly
				// -> use default
				// row = new MarkActionRow(this,action);
				row = new MarkActionRow(mediator, this, action);
			}

			if (row != null) {
				c.fill = GridBagConstraints.NONE;
				c.gridx = GridBagConstraints.RELATIVE;
				c.gridy = i;
				c.weightx = 1.0;
				c.anchor = GridBagConstraints.NORTHWEST;
				gridbag.setConstraints(row.getContentPane(), c);

				list.add(row);
				panel.add(row.getContentPane());

				JButton addButton = new JButton(ImageLoader
						.getSmallImageIcon("stock_add_16.png"));
				addButton.setActionCommand("ADD");
				addButton.setMargin(new Insets(0, 0, 0, 0));
				addButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						add();
					}
				});

				JButton removeButton = new JButton(ImageLoader
						.getSmallImageIcon("stock_remove_16.png"));
				removeButton.setActionCommand(Integer.toString(i));
				removeButton.setMargin(new Insets(0, 0, 0, 0));

				final int index = i;
				removeButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						remove(index);
					}
				});

				/*
				 * c.gridx = GridBagConstraints.REMAINDER; c.anchor =
				 * GridBagConstraints.NORTHEAST; gridbag.setConstraints(
				 * removeButton, c ); panel.add( removeButton );
				 */
				JPanel buttonPanel = new JPanel();
				buttonPanel.setLayout(new GridLayout(0, 2, 2, 2));
				buttonPanel.add(removeButton);
				buttonPanel.add(addButton);

				c.insets = new Insets(2, 2, 2, 2);
				c.gridx = GridBagConstraints.REMAINDER;
				c.anchor = GridBagConstraints.NORTHEAST;
				gridbag.setConstraints(buttonPanel, c);
				panel.add(buttonPanel);
			}
		}

		c.weighty = 1.0;

		Component box = Box.createVerticalGlue();
		gridbag.setConstraints(box, c);
		panel.add(box);

		validate();
		repaint();
	}

	public void actionPerformed(ActionEvent e) {
		updateComponents(false);
		update();
	}

	/**
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent arg0) {
		updateComponents(false);
		update();
	}
}