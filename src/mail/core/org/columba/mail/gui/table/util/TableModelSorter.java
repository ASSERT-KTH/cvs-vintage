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
package org.columba.mail.gui.table.util;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Vector;

import org.columba.mail.gui.table.HeaderTableModel;
import org.columba.mail.message.Flags;
import org.columba.mail.message.HeaderInterface;

public class TableModelSorter extends TableModelPlugin {

	protected boolean dataSorting = false;

	protected boolean ascending = true;
	protected String sort = new String("In Order Received");

	protected Collator collator;

	public TableModelSorter(HeaderTableModel tableModel) {
		super(tableModel);

		collator = Collator.getInstance();

		//            mc = tableModel.getMessageCollection();

		//folder = tableModel.getFolder();

	}

	/*
	public void setWindowItem( WindowItem item )
	{
	this.config = item;
	
	
	sort = config.getSelectedHeader();
	if ( sort == null )
	    sort = new String("Status");
	ascending = config.getHeaderAscending();
	
	
	setSortingColumn( sort );
	setSortingOrder( ascending );
	
	}
	*/

	public void setDataSorting(boolean b) {
		dataSorting = b;
	}

	public boolean getDataSorting() {
		return dataSorting;
	}

	public String getSortingColumn() {
		return sort;
	}

	public boolean getSortingOrder() {
		return ascending;
	}

	public void setSortingColumn(String str) {
		sort = str;
		//config.setSelectedHeader( sort );

	}

	public void setSortingOrder(boolean b) {
		ascending = b;
		//config.setHeaderAscending( ascending );

	}

	public synchronized void sortTable(String str) {
		/*
		    folder = getHeaderTableModel().getFolder();
		    if ( folder == null ) return;
		    if ( folder.count() < 2 ) return;
		*/

		setSortingColumn(str);

		if (str.equals("In Order Received")) {
			setDataSorting(true);

			MessageNode rootNode = getHeaderTableModel().getRootNode();

			System.out.println("in order received");

			setDataSorting(false);
		} else {
			for (int i = 0; i < getHeaderTableModel().getColumnCount(); i++) {
				if (str.equals(getHeaderTableModel().getColumnName(i))) {

					setDataSorting(true);

					MessageNode rootNode = getHeaderTableModel().getRootNode();
					Vector v = rootNode.getVector();

					System.out.println("starting to sort");
					Collections.sort(
						v,
						new MessageHeaderComparator(
							getHeaderTableModel().getColumnNumber(
								getSortingColumn()),
							getSortingOrder()));

					//System.out.println("finished sorting");

					//tableModel.update();
					//getHeaderTableModel().fireTreeNodesChanged();
					//getHeaderTableModel().update();

					setDataSorting(false);

				}
			}
		}

	}

	public void sort(int column) {
		String c = getHeaderTableModel().getColumnName(column);

		if (getSortingColumn().equals(c)) {
			if (getSortingOrder())
				setSortingOrder(false);
			else
				setSortingOrder(true);
		}

		setSortingColumn(c);
		sortTable(c);
	}

	public void setSortingColumn(int column) {
		String c = getHeaderTableModel().getColumnName(column);

		if (getSortingColumn().equals(c)) {
			if (getSortingOrder())
				setSortingOrder(false);
			else
				setSortingOrder(true);
		}

		setSortingColumn(c);
	}

	public boolean manipulateModel(int mode) {
		sortTable(getSortingColumn());
		return true;
	}

	public int getSortInt() {
		return getHeaderTableModel().getColumnNumber(getSortingColumn());
	}

