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

package org.columba.addressbook.gui.table.util;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Vector;

import org.columba.addressbook.folder.HeaderItem;
import org.columba.addressbook.folder.HeaderItemList;
import org.columba.addressbook.gui.table.AddressbookTableModel;

public class TableModelSorter extends TableModelPlugin
{

	protected boolean dataSorting = false;

	protected boolean ascending = true;
	protected String sort = new String("displayname");

	protected Collator collator;

	public TableModelSorter(AddressbookTableModel tableModel)
	{
		super(tableModel);

		collator = Collator.getInstance();

	}

	public void setDataSorting(boolean b)
	{
		dataSorting = b;
	}

	public boolean getDataSorting()
	{
		return dataSorting;
	}

	public String getSortingColumn()
	{
		return sort;
	}

	public boolean getSortingOrder()
	{
		return ascending;
	}

	public void setSortingColumn(String str)
	{
		sort = str;
		//config.setSelectedHeader( sort );

	}

	public void setSortingOrder(boolean b)
	{
		ascending = b;
		//config.setHeaderAscending( ascending );

	}

	public synchronized void sortTable(String str)
	{

		setSortingColumn(str);

		if (str.equals("In Order Received"))
		{
			setDataSorting(true);

			/*
			MessageNode rootNode = getHeaderTableModel().getRootNode();
			
			Vector v = rootNode.getVector();
			*/

			System.out.println("in order received");

			setDataSorting(false);
		}
		else
		{
			for (int i = 0; i < getTableModel().getColumnCount(); i++)
			{
				if (str.equals(getTableModel().getColumnName(i)))
				{

					setDataSorting(true);

					
					//MessageNode rootNode = getHeaderTableModel().getRootNode();
					Vector v = getTableModel().getHeaderList().getVector();
					
					System.out.println("starting to sort");
					
					Collections.sort( v, new MessageHeaderComparator(
					    getTableModel().getColumnNumber( getSortingColumn() ) ,
					    getSortingOrder() ) );
					
					//getTableModel().update();

					setDataSorting(false);

				}
			}
		}

	}

	public void sort(int column)
	{

		
		String c = getTableModel().getColumnName(column);
		
		if ( getSortingColumn().equals(c) )
		{
		    if ( getSortingOrder() == true ) setSortingOrder( false );
		    else setSortingOrder( true );
		}
		
		setSortingColumn( c );
		
		sortTable( c );
		

	}

	public void setSortingColumn(int column)
	{
		
		String c = getTableModel().getColumnName( column );
		
		if ( getSortingColumn().equals(c) )
		{
		    if ( getSortingOrder() == true ) setSortingOrder( false );
		    else setSortingOrder( true );
		}
		
		setSortingColumn( c );
		
	}

	public boolean manipulateModel(int mode)
	{

		sortTable(getSortingColumn());

		return true;
	}

	public int getSortInt()
	{

		return getTableModel().getColumnNumber(getSortingColumn());

	}

	public int getInsertionSortIndex(HeaderItem newChild)
	{
		
		
		HeaderItemList list = getTableModel().getHeaderList();
		
		/*
		if ( getSortingColumn().equals("In Order Received") )
		{
		 
		    return rootNode.getChildCount();
		}
		*/
		
		
		MessageHeaderComparator comparator = new MessageHeaderComparator(
		                                         getTableModel().getColumnNumber( getSortingColumn() ),
		                                         getSortingOrder() );
		
		HeaderItem child;
		int compare;
		
		// no children !
		if ( list == null ) return 0;
		
		for ( int i=0; i<list.count(); i++ )
		{
		    child = (HeaderItem) list.get(i);
		    compare = comparator.compare( child, newChild );
		
		    if ( compare == -1 )
		    {
		
		    }
		    else if ( compare == 1 )
		    {
		        return i;
		    }
		
		}
		
		return list.count();
		
	}

	class MessageHeaderComparator implements Comparator
	{

		protected int column;

		protected boolean ascending;

		public MessageHeaderComparator(int sortCol, boolean sortAsc)
		{
			column = sortCol;
			ascending = sortAsc;
		}

		public int compare(Object ob1, Object ob2)
		{
			
			HeaderItem item1 = (HeaderItem) ob1;
			HeaderItem item2 = (HeaderItem) ob2;

			
			if ((item1 == null) || (item2 == null))
				return 0;

			int result = 0;

			String columnName = getTableModel().getColumnName(column);

			if (columnName.equals("type"))
			{
				String type1 = (String) item1.get("type");
				String type2 = (String) item2.get("type");

				if ((type1 == null) || (type2 == null))
					result = 0;

				if ( (type1.equals("contact")) && ( type2.equals("grouplist")) )
				{
					result = -1;
				}
				else if ( (type2.equals("contact")) && ( type2.equals("grouplist")) )
				{
					result = 1;
				}
				else
					result = 0;
			}
			
			else if (columnName.equals("Date"))
			{
				Date d1 = (Date) item1.get("Date");
				Date d2 = (Date) item2.get("Date");
				if ((d1 == null) || (d2 == null))
					result = 0;
				else
					result = d1.compareTo(d2);
			}
			
			else
			{
				Object o1 = item1.get(columnName);
				Object o2 = item2.get(columnName);

				if ((o1 != null) && (o2 == null))
					result = 1;
				else if ((o1 == null) && (o2 != null))
					result = -1;
				else if ((o1 == null) && (o2 == null))
					result = 0;

				else if (o1 instanceof String)
				{
					result = collator.compare((String) o1, (String) o2);
				}
			}

			if (!ascending)
				result = -result;
			return result;
			
		
		}

		public boolean equals(Object obj)
		{

			if (obj instanceof MessageHeaderComparator)
			{

				MessageHeaderComparator compObj = (MessageHeaderComparator) obj;

				return (compObj.column == column) && (compObj.ascending == ascending);

			}

			return false;

		}

	}

}