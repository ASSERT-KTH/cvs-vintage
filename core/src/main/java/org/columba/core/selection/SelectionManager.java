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
package org.columba.core.selection;

import java.util.Hashtable;

import org.columba.api.command.ICommandReference;
import org.columba.api.selection.ISelectionHandler;
import org.columba.api.selection.ISelectionListener;
import org.columba.api.selection.ISelectionManager;

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
 * @see org.columba.core.selection.SelectionHandler
 * @see org.columba.api.gui.frame.IFrameMediator
 * 
 * @author fdietz, tstich
 */
public class SelectionManager implements ISelectionManager {
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
	public void addSelectionHandler(ISelectionHandler handler) {
		selectionHandler.put(handler.getId(), handler);
	}

	/**
	 * @see org.columba.api.selection.ISelectionManager#registerSelectionListener(java.lang.String, org.columba.api.selection.ISelectionListener)
	 */
	public void registerSelectionListener(String id, ISelectionListener l) {
		SelectionHandler h = ((SelectionHandler) selectionHandler.get(id));

		h.addSelectionListener(l);
	}
	
	/**
	 * @see org.columba.api.selection.ISelectionManager#removeSelectionListener(java.lang.String, org.columba.api.selection.ISelectionListener)
	 */
	public void removeSelectionListener(String id, ISelectionListener l) {
		SelectionHandler h = ((SelectionHandler) selectionHandler.get(id));

		h.removeSelectionListener(l);
	}
	

	/**
	 * @see org.columba.api.selection.ISelectionManager#setSelection(java.lang.String, org.columba.api.command.ICommandReference)
	 */
	public void setSelection(String id, ICommandReference selection) {
		((ISelectionHandler) selectionHandler.get(id)).setSelection(selection);
	}

	/**
	 * @see org.columba.api.selection.ISelectionManager#getSelection(java.lang.String)
	 */
	public ICommandReference getSelection(String id) {
		return ((ISelectionHandler) selectionHandler.get(id)).getSelection();
	}

	/**
	 * @see org.columba.api.selection.ISelectionManager#getHandler(java.lang.String)
	 */
	public ISelectionHandler getHandler(String id) {
		return (ISelectionHandler) selectionHandler.get(id);
	}
}