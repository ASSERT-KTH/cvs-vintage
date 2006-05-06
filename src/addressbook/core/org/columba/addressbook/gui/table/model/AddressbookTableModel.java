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
package org.columba.addressbook.gui.table.model;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;

import org.columba.addressbook.model.ContactModelPartial;
import org.columba.addressbook.model.IContactModelPartial;

/**
 * Simple table model, using an extended TableModel interface.
 * 
 * @author fdietz
 */
public class AddressbookTableModel extends AbstractTableModel
		implements
			ContactItemTableModel {
	
	/** JDK 1.4+ logging framework logger, used for logging. */
    private static final Logger LOG = Logger
            .getLogger("org.columba.addressbook.gui.table.model");
    
	private String[] columns = {"displayname", "given", "family", "email;internet", "url"};
	private ContactModelPartial[] rows;

	private Map<String,IContactModelPartial> headerItemList;

	public AddressbookTableModel() {
		super();

	}

	public void setContactItemMap(Map<String,IContactModelPartial> list) {
		
		
		if ( list == null) {
			LOG.fine("map == null");
			
			rows = new ContactModelPartial[0];
		
			fireTableDataChanged();
			
			return;
			
		}
		
		
		this.headerItemList = list;

		if ( list.size() == 0) LOG.fine("map is empty");
		
		rows = new ContactModelPartial[list.size()];

		Iterator it = list.values().iterator();
		int i = 0;
		while (it.hasNext()) {
			rows[i++] = (ContactModelPartial) it.next();
		}

		fireTableDataChanged();
	}

	public void update() {
		rows = new ContactModelPartial[headerItemList.size()];

		Iterator it = headerItemList.values().iterator();
		int i = 0;
		while (it.hasNext()) {
			rows[i++] = (ContactModelPartial) it.next();
		}

		fireTableDataChanged();
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return columns.length;
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		if (rows == null)
			return 0;

		return rows.length;
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int column) {
		ContactModelPartial item = rows[row];

		switch (column) {
			case 0 :
				return item.getName();
			case 1 :
				return item.getFirstname();
			case 2 :
				return item.getLastname();
			case 3 :
				return item.getAddress();
			case 4 :
				return item.getWebsite();
			default :
				return "";

		}

	}
	/**
	 * @see org.columba.addressbook.gui.table.model.ContactItemTableModel#getHeaderItem(int)
	 */
	public IContactModelPartial getContactItem(int index) {

		return rows[index];
	}
	/**
	 * @return Returns the headerItemList.
	 */
	public Map<String,IContactModelPartial> getContactItemMap() {
		return headerItemList;
	}
}