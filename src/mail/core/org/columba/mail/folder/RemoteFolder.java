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
package org.columba.mail.folder;

import org.columba.core.xml.XmlElement;
import org.columba.mail.config.FolderItem;
import org.columba.mail.filter.FilterList;
import org.columba.mail.folder.search.*;

public abstract class RemoteFolder extends Folder {

	protected RemoteSearchEngine searchEngine;

	public RemoteFolder(FolderItem item) {
		super(item);

		XmlElement filterListElement = node.getElement("filterlist");
		if (filterListElement == null) {
			filterListElement = new XmlElement("filterlist");
			getFolderItem().getRoot().addElement(filterListElement);
		}

		filterList = new FilterList(filterListElement);

	}

	public AbstractSearchEngine getSearchEngine() {
		if (searchEngine == null)
			searchEngine = new RemoteSearchEngine(this);

		return searchEngine;
	}

}
