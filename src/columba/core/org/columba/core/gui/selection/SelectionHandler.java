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

package org.columba.core.gui.selection;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.columba.core.command.DefaultCommandReference;

public abstract class SelectionHandler {

	protected String id;
	protected List selectionListener;

	public SelectionHandler(String id) {
		this.id = id;
		
		selectionListener = new Vector();
	}

	/**
	 * @return String
	 */
	public String getId() {
		return id;
	}
	
	public void addSelectionListener(SelectionListener l) {
		selectionListener.add(l);
	}
	
	protected void fireSelectionChanged(SelectionChangedEvent e) {
		for (Iterator it = selectionListener.iterator(); it.hasNext();) {
			((SelectionListener) it.next()).selectionChanged( e );
		// for( int i=0; i<selectionListener.size(); i++) {
			// ((SelectionListener) selectionListener.get(i)).selectionChanged( e );
		}
	}
	
	public abstract DefaultCommandReference[] getSelection();

	public abstract void setSelection(DefaultCommandReference[] selection);
}
