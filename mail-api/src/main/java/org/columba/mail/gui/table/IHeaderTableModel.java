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
package org.columba.mail.gui.table;

import javax.swing.event.TableColumnModelListener;

import org.columba.mail.message.IHeaderList;

/**
 * @author fdietz
 *
 */
public interface IHeaderTableModel extends TableColumnModelListener {
	/**
	 * ***************************** implements TableModelModifier
	 * ******************
	 */
	void modify(Object[] uids);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.mail.gui.table.model.TableModelModifier#remove(java.lang.Object[])
	 */void remove(Object[] uids);

	void update();

	void clear();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.mail.gui.table.model.TableModelModifier#set(org.columba.mail.message.HeaderList)
	 */void set(IHeaderList headerList);

	/** ********************** getter/setter methods *************************** */
	void enableThreadedView(boolean b);

	//MessageNode getRootNode();

	//MessageNode getMessageNode(Object uid);
	
	//int getRow(MessageNode node);
	
	//MessageNode getMessageNodeAtRow(int index);
	String getColumnName(int column);
	void clearColumns();
	void addColumn(String c);
}