	public int getInsertionSortIndex(MessageNode newChild) {
		MessageNode rootNode = getHeaderTableModel().getRootNode();
		Vector v = rootNode.getVector();

		if (getSortingColumn().equals("In Order Received")) {
			return rootNode.getChildCount();
		}

		//System.out.println("column name: "+getSortingColumn() );
		//System.out.println("column number: "+ getHeaderTableModel().getColumnNumber( getSortingColumn() ) );

		MessageHeaderComparator comparator =
			new MessageHeaderComparator(
				getHeaderTableModel().getColumnNumber(getSortingColumn()),
				getSortingOrder());

		MessageNode child;
		int compare;

		// no children !
		if (v == null)
			return 0;

		for (int i = 0; i < v.size(); i++) {
			child = (MessageNode) v.get(i);
			compare = comparator.compare(child, newChild);

			if (compare == -1) {

			} else if (compare == 1) {
				return i;
			}

		}

		return v.size();
	}

	class MessageHeaderComparator implements Comparator {

		protected int column;

		protected boolean ascending;

		public MessageHeaderComparator(int sortCol, boolean sortAsc) {
			column = sortCol;
			ascending = sortAsc;
		}

		public int compare(Object o1, Object o2) {
			//Integer int1 = (Integer) o1;
			//Integer int2 = (Integer) o2;

			MessageNode node1 = (MessageNode) o1;
			MessageNode node2 = (MessageNode) o2;

			//Message message1 = folder.get( int1.intValue() );
			HeaderInterface header1 = (HeaderInterface) node1.getUserObject();
			//Message message2 = folder.get( int2.intValue() );
			HeaderInterface header2 = (HeaderInterface) node2.getUserObject();

			if ((header1 == null) || (header2 == null))
				return 0;

			/*
			        Rfc822Header header1 = message1.getHeader();
			        Rfc822Header header2 = message2.getHeader();
			*/

			int result = 0;

			String columnName = getHeaderTableModel().getColumnName(column);

			if (columnName.equals("Status")) {
				Flags flags1 = header1.getFlags();
				Flags flags2 = header2.getFlags();

				if ((flags1 == null) || (flags2 == null))
					result = 0;

				if ((flags1.getSeen()) && (!flags2.getSeen())) {
					result = -1;
				} else if ((!flags1.getSeen()) && (flags2.getSeen())) {
					result = 1;
				} else
					result = 0;
			} else if (columnName.equals("Flagged")) {
				Flags flags1 = header1.getFlags();
				Flags flags2 = header2.getFlags();

				boolean f1 = flags1.getFlagged();
				boolean f2 = flags2.getFlagged();

				if (f1 == f2) {
					result = 0;
				} else if (f1) { // define false < true
					result = 1;
				} else {
					result = -1;
				}
			} else if (columnName.equals("Attachment")) {
				boolean f1 =
					((Boolean) header1.get("columba.attachment"))
						.booleanValue();
				boolean f2 =
					((Boolean) header2.get("columba.attachment"))
						.booleanValue();

				if (f1 == f2) {
					result = 0;
				} else if (f1) { // define false < true
					result = 1;
				} else {
					result = -1;
				}
			} else if (columnName.equals("Date")) {
				Date d1 = (Date) header1.get("columba.date");
				Date d2 = (Date) header2.get("columba.date");
				if ((d1 == null) || (d2 == null))
					result = 0;
				else
					result = d1.compareTo(d2);
			} else if (columnName.equals("Size")) {
				int i1 = ((Integer) header1.get("columba.size")).intValue();
				int i2 = ((Integer) header2.get("columba.size")).intValue();

				if (i1 == i2) {
					result = 0;
				} else if (i1 > i2) {
					result = 1;
				} else {
					result = -1;
				}
			} else {
				Object item1 = header1.get(columnName);
				Object item2 = header2.get(columnName);

				if ((item1 != null) && (item2 == null))
					result = 1;
				else if ((item1 == null) && (item2 != null))
					result = -1;
				else if ((item1 == null) && (item2 == null))
					result = 0;

				else if (item1 instanceof String) {
					result = collator.compare((String) item1, (String) item2);
				}
			}

			if (!ascending)
				result = -result;
			return result;
		}

		public boolean equals(Object obj) {

			if (obj instanceof MessageHeaderComparator) {

				MessageHeaderComparator compObj = (MessageHeaderComparator) obj;

				return (compObj.column == column)
					&& (compObj.ascending == ascending);

			}

			return false;

		}

	}

}
