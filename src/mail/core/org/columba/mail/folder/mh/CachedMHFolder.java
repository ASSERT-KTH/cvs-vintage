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

import org.columba.mail.config.FolderItem;
import org.columba.mail.folder.DataStorageInterface;
import org.columba.mail.folder.HeaderListStorage;
import org.columba.mail.folder.LocalFolder;
import org.columba.mail.folder.headercache.LocalHeaderListStorage;
import org.columba.mail.folder.search.DefaultSearchEngine;
import org.columba.mail.folder.search.LuceneQueryEngine;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class CachedMHFolder extends LocalFolder {
    public CachedMHFolder(FolderItem item, String path) {
        super(item, path);

        DefaultSearchEngine engine = new DefaultSearchEngine(this);
        boolean enableLucene = getConfiguration().getBoolean("property",
                "enable_lucene", false);
        if (enableLucene) {
            engine.setNonDefaultEngine(new LuceneQueryEngine(this));
        }
        setSearchEngine(engine);
    }

    /**
 * @param type
 */
    public CachedMHFolder(String name, String type, String path) {
        super(name, type, path);

        FolderItem item = getConfiguration();
        item.set("property", "accessrights", "user");
        item.set("property", "subfolder", "true");
    }

    public DataStorageInterface getDataStorageInstance() {
        if (dataStorage == null) {
            dataStorage = new MHDataStorage(this);
        }

        return dataStorage;
    }

    /**
     * @see org.columba.mail.folder.Folder#getHeaderListStorage()
     */
    public HeaderListStorage getHeaderListStorage() {
        if (headerListStorage == null) {
            headerListStorage = new LocalHeaderListStorage(this);
        }

        return headerListStorage;
    }
}
