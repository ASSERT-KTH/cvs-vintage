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

public interface ISelectionHandler {

	/**
	 * Get id of component.
	 * 
	 * @return String id of component
	 */
	public abstract String getId();
	
	/**
	 * Get current selection.
	 * 
	 * @return current selection
	 */
	public abstract ICommandReference getSelection();

	/**
	 * Set new selection.
	 * 
	 * @param selection
	 *            new selection
	 */
	public abstract void setSelection(ICommandReference selection);

}