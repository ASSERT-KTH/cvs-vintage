// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.mail.folder;

import org.columba.core.config.AdapterNode;
import org.columba.mail.config.FolderItem;

public abstract class RemoteFolder extends Folder {

	protected RemoteSearchEngine searchEngine;

	public RemoteFolder(AdapterNode node, FolderItem item) {
		super(node, item);

	}

	public SearchEngineInterface getSearchEngine() {
		if (searchEngine == null)
			searchEngine = new RemoteSearchEngine(this);

		return searchEngine;
	}

}
