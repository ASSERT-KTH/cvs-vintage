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

package org.columba.mail.gui.table.model;

import org.columba.core.config.TableItem;
import org.columba.core.config.WindowItem;
import org.columba.mail.config.MailConfig;
import org.columba.mail.message.HeaderList;

/**
 * @author fdietz
 *
 * Extends <class>BasicTableModelSorter</class> with Columba
 * specific stuff.
 * 
 * Sorting order and column are initially loaded/saved from
 * an xml configuration file.
 * 
 * It especially implements <interface>TableModelModifier</interface>.
 * 
 */
public class TableModelSorter extends BasicTableModelSorter {

	protected WindowItem config;

	public TableModelSorter(TreeTableModelInterface tableModel) {
		super(tableModel);

		TableItem headerTableItem =
			(TableItem) MailConfig.getMainFrameOptionsConfig().getTableItem();

		setSortingColumn(headerTableItem.get("selected"));
		setSortingOrder(headerTableItem.getBoolean("ascending"));
		//collator = Collator.getInstance();

	}

	public void saveConfig() {
		TableItem tableItem =
			(TableItem) MailConfig.getMainFrameOptionsConfig().getTableItem();

		boolean ascending = getSortingOrder();
		String sortingColumn = getSortingColumn();

		tableItem.set("ascending", ascending);
		tableItem.set("selected", sortingColumn);
	}

	public void setWindowItem(WindowItem item) {
		this.config = item;

		// FIXME
		//sort = config.getSelectedHeader();
		if (sort == null)
			sort = new String("Status");

		// FIXME

		//ascending = config.getHeaderAscending();
		ascending = true;

		setSortingColumn(sort);
		setSortingOrder(ascending);

	}

	/*
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
	    */

	public void setSortingColumn(String str) {
		sort = str;
		// FIXME
		//config.setSelectedHeader( sort );

	}

	public void setSortingOrder(boolean b) {
		ascending = b;
		// FIXME
		//config.setHeaderAscending( ascending );

	}

	/******************************* implements TableModelModifier *******************/

	/* (non-Javadoc)
	 * @see org.columba.mail.gui.table.model.TableModelModifier#modify(java.lang.Object[])
	 */
	public void modify(Object[] uids) {
		super.modify(uids);

	}

	/* (non-Javadoc)
	 * @see org.columba.mail.gui.table.model.TableModelModifier#remove(java.lang.Object[])
	 */
	public void remove(Object[] uids) {
		super.remove(uids);

	}

	public void sort(int column) {
		super.sort(column);

		// notify tree
		getRealModel().getTreeModel().nodeStructureChanged(getRootNode());

		// notify table 
		getRealModel().fireTableDataChanged();
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.gui.table.model.TableModelModifier#update()
	 */
	public void update() {

		super.update();

		// sort table model data
		sort(getSortInt());

		// notify tree
		getRealModel().getTreeModel().nodeStructureChanged(getRootNode());

		// notify table 
		getRealModel().fireTableDataChanged();
	}

	/* (non-Javadoc)
		 * @see org.columba.mail.gui.table.model.TreeTableModelInterface#set(org.columba.mail.message.HeaderList)
		 */
	public void set(HeaderList headerList) {

		super.set(headerList);
		
		update();
	}

}
