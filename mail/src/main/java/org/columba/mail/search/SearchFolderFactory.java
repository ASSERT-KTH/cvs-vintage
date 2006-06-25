package org.columba.mail.search;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.columba.core.command.CommandProcessor;
import org.columba.core.filter.FilterCriteria;
import org.columba.core.folder.IFolder;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.IMailFolder;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.folder.command.SaveFolderConfigurationCommand;
import org.columba.mail.folder.virtual.VirtualFolder;
import org.columba.mail.gui.tree.FolderTreeModel;

public class SearchFolderFactory {

	// not used currently
	public static VirtualFolder prepareSearchFolder(FilterCriteria c,
			IFolder folder) throws Exception {
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

	/**
	 * Return list of all source folders we going to query.
	 * 
	 * @return list of source folders
	 */
	public static List<IMailbox> getAllSourceFolders() {
		IMailFolder rootFolder = (IMailFolder) FolderTreeModel.getInstance()
				.getRoot();

		List<IMailbox> v = new Vector<IMailbox>();
		saveFolder(rootFolder, v);

		return v;
	}

	private static void saveFolder(IMailFolder parentFolder, List<IMailbox> list) {
		IMailFolder child;

		for (Enumeration e = parentFolder.children(); e.hasMoreElements();) {
			child = (IMailFolder) e.nextElement();

			if (child instanceof IMailbox) {
				if (child.getName().equalsIgnoreCase("Inbox")) {
					list.add((IMailbox) child);
				}
			}

			saveFolder(child, list);
		}
	}
}
