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

package org.columba.addressbook.gui.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.columba.core.config.AdapterNode;
import org.columba.core.config.HeaderTableItem;
import org.columba.addressbook.config.AddressbookConfig;
import org.columba.addressbook.config.AddressbookXmlConfig;
import org.columba.addressbook.config.FolderItem;
import org.columba.addressbook.folder.Folder;
import org.columba.addressbook.folder.HeaderItem;
import org.columba.addressbook.folder.HeaderItemList;
import org.columba.addressbook.gui.table.util.AddressbookCommonHeaderRenderer;
import org.columba.addressbook.gui.table.util.HeaderColumn;
import org.columba.addressbook.gui.table.util.HeaderColumnInterface;
import org.columba.addressbook.gui.table.util.TableModelFilteredView;
import org.columba.addressbook.gui.table.util.TableModelSorter;
import org.columba.addressbook.gui.table.util.TypeHeaderColumn;
import org.columba.addressbook.main.AddressbookInterface;
import org.columba.addressbook.util.AddressbookResourceLoader;

public class AddressbookTable extends JPanel implements ListSelectionListener
{
	private AddressbookInterface addressbookInterface;
	private AddressbookXmlConfig config;

	private JTable table;
	private AddressbookTableModel addressbookModel;
	private GroupTableModel groupModel;

	public JScrollPane scrollPane;
	private AdapterNode node;
	
	private TableModelFilteredView filteredView;
	private TableModelSorter sorter;
	
	private FilterToolbar toolbar;

	public AddressbookTable(AddressbookInterface i)
	{
		this.addressbookInterface = i;

		config = AddressbookConfig.getAddressbookConfig();

		addressbookModel = new AddressbookTableModel();
		
		filteredView = new TableModelFilteredView(addressbookModel);
		sorter = new TableModelSorter( addressbookModel );
		
		addressbookModel.registerPlugin(filteredView);
		addressbookModel.registerPlugin(sorter);
		
		table = new JTable(addressbookModel);
		table.setIntercellSpacing(new Dimension(0, 0));
		table.setShowGrid(false);
		table.setAutoResizeMode(table.AUTO_RESIZE_ALL_COLUMNS);
		table.getSelectionModel().addListSelectionListener(this);
		
		addMouseListenerToHeaderInTable();

		setLayout( new BorderLayout() );
		
		scrollPane = new JScrollPane(table);
		scrollPane.getViewport().setBackground(Color.white);
		
		add( scrollPane, BorderLayout.CENTER );
		
		toolbar = new FilterToolbar( this );
		add( toolbar, BorderLayout.NORTH );
	}

	public void valueChanged(ListSelectionEvent e) 
	{
       	addressbookInterface.actionListener.changeActions();
    }
    
	public AddressbookTableModel getTableModel()
	{
		return  addressbookModel;
	}
	
	public TableModelFilteredView getTableModelFilteredView()
	{
		return filteredView;
	}
	
	public JTable getTableView()
	{
		JTable table = new JTable(addressbookModel);

		return table;
	}

	/*
	public JList getListView()
	{
		JList list = new JList();
		list.setModel( addressbookModel );
		
		return list;
	}
	*/

	public void setupColumn(String name)
	{

		try
		{
			TableColumn tc = table.getColumn(name);
			if (tc != null)
			{
				HeaderColumnInterface column = addressbookModel.getHeaderColumn(name);

				//System.out.println("renderer set:");
				tc.setCellRenderer( (TableCellRenderer) column);

				
				if (column.getValueString() != null)
					tc.setHeaderRenderer(
						new AddressbookCommonHeaderRenderer(column.getValueString(),sorter));
				else
					tc.setHeaderRenderer(new AddressbookCommonHeaderRenderer(column.getName(), sorter));

				if (column.getColumnSize() != -1)
				{
					//System.out.println("size is locked:" + column.getColumnSize());
					tc.setHeaderRenderer( new AddressbookCommonHeaderRenderer("",sorter) );

					tc.setMaxWidth(column.getColumnSize());
					tc.setMinWidth(column.getColumnSize());
				}
			}

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

	}
	
	protected void addMouseListenerToHeaderInTable()
	{
		final JTable tableView = table;

		tableView.setColumnSelectionAllowed(false);

		MouseAdapter listMouseListener = new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				TableColumnModel columnModel = tableView.getColumnModel();
				int viewColumn = columnModel.getColumnIndexAtX(e.getX());
				int column = tableView.convertColumnIndexToModel(viewColumn);

				if (e.getClickCount() == 1 && column != -1)
				{
					sorter.sort(column);
					addressbookModel.update();
					
					//mainInterface.mainFrame.getMenu().updateSortMenu();
				}
			}
		};

