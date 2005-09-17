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
import org.columba.mail.config.IFolderItem;
import org.columba.mail.folder.AbstractLocalFolder;
import org.columba.mail.folder.IDataStorage;
import org.columba.mail.folder.search.LuceneQueryEngine;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class CachedMHFolder extends AbstractLocalFolder {
    public CachedMHFolder(FolderItem item, String path) {
        super(item, path);

        boolean enableLucene = getConfiguration().getBooleanWithDefault("property",
                "enable_lucene", false);
        if (enableLucene) {
            getSearchEngine().setNonDefaultEngine(new LuceneQueryEngine(this));
        }                
    }

    /**
 * @param type
 */
    public CachedMHFolder(String name, String type, String path) {
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

    
    public IDataStorage getDataStorageInstance() {
        if (dataStorage == null) {
            dataStorage = new MHDataStorage(this);
        }

        return dataStorage;
    }

}
