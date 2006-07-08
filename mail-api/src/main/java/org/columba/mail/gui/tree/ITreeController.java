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
package org.columba.mail.gui.tree;

import javax.swing.tree.TreeModel;

import org.columba.mail.folder.IMailFolder;

/**
 * @author fdietz
 * 
 */
public interface ITreeController {

	public enum SORTING_MODE_ENUM { UNSORTED, ALPHABETICAL, UNREAD_COUNT};
	
	/**
	 * Set the specified folder as seleceted.
	 * 
	 * @param folder
	 *            the new selected folder.
	 */
	void setSelected(IMailFolder folder);

	public IMailFolder getSelected();
	
	public void addFolderSelectionListener(IFolderSelectionListener l);

	public void removeFolderSelectionListener(IFolderSelectionListener l);
	
	TreeModel getModel();

	public void sortAscending(boolean ascending);

	public void setSortingEnabled(boolean enabled);
	
	public void setSortingMode(SORTING_MODE_ENUM sortingMode, boolean ascending);
	
}