		JTableHeader th = tableView.getTableHeader();
		th.addMouseListener(listMouseListener);
	}
	
	public HeaderTableItem getHeaderTableItem()
	{
		return AddressbookConfig.getAddressbookOptionsConfig().getHeaderTableItem();
			
	}

	public void setupRenderer()
	{
		HeaderTableItem headerItemList =
			AddressbookConfig.getAddressbookOptionsConfig().getHeaderTableItem();

		HeaderColumn c;

		for (int i = 0; i < headerItemList.count(); i++)
		{

			String name = headerItemList.getName(i);
			//System.out.println("name:" + name);
			name = name.toLowerCase();
			int size = headerItemList.getSize(i);
			int position = headerItemList.getPosition(i);
			boolean enabled = headerItemList.getEnabled(i);
			if ( enabled == false ) continue;
			
			int index = name.indexOf(";");
			if (index != -1)
			{
				String prefix = AddressbookResourceLoader.getString("header", name.substring(0, index));
				String suffix =
					AddressbookResourceLoader.getString("header", name.substring(index + 1, name.length()));

				c = new HeaderColumn(name, prefix + "(" + suffix + ")");
			}
			else if (name.equals("type"))
			{
				c = new TypeHeaderColumn(name, AddressbookResourceLoader.getString("header", name));
				//System.out.println("typeHeadercolumn created");
			}
			else
			{
				c = new HeaderColumn(name, AddressbookResourceLoader.getString("header", name));

			}
			addressbookModel.addColumn(c);

		}

		for (int i = 0; i < headerItemList.count(); i++)
		{
			String name = headerItemList.getName(i);
			boolean enabled = headerItemList.getEnabled(i);
			if ( enabled == false ) continue;
			
			name = name.toLowerCase();
			setupColumn(name);
		}

	}

	public void setFolder(Folder folder)
	{
		Folder f = (Folder) folder;
		if ( f == null ) 
		{
			addressbookModel.setHeaderList(null);
			return;
		}
		
		FolderItem item = f.getFolderItem();
		if (item.getType().equals("root"))
			addressbookModel.setHeaderList(null);
		else
		{
			HeaderItemList list = folder.getHeaderItemList();

			addressbookModel.setHeaderList(list);
		}
	}

	public void setHeaderItemList(HeaderItemList list)
	{
		addressbookModel.setHeaderList(list);
	}

	/*
	public void setNode(AdapterNode n)
	{
		if (n.getName().equals("addressbook"))
		{
			HeaderItemList list = new HeaderItemList();
	
			AdapterNode nameNode = n.getChild(0);
			System.out.println("nameNode:" + nameNode.getName());
	
			AdapterNode listNode = n.getChild(1);
			System.out.println("listNode:" + listNode.getName());
	
			for (int j = 0; j < listNode.getChildCount(); j++)
			{
				AdapterNode child = (AdapterNode) listNode.getChild(j);
				System.out.println("child:" + child.getName());
	
				HeaderItem item = new HeaderItem();
				for (int x = 0; x < child.getChildCount(); x++)
				{
					AdapterNode node = child.getChild(x);
					System.out.println("child:" + node.getValue());
	
					item.add(node.getName(), node.getValue());
				}
				list.add(item);
	
			}
	
			addressbookModel.setHeaderList(list);
			//addressbookModel.setNode( n );
	
			//addressbookModel.update();
			//table.setModel( addressbookModel );
			//setupRenderer();
	
			//node = n.getChild("list");
		}
		else
		{
			groupModel.setNode(n);
			groupModel.update();
	
			table.setModel(groupModel);
			setupRenderer();
	
			node = n;
	
		}
	
	}
	*/

	public void update()
	{
		addressbookModel.update();

		/*
		if ( isGroup() == true )
		{
		    groupModel.update();
		      //table.setModel( groupModel );
		}
		else
		{
		    addressbookModel.update();
		      //table.setModel( addressbookModel );
		}
		*/

	}

	/*
	protected boolean isGroup()
	{
	    if ( node.getName().equals("group") )
	        return true;
	    else
	        return false;
	}
	*/

	public HeaderItem[] getSelectedItems()
	{
		int[] rows = table.getSelectedRows();
		HeaderItem[] nodes = new HeaderItem[rows.length];

		for (int i = 0; i < rows.length; i++)
		{
			nodes[i] = addressbookModel.getHeaderItem(rows[i]);
		}

		return nodes;
		
	}

	public HeaderItem getSelectedItem()
	{
		int row = table.getSelectedRow();

		HeaderItem item = addressbookModel.getHeaderItem(row);

		return item;
	}

	public Object getSelectedUid()
	{
		//AdapterNode child = null;

		int row = table.getSelectedRow();

		HeaderItem item = addressbookModel.getHeaderItem(row);
		Object uid = item.getUid();

		return uid;

		
	}
	
	public Object[] getSelectedUids()
	{
		int[] rows = table.getSelectedRows();
		Object[] uids = new Object[ rows.length ];
		
		HeaderItem item;

		for (int i = 0; i < rows.length; i++)
		{
			item = addressbookModel.getHeaderItem(rows[i]);
			Object uid = item.getUid();
			uids[i] = uid;
		}

		return uids;
		
	}

	public void remove()
	{
		/*
		AdapterNode[] nodes = getSelectedNodes();
		
		
		for ( int i=0; i<nodes.length; i++ )
		{
		nodes[i].remove();
		}
		
		//if ( isGroup() == true ) groupModel.setNode( node );
		
		update();
		
		addressbookInterface.tree.update();
		*/

	}

	/*
	public AdapterNode getPropertiesNode()
	{
	    AdapterNode child = null;
	
	    int row = table.getSelectedRow();
	
	    if ( row != -1 )
	    {
	        if ( node.getName().equals("group") )
	        {
	            GroupItem item = config.getGroupItem( node );
	            int uid = item.getUid( row );
	
	            child = config.getNode( uid );
	            
	        }
	        else
	        {    
	            child = node.getChild(row);
	        }
	    }
	    
	
	    return child;
	}
	*/

}