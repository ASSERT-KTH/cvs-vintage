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

package org.columba.addressbook.gui.util;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.columba.addressbook.config.AdapterNode;
import org.columba.addressbook.folder.ContactCard;

/**
 * @version 	1.0
 * @author
 */
public class PropertyEditor
	extends JDialog
	implements ActionListener, TableModelListener
{
	protected JTable table;
	protected DefaultTableModel model;

	protected JButton addButton;
	protected JButton removeButton;

	protected ContactCard item;

	public PropertyEditor(ContactCard item, JFrame frame)
	{
		super(frame);

		init();

		if (item != null)
		{
			this.item = item;

			for (int i = 0; i < item.getRootNode().getChildCount(); i++)
			{
				AdapterNode child = item.getRootNode().getChildAt(i);
				System.out.println("node-name:" + child.getName());
				String name = child.getName();

				// found key, now search for attributes
				String value = (String) child.getValue();
				System.out.println("node-value:" + value);

				Object row[] = new Object[2];
				row[0] = name;
				row[1] = value;
				model.addRow(row);
			}
			/*
			for (Enumeration e = item.getKeys(); e.hasMoreElements();)
			{
				
				String key = (String) e.nextElement();
				Object value = (Object) item.getValue(key);
				
				Object row[] = new Object[2];
					row[0] = key;
					row[1] = value;
					model.addRow(row);
					
			
				
			
			}
			*/

		}
	}

	protected void init()
	{
		getContentPane().setLayout(new BorderLayout());

		model = new DefaultTableModel()
		{
			public boolean isCellEditable(int row, int col)
			{
				return (col != 0);
			}
		};

		model.addColumn("key");
		model.addColumn("value");

		table = new JTable();
		table.setModel(model);
		model.addTableModelListener(this);

		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setColumnSelectionAllowed(false);

		JScrollPane scrollPane = new JScrollPane(table);
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		addButton = new JButton("Add");
		addButton.setActionCommand("ADD");
		addButton.addActionListener(this);

		removeButton = new JButton("Remove");
		removeButton.setActionCommand("REMOVE");
		removeButton.addActionListener(this);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setAlignmentX(0);
		bottomPanel.setLayout(new GridLayout(0, 2, 10, 10));

		bottomPanel.add(addButton);
		bottomPanel.add(removeButton);

		getContentPane().add(bottomPanel, BorderLayout.SOUTH);

		ListSelectionModel sel = new DefaultListSelectionModel();
		table.setSelectionModel(sel);
		sel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sel.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent ev)
			{
				removeButton.setEnabled(!(table.getSelectionModel().isSelectionEmpty()));
			}
		});

		pack();

		setVisible(true);

	}

	public void actionPerformed(ActionEvent ev)
	{
		Object o = ev.getActionCommand();

		if (o.equals("ADD"))
		{
			Object row[] = new Object[2];
			row[0] = "key";
			row[1] = "value";
			model.addRow(row);

			//item.add(row[0], row[1]);
		}
		else if (o.equals("REMOVE"))
		{
			int rows[] = table.getSelectedRows();
			String key = (String) table.getValueAt(rows[0], 0);
			System.out.println("remove key:" + key);

			//item.remove(key);

			model.removeRow(rows[0]);

		}
	}

	public void tableChanged(TableModelEvent e)
	{
		if (e.getType() == TableModelEvent.UPDATE)
		{
			int row = e.getFirstRow();
			int column = e.getColumn();
			String columnName = model.getColumnName(column);
			Object data = model.getValueAt(row, column);

			System.out.println("data:" + data);
		}

	}

}