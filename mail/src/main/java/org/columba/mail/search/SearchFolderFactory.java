package org.columba.mail.search;

import org.columba.core.filter.FilterCriteria;
import org.columba.core.folder.IFolder;
import org.columba.mail.folder.virtual.VirtualFolder;
import org.columba.mail.gui.tree.FolderTreeModel;

public class SearchFolderFactory {

	public static VirtualFolder prepareSearchFolder(FilterCriteria c, IFolder folder) throws Exception {
		// get search folder
		VirtualFolder searchFolder = (VirtualFolder) FolderTreeModel
				.getInstance().getFolder(106);

		// remove old filters
		searchFolder.getFilter().getFilterRule().removeAll();

		// add filter criteria
		searchFolder.getFilter().getFilterRule().add(c);

		// don't search in subfolders recursively
		searchFolder.getConfiguration().setString("property",
				"include_subfolders", "false");

		int uid = folder.getUid();

		// set source folder UID
		searchFolder.getConfiguration().setInteger("property", "source_uid",
				uid);

		// search history folders are deactivated by default
		searchFolder.deactivate();

		// add search to history
		searchFolder.addSearchToHistory();
		
		return searchFolder;
	}
}
