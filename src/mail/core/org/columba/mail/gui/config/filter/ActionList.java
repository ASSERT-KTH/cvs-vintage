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
import org.columba.mail.gui.util.*;
import org.columba.mail.gui.tree.util.*;
import org.columba.mail.filter.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import java.awt.*;
import java.awt.event.*;

import java.util.Vector;

class ActionList extends JPanel implements ActionListener {

	private Config config;

	private Filter filter;

	private HeaderTableItem v;

	private Vector list;

	private JPanel panel;

	protected GridBagLayout gridbag = new GridBagLayout();
	protected GridBagConstraints c = new GridBagConstraints();

	public ActionList(Filter filter, JFrame frame) {
		super();

		this.config = MainInterface.config;
		this.filter = filter;

		list = new Vector();

		panel = new JPanel();
		JScrollPane scrollPane = new JScrollPane(panel);

		setLayout(new BorderLayout());

		scrollPane.setPreferredSize(new Dimension(500, 50));
		add(scrollPane, BorderLayout.CENTER);

		update();

	}

	public void initComponents() {
	}

	public void updateComponents(boolean b) {
		if (b == false) {

			for (int i = 0; i < list.size(); i++) {
				DefaultActionRow row = (DefaultActionRow) list.get(i);
				row.updateComponents(false);
			}
		}
	}

	public void add() {

		boolean allowed = true;
		for (int i = 0; i < filter.getActionCount(); i++) {
			FilterAction action = filter.getFilterAction(i);
			String name = action.getAction();

			if ((action.equals("move")) || (action.equals("delete")))
				allowed = false;

		}

		if (allowed) {
			updateComponents(false);
			filter.addEmptyAction();
		}

		update();
	}

	public void remove(int i) {
		if (filter.getActionCount() > 1) {

			updateComponents(false);

			filter.removeAction(i);

			update();
		}

	}

	public void update() {
		panel.removeAll();
		list.clear();

		panel.setLayout(gridbag);

		/*
		c.fill = GridBagConstraints.NONE;
		c.gridx = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.NORTHWEST;
		*/

		for (int i = 0; i < filter.getActionCount(); i++) {
			FilterAction action = filter.getFilterAction(i);
			int type = action.getActionInt();
			DefaultActionRow row = null;

			if (type == 0) {
				// move
				row = new ActionRow(this, action);
			} else if (type == 1) {
				// copy
				row = new ActionRow(this, action);
			} else if (type == 2) {
				// mark as read
				row = new MarkActionRow(this, action);
			} else if (type == 3) {
				// delete
				row = new MarkActionRow(this, action);
			}

			if (row != null) {
				c.fill = GridBagConstraints.NONE;
				c.gridx = GridBagConstraints.RELATIVE;
				c.gridy = i;
				c.weightx = 1.0;
				c.anchor = GridBagConstraints.NORTHWEST;
				gridbag.setConstraints(row, c);

				list.add(row);
				panel.add(row);

				JButton addButton =
					new JButton(
						ImageLoader.getSmallImageIcon("stock_add_16.png"));
				addButton.setActionCommand("ADD");
				addButton.setMargin(new Insets(0, 0, 0, 0));
				addButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						add();
					}
				});

				JButton removeButton =
					new JButton(
						ImageLoader.getSmallImageIcon("stock_remove_16.png"));
				removeButton.setActionCommand(new Integer(i).toString());
				removeButton.setMargin(new Insets(0, 0, 0, 0));
				final int index = i;
				removeButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						remove(index);
					}
				});

				/*
				    c.gridx = GridBagConstraints.REMAINDER;
				    c.anchor = GridBagConstraints.NORTHEAST;
				    gridbag.setConstraints( removeButton, c );
				    panel.add( removeButton );
				 */

				JPanel buttonPanel = new JPanel();
				buttonPanel.setLayout(new GridLayout(0, 2, 2, 2));
				buttonPanel.add(removeButton);
				buttonPanel.add(addButton);

				//c.insets = new Insets(1, 2, 1, 2);
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
		System.out.println("actionperformed");

		updateComponents(false);
		update();
	}

}
