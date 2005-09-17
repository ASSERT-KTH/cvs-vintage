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

package org.columba.mail.folder.mbox;


import org.columba.mail.config.FolderItem;
import org.columba.mail.config.IFolderItem;
import org.columba.mail.folder.AbstractLocalFolder;
import org.columba.mail.folder.IDataStorage;
import org.columba.mail.folder.search.LuceneQueryEngine;

public class CachedMboxFolder extends AbstractLocalFolder {

	
	
	/**
	 * Constructs the CachedMboxFolder.java.
	 * 
	 * @param item
	 * @param path
	 */
	public CachedMboxFolder(FolderItem item, String path) {
        super(item, path);

        boolean enableLucene = getConfiguration().getBooleanWithDefault("property",
                "enable_lucene", false);
        if (enableLucene) {
            getSearchEngine().setNonDefaultEngine(new LuceneQueryEngine(this));
        }        
	}

	/**
	 * Constructs the CachedMboxFolder.java.
	 * 
	 * @param name
	 * @param type
	 * @param path
	 */
	public CachedMboxFolder(String name, String type, String path) {
        super(name, type, path);

        IFolderItem item = getConfiguration();
        item.setString("property", "accessrights", "user");
        item.setString("property", "subfolder", "true");

        boolean enableLucene = getConfiguration().getBooleanWithDefault("property",
                "enable_lucene", false);
        if (enableLucene) {
            getSearchEngine().setNonDefaultEngine(new LuceneQueryEngine(this));
        }        
	}

	
	
	/**
	 * @see org.columba.mail.folder.AbstractLocalFolder#getDataStorageInstance()
	 */
	public IDataStorage getDataStorageInstance() {
        if (dataStorage == null) {
            dataStorage = new MboxDataStorage(this);
        }

        return dataStorage;
	}

	/**
	 * @see org.columba.mail.folder.AbstractMessageFolder#save()
	 */
	public void save() throws Exception {
		((MboxDataStorage)getDataStorageInstance()).save();
	}
}
