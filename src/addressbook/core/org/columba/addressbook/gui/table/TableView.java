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

import org.columba.addressbook.config.AdapterNode;
import org.columba.addressbook.config.AddressbookConfig;
import org.columba.addressbook.config.FolderItem;
import org.columba.addressbook.folder.Folder;
import org.columba.addressbook.folder.HeaderItem;
import org.columba.addressbook.folder.HeaderItemList;
import org.columba.addressbook.gui.frame.AddressbookFrameController;
import org.columba.addressbook.gui.table.util.AddressbookCommonHeaderRenderer;
import org.columba.addressbook.gui.table.util.HeaderColumn;
import org.columba.addressbook.gui.table.util.HeaderColumnInterface;
import org.columba.addressbook.gui.table.util.TableModelFilteredView;
import org.columba.addressbook.gui.table.util.TableModelSorter;
import org.columba.addressbook.gui.table.util.TypeHeaderColumn;
import org.columba.addressbook.util.AddressbookResourceLoader;
import org.columba.core.config.TableItem;
import org.columba.core.gui.frame.FrameController;

public class TableView extends JPanel implements ListSelectionListener
{
	//private AddressbookInterface addressbookInterface;
	//private AddressbookXmlConfig config;

	private JTable table;
	private AddressbookTableModel addressbookModel;
	private GroupTableModel groupModel;

	public JScrollPane scrollPane;
	private AdapterNode node;
	
	private TableModelFilteredView filteredView;
	private TableModelSorter sorter;
	
	private FilterToolbar toolbar;

	AddressbookFrameController frameController;
	
	public TableView(AddressbookFrameController frameController)
	{
		this.frameController = frameController;
		
		//this.addressbookInterface = i;

	//	config = AddressbookConfig.getAddressbookConfig();

		addressbookModel = new AddressbookTableModel();
		
		filteredView = new TableModelFilteredView(addressbookModel);
		sorter = new TableModelSorter( addressbookModel );
		
		addressbookModel.registerPlugin(filteredView);
		addressbookModel.registerPlugin(sorter);
		
		table = new JTable(addressbookModel);
		table.setIntercellSpacing(new Dimension(0, 0));
		table.setShowGrid(false);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
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
      // 	addressbookInterface.actionListener.changeActions();
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
	
	public TableItem getHeaderTableItem()
	{
		// FIXME
		return null;
		//return AddressbookConfig.getAddressbookOptionsConfig().getHeaderTableItem();
			
	}

	public void setupRenderer()
	{
		
		TableItem headerItemList =
			new TableItem(AddressbookConfig.get("options").getElement("/options/gui/table"));

		HeaderColumn c;

		for (int i = 0; i < headerItemList.count(); i++)
		{
			org.columba.core.config.HeaderItem e = headerItemList.getHeaderItem(i);
			
			String name = e.get("name");
			//System.out.println("name:" + name);
			name = name.toLowerCase();
			int size = e.getInteger("size");
			int position = e.getInteger("position");
			boolean enabled = e.getBoolean("enabled");
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
			org.columba.core.config.HeaderItem e = headerItemList.getHeaderItem(i);
			
			String name = e.get("name");
			boolean enabled = e.getBoolean("enabled");
			
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
		HeaderItemList list = folder.getHeaderItemList();

					addressbookModel.setHeaderList(list);
		/*
		if (item.getType().equals("root"))
			addressbookModel.setHeaderList(null);
		else
		{
			HeaderItemList list = folder.getHeaderItemList();

			addressbookModel.setHeaderList(list);
		}
		*/
		
		
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
		if (row == -1) return null;

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

	/**
	 * @return FrameController
	 */
	public FrameController getFrameController() {
		return frameController;
	}

}
