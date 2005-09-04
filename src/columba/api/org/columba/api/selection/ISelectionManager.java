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
package org.columba.api.selection;

import org.columba.api.command.ICommandReference;

public interface ISelectionManager {

	/**
	 * Register selection listener at selecton handler with id.
	 * 
	 * @param id
	 *            ID of selection handler
	 * @param l
	 *            listener interested in selection changes
	 */
	public abstract void registerSelectionListener(String id,
			ISelectionListener l);

	/**
	 * Remove selection listener.
	 * 
	 * @param id	id of selection handler
	 * @param l		listener
	 */
	public abstract void removeSelectionListener(String id, ISelectionListener l);

	/**
	 * Set current selection.
	 * 
	 * @param id
	 *            ID of selection handler
	 * @param selection
	 *            new selection for this handler
	 */
	public abstract void setSelection(String id, ICommandReference selection);

	/**
	 * Get current selection of specific selection handler.
	 * 
	 * @param id
	 *            ID of selection handler
	 * @return reference of current selection of this handler
	 */
	public abstract ICommandReference getSelection(String id);

	/**
	 * Get selection handler.
	 * 
	 * @param id
	 *            ID of selection handler
	 * @return SelectionHandler
	 */
	public abstract ISelectionHandler getHandler(String id);

	/**
	 * Add selection handler
	 * 
	 * @param handler
	 */
	public abstract void addSelectionHandler(ISelectionHandler handler);
	
}