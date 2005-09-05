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

package org.columba.mail.folder.mh;

import java.io.IOException;

import org.columba.mail.config.FolderItem;
import org.columba.mail.config.IFolderItem;
import org.columba.mail.folder.AbstractLocalFolder;
import org.columba.mail.folder.IDataStorage;
import org.columba.mail.folder.headercache.LocalHeaderCache;
import org.columba.mail.folder.headercache.PersistantHeaderList;
import org.columba.mail.folder.headercache.SyncHeaderList;
import org.columba.mail.folder.search.DefaultSearchEngine;
import org.columba.mail.folder.search.LuceneQueryEngine;
import org.columba.mail.message.IHeaderList;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class CachedMHFolder extends AbstractLocalFolder {
    protected PersistantHeaderList headerList;

	public CachedMHFolder(FolderItem item, String path) {
        super(item, path);

        DefaultSearchEngine engine = new DefaultSearchEngine(this);
        boolean enableLucene = getConfiguration().getBooleanWithDefault("property",
                "enable_lucene", false);
        if (enableLucene) {
            engine.setNonDefaultEngine(new LuceneQueryEngine(this));
        }
        setSearchEngine(engine);
        
        headerList = new PersistantHeaderList(new LocalHeaderCache(this));
    }

    /**
 * @param type
 */
    public CachedMHFolder(String name, String type, String path) {
        super(name, type, path);

        IFolderItem item = getConfiguration();
        item.setString("property", "accessrights", "user");
        item.setString("property", "subfolder", "true");

        headerList = new PersistantHeaderList(new LocalHeaderCache(this));
    }

    public IDataStorage getDataStorageInstance() {
        if (dataStorage == null) {
            dataStorage = new MHDataStorage(this);
        }

        return dataStorage;
    }

	public IHeaderList getHeaderList() throws Exception {
		if( !headerList.isRestored()) {
			try {
				headerList.restore();
			} catch (IOException e) {
				SyncHeaderList.sync(this, headerList);
			}

			if( headerList.count() != getDataStorageInstance().getMessageCount()) {
				// 	Must be out of sync!
				SyncHeaderList.sync(this, headerList);
			}
		}
		
		
		return headerList;
	}

	/**
	 * @see org.columba.mail.folder.AbstractMessageFolder#save()
	 */
	public void save() throws Exception {
		super.save();
		
		headerList.persist();
	}
   

}
