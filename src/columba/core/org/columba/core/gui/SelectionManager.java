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
package org.columba.core.gui;

import java.util.Hashtable;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.gui.util.SelectionHandler;
import org.columba.core.gui.util.SelectionListener;

public class SelectionManager {

	private Hashtable selectionHandler;
	
	public SelectionManager() {
		selectionHandler = new Hashtable();
	}
	
	public void addSelectionHandler(SelectionHandler handler) {
		selectionHandler.put(handler.getId(), handler);
	}

	public void registerSelectionListener(String id, SelectionListener l) {
		((SelectionHandler)selectionHandler.get(id)).addSelectionListener(l);
	}
	
	public void setSelection(String id, DefaultCommandReference[] selection) {
		((SelectionHandler)selectionHandler.get(id)).setSelection(selection);
	}
	
	public DefaultCommandReference[] getSelection(String id) {
		return ((SelectionHandler)selectionHandler.get(id)).getSelection();
	}

	public SelectionHandler getHandler(String id) {
		return (SelectionHandler) selectionHandler.get(id);
	}
}
