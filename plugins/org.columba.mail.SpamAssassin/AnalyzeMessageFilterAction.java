
import org.columba.core.command.Command;
import org.columba.core.filter.AbstractFilterAction;
import org.columba.core.filter.FilterAction;
import org.columba.core.folder.DefaultFolderCommandReference;
import org.columba.core.folder.IFolder;

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

/**
 * Filter action which analyzes messages and marks them as spam
 * or non spam.
 * 
 * @author fdietz
 */
public class AnalyzeMessageFilterAction extends AbstractFilterAction {

	/**
	 * @see org.columba.core.filter.AbstractFilterAction#getCommand(org.columba.core.filter.FilterAction,
	 *      org.columba.core.folder.IFolder, java.lang.Object[])
	 */
	public Command getCommand(FilterAction filterAction, IFolder srcFolder,
			Object[] uids) throws Exception {

		return new AnalyzeMessageCommand(new DefaultFolderCommandReference(
				srcFolder, uids));
	}

}