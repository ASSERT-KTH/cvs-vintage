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

import org.columba.mail.message.HeaderList;

/**
 * @author fdietz
 *
 * This is the Controller for the <class>HeaderTableModel</class>. 
 * 
 * Dont' update the <class>HeaderTableModel</class> directly, always
 * use <class>TableModelUpdateManager</class>.
 */
public class TableModelUpdateManager  {

	private TreeTableModelDecorator decorator;

	public TableModelUpdateManager(TreeTableModelDecorator decorator) {
		this.decorator = decorator;
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.gui.table.model.TableModelModifier#modify(java.lang.Object[])
	 */
	public void modify(Object[] uids) {

		// just send an update event to the TableModel
		decorator.modify(uids);

	}

	/* (non-Javadoc)
	 * @see org.columba.mail.gui.table.model.TableModelModifier#remove(java.lang.Object[])
	 */
	public void remove(Object[] uids) {

		decorator.remove(uids);

	}

	public void update() {
		decorator.update();
	}

	public void set(HeaderList headerList) {
		decorator.set(headerList);
	}

}
