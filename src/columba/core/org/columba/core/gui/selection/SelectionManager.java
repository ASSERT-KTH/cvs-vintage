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

import org.columba.core.command.DefaultCommandReference;

import java.util.Hashtable;

/**
 * Manages selection handling of a complete frame which can have many different
 * components with a selection model.
 * <p>
 * It additionally wraps almost all methods of {@link SelectionHandler}. So,
 * there's no need to directly access {@link SelectionHandler}.
 * <p>
 * The <code>org.columba.core.gui.frame</code> package makes highly use of
 * this class to manage all its selection stuff.
 * <p>
 * SelectionHandler has an id <code>String</code> as attribute. This makes it
 * easy to indentify the SelectionHandler.
 * 
 * @see org.columba.core.gui.selection.SelectionHandler
 * @see org.columba.core.gui.frame.FrameMediator
 * 
 * @author fdietz, tstich
 */
public class SelectionManager {
	/**
	 * Map for storing all selection handlers
	 *  
	 */
	private Hashtable selectionHandler;

	/**
	 * default constructor
	 */
	public SelectionManager() {
		// init Map
		selectionHandler = new Hashtable();
	}

	/**
	 * Add selection handler
	 * 
	 * @param handler
	 */
	public void addSelectionHandler(SelectionHandler handler) {
		selectionHandler.put(handler.getId(), handler);
	}

	/**
	 * Register selection listener at selecton handler with id.
	 * 
	 * @param id
	 *            ID of selection handler
	 * @param l
	 *            listener interested in selection changes
	 */
	public void registerSelectionListener(String id, SelectionListener l) {
		SelectionHandler h = ((SelectionHandler) selectionHandler.get(id));

		h.addSelectionListener(l);
	}

	/**
	 * Set current selection.
	 * 
	 * @param id
	 *            ID of selection handler
	 * @param selection
	 *            new selection for this handler
	 */
	public void setSelection(String id, DefaultCommandReference selection) {
		((SelectionHandler) selectionHandler.get(id)).setSelection(selection);
	}

	/**
	 * Get current selection of specific selection handler.
	 * 
	 * @param id
	 *            ID of selection handler
	 * @return reference of current selection of this handler
	 */
	public DefaultCommandReference getSelection(String id) {
		return ((SelectionHandler) selectionHandler.get(id)).getSelection();
	}

	/**
	 * Get selection handler.
	 * 
	 * @param id
	 *            ID of selection handler
	 * @return SelectionHandler
	 */
	public SelectionHandler getHandler(String id) {
		return (SelectionHandler) selectionHandler.get(id);
	}
}