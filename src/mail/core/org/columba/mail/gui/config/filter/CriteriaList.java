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

import org.columba.mail.config.*;
import org.columba.mail.message.*;
import org.columba.core.config.*;
import org.columba.core.gui.util.*;
import org.columba.main.*;
import org.columba.mail.filter.*;
import org.columba.mail.gui.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

public class CriteriaList extends JPanel implements ActionListener {

	private Config config;
	private Filter filter;

	private HeaderTableItem v;
	private Vector list;
	private JPanel panel;

	public CriteriaList(Filter filter) {
		super();

		this.config = MainInterface.config;
		this.filter = filter;

		list = new Vector();

		panel = new JPanel();
		JScrollPane scrollPane = new JScrollPane(panel);

		scrollPane.setPreferredSize(new Dimension(500, 100));
		setLayout(new BorderLayout());

		add(scrollPane, BorderLayout.CENTER);

		update();
	}

	public void updateComponents(boolean b) {
		if (b == false) {

			for (int i = 0; i < list.size(); i++) {
				DefaultCriteriaRow row = (DefaultCriteriaRow) list.get(i);
				row.updateComponents(false);
			}
		}

	}

	public void add() {

		filter.addEmptyCriteria();

		updateComponents(false);

		update();

	}

	public void remove(int i) {

		FilterRule rule = filter.getFilterRule();

		if (rule.count() > 1) {
			updateComponents(false);

			filter.removeCriteria(i);

			update();
		}

	}

	public void update() {
		panel.removeAll();
		list.clear();

		System.out.println("CriteriaList -> update() ");

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		panel.setLayout(gridbag);

		/*
		c.fill = GridBagConstraints.NONE;
		c.gridx = GridBagConstraints.RELATIVE;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.NORTHWEST;
		*/

		FilterRule rule = filter.getFilterRule();

		for (int i = 0; i < rule.count(); i++) {
			FilterCriteria criteria = rule.get(i);
			String type = criteria.getType();
			DefaultCriteriaRow column;

			c.fill = GridBagConstraints.NONE;
			//c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = GridBagConstraints.RELATIVE;
			c.gridy = i;
			c.weightx = 1.0;
			c.anchor = GridBagConstraints.NORTHWEST;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;

			if (type.equalsIgnoreCase("Custom Headerfield")) {
				column = new CustomHeaderCriteriaRow(this, criteria);
				gridbag.setConstraints(column, c);
			} else if (type.equalsIgnoreCase("Date")) {
				column = new DateCriteriaRow(this, criteria);
				gridbag.setConstraints(column, c);
			} else if (type.equalsIgnoreCase("Size")) {
				column = new SizeCriteriaRow(this, criteria);
				gridbag.setConstraints(column, c);
			} else if (type.equalsIgnoreCase("Flags")) {
				column = new FlagsCriteriaRow(this, criteria);
				gridbag.setConstraints(column, c);
			} else if (type.equalsIgnoreCase("Priority")) {
				column = new PriorityCriteriaRow(this, criteria);
				gridbag.setConstraints(column, c);
			} else if (type.equalsIgnoreCase("Body")) {
				column = new BodyCriteriaRow(this, criteria);
				gridbag.setConstraints(column, c);
			} else {
				column = new HeaderCriteriaRow(this, criteria);
				gridbag.setConstraints(column, c);
			}

			list.add(column);

			panel.add(column);

		
			JButton addButton = new JButton(ImageLoader.getSmallImageIcon("stock_add_16.png") );
			addButton.setActionCommand("ADD");
			addButton.setMargin(new Insets(0,0,0,0) );
			addButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					add();
				}
			});
			
			
			/*
			//c.insets = new Insets(1, 2, 1, 2);
			c.gridx = GridBagConstraints.RELATIVE;
			c.anchor = GridBagConstraints.NORTHEAST;
			c.weightx = 1.0;
			gridbag.setConstraints(addButton, c);
			panel.add(addButton);
			*/
			
			JButton removeButton =
				new JButton(
					ImageLoader.getSmallImageIcon("stock_remove_16.png"));
			removeButton.setMargin(new Insets(0,0,0,0) );
			removeButton.setActionCommand(new Integer(i).toString());
			final int index = i;
			removeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					remove(index);
				}
			});
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout( new GridLayout(0,2,2,2) );			
			buttonPanel.add( removeButton );
			buttonPanel.add( addButton );

			//c.insets = new Insets(1, 2, 1, 2);
			c.gridx = GridBagConstraints.REMAINDER;
			c.anchor = GridBagConstraints.NORTHEAST;
			gridbag.setConstraints(buttonPanel, c);
			panel.add(buttonPanel);
		}

		c.weighty = 1.0;
		Component box = Box.createVerticalGlue();
		gridbag.setConstraints(box, c);
		panel.add(box);

		validate();
		repaint();

	}

	public void actionPerformed(ActionEvent e) {
		System.out.println("actionperformed");

		updateComponents(false);
		update();
	}
}
