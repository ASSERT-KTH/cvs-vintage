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

import java.util.List;
import java.util.Vector;

import org.columba.api.command.ICommandReference;
import org.columba.api.selection.ISelectionHandler;
import org.columba.api.selection.ISelectionListener;
import org.columba.api.selection.SelectionChangedEvent;

/**
 * Handles the selection of a component identified with an id.
 * <p>
 * We need the id for the {@link SelectionManager}which keeps a map of
 * selection handlers.
 * <p>
 * 
 * 
 * @author fdietz, tstich
 */
public abstract class SelectionHandler implements ISelectionHandler {
	/**
	 * id of component for later identification
	 */
	protected String id;

	/**
	 * list of selection listeners
	 */
	protected List selectionListener;

	/**
	 * Default constructor
	 * 
	 * @param id
	 *            id of component
	 */
	public SelectionHandler(String id) {
		this.id = id;

		selectionListener = new Vector();
	}

	/**
	 * Get id of component.
	 * 
	 * @return String id of component
	 */
	public String getId() {
		return id;
	}

	/**
	 * Add selection listener.
	 * 
	 * @param l
	 *            selectionlistener
	 */
	public void addSelectionListener(ISelectionListener l) {
		selectionListener.add(l);
	}

	/**
	 * Fire a selection has changed event.
	 * <p>
	 * Notify all listeners for a change.
	 * 
	 * @param e
	 *            change event
	 */
	protected void fireSelectionChanged(SelectionChangedEvent e) {
		for ( int i=0; i<selectionListener.size(); i++) {
			((ISelectionListener)selectionListener.get(i)).selectionChanged(e);
		}
	}

	/**
	 * @see org.columba.api.selection.ISelectionHandler#getSelection()
	 */
	public abstract ICommandReference getSelection();

	/**
	 * @see org.columba.api.selection.ISelectionHandler#setSelection(org.columba.api.command.ICommandReference)
	 */
	public abstract void setSelection(ICommandReference selection);

	/**
	 * @param command
	 */
	public void removeSelectionListener(ISelectionListener l) {
		selectionListener.remove(l);
	}
}